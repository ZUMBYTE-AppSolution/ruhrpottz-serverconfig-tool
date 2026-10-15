# PZ Server Architect 0.4.0 Preview
Copyright by Zumbyte - Appsolution | www.zumbyte.de

## Start

Dies ist eine echte Java-Desktop-Anwendung mit Swing, kein Browserfenster.
Das Java-Paket benötigt Java 21 oder neuer. Entpacke das gesamte Paket und
starte START-Windows.cmd, START-macOS.command oder START-Linux.sh.
Unter Linux kann je nach Desktop zuerst das Ausführungsrecht nötig sein:
`chmod +x START-Linux.sh`.

Ein nativ verpacktes Programm enthält die Java-Laufzeit. Das lokale Linux-Paket
wurde auf Debian 13 x64 gebaut. Andere Distributionen können Bibliotheken oder
eine neuere glibc benötigen. Die Java-Ausgabe ist die Alternative mit einer
passenden Java-21-Laufzeit deines Systems.

Das portable Windows-Paket wurde als echte Desktop-App gestartet und bedient.
Der private Actions-Workflow baut zusätzlich MSI, ein unsigniertes MSIX-Testpaket,
macOS-DMGs und Linux-DEB. Eine systemweite Installation dieser Pakete ist noch
nicht bestätigt. Die Oberfläche verwendet Swing.

## 1. Ausgangspunkt

- Neues Profil: minimale eigene Ausgangsbasis, KEIN offizielles Vanilla-Preset.
- Dateipaar importieren: `<name>.ini` und `<name>_SandboxVars.lua` auswählen.
- Meine Profile: einen vorher gespeicherten Arbeitsstand öffnen.

Die Dateinamen müssen zusammenpassen. Originaldateien werden nicht überschrieben.
Der Katalog basiert auf Build 42. Verwende bevorzugt die Dateien, die genau dein
Serverbuild erzeugt hat. VERSION in der Lua-Datei ist das Dateischema und NICHT
die Spielversionsnummer. Neue Arbeitsstände verwenden als Ausgangsbasis Schema 6.

## 2. Einstellungen richtig bedienen

360 Einträge sind im Katalog, zusätzliche skalare Mod-Einstellungen werden beim
Import sichtbar. Suche mit Enter öffnen; innerhalb der Einstellungsansicht wird
bereits beim Tippen gefiltert. Suche findet Titel, Schlüssel und Originalkommentare.

Bei jeder Einstellung siehst du Wirkung, Einheit, bekannten Wertebereich,
Auswahlstufen, den importierten Originalwert und den aktuellen Wert. Bei nicht
belegten Grenzen oder Modwirkungen wird die Unsicherheit ausdrücklich angezeigt.
Nicht alle 360 Felder besitzen vollständig verifizierte Wertebereiche für jeden
Build. Der Katalog ist kein Nachweis universeller Versionskompatibilität.

Ein/Aus-Funktionen sind echte Toggle-Switches. Technische true/false-Werte bleiben
Booleans. Dokumentierte numerische 0/1-Schalter behalten ihre Kodierung. Eine normale
Zahl 0 oder 1 wird nicht allein wegen ihres aktuellen Werts zum Schalter.

"Nicht gesetzt" heißt: dieser Schlüssel steht nicht in der Datei. Es ist weder
false noch 0. Bei Dropdowns entfernt die Auswahl "Nicht gesetzt" den Schlüssel.
"Original wiederherstellen" stellt den importierten Wert wieder her oder entfernt
eine seitdem hinzugefügte Einstellung.

Mehrfachauswahlen erhalten Dropdowns. Zahlen erhalten editierbare Felder mit
Validierung. Text-/Zahlenänderungen werden mit Anwenden oder Enter übernommen;
beim Seitenwechsel wird eine ausstehende gültige Eingabe ebenfalls übernommen.
Ungültige Eingaben verhindern den Seitenwechsel. Originalkommentare mit eindeutig
nummerierten `1 = ...`-Zeilen können eine Auswahlliste ergänzen/ersetzen; Min/Max-
Angaben können Grenzen liefern. Kommentare selbst sind keine externe Verifikation.

Filter: alle, vorhandene Werte, Änderungen, Favoriten und zusätzliche Mod-Felder.
Favoriten werden in den lokalen App-Einstellungen gespeichert.

## 3. Vorlagen und Quick Start

Acht allgemeine Vorschläge: Koop & Freunde, PvE Community, Survival ausgewogen,
Hardcore Survival, PvP Survival, Roleplay mit Zombies, Ziviles Roleplay,
Bauen & Testen. Es gibt kein RuhrpottZ-Konfigurationsprofil.

