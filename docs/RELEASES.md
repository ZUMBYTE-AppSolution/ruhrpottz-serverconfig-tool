# Build- und Release-Ablauf

`main` im [öffentlichen Repository](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool)
enthält veröffentlichte Stände. Unveröffentlichte Arbeit liegt auf `test` im
separaten privaten Repository. GitHub verwaltet Sichtbarkeit je Repository;
ein privater Branch in einem öffentlichen Repository ist nicht möglich.

## Automatische Pakete

Jeder Push nach `main` oder `test` und jeder Pull Request führt die vier
Plattformjobs aus:

| Plattform | Pakete |
|---|---|
| Windows x64 | MSI, portables ZIP, unsigniertes MSIX-Entwicklungspaket |
| macOS Intel | DMG, portables ZIP |
| macOS Apple Silicon | DMG, portables ZIP |
| Linux x64 | DEB, portables TAR.GZ |

Alle Pakete erhalten eine SHA-256-Datei. Die Buildjobs kompilieren Java 21 und
führen die Offline-Prüfungen aus. Linux prüft zusätzlich die echte Oberfläche
und den gebündelten Launcher unter Xvfb. Ein versionierter Tag `vX.Y.Z` auf
einem Commit von `main` startet dieselbe Matrix erneut. Erst wenn alle neun
Pakete und ihre Prüfsummen vollständig und korrekt sind, entsteht ein
öffentliches Preview-Release. Ein vorhandenes Release wird nicht überschrieben.

Das zusätzliche Archiv eines privaten Testlaufs ist optional, wenn GitHub
keinen Actions-Artefaktspeicher mehr zulässt; der Lauf weist dann ausdrücklich
auf fehlende Downloads hin. Build-, UI- und Paketierungsschritte bleiben
verpflichtend. Im öffentlichen Release sind Upload, vollständige Paketmatrix
und Prüfsummenprüfung weiterhin verpflichtende Schritte.

Die stabile Updateprüfung liest ausschließlich GitHub Releases. Sie ignoriert
Preview-Releases und verwendet nur die passenden versionierten Installer.
JARs und portable Archive sind keine automatischen Updateinstaller.

## Windows MSIX

Das Windows-App-Image wird mit MakeAppx aus dem Windows SDK verpackt. Das
Entwicklungspaket heißt `*-dev-unsigned.msix` und enthält eine lokale
Entwicklungsidentität. Es ist ein echtes MSIX, aber ohne vertrauenswürdige
Signatur nicht als Direktinstallation freigegeben. Für eine spätere
Microsoft-Store-Einreichung die reservierten Werte `Package/Identity/Name` und
`Package/Identity/Publisher` als `MSIX_IDENTITY_NAME` und `MSIX_PUBLISHER` im
Build setzen und das Paket über Partner Center einreichen. Die Reservierung,
Store-Zertifizierung und Veröffentlichung sind derzeit offen.

Für sofortiges Testen unter Windows das portable ZIP vollständig entpacken und
`PZServerArchitect.exe` starten. MSI und ZIP gehören zum
[v0.4.1-Preview](https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases/tag/v0.4.1).
Für macOS bleiben Herausgebersignatur und Notarisierung offen.

macOS-Paketmetadaten verwenden wegen der Apple-Versionsregel `1.4.1`;
App-Version, Dateinamen und Release-Tag bleiben `0.4.1`.
