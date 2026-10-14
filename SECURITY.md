# Sicherheitsgrenzen

Der Updater lädt nur passende stabile Pakete aus dem fest gebundenen PZ-Repository. Die direkte GitHub-Quelle wird über HTTPS angesprochen und verlangt GitHubs Asset-Digest. Ein Hash beweist Integrität, aber keine unabhängige Herausgeberidentität. Die Windows-/macOS-Herausgebersignierung ist hier nicht implementiert.

Ein konfigurierter Masterserver benötigt einen gepinnten Ed25519-Schlüssel. Ungültige, abgelaufene oder produktfremde Manifeste werden nicht durch einen stillen Wechsel zu GitHub umgangen. Schlüssel aus Serverantworten werden nicht als neue Vertrauensanker akzeptiert.

Downloadgröße, Metadatengröße, JSON-Tiefe, Zielpfade und HTTPS-Redirects sind begrenzt. Es wird kein heruntergeladener Shellcode ausgeführt. Die spätere explizite Installationsaktion öffnet das geprüfte Paket lediglich mit dem Betriebssystem-Handler und umgeht keine Schutzmechanismen. Aktuell existieren keine native GUI, Auto-Swap-Installation oder Rollbackimplementierung.

Lokale Angreifer mit denselben Benutzerrechten, kompromittierte Herausgeber-/CI-Konten und kompromittierte Signierschlüssel sind nicht vollständig abgedeckt. Metadaten-Replay innerhalb eines noch gültigen Fensters ist nicht durch persistente Highest-Seen-Daten verhindert. Dies ist kein vollständiger TUF-Client.

Keine privaten Schlüssel, Tokens, Zugangsdaten oder Serverkonfigurationen in Issues, Logs, Releases oder das Repository schreiben. Verdächtige Pakete nicht starten. Bei fehlender Signatur oder Hash-Abweichung bleibt das Update gesperrt.
