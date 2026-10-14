#!/usr/bin/env python3
"""Bundle only allowlisted source, docs and compiled updater output; never user data or secrets."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
from hashlib import sha256
from build import ROOT, VERSION

out = ROOT / 'build' / f'PZ-Server-Architect-Update-SDK-{VERSION}.zip'
jar = ROOT / 'build' / f'PZ-Server-Architect-Updater-{VERSION}.jar'
if not jar.is_file():
    raise SystemExit('Run tools/build.py first')
files = []
for folder in ('src', 'docs', 'tools', 'assets', '.github'):
    for path in (ROOT / folder).rglob('*'):
        if path.is_file() and not path.is_symlink() and '__pycache__' not in path.parts and path.suffix not in ('.pyc', '.pem', '.key'):
            files.append(path)
for name in ('README.md', 'CHANGELOG.md', 'NOTICE', 'SECURITY.md', '.gitignore'):
    path = ROOT / name
    if path.is_file():
        files.append(path)
files += [jar, ROOT / 'build' / 'test-report.txt']
with ZipFile(out, 'w', ZIP_DEFLATED) as archive:
    for path in sorted(set(files)):
        archive.write(path, Path('PZ-Server-Architect-Update-SDK') / path.relative_to(ROOT))
checksum = sha256(out.read_bytes()).hexdigest()
(out.parent / (out.name + '.sha256')).write_text(f'{checksum}  {out.name}\n', encoding='ascii')
print(out)
