# GitHub-Übernahme des Desktopstands 0.3.0

Der lokale Stand basiert auf dem zuletzt gelesenen SDK-Commit
`6b1df5f7f25e49b3a56afd7dd482f9f887286541`. Dessen komplette Quelldatei-Baum-ID
`971a73ff4058ed522b908298faa6392a23e14a67` wurde bytegenau mit dem GitHub-Stand
verglichen. Die Veröffentlichung ist als eigener Branch
`feat/desktop-preview-0.3.0` vorgesehen, nicht als Überschreiben von `main`.

Ein bestehender Patch aus älteren ZIPs gehört nicht in das Repository.
Die Übergabe enthält stattdessen einen tatsächlichen lokalen Git-Commit in einem
Git-Bundle sowie optional dessen Format-Patch. Ein Bundle ist keine Installation
der Anwendung und veröffentlicht nichts von allein.

## In einer bereits angemeldeten lokalen Arbeitskopie

Zuerst ungesicherte Arbeit aufbewahren. Dann den aktuellen Remote-Stand abrufen
und das beigefügte Bundle prüfen und importieren:

```sh
git fetch origin
git bundle verify /pfad/PZ-Server-Architect-0.3.0.bundle
git fetch /pfad/PZ-Server-Architect-0.3.0.bundle refs/heads/feat/desktop-preview-0.3.0
git show --stat FETCH_HEAD
# Nach Kontrolle, ohne Force-Push:
git push origin FETCH_HEAD:refs/heads/feat/desktop-preview-0.3.0
```

`/pfad/` ist durch den tatsächlichen Ort des Bundles zu ersetzen. Für eine
automatische Übernahme in einer **neuen** Arbeitskopie liegt dem Übergabepaket
ein Launcher bei. Er benötigt Git, Python 3 und optional die GitHub CLI für
About-Beschreibung und Pull Request. Es werden keine Zugangsdaten im Paket gespeichert.

## Beschreibung und Pull Request

Die vorgesehenen About-Daten stehen in `docs/REPOSITORY-METADATA.json`. Die
ausführliche PR-Beschreibung liegt in `docs/PULL-REQUEST.md`. Diese Dateien allein
ändern die GitHub-Metadaten nicht. Ein neuer PR kann vom Desktopbranch nach
`main` erstellt werden; der alte SDK-PR #1 wird nicht automatisch geschlossen.

## Releases

Der vorbereitete manuelle Workflow `Draft desktop preview` erzeugt erst nach
den Builds einen Draft als Prerelease. Es wird kein stabiler Release automatisch
veröffentlicht. Fehlende Plattformpakete nicht als vorhanden ausgeben; Installer
und tatsächliche Updatezyklen vor stabiler Freigabe testen.

Der erfolgreiche Remote-Push ist separat zu bestätigen. Weder ein lokaler
Commit noch ein bestandener Offline-Test beweist eine GitHub-Veröffentlichung.
