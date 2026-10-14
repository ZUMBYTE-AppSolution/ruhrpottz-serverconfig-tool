# GitHub-only Updater und App-Start

## Stand

Java-21-Updater-SDK 0.2.0. `StartupUpdates` ist implementiert und offline getestet; die neue Konfigurator-GUI ist noch nicht vorhanden. Kein Masterserver oder anderer Zumbyte-Dienst wird benötigt, geändert oder angesprochen.

## Einbindung in die spätere JavaFX-App

```java
var config = UpdateClient.Config.github(appVersion, appData.resolve("updates"));
var client = new UpdateClient(config);
var updater = new StartupUpdates(client, Platform::runLater, controller::showUpdateEvent);
// Nach stage.show():
updater.start(StartupUpdates.Mode.AUTO_DOWNLOAD);
// Beim endgültigen Schließen:
// updater.close();
```

`Platform` und `controller` gehören zur noch zu implementierenden Oberfläche. AUTO_DOWNLOAD ist die gewünschte spätere App-Voreinstellung mit sichtbarer Umschaltung auf Benachrichtigen oder manuell. Der SDK-CLI führt ohne Parameter keinen Netzwerkzugriff aus.

## Versionserkennung

Die fest gebundene öffentliche API lautet:

```text
https://api.github.com/repos/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases?per_page=100
```

Bei weiteren Seiten werden ausschließlich passende Seiten-URLs selbst erzeugt, nicht beliebige Link-Adressen übernommen. Höchstens fünf Seiten werden eingelesen; ist die Liste dann noch unvollständig, schlägt die Prüfung ausdrücklich fehl. Kein falscher „aktuell“-Status aus einer abgeschnittenen Liste.

Innerhalb der vollständigen, erlaubten Auswahl wird die höchste stabile SemVer-Version mit der installierten Version verglichen. Die Reihenfolge der GitHub-Antwort ist dafür unerheblich. Drafts, Prereleases und nicht passende SDK-Tags bleiben ausgeschlossen. Ein neuer Commit oder ein allein angelegter Git-Tag ist noch kein App-Release.

Danach muss genau das erwartete Plattformpaket vorhanden und vollständig hochgeladen sein. Dateiname, GitHub-Repository, Tag, Version, Größe und SHA-256-Digest müssen zusammenpassen. Fehlende Pakete oder Prüfsummen werden nicht durch beliebige ZIP-/Quellcode-Dateien ersetzt.

## Ablauf beim Start

Fenster anzeigen, im separaten Worker prüfen, Changelog bereitstellen, passendes neueres Paket herunterladen und prüfen, dann „Update bereit“ melden. Bereits vorhandene gültige Cachepakete können nach erneuter Prüfung verwendet werden. Installation bleibt eine ausdrückliche Aktion nach gesichertem Arbeitsstand.

Der OS-Handler wird erst nach erneuter Paketprüfung geöffnet. Das Öffnen einer MSI/DMG/DEB ist keine Bestätigung einer erfolgreichen Installation. Native Installationssteuerung, App-Neustart und Rollback sind noch nicht umgesetzt.

## GitHub-Abfragelimits und Offlinebetrieb

Für öffentliche Daten ist kein Token erforderlich. GitHub nennt für nicht authentifizierte API-Aufrufe derzeit 60 Anfragen pro Stunde **je öffentlicher Quell-IP**, gemeinsam mit anderen Programmen hinter dieser IP.

Bei HTTP 429 beziehungsweise einem erkannten primären/sekundären Limit bei 403 wird `Retry-After` oder `X-RateLimit-Reset` berücksichtigt; ohne verwertbare Zeit mindestens 60 Sekunden. Weitere Prüfungen derselben Clientinstanz erzeugen vor diesem Zeitpunkt keine Netzwerkanfrage. Die Wartezeit wird nicht über Prozessneustarts gespeichert. Es gibt keine selbsttätige Endlosschleife und keinen Server-Fallback.

Ein normaler Zugriffsfehler 403 ohne Limit-Indikator wird nicht als Rate-Limit ausgegeben. Offline, Timeout und ungültige Metadaten bleiben Fehlerzustände. Die vorhandene App und deren lokale Arbeit dürfen dadurch nicht blockiert werden. Der Zeitpunkt bezeichnet die früheste erneute Prüfung, keine Zusage einer dann erfolgreichen Anfrage.

CLI: Exitcode 0 bei erfolgreich ermitteltem Zustand, 2 bei Abfragelimit und 1 bei anderen Fehlern. Ohne Argumente nur Hilfe.

## Datenschutz und Sicherheit

Keine Profile, Serverkennwörter oder Konfigurationsdateien werden übertragen. GitHub erhält die üblichen Verbindungsdaten einschließlich öffentlicher IP und des App-User-Agents. Release Notes werden als Text behandelt. Prüfsummen ersetzen keine eigenständige Betriebssystem-Herausgebersignatur.

## Offizielle Quellen

- GitHub Releases API: https://docs.github.com/en/rest/releases/releases
- GitHub Rate Limits: https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api

Technischer Abgleich: 14. Oktober 2026. Keine Aussagen über künftig unverändert bleibende Anbieterlimits.
