#!/usr/bin/env python3
"""Collect only a complete, checksum-verified desktop release matrix."""
import hashlib
import shutil
import sys
from pathlib import Path

VERSION = '0.3.0'
TARGETS = {
    'windows-x64': ('msi', 'portable.zip'),
    'macos-x64': ('dmg', 'portable.zip'),
    'macos-arm64': ('dmg', 'portable.zip'),
    'linux-x64': ('deb', 'portable.tar.gz'),
}


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit('Usage: verify_release.py ARTIFACTS_DIR OUTPUT_DIR')
    source, output = map(Path, sys.argv[1:])
    if not source.is_dir() or output.exists():
        raise RuntimeError('Artifact directory missing or release output already exists')
    expected = {
        f'PZ-Server-Architect-{VERSION}-{target}-{kind}' if kind.startswith('portable') else
        f'PZ-Server-Architect-{VERSION}-{target}.{kind}'
        for target, kinds in TARGETS.items() for kind in kinds
    }
    found = {}
    for path in source.rglob('*'):
        if path.is_file() and path.name.endswith(('.msi', '.dmg', '.deb', '.zip', '.tar.gz', '.sha256')):
            if path.name in found:
                raise RuntimeError('Duplicate asset: ' + path.name)
            found[path.name] = path
    required = expected | {name + '.sha256' for name in expected}
    if set(found) != required:
        raise RuntimeError(f'Asset mismatch: missing={sorted(required - set(found))}, unexpected={sorted(set(found) - required)}')
    checksums = []
    for name in sorted(expected):
        digest = hashlib.sha256(found[name].read_bytes()).hexdigest()
        if found[name].stat().st_size == 0:
            raise RuntimeError('Empty package: ' + name)
        sidecar = found[name + '.sha256'].read_text(encoding='ascii').strip()
        if sidecar != f'{digest}  {name}':
            raise RuntimeError('Checksum mismatch: ' + name)
        checksums.append(sidecar)
    output.mkdir()
    for name, path in found.items():
        shutil.copy2(path, output / name)
    (output / 'SHA256SUMS.txt').write_text('\n'.join(checksums) + '\n', encoding='ascii')
    print(f'PASS: {len(expected)} native/portable packages and SHA-256 sidecars verified.')


if __name__ == '__main__':
    main()
