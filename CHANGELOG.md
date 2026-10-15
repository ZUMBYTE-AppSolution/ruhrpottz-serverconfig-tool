# Changelog

Änderungen an PZ Server Architect, nach Versionen geordnet. Das Format folgt [Keep a Changelog](https://keepachangelog.com/de/1.1.0/).

## [Unreleased]

Noch keine öffentlich freigegebenen Änderungen.

## [0.3.0] - 2026-10-15 · Preview

### Hinzugefügt

- Bedienbare Java-Swing-App mit 360 Katalogeinträgen, Quick Start, acht Vorlagen, Profilen, Import, Prüfung und Export.
- GitHub-only-Updater ohne Masterserver und mit verifiziertem Paketdownload.
- Versionierter [GitHub-Release](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases/tag/v0.3.0) mit Windows MSI/ZIP, macOS DMG/ZIP für Intel und Apple Silicon sowie Linux DEB/TAR.GZ und SHA-256-Dateien.
- Handbuch, echte Bildschirmbilder, Support- und Sicherheitsinformationen.

### Korrigiert

- Fehlende Werte von `false` und `0` unterschieden; entfernte Dropdown-Werte löschen den Schlüssel wirklich.
- Ungültige ausstehende Eingaben beim Seitenwechsel abgefangen.
- Geheimnisfelder beim Profilspeichern ausgenommen; ihr nativer Export bleibt bewusst möglich.

### Geprüft

- Vier [GitHub-Actions-Jobs](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions/runs/38703376018) mit Java-Build, Offline-Prüfungen und Plattformpaketen erfolgreich.
- Die Paketinstallation und der vollständige Live-Updatezyklus waren dabei nicht Teil des Nachweises.

## [0.2.0] - 2026-10-14 · Updater-Grundlage

- Updatequelle auf GitHub Releases begrenzt; Masterserver-Adapter entfernt.
- Paket- und Versionsprüfung, Prüfsummen, Abfragelimits und sichere GitHub-Weiterleitungen ergänzt.

## [0.1.0] - ursprüngliche SDK-Grundlage

- Erste repositorygebundene Release-Prüfung und Offline-Tests des Updaters.
