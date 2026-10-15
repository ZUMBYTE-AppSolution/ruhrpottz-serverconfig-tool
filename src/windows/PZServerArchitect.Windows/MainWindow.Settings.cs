using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace PZServerArchitect.Windows;

public partial class MainWindow
{
    private UIElement SettingsPage()
    {
        var root = new Grid();
        root.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        root.RowDefinitions.Add(new RowDefinition { Height = new GridLength(1, GridUnitType.Star) });
        var toolbar = new Grid { Margin = new Thickness(0, 0, 0, 18) };
        toolbar.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        toolbar.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
        var groupHeader = new StackPanel();
        groupHeader.Children.Add(Text("THEMA", 10, muted: true, bold: true));
        var groups = new ComboBox { Width = 290, HorizontalAlignment = HorizontalAlignment.Left, Margin = new Thickness(0, 5, 0, 0) };
        groups.Items.Add(new CategoryChoice("all", "Alle Themen"));
        groups.Items.Add(new CategoryChoice("changed", "Geänderte Werte"));
        foreach (var c in state.GetProperty("categories").EnumerateArray()) groups.Items.Add(new CategoryChoice(c.S("id"), c.S("title")));
        groups.SelectedItem = groups.Items.Cast<CategoryChoice>().FirstOrDefault(c => c.Id == category) ?? groups.Items[0];
        groupHeader.Children.Add(groups); toolbar.Children.Add(groupHeader);
        var searchHeader = new StackPanel(); searchHeader.Children.Add(Text("SUCHEN", 10, muted: true, bold: true));
        var search = new TextBox { Width = 286, Text = searchText, ToolTip = "Einstellungen nach Name oder Schlüssel suchen", Margin = new Thickness(0, 5, 0, 0) };
        searchHeader.Children.Add(search);
        searchHeader.SetValue(Grid.ColumnProperty, 1); toolbar.Children.Add(searchHeader);
        root.Children.Add(toolbar);

        var panel = new Grid(); panel.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(335) });
        panel.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(18) });
        panel.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        Grid.SetRow(panel, 1); root.Children.Add(panel);
        var list = new ListBox { Padding = new Thickness(9), HorizontalContentAlignment = HorizontalAlignment.Stretch };
        var listPanel = Card(list, new Thickness(6)); Grid.SetColumn(listPanel, 0); panel.Children.Add(listPanel);
        var detail = new ContentControl(); Grid.SetColumn(detail, 2); panel.Children.Add(detail);

        void Fill()
        {
            category = ((CategoryChoice?)groups.SelectedItem)?.Id ?? "all";
            searchText = search.Text.Trim();
            var filtered = settings.Where(s => category == "all" || (category == "changed" ? s.Modified : s.Category == category))
                .Where(s => searchText.Length == 0 || s.Title.Contains(searchText, StringComparison.CurrentCultureIgnoreCase) || s.Key.Contains(searchText, StringComparison.OrdinalIgnoreCase))
                .OrderByDescending(s => s.Modified).ThenByDescending(s => s.Present).ThenBy(s => s.Title).ToList();
            list.Items.Clear();
            foreach (var s in filtered)
            {
                var content = Column();
                content.Children.Add(Text(s.Title, 14, bold: true));
                content.Children.Add(Text(s.Key + (s.Modified ? "  ·  geändert" : s.Present ? "" : "  ·  nicht gesetzt"), 11, muted: true));
                list.Items.Add(new ListBoxItem { Content = content, Tag = s });
            }
            var match = list.Items.Cast<ListBoxItem>().FirstOrDefault(i => ((SettingItem)i.Tag).Id == selectedSetting);
            list.SelectedItem = match ?? list.Items.Cast<ListBoxItem>().FirstOrDefault();
            if (list.Items.Count == 0) detail.Content = Card(Text("Keine passenden Einstellungen gefunden. Wähle ein anderes Thema oder kürze die Suche.", 15, muted: true));
        }
        groups.SelectionChanged += (_, _) => Fill();
        search.TextChanged += (_, _) => Fill();
        list.SelectionChanged += (_, _) =>
        {
            if (list.SelectedItem is ListBoxItem item && item.Tag is SettingItem s) { selectedSetting = s.Id; detail.Content = SettingDetail(s); }
        };
        Fill();
        return root;
    }

    private UIElement SettingDetail(SettingItem s)
    {
        var content = Column();
        content.Children.Add(Text(s.Category.ToUpperInvariant() + "  /  " + (s.Id.StartsWith("ini:") ? "SERVER-INi" : "SANDBOX-LUA"), 11, muted: true, bold: true));
        var title = Text(s.Title, 24, bold: true); title.Margin = new Thickness(0, 7, 0, 7); content.Children.Add(title);
        var description = Text(s.Description, 14, muted: true); description.Margin = new Thickness(0, 0, 0, 22); content.Children.Add(description);
        content.Children.Add(Text("AKTUELLER WERT", 11, muted: true, bold: true));
        var editRow = new Grid { Margin = new Thickness(0, 8, 0, 10) };
        editRow.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        editRow.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
        string readValue = s.Value;
        if (s.Choices.Count > 0 || s.Type == "boolean")
        {
            var choice = new ComboBox { Margin = new Thickness(0, 0, 10, 0) };
            var options = s.Choices.Count > 0 ? s.Choices : new List<(string Value, string Title)> { ("true", "Ein"), ("false", "Aus") };
            foreach (var option in options) choice.Items.Add(new ValueChoice(option.Value, option.Title));
            var selected = choice.Items.Cast<ValueChoice>().FirstOrDefault(o => o.Value == s.Value);
            if (selected is null && s.Present) choice.Items.Insert(0, new ValueChoice(s.Value, s.Value + " (Original)"));
            choice.SelectedItem = selected ?? (choice.Items.Count > 0 ? choice.Items[0] : null);
            editRow.Children.Add(choice);
            readValue = s.Value;
            var apply = Action("Übernehmen", () => Mutate("set", new { id = s.Id, value = ((ValueChoice?)choice.SelectedItem)?.Value ?? "" }), true);
            Grid.SetColumn(apply, 1); editRow.Children.Add(apply);
        }
        else if (s.Secret)
        {
            var password = new PasswordBox { Background = Brush("#20293B"), Foreground = Brush("#F2F5FC"), BorderBrush = Brush("#344056"), Padding = new Thickness(10, 7, 10, 7), Margin = new Thickness(0, 0, 10, 0), Password = s.Value };
            editRow.Children.Add(password);
            var apply = Action("Übernehmen", () => Mutate("set", new { id = s.Id, value = password.Password }), true);
            Grid.SetColumn(apply, 1); editRow.Children.Add(apply);
        }
        else
        {
            var input = new TextBox { Text = s.Value, Margin = new Thickness(0, 0, 10, 0) };
            editRow.Children.Add(input);
            var apply = Action("Übernehmen", () => Mutate("set", new { id = s.Id, value = input.Text }), true);
            Grid.SetColumn(apply, 1); editRow.Children.Add(apply);
            input.KeyDown += (_, e) => { if (e.Key == System.Windows.Input.Key.Enter) { apply.RaiseEvent(new RoutedEventArgs(Button.ClickEvent)); e.Handled = true; } };
        }
        content.Children.Add(editRow);
        var meta = Text($"Schlüssel: {s.Key}   ·   {(s.Present ? "In der Datei vorhanden" : "Noch nicht gesetzt")}" + (s.Unit.Length > 0 ? $"   ·   Einheit: {s.Unit}" : ""), 11, muted: true);
        meta.Margin = new Thickness(0, 0, 0, 15); content.Children.Add(meta);
        var actions = Row(Action("Original wiederherstellen", () => Mutate("restore", new { id = s.Id })), Action("Schlüssel entfernen", () => Mutate("unset", new { id = s.Id })));
        actions.Margin = new Thickness(0, 0, 0, 26); content.Children.Add(actions);
        if (s.Risk.Length > 0)
        {
            var warning = Card(Text("Hinweis: " + s.Risk, 13), new Thickness(15));
            warning.Background = Brush("#47342D"); warning.BorderBrush = Brush("#93664C"); warning.Margin = new Thickness(0, 0, 0, 16); content.Children.Add(warning);
        }
        content.Children.Add(Text("WAS BEDEUTET DER WERT?", 11, muted: true, bold: true));
        var guide = Text(s.Guide, 13, muted: true); guide.Margin = new Thickness(0, 8, 0, 0); content.Children.Add(guide);
        return Card(Scroll(content));
    }

    private sealed record CategoryChoice(string Id, string Title) { public override string ToString() => Title; }
    private sealed record ValueChoice(string Value, string Title) { public override string ToString() => Title; }
}
