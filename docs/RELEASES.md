# Releases und Changelogs

## Aktueller Lieferstand

Das Updater-SDK ist keine vollständige Desktop-App. CI kompiliert und testet das Java-Modul auf vorgesehenen Windows-, macOS- und Linux-Runnern und stellt JAR plus Testbericht als Workflow-Artefakte bereit. Ein vorhandener Workflow ist kein Nachweis, dass er bereits erfolgreich gelaufen ist.

`Draft updater SDK release` ist ausschließlich manuell auslösbar. Er erstellt nach Tests einen **Draft und Prerelease** mit dem nicht als App-SemVer interpretierbaren Tag `sdk-v0.1.0`. Dieser Entwurf enthält das SDK, Quellcode, JAR, Prüfsumme und Testbericht. Er wird nicht automatisch öffentlich geschaltet und vom App-Updater nicht angeboten. Wiederholtes Erstellen eines bereits vorhandenen Tags wird nicht still überschrieben.

## Spätere echte App-Releases

App-Tags sollen `vMAJOR.MINOR.PATCH` verwenden. Erwartete native Paketnamen:

- `PZ-Server-Architect-1.2.0-windows-x64.msi`
- `PZ-Server-Architect-1.2.0-windows-arm64.msi`
- `PZ-Server-Architect-1.2.0-macos-x64.dmg`
- `PZ-Server-Architect-1.2.0-macos-arm64.dmg`
- `PZ-Server-Architect-1.2.0-linux-x64.deb`
- `PZ-Server-Architect-1.2.0-linux-arm64.deb`

Dies sind Namensbeispiele, keine schon existierenden Dateien oder freigegebenen Versionen. Nur tatsächlich gebaute Plattformkombinationen veröffentlichen. Fehlt ein natives Paket, meldet der Client die Lücke. Keine Umdeutung einer JAR, ZIP oder Source-Datei zum Installer.

Die native Buildkette mit JavaFX/jpackage, Runtime-Bundling, Signierung, Installationsprüfung und Freigabe ist noch zu implementieren. Vor Veröffentlichung: Plattformtests, vollständige Assets, klare Release Notes, SHA-256-Digests und erst anschließend Veröffentlichung des bislang als Draft geführten Releases. Der Masterserver darf erst danach die Version anbieten.

## Änderungen verständlich halten

`CHANGELOG.md` unterscheidet implementierte Funktionen und offene Arbeiten. Release Notes sollen Neue Funktionen, Fehlerkorrekturen, Sicherheit/Updates, bekannte Einschränkungen und Migrationshinweise enthalten. GitHubs Kategorien dafür stehen in `.github/release.yml`.

Nie Testzahlen, Installer-Signaturen, Masterserver-Verfügbarkeit oder Plattformkompatibilität behaupten, die nicht durch den jeweiligen Release-Build belegt wurden.
