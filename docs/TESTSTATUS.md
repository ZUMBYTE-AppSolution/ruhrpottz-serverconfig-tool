# Prüfstatus

## Bestätigt

- Das öffentliche v0.3.0-Preview liegt mit acht Plattformpaketen und
  SHA-256-Dateien auf [GitHub Releases](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases/tag/v0.3.0).
- Der [v0.3.0-Plattformlauf](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions/runs/38703376018)
  war auf Windows x64, macOS Intel/Apple Silicon und Linux x64 erfolgreich.
- Der v0.4.0-Teststand wurde lokal unter Windows mit Java 21 kompiliert.
  128 Updater- und 941 App-Prüfungen bestanden.
- Die v0.4.0-Oberfläche wurde unter Windows sichtbar gerendert und in 32
  UI-Prüfschritten bedient; die Bilder in der README stammen aus diesem Lauf.
- Ein portables Windows-x64-ZIP und ein echtes, unsigniertes MSIX-Testpaket
  wurden lokal erstellt; der gebündelte EXE-Starter antwortete auf `--version`.
- Der [private v0.4.0-Plattformlauf](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool-dev/actions/runs/38710901694)
  war in allen vier Jobs erfolgreich. Die neun Paketdateien aus den vier
  Actions-Artefakten wurden vollständig heruntergeladen und mit ihren
  SHA-256-Dateien nachgerechnet: Windows MSI/ZIP/MSIX, macOS je DMG/ZIP,
  Linux DEB/TAR.GZ.

## Noch offen

- Ein öffentlicher v0.4.0-Actions-Lauf und Release sind noch nicht erfolgt.
- Die v0.4.0-Paketinstaller wurden noch nicht systemweit installiert.
- Microsoft-Store-Paketidentität, Signierung und Store-Freigabe fehlen.
- Der vollständige Live-Update- und Installationszyklus sowie ein echter
  Spieltest mit Project Zomboid, AMP und Mods sind nicht belegt.
- Ein erfolgreicher Liveabruf des RuhrpottZ-Serverstatus ist nicht belegt.

Ein Build oder Screenshot ist kein Nachweis für eine erfolgreiche Installation
oder für das Verhalten auf einem produktiven Spielserver.
