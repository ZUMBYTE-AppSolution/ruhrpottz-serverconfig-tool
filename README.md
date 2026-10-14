# PZ Server Architect

Allgemeiner Project-Zomboid-Server- und Sandbox-Konfigurator von **Zumbyte - Appsolution**.
Graphit-Lila-Design, verständliche Werte-Erklärungen, Profile und Quick-Start-Assistent sind die vereinbarte Produktrichtung. Der Repositoryname bleibt unverändert; die App heißt nicht RuhrpottZ Config Studio.

> **Aktueller Stand: Entwicklungsgrundlage mit kompiliertem und offline getestetem Java-Updater-SDK. Die neue Desktop-GUI, der Konfigurationseditor, native Installer und die Live-Masterserver-Anbindung sind noch nicht implementiert. Dieses Repository ist noch kein fertiger Konfigurator.**

## Bereits implementiert

- Java-21-Updater ohne externe Bibliotheken: stabile GitHub-Releases des fest gebundenen Repositories, plattformspezifische Paketauswahl und Changelog.
- Asynchrone Startsteuerung für manuelle Prüfung, Benachrichtigung oder automatischen Download. GUI-Einbindung über einen UI-Executor vorbereitet.
- Größen- und SHA-256-Prüfung, begrenzte HTTPS-Weiterleitungen, Zeit-/Größenlimits, gesperrter Download-Cache und Aufräumen unvollständiger Dateien.
- Optionaler Masterserver-Adapter mit fest gepinntem Ed25519-Schlüssel, Produkttrennung, Ablaufzeit und ohne unsicheren Rückfall auf andere Quellen.
- Erneute Paketprüfung vor einem ausdrücklich ausgelösten Öffnen im Betriebssystem-Installer. Keine stille Installation, kein Umgehen von UAC/Gatekeeper und kein Austausch laufender Programmdateien.
- Offline-Signierwerkzeug, Tests, CI-Vorbereitung und Workflow für einen klar gekennzeichneten SDK-Release-Entwurf.

## Dokumentation

- [Feinkonzept](docs/FEINKONZEPT.md)
- [Updater und App-Start-Integration](docs/UPDATER.md)
- [VeliShell-Masterserver: Integrationsvertrag und fehlende Anbindungsdaten](integration/masterserver/README.md)
- [Release-Ablauf und Paketnamen](docs/RELEASES.md)
- [Prüfstand und Grenzen](docs/TESTSTATUS.md)
- [Änderungsprotokoll](CHANGELOG.md)
- [Sicherheitsgrenzen](SECURITY.md)

## Entwickler-Build

Benötigt werden JDK 21 und Python 3. Der spätere Endnutzer-Installer soll seine Laufzeit enthalten. Der jetzige SDK-Build ist kein solcher Installer.

```sh
python tools/build.py
python tools/create_sdk.py
java -jar build/PZ-Server-Architect-Updater-0.1.0.jar --help
```

Eine bewusst ausgelöste öffentliche Updateprüfung des Zielrepositories ist möglich mit:

```sh
java -jar build/PZ-Server-Architect-Updater-0.1.0.jar --current 0.1.0 --cache ./update-cache
```

`--download` aktiviert zusätzlich den verifizierten Download. Ohne Argumente führt das Hilfsprogramm keinen Netzwerkzugriff aus. Noch fehlende App-Releases werden als fehlend gemeldet, nicht als erfolgreicher Live-Updatetest.

## RuhrpottZ

RuhrpottZ wird später ausschließlich als separate Community-Empfehlung eingebunden, nicht als Konfigurationsvorlage oder Voreinstellung. Server: https://pzservers.net/server/75212135. **Discord folgt.** Keine erfundene Einladung und kein fest einprogrammierter Online-Status.

---
Copyright by Zumbyte - Appsolution | www.zumbyte.de
