# PZ Server Architect: Produktrichtung

**Soll-Konzept, nicht mit der aktuellen Implementierung gleichzusetzen.** Die aktuelle Umsetzung steht im README und CHANGELOG.

## Allgemeine App statt einzelner Server-Konfiguration

Neutraler Desktop-Konfigurator für Project Zomboid mit Server-INI und SandboxVars. Survival, Koop, PvE, PvP, Roleplay und Testbetrieb werden gleichermaßen unterstützt. Keine fest eingebrannte RuhrpottZ-Vorlage, kein genereller Zwang zu einer Welt ohne Zombies. Ein ausdrücklich gewähltes ziviles Profil bleibt möglich.

Name: PZ Server Architect. Herausgeber: `Copyright by Zumbyte - Appsolution | www.zumbyte.de`. Das Repository darf seinen bestehenden Namen behalten. RuhrpottZ ist lediglich eine separate Community-Empfehlung mit dem Text `German ZomboID Roleplay Project`, der angegebenen pzservers.net-Seite und dem Hinweis `Discord folgt`.

## Design und Start

Bestehendes Graphit-Lila-Design und vorhandenes PZ-App-Icon beibehalten. Dezente Verläufe, Button-Glow, 6 logische Pixel Grundradius, keine Scanlines. UI-Größe über Schrift-, Abstands- und Control-Maße ändern, nicht über Gesamtzoom. JavaFX-Oberfläche mit Sidebar, Arbeitsbereich, Hilfe-Panel und sichtbarem Speicher-/Prüfzustand. Keine als Desktop-App verkleidete lokale HTML-Datei.

Erster Start mit **Assistent**, **Dateien importieren** oder **Profil öffnen**. Beschädigter Arbeitsstand führt zu Wiederherstellungsangeboten, nicht zu einem leeren Fenster. Offline müssen Konfiguration, Profile und Hilfen weiterhin funktionieren.

## Navigation

| Seite | Aufgaben |
|---|---|
| Übersicht | Arbeitsbereich, Zielbuild, Dateipaar, Änderungen und Warnungen. |
| Quick Start | Fragenbasierte Erstellung mit begründeten Vorschlägen. |
| Server & Zugang | Name, Slots, Sichtbarkeit, Kennwort, Zugang und Netzwerk. |
| Multiplayer & Schutz | PvP, Häuser, Fraktionen, Voice, Chat und Namen. |
| Welt, Zeit & Wetter | Tageslänge, Startbedingungen, Wetter, Versorgung und Erosion. |
| Zombies | Population, Verhalten, Wiederkehr, Verteilung und Infektion. |
| Loot & Wirtschaft | Gegenstandsgruppen, Nachschub und Haltbarkeit. |
| Charakter & Gesundheit | Bedürfnisse, Schlaf, Verletzungen, XP und Lesen. |
| Fahrzeuge | Verfügbarkeit, Zustand, Schlüssel, Verbrauch und Schäden. |
| Natur & Landwirtschaft | Belegte Tier-, Pflanzen- und Ertragsoptionen. |
| Wartung & Leistung | Speicherung, Backups und kontrollierte Bereinigung. |
| Mods & Karten | Mod-/Workshop-IDs, Reihenfolge und erkannte Modfelder. |
| Profile & Historie | Speichern, Kopieren, Vergleichen, Löschen und Wiederherstellen. |
| Servermonitor | Öffentliche Daten mit Quellen- und Zeitangabe. |
| Prüfen & Export | Fehler, Diff, Quelltext, native Dateien und ZIP. |
| App & Hilfe | Skalierung, Sprache, Katalog, Diagnose und Updates. |

## Eingaben und ausführliche Werte-Erklärungen

Jedes bekannte Feld zeigt technischen Schlüssel, Datentyp, erlaubte Werte, Einheit, Grenzen, Sonderwerte, Originalwert, aktuellen Wert, verifizierten Standard und nachvollziehbare Auswirkungen. Dropdown-Auswahlen brauchen eine eigene Erklärung pro Stufe. Ein Empfehlungsvorschlag ist kein offizieller Standardwert.

Echte Booleans erhalten Toggle-Switches. Dokumentierte numerische 0/1-Schalter ebenfalls, unter Erhalt der technischen Schreibweise. Ein beliebiges Zahlenfeld wird nicht zum Toggle, nur weil sein aktueller Wert 0 oder 1 ist. `PVP=true/false` darf nicht in `PVP=1/0` umgeschrieben werden. Fehlend ist ein zusätzlicher Bearbeitungszustand und nicht identisch mit false.

Zahlenfelder erhalten Einheit, Schrittweite und Eingabevalidierung. Sonderwert 0 wird gesondert erklärt. Bei Wiederkehr-Intervallen kann beispielsweise 0 deaktivieren und eine positive Zahl ein Intervall bedeuten; deshalb bleibt der Zahlenwert bearbeitbar. Alle konkreten Grenzen, Enum-Zuordnungen und Wirkungen müssen am gewählten Build geprüft werden.

Clientseitige Grafikoptionen werden getrennt dargestellt. Keine Behauptung, ein serverseitiger Wert könne maximale Renderdistanz oder beliebige Kamerafunktionen auf jedem Client erzwingen.

## Vorlagen

Unveränderte Originalbasis des gewählten Builds, Koop & Freunde, PvE Community, Survival ausgewogen, Hardcore Survival, PvP Survival, Roleplay mit Zombies, Ziviles Roleplay ohne Zombies, Bauen & Testen und Langzeitwelt. Vorlagen sind schreibgeschützt; Bearbeitung erzeugt eigene Profile.

