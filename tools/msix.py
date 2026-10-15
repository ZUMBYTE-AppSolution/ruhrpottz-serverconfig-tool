"""Wrap the Windows app image as an unsigned MSIX build artifact.

Partner Center identity values can be supplied through MSIX_IDENTITY_NAME and
MSIX_PUBLISHER. The defaults are deliberately development-only. Signing and
Store submission are separate steps.
"""
import os
import shutil
import subprocess
from pathlib import Path
from xml.sax.saxutils import escape

from build import ROOT, VERSION


def sdk_tool() -> Path:
    kits = Path(os.environ.get('ProgramFiles(x86)', r'C:\Program Files (x86)')) / 'Windows Kits/10/bin'
    candidates = sorted(kits.glob('*/x64/makeappx.exe'), reverse=True)
    if not candidates:
        raise RuntimeError('Windows SDK MakeAppx.exe not found')
    return candidates[0]


def create(image: Path, out: Path) -> Path:
    name = os.environ.get('MSIX_IDENTITY_NAME', 'de.zumbyte.PZServerArchitect.Dev')
    publisher = os.environ.get('MSIX_PUBLISHER', 'CN=Zumbyte Appsolution Development')
    if not name or not publisher:
        raise RuntimeError('MSIX package identity is incomplete')
    stage = out / 'msix-staging'
    if stage.exists():
        raise RuntimeError(f'Refusing to overwrite {stage}')
    shutil.copytree(image, stage)
    shutil.copytree(ROOT / 'packaging/msix/Assets', stage / 'Assets')
    version = VERSION + '.0'
    manifest = f'''<?xml version="1.0" encoding="utf-8"?>
<Package xmlns="http://schemas.microsoft.com/appx/manifest/foundation/windows10"
 xmlns:uap="http://schemas.microsoft.com/appx/manifest/uap/windows10"
 xmlns:uap10="http://schemas.microsoft.com/appx/manifest/uap/windows10/10"
 xmlns:rescap="http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities"
 IgnorableNamespaces="uap10 rescap">
 <Identity Name="{escape(name)}" Version="{version}" Publisher="{escape(publisher)}" ProcessorArchitecture="x64" />
 <Properties><DisplayName>PZ Server Architect</DisplayName><PublisherDisplayName>Zumbyte - Appsolution</PublisherDisplayName>
  <Description>Project Zomboid server configurator</Description><Logo>Assets\\Logo150.png</Logo></Properties>
 <Resources><Resource Language="de-DE" /></Resources>
 <Dependencies><TargetDeviceFamily Name="Windows.Desktop" MinVersion="10.0.19041.0" MaxVersionTested="10.0.26100.0" /></Dependencies>
 <Capabilities><rescap:Capability Name="runFullTrust" /></Capabilities>
 <Applications><Application Id="PZServerArchitect" Executable="PZ Server Architect.exe"
  uap10:RuntimeBehavior="packagedClassicApp" uap10:TrustLevel="mediumIL">
  <uap:VisualElements DisplayName="PZ Server Architect" Description="Project Zomboid server configurator"
   Square150x150Logo="Assets\\Logo150.png" Square44x44Logo="Assets\\Logo44.png" BackgroundColor="#171827" />
 </Application></Applications>
</Package>
'''
    (stage / 'AppxManifest.xml').write_text(manifest, encoding='utf-8')
    package = ROOT / 'build/dist' / f'PZ-Server-Architect-{VERSION}-windows-x64-dev-unsigned.msix'
    if package.exists():
        raise RuntimeError(f'Refusing to overwrite {package}')
    command = [str(sdk_tool()), 'pack', '/d', str(stage), '/p', str(package), '/h', 'SHA256']
    result = subprocess.run(command, text=True, capture_output=True, encoding='utf-8', errors='replace')
    if result.returncode:
        raise RuntimeError(f'MakeAppx failed: {result.stdout}\n{result.stderr}')
    if not package.is_file() or package.stat().st_size == 0:
        raise RuntimeError('MakeAppx did not create a package')
    print(f'Created unsigned development MSIX: {package.name} (identity {name})')
    return package
