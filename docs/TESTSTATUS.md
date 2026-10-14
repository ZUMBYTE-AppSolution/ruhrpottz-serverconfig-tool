# Prüfstand

## Lokal ausgeführt

14. Oktober 2026: OpenJDK / javac 21.0.12.1 unter Linux.

- Alle Java-Quellen mit `--release 21 -Xlint:all -Werror` kompiliert.
- 110 Offline-Assertions bestanden, darunter JSON-Grenzen, SemVer, Plattformauswahl, Produktbindung, stabile Releases, falsche Prüfsummen/Dateigrößen, temporäre Dateien, manipulierte/gealterte Manifeste, falsche Schlüssel, Signierer-Interoperabilität, asynchroner Download und Schutz ungespeicherter Arbeit.
- Ein Test-Signierschlüssel wird ausschließlich temporär erzeugt und wieder entfernt. Kein Produktionsschlüssel existiert in diesem Paket.

## Nicht durch diesen lokalen Lauf belegt

Echte GitHub-Downloads über Java-HTTPS, HTTP-Redirect-/Timeoutverhalten in einem realen Netz, Windows-/macOS-Prozessstart, echte Installer, Betriebssystem-Herausgebersignaturen, JavaFX-GUI, Datenerhalt des künftigen Konfigurationseditors, VeliShell-Masterserver-Anbindung und Project-Zomboid-Verhalten.

Die Netzwerkquelle wird in den Offline-Tests durch kontrollierte Streams ersetzt. Paket-Bytes sind inerte Fixtures, keine ausführbaren Installer. Die mitgelieferte CI-Matrix ist erst nach tatsächlich beobachteten erfolgreichen Läufen zusätzlich als getestet zu kennzeichnen.
