# Updater und App-Start

## Implementiert versus noch einzubinden

Das SDK kompiliert mit Java 21. Die neue Desktop-App existiert noch nicht in diesem Repository. `StartupUpdates` ist der ausführbare, getestete Startcontroller, nicht ein bereits angeschlossener Button in einer fertigen GUI.

Für die spätere JavaFX-App:

```java
var config = UpdateClient.Config.github(appVersion, appData.resolve("updates"));
var client = new UpdateClient(config);
var updater = new StartupUpdates(client, Platform::runLater, controller::showUpdateEvent);
// Nach stage.show(), nicht im blockierenden UI-Konstruktor:
updater.start(StartupUpdates.Mode.AUTO_DOWNLOAD);
// Beim tatsächlichen Schließen der Anwendung:
// updater.close();
```

`Platform` und `controller` gehören zur noch zu implementierenden JavaFX-Oberfläche. Das SDK benötigt JavaFX nicht zum Kompilieren. AUTO_DOWNLOAD ist die für dieses Produkt gewünschte App-Voreinstellung. Endnutzer erhalten eine sichtbare Einstellung für manuell, Benachrichtigen oder automatisch laden. Der Updater wird nicht als Betriebssystem-Autostartdienst installiert.

## Ablauf

1. Fenster starten und Updatezustand anzeigen.
2. Neueste stabile, zu Repository und Produkt gehörende Version prüfen.
3. Ein passendes Paket für Betriebssystem und CPU auswählen; fehlt es, ausdrücklich melden.
4. Changelog als Text darstellen, nicht als ungeprüftes HTML.
5. Bei AUTO_DOWNLOAD begrenzt in den App-Cache laden.
6. Größe und SHA-256 prüfen; erst dann den fertigen Dateinamen freigeben.
7. Nach expliziter Installationsaktion und gesichertem Arbeitsstand erneut prüfen und den OS-Installer öffnen.

Der letzte Schritt wird nicht unbeaufsichtigt beim Start ausgelöst. Das Öffnen einer DMG oder DEB garantiert noch keine abgeschlossene Installation oder einen Neustart. Native Installationsautomatisierung, Rollback und GUI-Integration sind nicht implementiert. Die App darf nicht behaupten, ein Update sei installiert, nur weil ein Paket heruntergeladen wurde.

## Quellen

Ohne konfigurierte Masterserver-URL wird das explizit gebundene öffentliche Repository direkt über die GitHub-Releases-API geprüft. Drafts, Prereleases und nicht zu SemVer passende SDK-Tags werden ausgeschlossen. Es werden maximal die 100 zuletzt von dieser API gelieferten Releases betrachtet; innerhalb dieser Auswahl zählt die höchste stabile SemVer-Version.

Fehlt der von GitHub gelieferte SHA-256-Digest, wird kein automatischer Download freigegeben. Integritätsprüfung ist nicht gleichbedeutend mit einer eigenen Herausgebersignatur. Ein kompromittiertes Herausgeberkonto ist außerhalb dieses Schutzumfangs.

Mit explizit konfigurierter HTTPS-Mastermanifest-URI und einem fest gepinnten Ed25519-SPKI-Public-Key wird ausschließlich diese Quelle verwendet. Bei ungültiger Signatur, Ablauf oder Zurücknahme gibt es keinen automatischen GitHub-Fallback, der die Freigabe des Masterservers umgehen würde.

## Benutzer- und Datenschutz

Keine Profile, Kennwörter, Konfigurationsdateien oder Hardwarelisten werden gesendet. Die üblichen Verbindungsdaten wie öffentliche IP fallen beim angesprochenen Anbieter an. Fehlermeldungen geben keine vollständigen URLs, Query-Tokens, privaten Dateipfade oder Serverantworten an die UI weiter.

Download- und Signaturfehler lassen den vorhandenen Arbeitsstand unberührt. Ein beschädigtes bereits fertiges Cachepaket wird abgewiesen; es wird nicht als gültig wiederverwendet. Die spätere UI benötigt dafür eine bewusste Cache-Bereinigungsaktion. Unvollständige `.part`-Dateien werden bei regulärem Fehlerabbruch entfernt; Prozess-/Stromausfall-Bereinigung ist als zusätzliche Startprüfung noch offen.
