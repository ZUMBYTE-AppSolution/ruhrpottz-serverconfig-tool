# GitHub-only-Updater in der Desktop-App

Die zuvor eigenständige Java-Bibliothek ist jetzt in `App` angeschlossen.
`StartupUpdates` arbeitet auf einem Workerthread; UI-Ereignisse gehen über
SwingUtilities.invokeLater. Beim Start wird die gespeicherte Updateeinstellung
verwendet, standardmäßig AUTO_DOWNLOAD. `--offline` deaktiviert Abfragen in Tests.

App, Updates & Hilfe enthält Modusauswahl, manuelle Prüfung, Downloadfortschritt,
Release-Seite, Changelog und einen Button zum Öffnen eines geprüften Installers.
Vor Installation werden ausstehende Eingaben übernommen, ein schmutziger
Arbeitsbereich gespeichert und die Nutzerbestätigung eingeholt. Das Paket wird
erneut auf Größe und SHA-256 geprüft und dem OS-Handler übergeben.

Es gibt keine VeliShell-Anbindung oder Masterserver-Konfiguration. Quellen sind
an ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool gebunden. Der Client benötigt
kein Token. Versionsnummern werden semantisch statt lexikografisch verglichen.
Drafts, Prereleases, SDK-Tags, alte Versionen und unpassende Plattformdateien werden
ausgeschlossen. Erreichbarkeits-/Rate-Limit-Fehler werden nicht als aktuell gemeldet.

Vorhandene Schutzgrenzen: HTTPS, begrenzte Redirectziele, Dateigröße, SHA-256,
Timeouts, Download-Lock, temporäre Dateien und Prüfen eines wiederverwendeten Cache.
Ein Hash ist keine unabhängige Herausgebersignatur. Ein kompromittiertes
GitHub-Herausgeberkonto bleibt ein Risiko. Keine unbeaufsichtigte Installation,
kein Swap der laufenden Dateien, kein Rollback und kein automatischer Neustart.

Der echte vollständige Live-Updatezyklus ist noch nicht mit dieser App geprüft.
Alle GUI-Prüfungen liefen offline. Netzwerkfehler sollen den Editor nicht blockieren.
