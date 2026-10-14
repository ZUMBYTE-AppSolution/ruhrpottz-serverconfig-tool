## PZ Server Architect 0.3.0 Preview

Übernimmt die gebaute Java-Swing-Desktop-App auf Basis des GitHub-only-Updaters.
Die neutrale Produktausrichtung, das PZ-App-Icon und das Graphit-Lila-Design bleiben
erhalten. RuhrpottZ erscheint nur als gesonderte Community-Empfehlung.

## Enthalten

- 360 Katalogeinträge in 17 Bereichen und importierte Modfelder.
- Toggle-Switches für echte Ein/Aus-Felder, Dropdowns, Zahlen-/Textfelder und Werte-Hilfen.
- Quick Start mit neun Fragen, acht allgemeine Vorlagen und Änderungsvorschau.
- Profile mit Historie/Papierkorb, JSON-Import/-Export und Undo/Redo.
- INI-/Sandbox-Lua-Import ohne Codeausführung, Quelltextprüfung, Originalvergleich und neues Export-ZIP.
- GitHub-only-Updater mit Startprüfung, optionalem Download, Changelog und bewusster Installationsaktion.
- App-Icon für Plattformpakete, Handbuch und native Buildkette.
- README im Aufbau von VeliShell: Icon-Kopf, Preview-Badges, echte Screenshots, Funktionen, Installation, Datenschutz, Prüfgrenzen und Support.
- Changelog, Roadmap, Issue-/PR-Vorlagen und Zumbyte-Herausgeberhinweis.

## Erneut lokal geprüft

- Java 21, `--release 21 -Xlint:all -Werror`.
- 128 Updater- und 941 Editor-/Parser-/Profil-Assertions bestanden.
- 31 GUI-Prüfschritte mit einem tatsächlichen Swing-Fenster unter Linux/Xvfb bestanden.
- Neuer Build mit eingebetteten Ressourcen und identischen Anwendungscode-/Theme-Dateien.

Die Berichte liegen unter `docs/checks`. README-Bilder stammen aus dem neuen
lokalen GUI-Lauf. Keine privaten Serverdateien oder generierten Installer im Commit.

## Grenzen

Preview, kein stabiler App-Release. Windows-/macOS-GUI-/Installertests, vollständiger
Updatezyklus und Project-Zomboid-/Modbetrieb sind damit nicht belegt. Der Katalog
ist nicht für jeden Spielbuild vollständig verifiziert. Live-Serverstatus bisher
nur über Testdaten geprüft. Signierung/Notarisierung und Rollback bleiben offen.

Ein Push beziehungsweise erfolgreicher CI-Lauf muss anschließend tatsächlich
beobachtet werden. Kein Force-Push, automatischer Merge oder stabiles Release
durch die vorbereitete Übernahme. Bestehender SDK-PR #1 wird nicht automatisch geschlossen.

Copyright by Zumbyte - Appsolution | www.zumbyte.de
