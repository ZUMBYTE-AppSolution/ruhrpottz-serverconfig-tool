# VeliShell-Masterserver: PZ-Erweiterung

## Tatsächlicher Stand

Der Nutzer hat die Wiederverwendung seines existierenden VeliShell-Masterservers vorgegeben. In den erreichbaren Daten wurde bisher keine konkrete Server-URL, API oder Masterserver-Implementierung gefunden. VeliShell `main` wurde bei Commit `4d84f12f1f79b6b09ba560a1eac46ad90673297a` geprüft; dessen README beschreibt direktes GitHub-Releasepolling. Das beweist nicht, dass außerhalb dieses Repositories kein Masterserver existiert.

**Dieser Ordner beschreibt einen neuen Integrationsvertrag, keinen bereits installierten Dienst und keine tatsächlichen vorhandenen API-Endpunkte.** Es wird kein zweiter Masterserver ausgerollt und VeliShell wird nicht geändert.

## Benötigte Anbindungsdaten

Erforderlich sind die vorhandene Basis-URL, der tatsächliche Backend-Quellcode beziehungsweise das Repository, die aktuelle Routingstruktur und der bisherige Updatevertrag. Produktionsschlüssel nicht in Chats oder ins Repository kopieren. Für die App genügt später der öffentliche Verifikationsschlüssel.

## Erweiterungsregel

Eigene Produktkennung: `pz-server-architect`. Eigenes Repository: `ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool`. VeliShells bestehende Routen, Paketnamen, Produktkennung und Clientantworten müssen unverändert funktionieren. Der neue Produktadapter wird neben dem bestehenden Vertrag ergänzt.

Der tatsächliche Routenpfad wird erst gegen den vorhandenen Dienst festgelegt. Der Java-Client nimmt eine vollständige HTTPS-Manifest-URI aus der vertrauenswürdigen App-Konfiguration entgegen; der Pfad oder Host wird nicht aus einer importierten Zomboid-Konfiguration gelesen.

## Signierter Vertrag, Schema 1

Der Server gibt eine JSON-Hülle mit `payload` und `signature` zurück. Beide sind Standard-Base64. `payload` enthält die exakten UTF-8-Bytes des eigentlichen JSON. `signature` ist eine Ed25519-Signatur über exakt diese Bytes. Der Client verifiziert vor dem Parsen des Payloads. Kein vom Server steuerbares `alg`, kein Signaturschlüssel aus derselben Antwort.

Pflichtfelder des Payloads:

| Feld | Bedeutung |
|---|---|
| schema | Zahl 1. |
| product | Exakt `pz-server-architect`. |
| repository | Exakt das freigegebene GitHub-Repository. |
| channel | `stable`. |
| target | Passendes `windows-x64`, `windows-arm64`, `macos-x64`, `macos-arm64`, `linux-x64` oder `linux-arm64`. |
| minimumUpdater | Minimale kompatible Updater-SemVer. |
| issuedAt / expiresAt | ISO-8601-Zeitpunkte mit UTC/Offset. Maximal sieben Tage Gültigkeit, empfohlen wenige Minuten. |
| available | Boolean. false bedeutet derzeit keine Freigabe und löst keinen GitHub-Fallback aus. |

Bei `available=true` zusätzlich: `version`, `tag`, `notes` und `artifact` mit `name`, `url`, `size` und `sha256`. Version, Release-Tag, Dateiname und Zielplattform müssen zusammenpassen. Downloads bleiben an taggebundene Release-Assets des PZ-Repositories gebunden. Metadaten für VeliShell werden vom PZ-Client abgewiesen.

Das SDK unterstützt derzeit einen fest gepinnten öffentlichen Schlüssel. Schlüsselwechsel, Mehrfachschlüssel, persistente Highest-Seen-Metadaten gegen Replay innerhalb eines noch gültigen Zeitfensters und ein vollständiges TUF-Vertrauensmodell sind nicht implementiert. Keine Behauptung eines bereits vollständigen Supply-Chain-Schutzes.

## Veröffentlichung

Nur vollständig gebaute, getestete und bewusst freigegebene App-Releases anbieten. SDK-/Source-Releases bleiben ausgeschlossen. GitHub-Token liegen ausschließlich im Backend beziehungsweise CI-Geheimnisspeicher. Der Desktopclient benötigt kein GitHub-Token.

Ein privater Signierschlüssel wird außerhalb des öffentlichen Repositories verwaltet. Das beigefügte Java-Werkzeug signiert lokal ein vorhandenes Manifest mit einem vorhandenen Ed25519-PKCS#8-PEM-Schlüssel:

```sh
java -cp build/classes de.zumbyte.pzarchitect.update.ManifestSigner manifest.json /sicherer/pfad/private.pem signed-envelope.json
```

Das Werkzeug erstellt nur die Hülle. Es prüft nicht alle fachlichen Freigabebedingungen und veröffentlicht nichts. Eine echte Integration benötigt anschließend Regressionstests der bestehenden VeliShell-Antworten, PZ-Vertragstests und einen tatsächlichen Deploy-/Live-Test.
