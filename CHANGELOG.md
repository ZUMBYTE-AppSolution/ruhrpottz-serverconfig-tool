# Changelog

Änderungen an PZ Server Architect, nach Versionen geordnet. Das Format folgt [Keep a Changelog](https://keepachangelog.com/de/1.1.0/).

## [Unreleased]

### Offen

- Microsoft-Store-Identität reservieren und das MSIX mit dieser Identität für die Store-Abgabe erstellen.
- Code-Signing und macOS-Notarisierung für direkte Verteilung vorbereiten.
- Vollständige Installation und Updateübernahme auf allen Zielsystemen sowie Spieltest mit konkreter PZ-Version und Mods nachweisen.

## [0.4.0] - 2026-10-15 · privater Teststand

### Hinzugefügt

- Windows-MSIX-Build mit gebündelter Laufzeit, Manifest und App-Grafiken als **unsigniertes Entwicklungspaket**.
- Automatische Plattformmatrix für `main` und den privaten Branch `test`; Release-Workflow für versionierte Tags und vollständige Prüfsummenprüfung.
- Direkte Windows-Downloadwege und aktueller Release-Status auf der Projektstartseite.

### Geändert

- Übersicht mit klaren Einstiegsaktionen für Import, Quick Start und Einstellungen.
- Kompakte Navigation; alle 17 Einstellungsbereiche sind jetzt im Editor auswählbar.
- Größere Typografie, mehr Abstand, gerundete Karten sowie lila Buttons mit Glow bei Fokus und Berührung.
- Öffentliche und unveröffentlichte Arbeit auf zwei Repositorys verteilt: `main` öffentlich, `test` privat.

### Geprüft

- Java 21 kompiliert; 128 Updater- und 941 App-Prüfungen lokal bestanden.
- 32 UI-Prüfungen unter Windows mit echter Swing-Oberfläche bestanden.
- Windows x64 als portables Paket und MSIX-Testpaket lokal gebaut; die native App beantwortete `--version`.

## [0.3.0] - 2026-10-15 · öffentliches Preview-Release

### Hinzugefügt

- Bedienbare Java-Swing-App mit 360 Katalogeinträgen, Quick Start, acht Vorlagen, Profilen, Import, Prüfung und Export.
- GitHub-only-Updater ohne Masterserver; Plattformpakete für Windows x64, macOS Intel/Apple Silicon und Linux x64.
- Versionierter [GitHub-Release](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases/tag/v0.3.0) mit MSI/ZIP, DMG/ZIP, DEB/TAR.GZ und SHA-256-Dateien.

### Geprüft

- Vier [GitHub-Actions-Jobs](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions/runs/38703376018) mit Java-Build und Paketierung erfolgreich.
- Paketinstallationen und der vollständige Live-Updatezyklus waren dabei nicht Teil des Nachweises.

## [0.2.0] - 2026-10-14 · Updater-Grundlage

- Updatequelle auf GitHub Releases begrenzt; Masterserver-Adapter entfernt.
- Paket- und Versionsprüfung, Prüfsummen, Abfragelimits und sichere GitHub-Weiterleitungen ergänzt.

## [0.1.0] - ursprüngliche SDK-Grundlage

- Erste repositorygebundene Release-Prüfung und Offline-Tests des Updaters.
