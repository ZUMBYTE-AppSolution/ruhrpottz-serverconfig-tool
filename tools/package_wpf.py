#!/usr/bin/env python3
"""Build the native Windows WPF client with its local Java configuration engine."""
import argparse
import os
import shutil
import subprocess
import tempfile
import uuid
from pathlib import Path
from xml.sax.saxutils import escape
from zipfile import ZipFile, ZIP_DEFLATED

from build import ROOT, VERSION, run
from package import checksum
from msix import create as create_msix

APP = 'PZ Server Architect'
PROJECT = ROOT / 'src/windows/PZServerArchitect.Windows/PZServerArchitect.Windows.csproj'
CORE = ROOT / 'build' / f'PZ-Server-Architect-{VERSION}.jar'
UPGRADE = '5adfd3a2-30c4-4eaf-918a-dbd71e6fce14'


def wix_tool(name: str) -> str:
    found = shutil.which(name)
    if found:
        return found
    root = Path(os.environ.get('ProgramFiles(x86)', r'C:\Program Files (x86)')) / 'WiX Toolset v3.14/bin'
    tool = root / (name + '.exe')
    if not tool.is_file():
        raise RuntimeError(f'WiX tool missing: {name}')
    return str(tool)


def installer(image: Path, output: Path, work: Path) -> None:
    """Use stable component IDs/GUIDs so MSI upgrades preserve Windows Installer ownership."""
    files = sorted(p for p in image.rglob('*') if p.is_file())
    all_dirs = {Path('.')}
    for file in files:
        parent = file.relative_to(image).parent
        while parent != Path('.'):
            all_dirs.add(parent)
            parent = parent.parent
    dirs = sorted(all_dirs, key=lambda p: (len(p.parts), str(p)))
    directory_ids = {Path('.'): 'INSTALLDIR'}
    for i, directory in enumerate(d for d in dirs if d != Path('.')):
        directory_ids[directory] = f'Dir{i}'
    children = {parent: [] for parent in directory_ids}
    for directory in directory_ids:
        if directory != Path('.'):
            children[directory.parent].append(directory)
    components = {directory: [] for directory in directory_ids}
    refs = []
    namespace = uuid.UUID(UPGRADE)
    for index, file in enumerate(files):
        relative = file.relative_to(image)
        component_id = f'Cmp{index}'
        guid = uuid.uuid5(namespace, str(relative).lower().replace('\\', '/'))
        source = escape(str(file))
        components[relative.parent].append(
            f'<Component Id="{component_id}" Guid="{guid}" Win64="yes">'
            f'<File Id="File{index}" Source="{source}" KeyPath="yes" /></Component>')
        refs.append(f'<ComponentRef Id="{component_id}" />')

    # Start-menu shortcuts are per-user MSI resources. Keep them apart from
    # machine-wide files and give their component an HKCU registry key path.
    shortcut_guid = uuid.uuid5(namespace, 'start-menu-shortcut')
    components[Path('.')].append(
        f'<Component Id="StartMenuComponent" Guid="{shortcut_guid}" Win64="yes">'
        '<Shortcut Id="StartMenuShortcut" Directory="ProgramMenuDir" '
        'Name="PZ Server Architect" WorkingDirectory="INSTALLDIR" '
        'Target="[INSTALLDIR]PZServerArchitect.exe" Advertise="no" Icon="AppIcon" />'
        '<RemoveFolder Id="RemoveStartMenu" Directory="ProgramMenuDir" On="uninstall" />'
        '<RegistryValue Root="HKCU" Key="Software\\Zumbyte Appsolution\\PZ Server Architect" '
        'Name="installed" Type="integer" Value="1" KeyPath="yes" />'
        '</Component>')
    refs.append('<ComponentRef Id="StartMenuComponent" />')

    def directory_xml(relative: Path) -> str:
        nested = ''.join(directory_xml(d) for d in children[relative])
        return ''.join(components[relative]) + nested if relative == Path('.') else (
            f'<Directory Id="{directory_ids[relative]}" Name="{escape(relative.name)}">'
            + ''.join(components[relative]) + nested + '</Directory>')

    icon = escape(str(ROOT / 'src/main/resources/assets/icon.ico'))
    wix = f'''<?xml version="1.0" encoding="utf-8"?>
<Wix xmlns="http://schemas.microsoft.com/wix/2006/wi">
 <Product Id="*" Name="{APP}" Language="1031" Version="{VERSION}" Manufacturer="Zumbyte - Appsolution" UpgradeCode="{UPGRADE}">
  <Package InstallerVersion="500" Compressed="yes" InstallScope="perMachine" Platform="x64" />
  <MajorUpgrade DowngradeErrorMessage="Eine neuere Version ist bereits installiert." />
  <MediaTemplate EmbedCab="yes" />
  <Icon Id="AppIcon" SourceFile="{icon}" />
  <Property Id="ARPPRODUCTICON" Value="AppIcon" />
  <Property Id="WIXUI_INSTALLDIR" Value="INSTALLDIR" />
  <UIRef Id="WixUI_InstallDir" />
  <Directory Id="TARGETDIR" Name="SourceDir">
   <Directory Id="ProgramFiles64Folder"><Directory Id="INSTALLDIR" Name="{APP}">{directory_xml(Path('.'))}</Directory></Directory>
   <Directory Id="ProgramMenuFolder"><Directory Id="ProgramMenuDir" Name="{APP}" /></Directory>
  </Directory>
  <Feature Id="Complete" Title="{APP}" Level="1"><ComponentGroupRef Id="AppFiles" /></Feature>
 </Product>
 <Fragment><ComponentGroup Id="AppFiles">{''.join(refs)}</ComponentGroup></Fragment>
</Wix>'''
    wxs = work / 'app.wxs'
    wxs.write_text(wix, encoding='utf-8')
    run(wix_tool('candle'), '-nologo', '-arch', 'x64', '-out', str(work / 'app.wixobj'), str(wxs))
    run(wix_tool('light'), '-nologo', '-ext', 'WixUIExtension', '-out', str(output), str(work / 'app.wixobj'))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--installer', action='store_true', help='Also build MSI with WiX')
    parser.add_argument('--dist', type=Path, default=ROOT / 'build/dist')
    parser.add_argument('--native', type=Path, default=ROOT / 'build/native/windows-x64-wpf')
    args = parser.parse_args()
    if os.name != 'nt':
        raise RuntimeError('WPF packages must be built on Windows')
    if not CORE.is_file():
        raise RuntimeError('Run python tools/build.py first')
    out = args.native.resolve()
    dist = args.dist.resolve()
    if out.exists() or out.is_symlink():
        raise RuntimeError(f'Refusing to overwrite {out}')
    out.mkdir(parents=True)
    dist.mkdir(parents=True, exist_ok=True)
    image = out / 'image' / APP
    run('dotnet', 'publish', str(PROJECT), '-c', 'Release', '-r', 'win-x64', '--self-contained', 'true',
        '-p:PublishSingleFile=false', '-o', str(image))
    engine = image / 'engine'
    engine.mkdir()
    shutil.copy2(CORE, engine / CORE.name)
    run('jlink', '--add-modules', 'java.base,java.net.http,jdk.crypto.ec,jdk.localedata',
        '--strip-debug', '--no-header-files', '--no-man-pages', '--compress=2', '--output', str(image / 'runtime'))
    smoke_data = out / 'smoke-data'
    env = os.environ.copy()
    env['PZ_ARCHITECT_DATA'] = str(smoke_data)
    probe = subprocess.run([str(image / 'PZServerArchitect.exe'), '--smoke'], env=env, timeout=25, check=False)
    if probe.returncode:
        raise RuntimeError(f'WPF application smoke failed: exit {probe.returncode}')
    archive = dist / f'PZ-Server-Architect-{VERSION}-windows-x64-portable.zip'
    if archive.exists():
        raise RuntimeError(f'Archive already exists: {archive}')
    with ZipFile(archive, 'w', ZIP_DEFLATED) as zip_file:
        for file in sorted(image.rglob('*')):
            if file.is_file():
                zip_file.write(file, Path(APP) / file.relative_to(image))
        zip_file.write(ROOT / 'src/main/resources/HANDBUCH.md', 'HANDBUCH.md')
        zip_file.write(ROOT / 'NOTICE', 'NOTICE')
    checksum(archive)
    if args.installer:
        msi = dist / f'PZ-Server-Architect-{VERSION}-windows-x64.msi'
        if msi.exists():
            raise RuntimeError(f'MSI already exists: {msi}')
        work = out / 'wix'
        work.mkdir()
        installer(image, msi, work)
        checksum(msi)
    checksum(create_msix(image, out, executable='PZServerArchitect.exe', dist=dist))
    print('Windows WPF packages built and smoke-tested.')


if __name__ == '__main__':
    main()
