# Changelog

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
