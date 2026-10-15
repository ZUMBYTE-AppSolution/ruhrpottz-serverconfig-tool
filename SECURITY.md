# Sicherheitsgrenzen

## Vertrauensquelle

Der Updater liest ausschließlich das öffentliche GitHub-Repository `ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool`. Es gibt keine konfigurierbare Masterserver-URL, Schlüsselregistrierung, fremde Updatequelle oder VeliShell-Abhängigkeit. Der Client benötigt kein GitHub-Token und lädt keine Serverkonfigurationen, Profile oder Zugangsdaten hoch.

Nur stabile SemVer-App-Releases mit passendem Paketnamen, Zielsystem und einem von GitHub gelieferten SHA-256-Digest werden akzeptiert. Fehlt die Prüfsumme, gibt es keinen unsicheren Fallback. Ein Hash über HTTPS prüft Integrität, ist aber **keine unabhängige Herausgebersignatur**. Die Sicherheit des Herausgeberkontos und der Release-Buildkette bleibt entscheidend. Betriebssystem-Code-Signing ist nicht implementiert.

## Netzwerk und Dateien

Metadaten sind auf exakt die bekannten GitHub-Releaseseiten dieses Repositories begrenzt. Asset-Weiterleitungen dürfen nur GitHub und explizit erlaubte GitHub-Download-CDNs verwenden. Zeit-, Größen- und JSON-Grenzen sowie eine Obergrenze von fünf Releaseseiten verhindern unbeschränkte Verarbeitung. Bei unvollständigen Daten oder Abfragelimits wird kein falscher „aktuell“-Status ausgegeben.

Downloads erhalten temporäre Namen, Größen-/SHA-256-Prüfung und eine Sperrdatei. Vor dem ausdrücklich ausgelösten Öffnen im OS-Installer wird erneut geprüft. Es werden weder heruntergeladene Shellbefehle ausgeführt noch UAC, Gatekeeper oder andere OS-Schutzmechanismen umgangen.

## Bekannte Grenzen

Die Swing-GUI ist seit der Desktop-Preview 0.3.0 enthalten. Kein automatischer Dateiaustausch, kein Rollback und kein vollständiger Live-Installationstest. Rate-Limit-Wartezeiten gelten für die Lebensdauer derselben Clientinstanz, nicht persistent über App-Neustarts. Regelmäßig auftretende Strom-/Prozessabbrüche benötigen später eine ergänzende Bereinigung alter temporärer Dateien.

Lokale Angreifer mit denselben Benutzerrechten, kompromittierte GitHub-/CI-Konten und nach der Prüfung geänderte lokale Dateien sind nicht vollständig abgedeckt. Es gibt keinen kryptografisch signierten eigenen Updatekatalog oder vollständigen TUF-Client.

Keine Tokens, Kennwörter, privaten Schlüssel oder echte Serverdateien in öffentliche Issues, Logs oder Releases schreiben.

## Desktop-Preview 0.3.0

INI/Lua wird als beschränkte Datensyntax gelesen, nicht ausgeführt. Unbekannte
Syntax oder doppelte Schlüssel werden abgewiesen. Native Exporte enthalten
tatsächliche Werte einschließlich Kennwörtern. Gespeicherte Profile entfernen
erkannte Geheimnisfelder; freie Kommentare/Notizen sind nicht garantiert bereinigt.
Keine verschlüsselte OS-Tresorintegration in dieser Preview. Profile werden atomar
gespeichert und auf konkurrierende Änderungen geprüft. Ein ungespeicherter
Arbeitsstand kann bei Prozess-/Stromausfall verloren gehen.

Der öffentliche Serverstatus wird nur nach Klick aus einer festen HTTPS-Seite
gelesen. Ein Fehler bedeutet unbekannt, nicht sicher offline. Keine Telemetrie,
kein Upload von Konfigurationsdateien und kein Zugriff auf laufende Gameserver.
