# Prüfstand: GitHub-only Updater-SDK 0.2.0

## Lokal ausgeführt am 14. Oktober 2026

OpenJDK/javac 21.0.12.1 unter Linux. Alle Java-Quellen mit `--release 21 -Xlint:all -Werror` kompiliert. **128 Offline-Assertions bestanden.**

Geprüft: JSON-Grenzen, SemVer, Plattformnamen, feste GitHub-Endpunkte, stabile Releaseauswahl, SDK-Ausschluss, weitere Releaseseiten, manipulierte Links, abgeschnittene Suche, gleiche/ältere Versionen, beschädigte und unvollständige Downloads, SHA-256/Größe, Cacheprüfung, Rate-Limit-Wartezeiten, Retry-After, Reset-Zeit, asynchroner Download, fehlende Geheimnisse in Fehlermeldungen und Schutz ungespeicherter Arbeit vor Installerstart.

In den lokalen Tests ersetzen kontrollierte Streams die Netzwerkquelle. Paketbytes sind inerte Fixtures und werden nie ausgeführt. Die ursprünglichen Dateien wurden vor Bearbeitung über ihren Git-Tree-Hash mit dem aktuellen PR-Stand abgeglichen.

## GitHub Actions

Die vorhandene Matrix kompiliert und testet unter Windows, macOS und Linux. Für Linux ist zusätzlich eine reale öffentliche GitHub-Metadatenabfrage ohne `--download` vorgesehen. Der erzeugte Bericht beschreibt das tatsächliche Ergebnis, gegebenenfalls „noch kein Release“.

Ein konfigurierter Workflow ist kein Beleg eines bereits erfolgreichen Laufs. Ergebnisse von SDK 0.1.0 gelten nicht automatisch für 0.2.0. Der aktuelle PR-Lauf und seine Artefakte sind getrennt zu prüfen.

## Nicht belegt

Eine fertige Konfigurator-GUI, Datenerhalt des künftigen Editors, echte native Installer, OS-Herausgebersignaturen, automatischer App-Neustart, Rollback und ein vollständiger Live-Paketdownload mit anschließender Installation. Ein erfolgreicher Metadatenabruf prüft noch keinen Download-/Installationszyklus. Project Zomboid selbst wird durch diese Tests nicht gestartet.
