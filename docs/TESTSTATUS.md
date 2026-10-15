# Prüfstatus 0.3.0 Preview

## Bestätigt

- Das öffentliche [v0.3.0-Release](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases/tag/v0.3.0) enthält acht Plattformpakete und SHA-256-Dateien.
- Der [Plattformlauf 38703376018](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions/runs/38703376018) war auf Windows x64, macOS Intel und Apple Silicon sowie Linux x64 erfolgreich.
- Alle vier Jobs kompilierten Java 21 und führten 128 Updater- und 941 App-Offline-Prüfungen aus.
- Linux prüfte die Swing-Oberfläche unter Xvfb und startete den gebündelten Launcher mit sichtbarem Fenster.
- Windows erzeugte MSI und portables ZIP, macOS je DMG und portables ZIP, Linux DEB und portables TAR.GZ. Die App-Launcher beantworteten `--version`.

## Offen

- Systemweite Installation aller Pakete und vollständige GUI-Bedienung auf Windows und macOS.
- Vollständiger Live-Update- und Installationszyklus.
- Spieltest mit der konkreten Project-Zomboid-Version, Mods, Multiplayer und AMP.
- Erfolgreicher Liveabruf des RuhrpottZ-Serverstatus.
- Windows-Herausgebersignierung und macOS-Notarisierung.

Ein erfolgreicher Build oder Screenshot ist kein Nachweis für eine erfolgreiche
Installation oder das Verhalten auf einem produktiven Spielserver.
