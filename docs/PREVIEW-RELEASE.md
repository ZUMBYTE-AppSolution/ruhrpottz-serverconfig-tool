# PZ Server Architect 0.4.0 Preview

Die neue native Windows-Oberfläche führt über Import, Quick Start und Einstellungen.
Der Editor bietet eine direkte Auswahl aller 17 Bereiche. Karten, Schaltflächen,
Abstände und Lesbarkeit wurden überarbeitet. Profile, Vorlagen, Prüfung, Export
und der GitHub-only-Updater bleiben erhalten. macOS und Linux verwenden in
diesem Teststand weiterhin die Java-Oberfläche.

Windows x64: MSI, portables ZIP und ein **unsigniertes MSIX-Entwicklungspaket**.
macOS: DMG und portables ZIP für Intel und Apple Silicon. Linux x64: DEB und
portables TAR.GZ. Die Pakete enthalten die benötigte Laufzeit; Windows bündelt
.NET und die lokale Java-Konfigurationslogik.

Das MSIX verwendet bis zur Reservierung im Microsoft Partner Center eine
Entwicklungsidentität. Es ist kein Store-zugelassenes oder vertrauenswürdig
signiertes Installationspaket. Für einen sofortigen Windows-Test das portable
ZIP vollständig entpacken und `PZServerArchitect.exe` öffnen.

Die Releasekette veröffentlicht die Version nur, wenn alle Plattformjobs und
SHA-256-Prüfungen erfolgreich sind. Ein erfolgreicher Build beweist weder eine
Installerinstallation noch die Kompatibilität mit jeder PZ-Version oder Mod.
Vor dem Einsatz auf einem produktiven Server Backup und Testinstanz verwenden.

Vorabversionen werden vom stabilen Auto-Updater nicht automatisch installiert.
Weitere Änderungen: [Changelog](../CHANGELOG.md).
