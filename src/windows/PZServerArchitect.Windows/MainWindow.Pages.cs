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
        return Card(panel, new Thickness(34));
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
            var buttons = Row(Action("Öffnen", () => { if (CanReplace()) Mutate("load", new { id = item.Id }); }), Action("Papierkorb", () => DeleteProfile(item)));
            Grid.SetColumn(buttons, 1); row.Children.Add(buttons);
            var card = Card(row, new Thickness(17)); card.Margin = new Thickness(0, 0, 0, 9); panel.Children.Add(card);
        }
        panel.Children.Add(Text("Profile liegen nur auf diesem Gerät. Bekannte Kennwortfelder werden beim Speichern entfernt; ein Serverexport kann sie enthalten.", 12, muted: true));
        return Scroll(panel);
    }

    private void DeleteProfile(ProfileItem item)
    {
        if (MessageBox.Show(this, $"Profil „{item.Name}“ in den Papierkorb verschieben?", "Profil löschen", MessageBoxButton.YesNo, MessageBoxImage.Question) != MessageBoxResult.Yes) return;
        try { core.Call("deleteProfile", new { id = item.Id }); RefreshProfiles(); Navigate("profiles"); Notice("Profil in den Papierkorb verschoben"); } catch (Exception ex) { Error(ex); }
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
        var intro = Text("Kurze Wege zu Handbuch, Versionshinweisen und Projektseite.", 15, muted: true); intro.Margin = new Thickness(0, 7, 0, 26); panel.Children.Add(intro);
        panel.Children.Add(Row(Action("Handbuch öffnen", OpenManual, true), Action("GitHub Releases", () => OpenUrl("https://github.com/ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool/releases")), Action("Zumbyte", () => OpenUrl("https://www.zumbyte.de/"))));
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
