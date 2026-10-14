# Desktop-Releases

Die App-Version ist 0.3.0 Preview. Die vorhandene GitHub-only-Updatebibliothek ist
Version 0.2.0; diese beiden Versionen werden nicht gleichgesetzt.

`tools/build.py` baut die eigentliche App und führt die beiden Offline-Testpakete
aus. `tools/package.py` erzeugt ein natives App-Image mit Laufzeit und optional
einen Installer. Die Paketierung erfolgt auf dem jeweiligen Betriebssystem.

Die CI erstellt native portable Pakete für die angebotenen Runner. Der manuelle
Workflow `Draft desktop preview` ruft dieselbe Buildmatrix auf und veröffentlicht
nur einen **Release-Entwurf als Prerelease**. Er wird hier nicht automatisch
angestoßen. Vor Veröffentlichung sind die tatsächlichen Workflow-Ergebnisse und
native Installations-/Starttests zu prüfen. Fehlende Pakete werden nicht erfunden.

Der Updater erwartet für stabile Versionen taggebundene Assets:
`PZ-Server-Architect-VERSION-windows-x64.msi`,
`PZ-Server-Architect-VERSION-macos-x64.dmg`,
`PZ-Server-Architect-VERSION-macos-arm64.dmg`,
`PZ-Server-Architect-VERSION-linux-x64.deb`.
Weitere CPU-Kombinationen erst nach entsprechendem echten Build ergänzen.

Die portablen Archive und Java-JARs sind keine automatischen Installerpakete.
Eine Veröffentlichung als Draft oder Prerelease wird nicht automatisch geladen.
Stabile Releases verwenden vMAJOR.MINOR.PATCH, enthalten alle freigegebenen
Plattformdateien, Changelog und SHA-256-Digests aus GitHub. Source-only-/SDK-Tags
oder einzelne Commits lösen keine App-Updates aus.

Für Windows-MSI benötigt jpackage 21 kompatible WiX-Tools. Der portable Windows-
Build benötigt sie nicht. Die aktuelle CI baut daher auf Windows nur portable;
MSI lässt sich auf einer eingerichteten Windows-Buildmaschine gesondert erzeugen.
macOS-Pakete können unsigniert beziehungsweise ad-hoc-signiert sein. Sicherheits-
funktionen nicht deaktivieren; signierte/notarisierte öffentliche Pakete bleiben
als gesonderte Freigabeaufgabe offen.
