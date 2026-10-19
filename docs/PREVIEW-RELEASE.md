# PZ Server Architect 0.4.1

Dieses Update behebt die Werteübernahme und das Zurückspringen der Einstellungsliste.
Auswahlfelder werden sofort übernommen; Text-, Zahlen- und Kennwortfelder nach
350 Millisekunden Eingabepause. Der zusätzliche Übernehmen-Button entfällt.
Die letzte Eingabe wird auch vor einem Seitenwechsel, Speichern oder Export übernommen.

Die Windows-App behält beim Bearbeiten Liste, Reihenfolge, Auswahl, Fokus und
Cursor. Rückgängig/Wiederholen und das Zurücksetzen einzelner Werte springen
ebenfalls nicht mehr an den Anfang. Ungültige Zwischenwerte werden direkt am
Feld angezeigt und überschreiben den letzten gültigen Wert nicht.

Profile lassen sich oben direkt wechseln oder neu anlegen. Der Editor bietet
360 erklärte Katalogeinträge in 17 Bereichen, Suche, acht Vorlagen, Import,
Änderungsprüfung und Export. Der RuhrpottZ-Livestatus zeigt Status, Adresse,
Spieler, Karte, Version und Mods; er wird alle 15 Sekunden aktualisiert.
Ein eigener Discord-Button öffnet die Community-Einladung.

## Downloads

**Update von 0.4.0:** Unter „Hilfe & Updates“ auf „Jetzt prüfen“ und danach
„Paket laden“ klicken. v0.4.1 war zunächst fälschlich als GitHub-Vorabversion
markiert und wurde dadurch vom Updater ignoriert. Die Kennzeichnung ist
korrigiert; die vorhandenen Pakete sind unverändert. Die App kann intern
weiterhin „Preview“ anzeigen. Das beschreibt ihren Entwicklungsstand.

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

Lokal bestanden 35 WPF-Editorprüfungen, 55 Prüfungen der Java-Oberfläche sowie
128 Updater- und 941 App-Prüfungen. Die Releasekette wiederholt die Prüfungen
und veröffentlicht nur nach erfolgreichem Plattformbau und Prüfsummenvergleich.
Der Windows-Starter und die Oberfläche wurden getestet;
systemweite Installerinstallationen, ein vollständiger Updatezyklus und
Spieltests mit konkreten PZ-Versionen, AMP und Mods stehen noch aus.

Profile und Konfigurationen bleiben lokal. Es gibt keinen Masterserver und
keinen Konfigurations-Upload. Entwürfe und ausdrücklich als GitHub-Vorabversion
markierte Releases werden vom Updater nicht angeboten.

Weitere Änderungen: [Changelog](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/blob/v0.4.1/CHANGELOG.md).
