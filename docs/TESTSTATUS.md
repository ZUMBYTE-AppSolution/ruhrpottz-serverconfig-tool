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

## GitHub Actions am 15. Oktober 2026

[Desktop-Build 38703376018](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions/runs/38703376018)
auf Commit `3a28569298894645c9b141a583e87553a89e8c12` ist in allen vier
Jobs erfolgreich: Windows 2022 x64, macOS 14 arm64, macOS 15 Intel und
Ubuntu 22.04 x64. Jeder Job hat ein Actions-Artefakt hochgeladen.

- Alle Plattformen kompilierten Java 21 und bestanden die 128 Updater- sowie
  941 App-Assertions. Linux führte zusätzlich die Swing-UI-Prüfung unter Xvfb aus.
- Windows erzeugte ein MSI und portables ZIP, macOS je ein DMG und portables
  ZIP, Linux ein DEB und portables TAR.GZ. Die nativen App-Images enthalten
  die Java-Laufzeit; ihre Launcher beantworteten `--version`.
- Der gebündelte Linux-Launcher zeigte unter Xvfb ein echtes 1400×920-Fenster.
- Die Installer wurden nicht systemweit installiert. Die Matrix allein belegt
  noch keine öffentliche Veröffentlichung oder einen kompletten Updatezyklus.

## Weiterhin nicht belegt

- Systemweite Installation der Windows-/macOS-/Linux-Installer und vollständige
  GUI-Bedienung unter Windows/macOS.
- Ein öffentliches Release, solange der Tag-Workflow nicht erfolgreich
  abgeschlossen und die Assets auf GitHub geprüft wurden.
- Vollständige Versionskompatibilität aller Katalogwerte mit sämtlichen PZ-Builds.
- Spieltest mit Project Zomboid, Modkombination, Multiplayer und AMP.
- Erfolgreicher Live-Serverstatusabruf; nur Parser-Fixture geprüft.
- Vollständiger automatischer Paketdownload mit tatsächlicher Updateinstallation.
- Zertifikatssignierung/Notarisierung durch einen Windows- oder Apple-Herausgeber.

Erstellte Bildschirmbilder sind echte lokale Programmdarstellungen, keine Mockups.

## Wiederholungsprüfung vor dem lokalen Git-Commit

Am 14. Oktober 2026 erneut ausgeführt: Java-Build mit 128 Updater- und 941
App-Assertions sowie 31 GUI-Prüfschritte unter Linux/Xvfb. Die README-Bilder
stammen aus diesem erneuten lokalen GUI-Lauf. Berichte: [Build](checks/test-report.txt)
und [GUI](checks/ui-test-report.txt).

Anwendungscode, Katalog, Theme und Icondateien wurden gegenüber dem gelieferten
0.3.0-Quellpaket bytegenau beibehalten. Änderungen dieses Schritts betreffen
Repositorydokumentation, Vorlagen, Screenshots und Workflow-Pinning.

Dieser Absatz beschreibt ausschließlich den damaligen lokalen Prüfstand. Der
spätere Push und der neue GitHub-Actions-Lauf sind oben separat belegt.
