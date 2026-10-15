# Desktop-Releases

Das öffentliche Repository führt freigegebene Stände auf `main` und stellt
versionierte Pakete über [GitHub Releases](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases) bereit.
Unveröffentlichte Entwicklung liegt auf `test` in einem separaten privaten
Repository. GitHub kennt keine privaten Branches innerhalb eines öffentlichen
Repositorys. Das [v0.3.0-Preview](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases/tag/v0.3.0)
ist tatsächlich veröffentlicht.

Die App-Version ist 0.3.0 Preview. Die vorhandene GitHub-only-Updatebibliothek ist
Version 0.2.0; diese beiden Versionen werden nicht gleichgesetzt.

`tools/build.py` baut die eigentliche App und führt die beiden Offline-Testpakete
aus. `tools/package.py` erzeugt ein natives App-Image mit Laufzeit und optional
einen Installer. Die Paketierung erfolgt auf dem jeweiligen Betriebssystem.

Die CI erstellt auf Windows x64, Linux x64, macOS Intel und macOS Apple Silicon
je einen nativen Installer und ein portables Paket mit Java-Laufzeit. Der
Tag `v0.3.0` startet dieselbe Matrix erneut. Nur wenn alle vier Jobs erfolgreich
sind und alle acht Pakete samt SHA-256-Dateien vorliegen und nachgerechnet wurden,
veröffentlicht `desktop-release.yml` ein öffentliches **Prerelease**. Der
Release-Job akzeptiert nur einen Commit, der in `main` enthalten ist, und
überschreibt kein vorhandenes Release.

Für den aktuellen v0.3.0-Tag ist dieser Ablauf erfolgreich abgeschlossen;
die acht Pakete liegen auf der Release-Seite. Der erneute Plattformbau wurde
auch unter [Actions](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/actions)
nachgewiesen.

Der Updater erwartet für stabile Versionen taggebundene Assets:
`PZ-Server-Architect-VERSION-windows-x64.msi`,
`PZ-Server-Architect-VERSION-macos-x64.dmg`,
`PZ-Server-Architect-VERSION-macos-arm64.dmg`,
`PZ-Server-Architect-VERSION-linux-x64.deb`.
Weitere CPU-Kombinationen erst nach entsprechendem echten Build ergänzen.

Die portablen Archive und Java-JARs sind keine automatischen Installerpakete.
Ein Prerelease wird vom stabilen App-Updater nicht automatisch geladen; die
Preview ist über die GitHub-Release-Seite manuell verfügbar.
Stabile Releases verwenden vMAJOR.MINOR.PATCH, enthalten alle freigegebenen
Plattformdateien, Changelog und SHA-256-Digests aus GitHub. Source-only-/SDK-Tags
oder einzelne Commits lösen keine App-Updates aus.

Für Windows-MSI benötigt jpackage 21 kompatible WiX-Tools. Der Windows-2022-
Runner stellt WiX 3.14 bereit; der Workflow setzt dessen Pfad vor der Paketierung.
Apples Paketversionsregel erlaubt keine führende Null. Deshalb tragen macOS-
Paketmetadaten `1.3.0`, während App-Version, Dateinamen und Release-Tag `0.3.0`
bleiben.
macOS-Pakete können unsigniert beziehungsweise ad-hoc-signiert sein. Sicherheits-
funktionen nicht deaktivieren; signierte/notarisierte öffentliche Pakete bleiben
als gesonderte Freigabeaufgabe offen.
