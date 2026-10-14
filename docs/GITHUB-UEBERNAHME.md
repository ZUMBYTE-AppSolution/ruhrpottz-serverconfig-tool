# Entwicklung im GitHub-Repository

Das Repository enthält den Desktop-Quellstand von PZ Server Architect. Der früher mitgelieferte manuelle Übernahme-Patch muss nach erfolgter Übernahme nicht mehr angewendet werden.

Vor Änderungen den aktuellen Branch auschecken und lokale Änderungen sichern. `python tools/build.py` kompiliert die Anwendung und führt beide Testreihen aus. `tools/package.py` baut native Pakete ausschließlich für das aktuelle System.

Änderungen erhalten einen verständlichen Commit, einen Eintrag in `CHANGELOG.md` und bei Benutzeränderungen eine Aktualisierung des Handbuchs. Ergebnisse der CI sind commitgebunden. Der Release-Workflow erstellt nur einen Entwurf; die stabile Freigabe bleibt ausdrücklich getrennt.

Keine Benutzerprofile, Kennwörter, generierten Paketarchive oder privaten Dateien in den Quellbaum aufnehmen. Buildausgaben gehören in Actions-Artefakte beziehungsweise bewusst veröffentlichte Releases.
