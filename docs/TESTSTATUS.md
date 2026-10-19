# Prüfstatus

## v0.4.1 · bestätigt

- 128 Updater- und 941 App-Prüfungen bestanden.
- 35 Prüfungen am realen WPF-Editor mit lokalem Java-Kern bestanden: automatische
  Übernahme, stabile Listenposition und Reihenfolge, Fokus/Cursor, Undo/Redo,
  ungültige Eingaben, Zurücksetzen und Seitenwechsel mit ausstehender Eingabe.
- 55 Java-UI-Prüfungen bestanden, einschließlich automatischer Werteübernahme
  und Scrollposition bei Bearbeitung und Rückgängig.
- Die automatische Übernahme wurde zusätzlich im sichtbaren Windows-Fenster
  bedient; der ausgewählte Eintrag blieb in der heruntergescrollten Liste stehen.
- Der [öffentliche Release-Lauf 39211135061](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions/runs/39211135061)
  bestand mit allen vier Plattformjobs, den UI-Prüfungen und der vollständigen
  Paketprüfung. [v0.4.1](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases/tag/v0.4.1)
  ist am 19. Oktober 2026 veröffentlicht worden.
- Alle neun Pakete, neun SHA-256-Dateien, `SHA256SUMS.txt` und beide Windows-Aliase
  sind öffentlich erreichbar. Paketprüfsummen und GitHub-Upload-Digests stimmen
  überein; die Alias-Dateien stimmen mit den versionierten Windows-Dateien überein.
- Die anfängliche GitHub-Vorabversionsmarkierung wurde korrigiert. Der öffentliche
  Endpunkt für das neueste reguläre Release liefert v0.4.1. Mit dem unveränderten
  Updater der installierten Windows-Version 0.4.0 wurden Updateerkennung, Download
  des v0.4.1-MSI und SHA-256-Prüfung bestätigt. Die Prüfung mit Versionsstand
  0.4.1 meldet erwartungsgemäß kein neueres Update. Es wurde kein Installer gestartet.
- Die Releasekette veröffentlicht nummerierte Versionen regulär und prüft nach
  der Veröffentlichung, dass das Release weder Entwurf noch Vorabversion ist.

## Bestätigt

- Das öffentliche [v0.4.0-Preview](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases/tag/v0.4.0)
  wurde am 16. Oktober 2026 veröffentlicht. Der
  [Release-Lauf 38968291434](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions/runs/38968291434)
  bestand mit vier Plattformjobs und erfolgreichem Veröffentlichungsjob.
- Alle neun v0.4.0-Pakete, neun SHA-256-Dateien, `SHA256SUMS.txt` und zwei
  Windows-Download-Aliase sind öffentlich erreichbar. Die Prüfsummen der Pakete
  stimmen mit den von GitHub erfassten Upload-Digests überein; beide Aliase
  enthalten dieselben Bytes wie ihre versionierten Dateien.
- Das öffentliche v0.3.0-Preview liegt mit acht Plattformpaketen und
  SHA-256-Dateien auf [GitHub Releases](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases/tag/v0.3.0).
- Der [v0.3.0-Plattformlauf](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions/runs/38703376018)
  war auf Windows x64, macOS Intel/Apple Silicon und Linux x64 erfolgreich.
- Der v0.4.0-Teststand wurde lokal unter Windows mit Java 21 kompiliert.
  128 Updater- und 941 App-Prüfungen bestanden.
- Die native WPF-Oberfläche wurde unter Windows sichtbar gerendert und bedient:
  Übersicht, Einstellungen, Profilwahl, Quick Start mit neun Schritten und
  Zurück/Weiter sowie die neu geordnete Community-Ansicht. Die aktuellen
  README-Bilder stammen aus diesem WPF-Lauf. Die früheren 46 UI-Prüfschritte
  betrafen noch die Java-Oberfläche.
- Ein portables Windows-x64-ZIP und ein echtes, unsigniertes MSIX-Testpaket
  wurden aus dem neuen Stand lokal erstellt. Beide SHA-256-Prüfsummen stimmen.
  Die gebündelte EXE wurde als sichtbares Windows-Fenster gestartet.
- Der private WPF-Vorlauf war in allen vier Jobs erfolgreich. Windows erzeugte MSI, portables ZIP und
  unsigniertes MSIX; macOS und Linux erzeugten ihre Java-basierten Pakete.
  Alle vier Actions-Artefakte wurden heruntergeladen; ihre Archiv-Digests und
  die SHA-256-Dateien aller neun Pakete stimmen.
- Die öffentliche RuhrpottZ-Statusseite antwortete mit HTTP 200. Die WPF-App
  zeigte daraus live Status, Adresse, Spieler, Karte, Version und Modzahl.

## Noch offen

- Die v0.4.0-Paketinstaller wurden noch nicht systemweit installiert.
- Microsoft-Store-Paketidentität, Signierung und Store-Freigabe fehlen.
- Der vollständige Live-Update- und Installationszyklus sowie ein echter
  Spieltest mit Project Zomboid, AMP und Mods sind nicht belegt.
- Die neue UI-Fassung ist auf den anderen Plattformen noch nicht installiert
  oder dort visuell begutachtet worden.

Ein Build oder Screenshot ist kein Nachweis für eine erfolgreiche Installation
oder für das Verhalten auf einem produktiven Spielserver.
