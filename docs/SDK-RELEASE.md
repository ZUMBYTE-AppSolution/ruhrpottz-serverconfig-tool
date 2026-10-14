# Updater-SDK 0.1.0

Entwicklungsbaustein für PZ Server Architect, **keine fertige Konfigurator-App**.

Enthalten sind Java-Quellen, ein kompiliertes Updater-JAR, Offline-Prüfungen und der vorgeschlagene VeliShell-Masterserver-Vertrag. Das JAR benötigt Java 21. Es besitzt keine Konfigurations-GUI und wird vom normalen App-Updater nicht als Installationspaket erkannt.

Der Workflow führt die Offline-Prüfungen auf seinem Linux-Runner aus. Windows-/macOS-CI, echte Installer, Betriebssystem-Signaturen und Live-Masterserver-Tests sind getrennt zu belegen. Alle Test-Installerbytes sind inerte Prüfdaten und werden niemals ausgeführt.

Keine öffentliche stabile App-Veröffentlichung durch diesen Entwurf.

Copyright by Zumbyte - Appsolution | www.zumbyte.de
