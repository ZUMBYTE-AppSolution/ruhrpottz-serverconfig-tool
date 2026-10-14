# Updater-SDK 0.2.0: GitHub-only

Entwicklungsbaustein für PZ Server Architect, **keine fertige Konfigurator-App**.

Enthalten sind Java-Quellen, ein kompiliertes Java-21-Updater-JAR, Offline-Prüfungen und die GitHub-only-Dokumentation. Der Masterserver-Adapter, seine Konfiguration und der Signierer wurden entfernt. SDK-Schnittstellen gegenüber 0.1.0 sind entsprechend vereinfacht.

Stabile App-Releases werden direkt über GitHub erkannt, nach Plattform ausgewählt und mit Größen-/SHA-256-Prüfung geladen. GitHub-Abfragelimits und begrenzte Pagination sind berücksichtigt. Das SDK-Release selbst bleibt ein Draft/Prerelease und wird nicht als App-Installer angeboten.

128 lokale Offline-Assertions sind bestanden. CI-Ergebnisse sind dem jeweiligen Lauf zu entnehmen. Alle Testpakete sind inerte Daten und werden niemals ausgeführt. Native GUI, Installer, OS-Code-Signing und ein kompletter Live-Installationsablauf sind nicht enthalten.

Copyright by Zumbyte - Appsolution | www.zumbyte.de