Der Assistent öffnet ein eigenes Fenster mit neun erklärten Schritten und
Zurück/Weiter: Spielrichtung, Zombiepopulation, Tageslänge, Alltagsloot,
Zombie-Wiederkehr, PvP, Lernfortschritt, Versorgung und Wetter/Sicht.
"Wie Vorlage" übernimmt die gewählte Spielrichtung.
Wetter und Versorgung bleiben ohne bewusste Antwort unverändert.

Vor der Übernahme erscheint ein Vergleich. "Nur bereits vorhandene Schlüssel
ändern" verhindert ungefragtes Ergänzen. Vorschläge verändern keine Servernamen,
Kennwörter, Ports, Workshop-/Mod-IDs oder Kartenreihenfolgen. Eine Vorlage ist ein
einzelner rückgängig machbarer Bearbeitungsschritt.

Vorlagen verwenden den eingebauten B42-Katalog. Besonders Tageslängen- und andere
Auswahlindizes können zwischen Builds anders sein. Bei abweichenden Kommentaren
nicht ungeprüft Vorlagen auf ältere Dateien anwenden; manuell kontrollieren.
Grobe ZombieRespawn-Auswahlen importierter Welten werden nicht blind umgedeutet.
Der Editor weist auf bestimmte widersprüchliche Kombinationen hin.

Population 0 löscht KEINE bereits gespeicherten Zombies. Die Konfiguration ändert
keine Savegames. Creative/Bauen ist kein Godmode und erteilt keine Adminrechte.
Klare Sicht/Tag regeln Wetter/Licht, nicht unbegrenzte clientseitige Renderdistanz.

## 4. Profile und Daten

Profile können oben über „Aktives Profil“ gewechselt oder neu angelegt sowie
gespeichert, geöffnet, dupliziert, umbenannt und in den Papierkorb verschoben
werden. Notizen und ein Serverdateiname gehören zum Profil.
Jede weitere Speicherung sichert die vorige Fassung; maximal zehn alte Fassungen
pro Profil bleiben erhalten. Eine Wiederherstellung erzeugt eine unabhängige Kopie.
Änderungen im aktuellen Arbeitsbereich besitzen bis zu 40 Undo-/Redo-Schritte.

Speicherung erfolgt als lokale JSON-Dateien außerhalb des Installationsordners:
Windows: %LOCALAPPDATA%\PZServerArchitect
macOS: ~/Library/Application Support/PZServerArchitect
Linux: $XDG_DATA_HOME/pz-server-architect bzw. ~/.local/share/pz-server-architect

Dateien werden atomar ersetzt, mit Schreibsperre und Konfliktprüfung bei Änderungen
durch eine andere Instanz. Beschädigte Profile werden als fehlerhaft angezeigt,
nicht still überschrieben. Ein Stromausfall kann dennoch einen ungespeicherten
Arbeitsstand verlieren: es gibt noch keine laufende Wiederherstellung jeder Eingabe.

Profil-JSONs können einzeln importiert/exportiert werden. Browserprofile des alten
HTML-Konfigurators werden nicht automatisch migriert. Nutze dafür dessen native
INI/Lua-Ausgabe. Tags, Sammelbackup aller Profile und Profil-zu-Profil-Diff sind in
dieser Preview noch nicht eingebaut. Vorhanden ist der Vergleich mit dem Original.

## 5. Kennwörter und Geheimnisse

Erkannte Kennwort-/Token-/Secret-Felder werden standardmäßig nicht im Profil
persistiert. Im aktuellen Arbeitsspeicher bleiben die Werte für den nativen Export
verfügbar. Beim erneuten Öffnen musst du entfernte Geheimnisse bewusst ergänzen
oder bewusst leer übernehmen. Bis dahin wird der Serverexport blockiert.

Freie Kommentare und Notizen können sensible Texte enthalten. Prüfe sie selbst
vor dem Teilen; das Werkzeug ist kein universeller Geheimnisscanner. Es gibt in
dieser Preview keine verschlüsselte Speicherung im Betriebssystem-Tresor.
Native Server-ZIPs enthalten tatsächliche Werte und möglicherweise Kennwörter!

## 6. Import, Quelltext und Export

INI-Schlüssel und unterstützte Lua-Datentabellen werden ohne Codeausführung
gelesen. Kommentare, unbekannte skalare Felder, Reihenfolgen und Zeilenenden
bleiben bei punktuellen Änderungen weitgehend erhalten. Ein reiner Import verändert
den Text nicht. Ein zusätzlicher Tabellenblock kann die Einrückung ergänzen.

