# Changelog

## Unreleased

### Implementiert: Updater-SDK 0.1.0
- Neutralen Produktnamen PZ Server Architect und produktgebundene Updatekennung `pz-server-architect` festgelegt.
- GitHub-Releaseprüfung mit stabiler SemVer-Auswahl, Plattformfilter und Changelog.
- Asynchronen Startcontroller mit AUTO_DOWNLOAD-Modus ergänzt.
- Verifizierte Downloads mit Größen-/SHA-256-Prüfung, HTTPS-Grenzen, Download-Lock und temporären Dateien.
- Optionalen signierten Masterserver-Adapter mit Ed25519, Produkt-/Repository-/Plattformprüfung und Ablaufzeit ergänzt.
- Offline-Manifest-Signierer, 110 lokal ausgeführte Offline-Assertions und Java-Build ergänzt.
- CI für Windows, macOS und Linux sowie einen manuellen SDK-Draft-Workflow vorbereitet.

### Noch offen
- JavaFX-Desktopoberfläche, Konfigurationseditor, Katalogmigration, Profile, Assistent und Servermonitor.
- Native App-Pakete und tatsächliche Installer-/GUI-Tests auf Windows, macOS und Linux.
- Identifikation und Erweiterung des realen VeliShell-Masterservers; seine aktuelle URL/API/Quelle liegt noch nicht vor.
- Produktionsschlüssel, Signierung und durchgängiger Live-Update-Test.

Kein öffentliches App-Release und kein Masterserver-Deployment durch diese Änderung.
