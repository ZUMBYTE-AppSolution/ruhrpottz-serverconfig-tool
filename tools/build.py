#!/usr/bin/env python3
"""Build the real desktop app and run both test suites. No network or publication."""
from pathlib import Path
import os
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
VERSION = '0.4.1'

def run(*args: str) -> str:
    result = subprocess.run(args, cwd=ROOT, text=True, encoding='utf-8', errors='replace',
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, check=False)
    print(result.stdout.encode(sys.stdout.encoding or 'utf-8', errors='replace').decode(sys.stdout.encoding or 'utf-8'), end='')
    if result.returncode:
        raise RuntimeError(f'{args[0]} failed with exit code {result.returncode}')
    return result.stdout

def main() -> None:
    for tool in ('java', 'javac', 'jar'):
        if not shutil.which(tool):
            raise RuntimeError(f'{tool} not found. A JDK 21 or newer is required for a developer build.')
    build = ROOT / 'build'
    build.mkdir(exist_ok=True)
    for sub in ('classes', 'test-classes'):
        dest = build / sub
        if dest.is_symlink():
            raise RuntimeError(f'Unexpected symlink: {dest.name}')
        if dest.exists():
            shutil.rmtree(dest)
        dest.mkdir()
    flags = ('--release', '21', '-encoding', 'UTF-8', '-Xlint:all', '-Werror')
    sources = sorted(str(p.relative_to(ROOT)) for p in (ROOT / 'src/main/java').rglob('*.java'))
    tests = sorted(str(p.relative_to(ROOT)) for p in (ROOT / 'src/test/java').rglob('*.java'))
    compiler = run('javac', '-version')
    run('javac', *flags, '-d', 'build/classes', *sources)
    shutil.copytree(ROOT / 'src/main/resources', build / 'classes', dirs_exist_ok=True)
    run('javac', *flags, '-cp', 'build/classes', '-d', 'build/test-classes', *tests)
    report = compiler
    cp = os.pathsep.join(('build/classes', 'build/test-classes'))
    report += run('java', '-cp', cp, 'de.zumbyte.pzarchitect.update.UpdateSelfTest')
    report += run('java', '-cp', cp, 'de.zumbyte.pzarchitect.app.AppSelfTest', 'build/preset-fixtures')
    (build / 'test-report.txt').write_text(report, encoding='utf-8')
    jar = f'build/PZ-Server-Architect-{VERSION}.jar'
    run('jar', '--create', '--file', jar, '--main-class', 'de.zumbyte.pzarchitect.app.App', '-C', 'build/classes', '.')
    run('java', '-jar', jar, '--version')
    print(f'Desktop application built: {jar}')

if __name__ == '__main__':
    try:
        main()
    except (OSError, RuntimeError) as error:
        print(error, file=sys.stderr)
        sys.exit(1)