Unterstützt: SandboxVars-Zuweisung, verschachtelte Tabellen, Zeichenketten,
Boolesche Werte, Dezimalzahlen, nil, einfache Arrays, zitierte und numerische
Schlüssel und Kommentare. Nicht unterstützt sind ausführbarer Lua-Code, Funktionen,
Ausdrücke, doppelte Schlüssel, sehr tiefe Tabellen und bestimmte Lua-Sonderformen.
Unbekannte oder unsichere Syntax wird abgewiesen, statt sie beschädigt zu exportieren.
INI-Dateien mit Sektionen werden nicht als normale PZ-Serverdateien akzeptiert.
Dateigrößen sind auf 1 MB je importierter Datei begrenzt.

Der Quelltexteditor prüft beim Übernehmen. Die Exportseite zeigt maskierte
Änderungen und Hinweise. Exportiert wird ein NEUES ZIP mit beiden korrekt benannten
Dateien und einer Warn-/Installationsdatei. Vorhandene ZIPs werden nicht überschrieben.
Es gibt keine direkte Übernahme in einen laufenden Server und keinen SSH/AMP-Zugriff.

Vor der Übernahme: Server stoppen, vollständiges Backup erstellen, Dateien prüfen,
Dateipaar ersetzen und Startlog ansehen. AMP kann Panelwerte darüber schreiben.
Kein Save-Reset, kein Serverneustart, kein Workshop-Upload durch dieses Programm.

## 7. Updates

Der existierende GitHub-only-Updater ist jetzt mit der Oberfläche verbunden.
Standard: beim Start prüfen und passende neuere stabile Pakete automatisch laden.
Unter App, Updates & Hilfe kann auf Benachrichtigung oder manuell umgestellt werden.
Das Verhalten wird gespeichert. Kein eigener Masterserver, kein Nutzer-Token.

Drafts, Prereleases, SDK-Tags, ältere Versionen und fremde Repositories werden nicht
angeboten. Passende Dateien müssen exakte Plattformnamen, Größe und GitHub-SHA-256
besitzen. Fehler und API-Limits werden nicht als "aktuell" dargestellt.

Heruntergeladen ist nicht installiert. Ein geprüftes Paket wird erst nach einer
bewussten Aktion und gespeichertem Arbeitsstand an den OS-Installer übergeben.
Keine stille Installation, kein Rollback, kein automatisches Beenden/Neustarten
der App. Die konkreten Pakettypen sind MSI unter Windows, DMG unter macOS und DEB
unter Linux. Andere Linux-Paketmanager sind damit nicht automatisch abgedeckt.

Ein ZIP/JAR der Preview ist kein automatisches App-Update. Ein Release muss erst
vollständig gebaut, hochgeladen und als stabil veröffentlicht sein, damit es
angeboten wird. Der öffentliche Stand ist v0.3.0; v0.4.0 bleibt Teststand.
Die App arbeitet auch ohne Netzwerk. `--offline` unterbindet in Testläufen ihre
Update- und Serverabfragen; bewusst geöffnete Browserlinks bleiben eigene Aktionen.

## 8. RuhrpottZ und Serverstatus

RuhrpottZ ist ein getrennter Community-Bereich: German ZomboID Roleplay Project.
Der Discord-Button öffnet die Einladung https://discord.gg/hWz2HJcakH.

Beim Öffnen und danach alle 15 Sekunden wird der öffentliche Status über HTTPS
von pzservers.net/server/75212135 abgerufen, solange die Seite sichtbar ist.
Quelle und Abrufzeit werden angezeigt. Änderungen am Seitenformat, Sperren oder
Netzfehler bedeuten "unbekannt", nicht sicher "offline". Der Java-Abruf und die
Auswertung der Live-Seite wurden geprüft; die Serverliste kann zeitversetzt sein.
Keine erfundenen CPU-/RAM-/Ping-/Uptime-Werte und kein automatisches Spielertracking.

## 9. Hilfe und Grenzen

Strg/Cmd+S Speichern, Strg/Cmd+F Suchen, Strg/Cmd+E Export,
Strg/Cmd+Z Rückgängig, Strg/Cmd+Y Wiederholen.
UI-Größe 90/100/110/125 Prozent gilt nach App-Neustart.

Die Oberfläche wurde unter Windows als echtes Swing-Fenster geprüft. Windows,
macOS und Linux wurden für die öffentliche Version 0.3.0 auf GitHub gebaut.
Die private 0.4.0-Plattformmatrix war erfolgreich; die aktuelle Neugestaltung
wird erneut gebaut. Ein Spieltest mit Project Zomboid, deiner Modkombination und
AMP steht aus. Aktuelle Ergebnisse: docs/TESTSTATUS.md.

Unabhängiges Werkzeug, nicht mit The Indie Stone verbunden.