Vorlagen ändern keine Kennwörter, Konten, Hostadressen, Workshop-IDs oder Kartenreihenfolgen. Leistungsvorschläge sind keine Zusage bestimmter FPS/Spielerzahlen und dürfen keine Integritätsprüfungen pauschal abschalten. Eine neue Welt ohne Zombies löscht keine vorhandenen Zombies aus einem alten Save.

## Quick-Start-Fragen

| Frage | Antwortmöglichkeiten |
|---|---|
| Ausgangspunkt? | Neu, vorhandene Dateien, Vorlage kopieren. |
| Zielversion/Welt? | Geprüfter Build; neue oder bestehende Welt. |
| Spielrichtung? | Koop, PvE, Survival, PvP, Roleplay, Bau/Test. |
| Zombiepopulation? | Aus, niedrig, normal, hoch, individuell. |
| Bedrohung/Wiederkehr? | Einfach, ausgewogen, gefährlich; Wiederkehr gesondert ja/nein. |
| Loot/Nachschub? | Knapp, ausgewogen, großzügig; nie oder kontrolliert. |
| Tageslänge? | Lesbare reale Zeitwerte aus dem Buildkatalog. |
| Komfort? | Realistisch, ausgewogen, entspannt. |
| PvP? | Aus, mit Sicherheitssystem, offen. Keine garantierte Einvernehmlichkeit behaupten. |
| Versorgung? | Früher Ausfall, später Ausfall, dauerhaft. |
| Fahrzeuge? | Knapp, normal, Schwerpunkt. |
| Zugang/Größe? | Öffentlich oder privat, tatsächlicher Schutz und Slots. |
| Wetter/Kartenkomfort? | Belegte Optionen mit abschließender Gesamtschau. |

Jede Frage erlaubt unverändert lassen, Vorschlag übernehmen oder selbst festlegen. Vorschläge nennen ihren Grund und überschreiben keine gesperrten manuellen Entscheidungen. Ergebnis: Profil, lesbare Zusammenfassung, Original-/Neuwert-Diff und kontrollierter Export. Kein automatischer Serverneustart oder Save-Reset.

## Profile und Dateien

Lokale persistente Profile außerhalb des Installationsordners, ohne Browser-LocalStorage. Anlegen, Speichern, Umbenennen, Duplizieren, Tags, Notizen, JSON-Import/-Export, Vergleich, Versionen, Undo/Redo und Papierkorb. Keine künstliche 100-Profile-Grenze. Beschädigte Profile isolieren, Sicherungen und Wiederherstellung anbieten.

Geheimnisse standardmäßig nur in der Sitzung; dauerhafte Speicherung nur in unterstütztem OS-Geheimnisspeicher. Geteilte Profile und Diagnoseberichte vor dem Export auf Geheimnisse prüfen. Native Serverdateien benötigen ihre echten Zugangswerte; fehlende Geheimnisse dürfen Schutz nicht unbemerkt entfernen.

INI- und Lua-Import erhalten unbekannte Werte, Kommentare und Reihenfolgen soweit unterstützt. Lua wird niemals ausgeführt. Komplexes nicht unterstütztes Lua wird nicht still beschädigt exportiert. Duplikate, externe Dateiänderungen, unpassende Dateipaare und fehlende Rechte werden angezeigt. Dateiübernahme mit Vorschau, Sicherung und Wiederherstellungsjournal für das Dateipaar. AMP-Overrides erklären, nicht heimlich überschreiben.

## Katalog und Prüfgrenzen

Spielbuild, Dateischema und Katalogversion getrennt speichern. Nur tatsächlich geprüfte Felder als geprüft kennzeichnen; unbekannte Modoptionen bleiben unbekannt statt erfundene Wirkungen zu erhalten. Versionswechsel brauchen eine bestätigte Migration. Profile werden durch ein Katalogupdate nicht automatisch geändert.

Prüfstufen: Fehler, Warnung und Hinweis. Typen, Grenzen, Widersprüche, Zugangswerte, Sonderfälle und Exportfähigkeit prüfen. Syntaxprüfung ist kein Spieltest.

## Servermonitor

RuhrpottZ ist unter https://pzservers.net/server/75212135 gelistet. Keine veralteten Spielerzahlen fest ins Programm schreiben. Jeder Abruf zeigt Quelle, Zeit und bei Problemen den letzten bekannten Stand. Ein Timeout ist keine sichere Offline-Diagnose. Eine API-Adresse muss tatsächlich geprüft werden. A2S ist ein optionaler gesonderter Adapter mit bestätigtem Query-Port.

Öffentlich nicht verfügbare CPU-/RAM-Daten, vollständige Modlisten oder Uptime nicht erfinden. Kein Tracken von Spielerpositionen. Discord bleibt ohne Einladung deaktiviert. Werbung blockiert keine Funktion und überschreibt kein Serverprofil.

## Updates und Releases

Automatische Prüfung und verifizierter Download beim App-Start, mit sichtbarer Einstellung. Installation erst nach gesichertem Arbeitsstand und ausdrücklicher Installationsaktion. Updates vollständig über GitHub Releases des festen Repositories: Versionen, Changelogs, native Plattformpakete und SHA-256-Digests. Kein Masterserver, keine VeliShell-Abhängigkeit und keine Tokens im Client. Abfragelimits und unvollständige Prüfungen ausdrücklich anzeigen statt einen falschen aktuellen Stand zu melden.

Die derzeitige Implementierung deckt davon den Java-Updaterbaustein ab. GUI, nativer Konfigurator, Installer und vollständiger Live-Installationstest bleiben offen. Zielpakete enthalten später eine Java-Laufzeit. Jede Windows-/macOS-/Linux-Architektur muss separat gebaut und gestartet werden, bevor sie als unterstützt gilt.
