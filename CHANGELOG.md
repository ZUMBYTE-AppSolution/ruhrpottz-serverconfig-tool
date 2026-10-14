# Changelog

## Repository-Vorbereitung · 2026-10-14

### Dokumentation und Präsentation
- Projektstartseite im Aufbau von VeliShell mit PZ-App-Icon, Preview-Kennzeichnung, echten UI-Aufnahmen und vollständiger Funktions-/Installationsbeschreibung.
- Support, Entwicklungsleitfaden, Roadmap, Pull-Request-Vorlage und Issue-Vorlagen ergänzt.
- Veraltete Aussage zur fehlenden GUI in SECURITY.md korrigiert; die tatsächliche Swing-Umsetzung ausdrücklich dokumentiert.
- Release-Workflow auf eine verifizierte Action-Version festgelegt.
- App-Code, Katalog, Graphit-Lila-Theme und Icons unverändert aus dem gebauten 0.3.0-Stand übernommen.

### Veröffentlichungsstatus
- Lokal commitfähiger beziehungsweise committeter Stand; keine Behauptung eines erfolgreichen Pushs.
- Ein Git-Commit erzeugt kein öffentliches Release. Neue GitHub-CI und native Plattformtests sind erst nach dem Push beziehungsweise ihren tatsächlichen Läufen belegt.

## 0.3.0 Preview · 2026-10-14

### Neue Desktop-App
- Echte Swing-Oberfläche im vorhandenen Graphit-Lila-Design mit eingebundenem App-Icon.
- 360 Katalogeinträge, zusätzlich importierte Modfelder, Werte-Hilfen und Originalkommentare.
- Toggle-Switches für echte Ein/Aus-Werte, Dropdowns und validierte Zahlen/Textfelder.
- Neun Fragen im Quick Start, acht neutrale Vorlagen und Änderungsvorschau.
- Lokale Profile, zehn Wiederherstellungspunkte, Papierkorb, JSON-Import/-Export und Undo/Redo.
- Datentabellenparser ohne Lua-Ausführung; INI/Lua-Kommentare und Reihenfolgen erhalten.
- Maskierter Originalvergleich, validierter Quelltexteditor und neues Export-ZIP.
- GitHub-only-Updater tatsächlich mit der Startlogik und Updateansicht verbunden.
- Separater RuhrpottZ-Hinweis und manueller Parser für öffentliche Serverlisten-Daten.
- Java-Ausgabe, plattformeigene jpackage-Buildkette, Linux-x64-Pakete und Handbuch.

### Korrekturen
- Fehlende Werte von false/0 unterschieden; Dropdown-Zustand entfernt Schlüssel tatsächlich.
- Übernahme ungültiger ausstehender Eingaben beim Seitenwechsel verhindert.
- Bewusstes Leeren einer redigierten Kennworteingabe entsperrt den Export.
- Positive Populationsvorlagen lösen vorherige zivile Populations-/Migrationssperren.
- Vorlagen behalten keine künstliche RuhrpottZ-Identität oder Serverzugangsdaten.

### Prüfungen und Liefergrenzen
- Java 21 mit -Xlint:all -Werror kompiliert; 941 Editor-/Profil- und 128 Updater-Assertions.
- 31 GUI-Prüfungen mit einem tatsächlich geöffneten Swing-Fenster unter Linux/Xvfb.
- Linux-Paket gebaut, Java-Plattformstarter enthalten; Windows/macOS noch nicht ausgeführt.
- Kein neuer GitHub-Commit, CI-Lauf oder Release durch diesen lokalen Entwicklungsschritt.
- Kein vollständiger Live-Update-/Installerzyklus oder Project-Zomboid-/AMP-Spieltest.

## Unreleased: Updater-SDK 0.2.0

### Geändert
- Auf ausdrücklichen Wunsch vollständig auf GitHub Releases umgestellt.
- Masterserver-Adapter, Schlüsselkonfiguration, Offline-Manifest-Signierer und Integrationsvertrag entfernt.
- `Config` und `Candidate` auf GitHub-only vereinfacht; ehemalige Masterserver-CLI-Argumente entfernt. Dies ändert die SDK-Schnittstelle gegenüber 0.1.0.
- GitHub-Abfragelimits werden mit Retry-After beziehungsweise Reset-Zeit behandelt. Weitere Prüfungen desselben Clients warten bis dahin; keine automatische Retry-Schleife.
- Release-Pagination bis fünf Seiten ergänzt. Link-Header können keine fremde Download-/API-Quelle einschleusen. Bei einer unvollständigen Prüfung bleibt der Versionsstand unbekannt.
- GitHub-API-Metadaten und Weiterleitungen auf das feste Repository eingeschränkt.
- Dokumentation und SDK-Draft-Workflow aktualisiert. Graphit-Lila-Design, PZ-App-Icon und Konfigurator-Funktionskonzept unverändert.

### Geprüft
- Java-21-Kompilierung mit `-Xlint:all -Werror` und 128 lokale Offline-Assertions bestanden.
- CI-Matrix bleibt für Windows, macOS und Linux eingerichtet; Linux erhält zusätzlich eine echte öffentliche Metadatenabfrage ohne Download.
- Ergebnisse dieser neuen CI-Läufe sind unabhängig von den bereits erfolgreichen 0.1.0-Läufen zu prüfen.

### Noch offen
- JavaFX-Konfigurator, Editor, Katalogmigration, Profile, Assistent und Servermonitor.
- Native Installer, Betriebssystem-Signierung und vollständiger Live-Update-/Installationstest.

## Updater-SDK 0.1.0: ursprüngliche Entwicklungsgrundlage

Repositorygebundene GitHub-Releaseprüfung, Downloadprüfung, asynchroner Startcontroller und Dokumentation. Damals 110 lokal bestandene Offline-Assertions sowie erfolgreiche Windows-/macOS-/Linux-CI. Der damalige optionale Masterserver-Entwurf ist durch die GitHub-only-Entscheidung vollständig abgelöst.

Kein öffentliches App-Release durch diese SDK-Änderung.
