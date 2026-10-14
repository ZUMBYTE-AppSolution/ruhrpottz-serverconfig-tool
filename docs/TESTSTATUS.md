# Prüfstand 0.3.0 Preview

## Lokal tatsächlich ausgeführt

Umgebung: Debian 13 x64, OpenJDK/javac 21.0.12.1, UTF-8, Xvfb für GUI.

- Java-Code mit --release 21 -Xlint:all -Werror kompiliert.
- 128 bestehende Offline-Prüfungen des GitHub-only-Updaters bestanden.
- 941 Editor-/Profil-/Parser-Prüfungen bestanden: alle Katalogeinträge,
  typisierte Eingaben, Kommentare/Zeilenenden, Duplikate und Codeablehnung,
  Presets, Profilhistorie, Papierkorb, Konflikte, Geheimnisbehandlung und Export.
- 31 UI-Prüfungen: echtes Swing-Fenster, Seitenwechsel, Toggle-Bedienung,
  Modelldaten, kleineres Fenster und gerenderte Screenshots.
- Neun unabhängige Syntaxprüfungen der erzeugten Lua-Dateien mit Lua 5.4; keine Codeausführung und nicht die PZ-Laufzeit.
- Assistent-Vorschau, Übernahme und Profilspeicherung zusätzlich über die echten Buttons geprüft.
- JAR mit eingebetteten Ressourcen und nativem App-Icon gestartet.
- Linux-x64-App-Image mit gebündelter Java-Laufzeit sowie DEB-Paket gebaut.
- DEB in einen separaten Ordner entpackt und dessen nativen Launcher mit der
  gebündelten Laufzeit gestartet: sichtbares 1400×920-Fenster aufgenommen.
  Keine systemweite Installation durch dpkg/apt ausgeführt.

Der vollständige aktuelle Buildbericht liegt im Lieferpaket unter test-report.txt.
Die lokale UI-Prüfung verwendet nur einen separaten temporären Datenordner und
Offline-Modus. Es wurden keine echten Userprofile oder Spielstände geändert.

## Nicht durch diesen Lauf belegt

- Windows-/macOS-Starter, deren native Installer und GUI-Bedienung.
- Eine neue GitHub-CI oder Veröffentlichung. Die vorhandene ältere erfolgreiche
  Updater-CI ist kein Nachweis dieser neuen Desktop-App.
- Vollständige Versionskompatibilität aller Katalogwerte mit sämtlichen PZ-Builds.
- Spieltest mit Project Zomboid, Modkombination, Multiplayer und AMP.
- Erfolgreicher Live-Serverstatusabruf; nur Parser-Fixture geprüft.
- Vollständiger automatischer Paketdownload mit tatsächlicher Updateinstallation.
- Zertifikatssignierung/Notarisierung durch einen Windows- oder Apple-Herausgeber.

Erstellte Bildschirmbilder sind echte lokale Programmdarstellungen, keine Mockups.
