# PZ Server Architect

Allgemeiner Project-Zomboid-Server- und Sandbox-Konfigurator von **Zumbyte - Appsolution**.
Graphit-Lila-Design, vorhandenes PZ-App-Icon, Werte-Erklärungen, Profile und Quick-Start bleiben die vereinbarte Produktrichtung. Der Appname ist unabhängig vom Repositorynamen.

> **Stand: Java-Updater-SDK 0.2.0. Noch keine fertige JavaFX-Konfigurator-App oder native Installer.** Die Änderung in 0.2.0 betrifft ausschließlich Updates und deren Dokumentation.

## Updates ausschließlich über GitHub

Kein Masterserver, keine zusätzliche API, keine VeliShell-Abhängigkeit und kein Benutzer-Token. Versionen, Release Notes, Prüfsummen und Downloadadressen kommen aus dem öffentlichen Repository `ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool`.

Implementiert sind:

- stabile Release-Erkennung mit SemVer-Vergleich und passendem Plattformpaket;
- asynchroner Startcontroller für manuell, Benachrichtigen und automatisch herunterladen;
- Changelog, Dateigrößen- und SHA-256-Prüfung, begrenzte HTTPS-Weiterleitungen und temporäre Downloads;
- GitHub-Abfragelimits mit Wiederholungszeit und Sperre weiterer Prüfungen im selben Client bis zu diesem Zeitpunkt;
- begrenzte Pagination über bis zu fünf Seiten mit je 100 Releases. Eine unvollständige Prüfung meldet einen Fehler, nicht „aktuell“;
- erneute Paketprüfung vor dem ausdrücklich ausgelösten Öffnen im Betriebssystem-Installer.

Drafts, Prereleases und SDK-Tags werden nicht als reguläre App-Updates angeboten. Ein bloßer Commit oder Tag ohne veröffentlichtes App-Release löst kein Update aus. Kein automatisches Ersetzen laufender Dateien, kein unbeaufsichtigter Installerstart und keine Umgehung von Betriebssystem-Schutzmechanismen.

## Dokumentation

- [Feinkonzept](docs/FEINKONZEPT.md)
- [GitHub-Updater und App-Start](docs/UPDATER.md)
- [Releases, Paketnamen und Veröffentlichung](docs/RELEASES.md)
- [Prüfstand](docs/TESTSTATUS.md)
- [Changelog](CHANGELOG.md)
- [Sicherheitsgrenzen](SECURITY.md)

## Entwickler-Build

JDK 21 und Python 3. Die spätere Endnutzer-App soll ihre Laufzeit enthalten; dieses SDK benötigt derzeit eine vorhandene Java-21-Laufzeit.

```sh
python tools/build.py
python tools/create_sdk.py
java -jar build/PZ-Server-Architect-Updater-0.2.0.jar --help
```

Eine ausdrücklich ausgelöste GitHub-Abfrage ohne Download:

```sh
java -jar build/PZ-Server-Architect-Updater-0.2.0.jar --current 0.1.0 --cache ./update-cache
```

Mit `--download` wird zusätzlich ein vorhandenes, passendes neueres App-Paket heruntergeladen und geprüft. Ohne Argumente erfolgt kein Netzwerkzugriff. Die früheren Masterserver-Argumente werden nicht mehr akzeptiert.

## Community-Empfehlung

RuhrpottZ bleibt ausschließlich eine getrennte Community-Empfehlung, kein eingebautes Konfigurationsprofil. Server: https://pzservers.net/server/75212135. **Discord folgt.** Keine erfundene Einladung oder fest einprogrammierte Spielerzahlen.

---
Copyright by Zumbyte - Appsolution | www.zumbyte.de
