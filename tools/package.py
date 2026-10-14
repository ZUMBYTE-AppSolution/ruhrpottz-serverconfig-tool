#!/usr/bin/env python3
"""Create a self-contained app for the CURRENT OS/CPU; not a cross-compiler.

Default: native launcher + runtime as portable archive. --installer also creates
MSI/DMG/DEB using jpackage and the packaging tools installed on this machine.
Existing output is refused. No installation, deployment or publication occurs.
"""
import argparse
import hashlib
import os
import platform
import shutil
import subprocess
import tarfile
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
from build import ROOT, VERSION, run

APP_NAME = 'PZ Server Architect'

def checksum(file: Path) -> None:
    h = hashlib.sha256()
    with file.open('rb') as source:
        for block in iter(lambda: source.read(1024 * 1024), b''):
            h.update(block)
    file.with_name(file.name + '.sha256').write_text(h.hexdigest() + '  ' + file.name + '\n', encoding='ascii')

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--installer', action='store_true')
    args = parser.parse_args()
    system = {'Windows': 'windows', 'Darwin': 'macos', 'Linux': 'linux'}.get(platform.system())
    arch = {'AMD64': 'x64', 'amd64': 'x64', 'x86_64': 'x64', 'aarch64': 'arm64', 'arm64': 'arm64'}.get(platform.machine())
    if not system or not arch:
        raise RuntimeError('Unsupported OS or CPU; no package was generated.')
    target = f'{system}-{arch}'
    for tool in ('jpackage', 'jlink'):
        if not shutil.which(tool):
            raise RuntimeError(f'{tool} not found; install a JDK with packaging tools.')
    jar = ROOT / 'build' / f'PZ-Server-Architect-{VERSION}.jar'
    if not jar.is_file():
        raise RuntimeError('Run python tools/build.py first.')
    out = ROOT / 'build' / 'native' / target
    if out.exists():
        raise RuntimeError(f'Refusing to overwrite {out}. Move old results aside before a new build.')
    out.mkdir(parents=True)
    app_input = out / 'input'
    app_input.mkdir()
    shutil.copy2(jar, app_input)
    runtime = out / 'runtime'
    run('jlink', '--add-modules', 'java.desktop,java.net.http,jdk.crypto.ec,jdk.localedata',
        '--strip-debug', '--no-header-files', '--no-man-pages', '--compress=2', '--output', str(runtime))
    image_dir = out / 'image'
    icon = ROOT / 'src/main/resources/assets' / ('icon.ico' if system == 'windows' else 'icon.icns' if system == 'macos' else 'icon.png')
    common = ['--name', APP_NAME, '--app-version', VERSION, '--vendor', 'Zumbyte - Appsolution',
              '--copyright', 'Copyright by Zumbyte - Appsolution | www.zumbyte.de',
              '--description', 'Project Zomboid server and sandbox configurator', '--icon', str(icon)]
    run('jpackage', '--type', 'app-image', '--dest', str(image_dir), '--input', str(app_input),
        '--main-jar', jar.name, '--runtime-image', str(runtime), *common)
    image = image_dir / (APP_NAME + ('.app' if system == 'macos' else ''))
    launcher = image / ('Contents/MacOS/' + APP_NAME if system == 'macos' else APP_NAME + '.exe' if system == 'windows' else 'bin/' + APP_NAME)
    run(str(launcher), '--version')
    # Only original program resources and the JDK runtime are packaged. Never bundle fonts.
    bad_fonts = [f for f in image.rglob('*') if f.suffix.lower() in ('.ttf', '.otf', '.woff', '.woff2')]
    if bad_fonts:
        raise RuntimeError('Unexpected font files in runtime. Distribution was stopped.')
    dist = ROOT / 'build' / 'dist'
    dist.mkdir(exist_ok=True)
    if system == 'linux':
        archive = dist / f'PZ-Server-Architect-{VERSION}-{target}-portable.tar.gz'
        if archive.exists():
            raise RuntimeError('Portable archive already exists.')
        with tarfile.open(archive, 'w:gz') as t:
            t.add(image, arcname=image.name)
            t.add(ROOT / 'src/main/resources/HANDBUCH.md', arcname='HANDBUCH.md')
            t.add(ROOT / 'NOTICE', arcname='NOTICE')
    else:
        archive = dist / f'PZ-Server-Architect-{VERSION}-{target}-portable.zip'
        if archive.exists():
            raise RuntimeError('Portable archive already exists.')
        if system == 'macos':
            run('ditto', '-c', '-k', '--sequesterRsrc', '--keepParent', str(image), str(archive))
        else:
            with ZipFile(archive, 'x', ZIP_DEFLATED) as z:
                for f in sorted(image.rglob('*')):
                    if f.is_file():
                        z.write(f, Path(image.name) / f.relative_to(image))
                z.write(ROOT / 'src/main/resources/HANDBUCH.md', 'HANDBUCH.md')
                z.write(ROOT / 'NOTICE', 'NOTICE')
    checksum(archive)
    if args.installer:
        kind = {'windows': 'msi', 'macos': 'dmg', 'linux': 'deb'}[system]
        extra = []
        if system == 'windows':
            extra = ['--win-menu', '--win-shortcut', '--win-dir-chooser', '--win-upgrade-uuid', '5adfd3a2-30c4-4eaf-918a-dbd71e6fce14']
        elif system == 'linux':
            extra = ['--linux-package-name', 'pz-server-architect', '--linux-shortcut', '--linux-app-category', 'Utility']
        else:
            extra = ['--mac-package-identifier', 'de.zumbyte.pzarchitect']
        installer_dir = out / 'installer'
        run('jpackage', '--type', kind, '--dest', str(installer_dir), '--app-image', str(image), *common, *extra)
        results = list(installer_dir.glob('*.' + kind))
        if len(results) != 1:
            raise RuntimeError('Expected exactly one native installer.')
        destination = dist / f'PZ-Server-Architect-{VERSION}-{target}.{kind}'
        if destination.exists():
            raise RuntimeError('Installer destination already exists.')
        shutil.copy2(results[0], destination)
        checksum(destination)
    print('Created native packages for ' + target + '. No installer was executed.')

if __name__ == '__main__':
    try:
        main()
    except (OSError, RuntimeError) as error:
        raise SystemExit(str(error)) from error
