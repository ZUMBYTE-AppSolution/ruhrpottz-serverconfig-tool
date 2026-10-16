# PZ Server Architect 0.4.0 Preview

Die Windows-App verwendet jetzt eine native WPF-Oberfläche mit lesbarer
Systemschrift, dunklen Buttons, linksbündiger Navigation und gestalteten Dropdowns.
Quick Start führt in einem eigenen Fenster durch neun erklärte Schritte mit
Zurück/Weiter und einer Vorschau vor der Übernahme.

Profile lassen sich oben direkt wechseln oder neu anlegen. Der Editor bietet
360 erklärte Katalogeinträge in 17 Bereichen, Suche, acht Vorlagen, Import,
Änderungsprüfung und Export. Der RuhrpottZ-Livestatus zeigt Status, Adresse,
Spieler, Karte, Version und Mods; er wird alle 15 Sekunden aktualisiert.
Ein eigener Discord-Button öffnet die Community-Einladung.

## Downloads

| Plattform | Pakete |
|---|---|
| Windows x64 · native WPF-App | MSI, portables ZIP, unsigniertes MSIX-Testpaket |
| macOS Intel · Java-Oberfläche | DMG, portables ZIP |
| macOS Apple Silicon · Java-Oberfläche | DMG, portables ZIP |
| Linux x64 · Java-Oberfläche | DEB, portables TAR.GZ |

Die benötigten Laufzeiten sind enthalten. Zum sofortigen Windows-Test das
portable ZIP vollständig entpacken und `PZServerArchitect.exe` öffnen.
Jedes Plattformpaket besitzt eine SHA-256-Datei; `SHA256SUMS.txt` enthält die
Prüfsummen der gesamten Plattformmatrix.

## Bekannte Grenzen

Das MSIX verwendet bis zur Reservierung im Microsoft Partner Center eine
Entwicklungsidentität. Es ist kein Store-zugelassenes oder vertrauenswürdig
signiertes Installationspaket. Windows-Herausgebersignierung und
macOS-Notarisierung sind noch offen.

Die Releasekette veröffentlicht nur nach erfolgreichem Plattformbau und
Prüfsummenvergleich. Der Windows-Starter und die Oberfläche wurden getestet;
systemweite Installerinstallationen, ein vollständiger Updatezyklus und
Spieltests mit konkreten PZ-Versionen, AMP und Mods stehen noch aus.

Profile und Konfigurationen bleiben lokal. Es gibt keinen Masterserver und
keinen Konfigurations-Upload. Vorabversionen werden vom stabilen Auto-Updater
nicht automatisch installiert.

Weitere Änderungen: [Changelog](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/blob/v0.4.0/CHANGELOG.md).
