# PZ Server Architect 0.3.0 Preview

Erste bedienbare native Java-Desktopoberfläche mit 360 Katalogeinträgen, neutralen
Vorlagen, Quick Start, Profilen, Import/Export und GitHub-only-Updater.

**Vorabversion, nicht für ungeprüfte Übernahme auf produktive Server.**
Spielbuild, Modkombination, Paketinstallation und vollständiger Updatezyklus
müssen auf den angebotenen Plattformen kontrolliert werden. Ein erfolgreicher
Build ist kein Beleg für einen erfolgreichen Spiel- oder Installationslauf.

Portable Ausgaben enthalten die Java-Laufzeit. Das gesamte Paket entpacken.
Nicht lediglich eine Launcherdatei aus ihrem App-Verzeichnis herauskopieren.
Windows wird als MSI und portables ZIP, macOS für Intel und Apple Silicon als
DMG und portables ZIP, Linux x64 als DEB und portables TAR.GZ angeboten.
Alle Ausgaben enthalten die benötigte Java-Laufzeit. SHA-256-Dateien und eine
gemeinsame SHA256SUMS.txt liegen bei. Der Linux-Launcher wurde unter Xvfb mit
sichtbarem App-Fenster geprüft; Windows/macOS-Launcher wurden beim Build mit
--version gestartet. Die Installer wurden nicht systemweit installiert.
Die macOS-Paketmetadaten verwenden wegen Apples Versionsregel 1.3.0; die
Produktversion und der GitHub-Tag bleiben 0.3.0.
macOS-Pakete sind nicht als mit Apple-Herausgeberzertifikat signiert/notarisiert
freigegeben. Betriebssystem-Sicherheitsfunktionen nicht deaktivieren.

Vorabversionen werden vom stabilen Auto-Updater nicht automatisch heruntergeladen.
Diese Preview wird manuell über die GitHub-Release-Seite bezogen.
Changelog, Handbuch und Prüfgrenzen befinden sich im Repository.

Copyright by Zumbyte - Appsolution | www.zumbyte.de
