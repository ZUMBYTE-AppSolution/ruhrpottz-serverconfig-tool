#!/usr/bin/env python3
"""Optional Linux/X11 test of a native launcher. Requires Pillow and xwininfo.
Run under Xvfb. Uses a disposable profile directory and no app network access.
"""
import os
import re
import subprocess
import sys
import tempfile
import time
from pathlib import Path
from PIL import ImageGrab

launcher = Path(sys.argv[1]).resolve()
out = Path(sys.argv[2]).resolve()
out.parent.mkdir(parents=True, exist_ok=True)
if not launcher.is_file():
    raise SystemExit('Native launcher missing')
with tempfile.TemporaryDirectory(prefix='pz-native-smoke-') as data:
    env = dict(os.environ)
    env['JAVA_TOOL_OPTIONS'] = '-Dpz.data=' + data
    env['LANG'] = 'C.UTF-8'
    with open(Path(data) / 'process.log', 'w') as log:
        p = subprocess.Popen([str(launcher), '--offline'], env=env, stdout=log, stderr=log)
        try:
            handle = None
            for _ in range(100):
                if p.poll() is not None:
                    raise RuntimeError('Native app terminated before showing a window')
                tree = subprocess.check_output(['xwininfo', '-root', '-tree'], text=True)
                match = re.search(r'(0x[0-9a-fA-F]+) "PZ Server Architect[^\n]*', tree)
                if match:
                    handle = match.group(1)
                    break
                time.sleep(.2)
            if not handle:
                raise RuntimeError('No visible native app window within 20 seconds')
            time.sleep(1)
            info = subprocess.check_output(['xwininfo', '-id', handle], text=True)
            def number(name):
                match = re.search(re.escape(name) + r'\s*:\s*(-?\d+)', info)
                if not match:
                    raise RuntimeError('Missing X11 geometry: ' + name)
                return int(match.group(1))
            x, y = number('Absolute upper-left X'), number('Absolute upper-left Y')
            w, h = number('Width'), number('Height')
            if w < 1000 or h < 700 or 'IsViewable' not in info:
                raise RuntimeError('Native app window is not usable or visible')
            ImageGrab.grab(bbox=(x, y, x+w, y+h)).save(out)
            print(f'PASS: native Linux launcher displayed an actual {w}x{h} app window using its bundled runtime.')
            print('Isolated temporary profiles, offline mode. No system installation or live game access.')
        finally:
            p.terminate()
            try:
                p.wait(timeout=10)
            except subprocess.TimeoutExpired:
                p.kill()
                p.wait(timeout=5)
