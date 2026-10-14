"""One-time, checksum-locked import of the user-provided desktop source. Removed after import."""
import base64
import hashlib
import io
import json
import lzma
from pathlib import Path, PurePosixPath
import tarfile

ROOT = Path(__file__).resolve().parents[1]
META = json.loads((ROOT / '_import/manifest.json').read_text(encoding='utf-8'))
parts = []
for index in range(14):
    raw = (ROOT / '_import' / f'part-{index:02}.txt').read_text(encoding='ascii')
    # Repair one known transport transcription; full archive and per-file hashes remain mandatory.
    if index == 12:
        raw = raw.replace('DT5pdGGmPqY', 'DT5pdGmPqY')
    if len(raw) != (3704 if index == 13 else 10000):
        raise ValueError('Invalid transport chunk size')
    parts.append(raw)
archive = base64.b64decode(''.join(parts), validate=True)
if len(archive) != 100276 or hashlib.sha256(archive).hexdigest() != META['sha256']:
    raise ValueError('Source archive checksum mismatch')
decoder = lzma.LZMADecompressor()
plain = decoder.decompress(archive, max_length=1_048_577)
if not decoder.eof or decoder.unused_data or len(plain) > 1_048_576:
    raise ValueError('Invalid source archive')
files = {}
with tarfile.open(fileobj=io.BytesIO(plain), mode='r:') as tar:
    for member in tar:
        path = PurePosixPath(member.name)
        if not member.isfile() or path.is_absolute() or any(p in ('.', '..', '.git', '.github', '_import') for p in path.parts):
            raise ValueError('Unsafe source entry')
        if str(path) not in META['files'] or str(path) in files or member.size > 512_000:
            raise ValueError('Unexpected source entry')
        stream = tar.extractfile(member)
        if stream is None:
            raise ValueError('Missing source data')
        data = stream.read(512_001)
        if len(data) != member.size or hashlib.sha256(data).hexdigest() != META['files'][str(path)]:
            raise ValueError('Source entry checksum mismatch: ' + str(path))
        files[str(path)] = data
if set(files) != set(META['files']):
    raise ValueError('Incomplete source archive')
for name, data in files.items():
    target = ROOT / name
    if target.is_symlink() or any(p.is_symlink() for p in target.parents if p != ROOT.parent):
        raise ValueError('Unexpected symbolic link')
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(data)
print(f'Verified and wrote {len(files)} desktop source/documentation/resource files.')
