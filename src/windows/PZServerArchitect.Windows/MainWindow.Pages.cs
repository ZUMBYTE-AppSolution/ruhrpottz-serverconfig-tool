using Microsoft.Win32;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace PZServerArchitect.Windows;

public partial class MainWindow
{
    private void OpenWizard()
    {
        var wizard = new WizardWindow(core, state.B("imported")) { Owner = this };
        if (wizard.ShowDialog() == true && wizard.UpdatedState is { } result)
        {
            state = result; ReadState(); RefreshProfiles(); Navigate("export"); Notice("Quick Start übernommen");
        }
    }

    private UIElement HomePage()
    {
        var page = Column();
        var hero = new Grid(); hero.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) }); hero.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(250) });
        var lead = Column();
        lead.Children.Add(Text("DEIN SERVERPROJEKT", 11, muted: true, bold: true));
        var headline = Text("Eine klare Basis für deine nächste Welt.", 28, bold: true); headline.Margin = new Thickness(0, 9, 0, 10); lead.Children.Add(headline);
        lead.Children.Add(Text("Importiere echte Serverdateien oder starte mit dem geführten Assistenten. Prüfe jede Änderung vor dem Export.", 15, muted: true));
        var buttons = Row(Action("Dateien importieren", ImportPair, true, "folder"), Action("Quick Start", () => Navigate("wizard"), false, "wizard"));
        buttons.Margin = new Thickness(0, 25, 0, 0); lead.Children.Add(buttons); hero.Children.Add(lead);
        var mark = Card(new StackPanel { Children = { Text("360", 39, bold: true), Text("erklärte Einstellungen", 13, muted: true), Text("Build 42 · mit eigenen Dateien abgleichen", 11, muted: true) } }, new Thickness(22));
        mark.Background = Brush("#35294C"); mark.Margin = new Thickness(20, 0, 0, 0); Grid.SetColumn(mark, 1); hero.Children.Add(mark);
        var heroCard = Card(hero, new Thickness(32)); heroCard.Margin = new Thickness(0, 0, 0, 20); page.Children.Add(heroCard);
        var summary = new Grid(); summary.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) }); summary.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        var current = Column(); current.Children.Add(Text("AKTIVES PROFIL", 11, muted: true, bold: true));
        current.Children.Add(Text(state.S("name"), 22, bold: true)); current.Children.Add(Text(state.S("baseName") + ".ini + SandboxVars.lua", 13, muted: true));
        current.Children.Add(Text(state.B("dirty") ? "Änderungen noch nicht gespeichert" : "Bereit zum Bearbeiten", 12, muted: true));
        var currentCard = Card(current); currentCard.Margin = new Thickness(0, 0, 10, 0); summary.Children.Add(currentCard);
        var progress = Column(); progress.Children.Add(Text("ARBEITSSTAND", 11, muted: true, bold: true));
        progress.Children.Add(Text(state.I("changed") + " geänderte Werte", 22, bold: true));
        progress.Children.Add(Text("Importieren  →  bearbeiten  →  prüfen  →  ZIP exportieren", 13, muted: true));
        var progressCard = Card(progress); progressCard.Margin = new Thickness(10, 0, 0, 0); Grid.SetColumn(progressCard, 1); summary.Children.Add(progressCard);
        page.Children.Add(summary);
        var quick = Column(); quick.Children.Add(Text("WEITERARBEITEN", 11, muted: true, bold: true));
        var row = Row(Action("Einstellungen öffnen", () => Navigate("settings"), false, "settings"), Action("Änderungen prüfen", () => Navigate("export"), false, "export"), Action("Rückgängig", () => Mutate("undo"), false, "undo")); row.Margin = new Thickness(0, 16, 0, 0); quick.Children.Add(row);
        var quickCard = Card(quick); quickCard.Margin = new Thickness(0, 20, 0, 0); page.Children.Add(quickCard);
        return Scroll(page);
    }

    private UIElement WizardPage()
    {
        var panel = Column();
        panel.Children.Add(Text("Deine Welt, Schritt für Schritt", 26, bold: true));
        var intro = Text("Neun verständliche Fragen führen zu einer Startkonfiguration. Du kannst vor der Übernahme alle vorgeschlagenen Änderungen sehen.", 15, muted: true); intro.Margin = new Thickness(0, 10, 0, 28); panel.Children.Add(intro);
        var steps = new[] { "01  Spielrichtung", "02  Zombiepopulation", "03  Tageslänge", "04  Loot", "05  Respawn", "06  PvP", "07  Lernen", "08  Versorgung", "09  Wetter" };
        var wrap = new WrapPanel(); foreach (var step in steps) { var chip = Card(Text(step, 13, bold: true), new Thickness(14, 9, 14, 9)); chip.Margin = new Thickness(0, 0, 8, 8); wrap.Children.Add(chip); } panel.Children.Add(wrap);
        var start = Action("Assistent starten", OpenWizard, true, "wizard"); start.HorizontalAlignment = HorizontalAlignment.Left; start.Margin = new Thickness(0, 24, 0, 0); panel.Children.Add(start);
        var note = Text("Der Assistent ändert den Arbeitsbereich erst nach deiner Bestätigung. Zugangsdaten, Mods und Kartenreihenfolgen bleiben erhalten.", 13, muted: true); note.Margin = new Thickness(0, 24, 0, 0); panel.Children.Add(note);
        var page = Column(); page.Children.Add(Card(panel, new Thickness(34)));
        var quickPresets = Column(); quickPresets.Children.Add(Text("Oder mit einer Vorlage beginnen", 19, bold: true));
        var presetIntro = Text("Acht Ausgangspunkte für unterschiedliche Spielstile. Vor der Übernahme siehst du jede Wertänderung.", 13, muted: true); presetIntro.Margin = new Thickness(0, 6, 0, 18); quickPresets.Children.Add(presetIntro);
        int index = 0;
        foreach (var preset in core.Call("presets").EnumerateArray())
        {
            int selected = index++;
            var entry = new Grid { Margin = new Thickness(0, 0, 0, 11) };
            entry.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) }); entry.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            var copy = Column(); copy.Children.Add(Text(preset.S("name"), 14, bold: true)); copy.Children.Add(Text(preset.S("description"), 12, muted: true)); entry.Children.Add(copy);
            var open = Action("Ansehen", () => OpenPreset(selected, preset.S("name"))); Grid.SetColumn(open, 1); open.Margin = new Thickness(15, 0, 0, 0); entry.Children.Add(open); quickPresets.Children.Add(entry);
        }
        var presetsCard = Card(quickPresets); presetsCard.Margin = new Thickness(0, 18, 0, 0); page.Children.Add(presetsCard);
        return Scroll(page);
    }

    private void OpenPreset(int index, string name)
    {
        var dialog = new PresetPreview(core, index, name, state.B("imported")) { Owner = this };
        if (dialog.ShowDialog() == true && dialog.UpdatedState is { } result)
        { state = result; ReadState(); RefreshProfiles(); Navigate("export"); Notice("Vorlage übernommen: " + name); }
    }

    private UIElement ProfilesPage()
    {
        var panel = Column();
        panel.Children.Add(Text("Lokal gespeichert", 20, bold: true));
        var intro = Text("Wähle oben im Profilmenü direkt ein anderes Profil. Hier kannst du speichern, kopieren oder ein gespeichertes Profil öffnen.", 14, muted: true); intro.Margin = new Thickness(0, 8, 0, 22); panel.Children.Add(intro);
        var actions = Row(Action("Aktuelles Profil speichern", () => SaveProfile(), true), Action("Als Kopie speichern", () => SaveProfile(true)), Action("Neues Profil", () => { if (CanReplace()) Mutate("new"); }, false, "plus"));
        actions.Margin = new Thickness(0, 0, 0, 23); panel.Children.Add(actions);
        if (profiles.Count == 0) panel.Children.Add(Text("Noch keine Profile gespeichert.", 15, muted: true));
        foreach (var item in profiles)
        {
            var row = new Grid(); row.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) }); row.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            var copy = Column(); copy.Children.Add(Text(item.Name, 15, bold: true)); copy.Children.Add(Text(item.Updated, 11, muted: true)); row.Children.Add(copy);
            var buttons = Row(Action("Öffnen", () => { if (CanReplace()) Mutate("load", new { id = item.Id }); }), Action("Umbenennen", () => RenameProfile(item)), Action("Verlauf", () => RestoreHistory(item)), Action("Papierkorb", () => DeleteProfile(item)));
            Grid.SetColumn(buttons, 1); row.Children.Add(buttons);
            var card = Card(row, new Thickness(17)); card.Margin = new Thickness(0, 0, 0, 9); panel.Children.Add(card);
        }
        var backups = Row(Action("Profil-JSON importieren", ImportProfile), Action("Profil-JSON exportieren", ExportProfile), Action("Papierkorb", RestoreTrash));
        backups.Margin = new Thickness(8, 20, 0, 14); panel.Children.Add(backups);
        panel.Children.Add(Text("Profile liegen nur auf diesem Gerät. Bekannte Kennwortfelder werden beim Speichern entfernt; ein Serverexport kann sie enthalten.", 12, muted: true));
        return Scroll(panel);
    }

    private void DeleteProfile(ProfileItem item)
    {
        if (MessageBox.Show(this, $"Profil „{item.Name}“ in den Papierkorb verschieben?", "Profil löschen", MessageBoxButton.YesNo, MessageBoxImage.Question) != MessageBoxResult.Yes) return;
        try { core.Call("deleteProfile", new { id = item.Id }); RefreshProfiles(); Navigate("profiles"); Notice("Profil in den Papierkorb verschoben"); } catch (Exception ex) { Error(ex); }
    }

    private void RenameProfile(ProfileItem item)
    {
        var prompt = new NamePrompt(item.Name) { Owner = this };
        if (prompt.ShowDialog() == true) Mutate("renameProfile", new { id = item.Id, name = prompt.Value }, "profiles");
    }
    private void RestoreHistory(ProfileItem item)
    {
        try
        {
            var entries = core.Call("history", new { id = item.Id }).EnumerateArray().ToList();
            if (entries.Count == 0) { MessageBox.Show(this, "Für dieses Profil gibt es noch keinen älteren Stand."); return; }
            var picker = new BackupPicker("Versionsverlauf", entries) { Owner = this };
            if (picker.ShowDialog() == true && picker.SelectedFile is { } file && CanReplace()) Mutate("restoreHistory", new { id = item.Id, file }, "profiles");
        }
        catch (Exception ex) { Error(ex); }
    }

    private void ImportProfile()
    {
        if (!CanReplace()) return;
        var dialog = new OpenFileDialog { Title = "Profil-JSON importieren", Filter = "Profil-JSON (*.json)|*.json" };
        if (dialog.ShowDialog(this) == true) Mutate("importProfile", new { path = dialog.FileName }, "profiles");
    }
    private void ExportProfile()
    {
        var dialog = new SaveFileDialog { Title = "Profil-JSON ohne bekannte Kennwortfelder exportieren", Filter = "Profil-JSON (*.json)|*.json", FileName = state.S("baseName") + "-profil.json" };
        if (dialog.ShowDialog(this) != true) return;
        try { core.Call("exportProfile", new { path = dialog.FileName }); Notice("Profil-JSON erstellt"); }
        catch (Exception ex) { Error(ex); }
    }
    private void RestoreTrash()
    {
        try
        {
            var entries = core.Call("trash").EnumerateArray().ToList();
            if (entries.Count == 0) { MessageBox.Show(this, "Der Papierkorb ist leer."); return; }
            var picker = new BackupPicker("Papierkorb", entries) { Owner = this };
            if (picker.ShowDialog() == true && picker.SelectedFile is { } file && CanReplace()) Mutate("restoreTrash", new { file }, "profiles");
        }
        catch (Exception ex) { Error(ex); }
    }

    private UIElement ExportPage()
    {
        var page = Column(); var review = core.Call("validate");
        var issues = review.GetProperty("issues").EnumerateArray().ToList();
        var header = Column(); header.Children.Add(Text(issues.Count == 0 ? "Keine erkannten Konflikte" : $"{issues.Count} Prüfpunkte", 22, bold: true));
        var body = issues.Count == 0 ? "Die Dateien lassen sich aus Sicht des Editors exportieren. Spielbuild und Mods an einer Testinstanz prüfen." : string.Join("\n\n", issues.Select(i => (i.B("error") ? "FEHLER · " : "HINWEIS · ") + i.S("text")));
        var message = Text(body, 14, muted: issues.Count == 0); message.Margin = new Thickness(0, 12, 0, 18); header.Children.Add(message);
        header.Children.Add(Row(Action("Server-ZIP exportieren", ExportZip, true, "export"), Action("Dateien importieren", ImportPair, false, "folder"), Action("INI-Quelltext", () => EditRaw("ini")), Action("Sandbox-Quelltext", () => EditRaw("lua"))));
        var card = Card(header); card.Margin = new Thickness(0, 0, 0, 20); page.Children.Add(card);
        page.Children.Add(Text("ÄNDERUNGEN GEGENÜBER DEM ORIGINAL", 11, muted: true, bold: true));
        var diff = new TextBox { Text = review.S("diff"), IsReadOnly = true, AcceptsReturn = true, TextWrapping = TextWrapping.Wrap, VerticalScrollBarVisibility = ScrollBarVisibility.Auto, MinHeight = 220, Margin = new Thickness(0, 10, 0, 15) };
        page.Children.Add(diff);
        page.Children.Add(Text("Ein Export enthält die tatsächlichen Serverwerte und gegebenenfalls Kennwörter. Server vor der Übernahme stoppen und sichern; AMP kann Werte beim Start überschreiben.", 12, muted: true));
        return Scroll(page);
    }

    private UIElement CommunityPage()
    {
        var panel = Column(); panel.Children.Add(Text("RuhrpottZ Server", 25, bold: true));
        panel.Children.Add(Text("Live-Daten direkt aus der öffentlichen Serverliste. Die Anzeige wird alle 15 Sekunden neu angefragt.", 14, muted: true));
        liveStatus = Text(serverStatus, 15); liveStatus.Margin = new Thickness(0, 25, 0, 24); panel.Children.Add(liveStatus);
        panel.Children.Add(Row(Action("Jetzt aktualisieren", () => _ = RefreshServerStatus(), false, "refresh"), Action("Discord beitreten", () => OpenUrl("https://discord.gg/hWz2HJcakH"), true, "discord"), Action("Serverseite öffnen", () => OpenUrl("https://pzservers.net/server/75212135"))));
        var caveat = Text("Quelle: pzservers.net. Eine nicht erreichbare Serverliste bestätigt keinen Serverausfall.", 12, muted: true); caveat.Margin = new Thickness(0, 24, 0, 0); panel.Children.Add(caveat);
        return Card(panel, new Thickness(32));
    }

    private UIElement HelpPage()
    {
        var panel = Column(); panel.Children.Add(Text("Hilfe & Updates", 25, bold: true));
        var intro = Text("Kurze Wege zu Handbuch, Versionshinweisen und Projektseite.", 15, muted: true); intro.Margin = new Thickness(0, 7, 0, 23); panel.Children.Add(intro);
        liveUpdate = Text(updateStatus, 14); liveUpdate.Margin = new Thickness(0, 0, 0, 14); panel.Children.Add(liveUpdate);
        var updateButtons = Row(Action("Jetzt prüfen", () => _ = CheckUpdate(true), false, "refresh"));
        if (updateState == "UPDATE_AVAILABLE") updateButtons.Children.Add(Action("Paket laden", () => _ = DownloadUpdate(), true));
        if (updatePath is not null) updateButtons.Children.Add(Action("Geprüftes Paket öffnen", InstallUpdate, true));
        panel.Children.Add(updateButtons);
        if (updateNotes.Length > 0) { var notes = Text(updateNotes, 12, muted: true); notes.Margin = new Thickness(0, 13, 0, 0); panel.Children.Add(notes); }
        var links = Row(Action("Handbuch öffnen", OpenManual), Action("GitHub Releases", () => OpenUrl("https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases")), Action("Zumbyte", () => OpenUrl("https://www.zumbyte.de/")));
        links.Margin = new Thickness(0, 27, 0, 0); panel.Children.Add(links);
        var info = Text("PZ Server Architect 0.4.0 Preview · Windows WPF\nProfil- und Konfigurationsdaten bleiben lokal. Es gibt keinen Masterserver und keinen Konfigurations-Upload.\n\nTastatur: Strg+S speichern · Strg+F suchen · Strg+E exportieren · Strg+Z/Y rückgängig/wiederholen", 13, muted: true);
        info.Margin = new Thickness(0, 32, 0, 0); panel.Children.Add(info); return Card(panel, new Thickness(32));
    }

    private void ImportPair()
    {
        if (!CanReplace()) return;
        var open = new OpenFileDialog { Title = "Server-INI auswählen", Filter = "Project Zomboid INI (*.ini)|*.ini" };
        if (open.ShowDialog(this) != true) return;
        var ini = open.FileName;
        var lua = Path.Combine(Path.GetDirectoryName(ini)!, Path.GetFileNameWithoutExtension(ini) + "_SandboxVars.lua");
        if (!File.Exists(lua))
        {
            var choose = new OpenFileDialog { Title = "Passende SandboxVars-Datei auswählen", Filter = "Lua-Datei (*.lua)|*.lua" };
            if (choose.ShowDialog(this) != true) return; lua = choose.FileName;
        }
        Mutate("import", new { ini, lua }, "home");
    }
    private void ExportZip()
    {
        var save = new SaveFileDialog { Title = "Neues Server-ZIP speichern", Filter = "ZIP-Archiv (*.zip)|*.zip", FileName = state.S("baseName") + "-config.zip", OverwritePrompt = true };
        if (save.ShowDialog(this) != true) return;
        try { core.Call("export", new { path = save.FileName }); Notice("Server-ZIP erstellt: " + Path.GetFileName(save.FileName)); MessageBox.Show(this, "Server-ZIP erstellt. Die Originaldateien wurden nicht verändert.", "Export", MessageBoxButton.OK, MessageBoxImage.Information); }
        catch (Exception ex) { Error(ex); }
    }
    private void EditRaw(string scope)
    {
        try
        {
            var raw = core.Call("raw");
            var editor = new RawEditor(scope == "ini" ? "Server-INI" : "SandboxVars-Lua", raw.S(scope)) { Owner = this };
            if (editor.ShowDialog() == true) Mutate("editRaw", new { scope, source = editor.Source }, "export");
        }
        catch (Exception ex) { Error(ex); }
    }
    private void OpenManual()
    {
        var path = Path.Combine(AppContext.BaseDirectory, "HANDBUCH.md");
        if (!File.Exists(path)) { Error(new IOException("Handbuch fehlt in der Installation.")); return; }
        new RawEditor("Handbuch", File.ReadAllText(path), true) { Owner = this }.ShowDialog();
    }
    private static void OpenUrl(string url) => Process.Start(new ProcessStartInfo(url) { UseShellExecute = true });

    private async Task CheckUpdate(bool visible)
    {
        if (updateBusy) return; updateBusy = true;
        updateStatus = "GitHub Releases werden geprüft …";
        if (liveUpdate is not null) liveUpdate.Text = updateStatus;
        try
        {
            var result = await Task.Run(() => core.Call("checkUpdate"));
            updateState = result.S("state"); updateStatus = result.S("message");
            updateNotes = result.S("notes"); if (updateNotes.Length > 5000) updateNotes = updateNotes[..5000] + " …";
            updatePath = null;
        }
        catch { updateState = ""; updateStatus = "Updateprüfung derzeit nicht möglich. Die App bleibt nutzbar."; }
        finally { updateBusy = false; if (liveUpdate is not null) liveUpdate.Text = updateStatus; if (visible && route == "about") Navigate("about"); }
    }
    private async Task DownloadUpdate()
    {
        if (updateBusy || updateState != "UPDATE_AVAILABLE") return; updateBusy = true;
        updateStatus = "Update wird geladen und anhand der SHA-256-Prüfsumme geprüft …";
        if (liveUpdate is not null) liveUpdate.Text = updateStatus;
        try
        {
            var result = await Task.Run(() => core.Call("downloadUpdate"));
            updatePath = result.S("path"); updateStatus = "Version " + result.S("version") + " wurde geprüft und ist bereit.";
        }
        catch { updatePath = null; updateStatus = "Download oder Prüfsumme fehlgeschlagen. Kein Paket wird installiert."; }
        finally { updateBusy = false; if (route == "about") Navigate("about"); }
    }
    private void InstallUpdate()
    {
        if (updatePath is null) return;
        if (state.B("dirty") && !SaveProfile()) return;
        if (MessageBox.Show(this, "Das geprüfte Installationspaket mit Windows öffnen?", "Update installieren", MessageBoxButton.YesNo, MessageBoxImage.Question) != MessageBoxResult.Yes) return;
        try
        {
            var file = core.Call("verifyUpdate").S("path");
            Process.Start(new ProcessStartInfo(file) { UseShellExecute = true });
            Notice("Installationspaket an Windows übergeben");
        }
        catch (Exception ex) { Error(ex); }
    }

    private async Task RefreshServerStatus()
    {
        if (statusBusy) return; statusBusy = true;
        try
        {
            using var response = await statusHttp.GetAsync("https://pzservers.net/server/75212135"); response.EnsureSuccessStatusCode();
            var html = await response.Content.ReadAsStringAsync(); if (html.Length > 1_000_000) throw new IOException("Antwort zu groß");
            var plain = WebUtility.HtmlDecode(Regex.Replace(html, "<[^>]+>", "\n"));
            plain = Regex.Replace(plain, "[ \t]+", " "); plain = Regex.Replace(plain, "\n{2,}", "\n");
            int start = plain.IndexOf("Server Details", StringComparison.OrdinalIgnoreCase), end = plain.IndexOf("Claim Server", Math.Max(0, start), StringComparison.OrdinalIgnoreCase);
            if (start < 0) throw new IOException("Serverdaten nicht gefunden");
            string data = plain.Substring(start, (end > start ? end : Math.Min(plain.Length, start + 1200)) - start).Trim();
            if (!data.Contains("Status", StringComparison.OrdinalIgnoreCase)) throw new IOException("Statusdaten nicht gefunden");
            serverStatus = data + "\n\nStand: " + DateTime.Now.ToString("HH:mm:ss") + " · pzservers.net";
        }
        catch { serverStatus = "Status derzeit unbekannt. Die Serverliste konnte nicht gelesen werden.\nLetzter Versuch: " + DateTime.Now.ToString("HH:mm:ss"); }
        finally { statusBusy = false; if (liveStatus is not null) liveStatus.Text = serverStatus; }
    }
}
