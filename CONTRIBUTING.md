# Entwicklung

## Stand erhalten

Die Desktop-App verwendet Java 21 und Swing. Das Graphit-Lila-Design, das App-Icon,
die technischen Schreibweisen der Einstellungen und die GitHub-only-Updatequelle
nicht ohne bewusste Entscheidung verändern. Fachliche Spielwerte und Empfehlungen
müssen klar von getesteten Standards getrennt werden.

## Vor einem Commit

`python tools/build.py` kompiliert mit `-Xlint:all -Werror` und startet die beiden
Offline-Testsuiten. Unter Linux kann die echte Oberfläche separat geprüft werden:

```sh
xvfb-run -a -s '-screen 0 1680x1100x24' java -cp build/classes:build/test-classes de.zumbyte.pzarchitect.app.UiSmoke build/screenshots
```

Keine Nutzerdaten, Logs, Zugangsdaten, privaten Schlüssel, generierten Installer oder
persönlichen Serverdateien committen. App-Icons und geprüfte UI-Aufnahmen sind eigene
Projektressourcen. Externe Font-Dateien werden nicht mitgeliefert.

## Änderungen beschreiben

Featurebranch verwenden, aussagekräftige Commit-Nachricht, Changelog und README bei
geändertem Verhalten aktualisieren. Tests und bekannte Grenzen im Pull Request nennen.
Keinen Force-Push auf fremde Änderungen, keine automatischen Merges oder stabilen
Releases ohne Prüfung. Der Draft-Workflow erzeugt nur einen Entwurf als Prerelease.

Der Inhalt dieses Dokuments vergibt keine zusätzliche Softwarelizenz.
