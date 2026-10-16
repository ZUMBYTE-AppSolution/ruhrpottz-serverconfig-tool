# Changelog

Änderungen an PZ Server Architect, nach Versionen geordnet. Das Format folgt [Keep a Changelog](https://keepachangelog.com/de/1.1.0/).

## [Unreleased]

### Offen

- Microsoft-Store-Identität reservieren und das MSIX mit dieser Identität für die Store-Abgabe erstellen.
- Code-Signing und macOS-Notarisierung für direkte Verteilung vorbereiten.
- Vollständige Installation und Updateübernahme auf allen Zielsystemen sowie Spieltest mit konkreter PZ-Version und Mods nachweisen.

## [0.4.0] - 2026-10-16 · öffentliches Preview-Release

### Hinzugefügt

- Windows-MSIX-Build mit gebündelter Laufzeit, Manifest und App-Grafiken als **unsigniertes Entwicklungspaket**.
- Native Windows-App mit WPF und lokal angebundener Java-Konfigurationslogik; macOS und Linux verwenden weiter die Java-Oberfläche.
- Automatische Plattformmatrix für `main` und den privaten Branch `test`; Release-Workflow für versionierte Tags und vollständige Prüfsummenprüfung.
- Direkte Windows-Downloadwege und aktueller Release-Status auf der Projektstartseite.

### Geändert

- Lesbare Windows-Systemschrift, höherer Textkontrast und ein durchgehend dunkles Oberflächenlayout.
- Schmale Seitenleiste mit linksbündigen Texten, lokal gebündelten Iconify-Icons und klarer Markierung der aktiven Seite.
- Quick Start als eigenes Assistentenfenster mit neun erklärten Schritten, Zurück/Weiter und abschließender Änderungsvorschau.
- Profilauswahl als Dropdown zum direkten Wechseln und Anlegen; Profilverwaltung mit verständlichem Leerzustand.
- Dunkle Buttons passend zur Seitenleiste, funktionsfähige Dropdowns mit mittig ausgerichtetem Iconify-Chevron und neu angeordnete Editor-Aktionen.
- RuhrpottZ mit geordnetem Live-Überblick für Status, Adresse, Spieler, Karte, Version und Mods; Discord-Button und automatischer Abruf beim Öffnen und alle 15 Sekunden.
- Öffentliche und unveröffentlichte Arbeit auf zwei Repositorys verteilt: `main` öffentlich, `test` privat.

### Korrigiert

- Die Windows-Seite „Hilfe & Updates“ zeigt nach manueller Prüfung und nach dem Download sofort die passenden nächsten Aktionen.

### Geprüft

- Java 21 kompiliert; 128 Updater- und 941 App-Prüfungen lokal bestanden.
- Der ältere Java-Oberflächenstand bestand 46 UI-Prüfungen; diese Zahl ist kein Nachweis für die neue WPF-Oberfläche.
- Die WPF-App kompiliert unter .NET 10 ohne Warnungen. Quick Start, Profilwechsel, Einstellungen und der neu gestaltete Live-Status wurden in einem laufenden Windows-Fenster geprüft.
- Windows x64 als portables WPF-ZIP und unsigniertes MSIX-Testpaket lokal neu gebaut und mit gebündelter EXE gestartet.
- Die öffentliche RuhrpottZ-Statusseite wurde mit dem WPF-Client live gelesen. Der angezeigte Status stammt aus dieser externen Serverliste.
- Der private WPF-Vorlauf bestand auf Windows, Linux und beiden macOS-Architekturen. Alle neun Pakete wurden heruntergeladen und anhand ihrer SHA-256-Dateien geprüft. Der öffentliche [Release-Workflow](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions/workflows/desktop-release.yml) wiederholt die Plattformmatrix und prüft alle Pakete vor der Veröffentlichung.

## [0.3.0] - 2026-10-15 · öffentliches Preview-Release

### Hinzugefügt

- Bedienbare Java-Swing-App mit 360 Katalogeinträgen, Quick Start, acht Vorlagen, Profilen, Import, Prüfung und Export.
- GitHub-only-Updater ohne Masterserver; Plattformpakete für Windows x64, macOS Intel/Apple Silicon und Linux x64.
- Versionierter [GitHub-Release](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases/tag/v0.3.0) mit MSI/ZIP, DMG/ZIP, DEB/TAR.GZ und SHA-256-Dateien.

### Korrigiert

- Fehlende Werte von `false` und `0` unterschieden; entfernte Dropdown-Werte löschen den Schlüssel wirklich.
- Ungültige ausstehende Eingaben beim Seitenwechsel abgefangen.
- Geheimnisfelder beim Profilspeichern ausgenommen; ihr nativer Export bleibt bewusst möglich.

### Geprüft

- Vier [GitHub-Actions-Jobs](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions/runs/38703376018) mit Java-Build und Paketierung erfolgreich.
- Paketinstallationen und der vollständige Live-Updatezyklus waren dabei nicht Teil des Nachweises.

## [0.2.0] - 2026-10-14 · Updater-Grundlage

- Updatequelle auf GitHub Releases begrenzt; Masterserver-Adapter entfernt.
- Paket- und Versionsprüfung, Prüfsummen, Abfragelimits und sichere GitHub-Weiterleitungen ergänzt.

## [0.1.0] - ursprüngliche SDK-Grundlage

- Erste repositorygebundene Release-Prüfung und Offline-Tests des Updaters.
