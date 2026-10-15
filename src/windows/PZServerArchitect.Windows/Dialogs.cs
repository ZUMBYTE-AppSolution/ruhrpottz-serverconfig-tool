using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace PZServerArchitect.Windows;

internal static class DialogStyle
{
    public static Brush Color(string value) => (Brush)new BrushConverter().ConvertFrom(value)!;
    public static TextBlock Label(string text, int size = 14, bool muted = false) => new() { Text = text, FontSize = size, Foreground = Color(muted ? "#B9C4D9" : "#F2F5FC"), TextWrapping = TextWrapping.Wrap, LineHeight = size * 1.45 };
    public static Button Button(string text, bool primary = false) => new() { Content = text, Style = (Style)Application.Current.FindResource(primary ? "PrimaryButton" : "SoftButton"), Margin = new Thickness(0, 0, 10, 0) };
}

internal sealed class ProfilePrompt : Window
{
    private readonly TextBox name, baseName;
    public string ProfileName => name.Text.Trim();
    public string BaseName => baseName.Text.Trim();
    public ProfilePrompt(string currentName, string currentBase)
    {
        Title = "Profil speichern"; Width = 520; Height = 410; MinWidth = 450; MinHeight = 370;
        WindowStartupLocation = WindowStartupLocation.CenterOwner; Background = DialogStyle.Color("#111625");
        var panel = new StackPanel { Margin = new Thickness(28) };
        panel.Children.Add(DialogStyle.Label("Profil speichern", 23));
        panel.Children.Add(new Border { Height = 17 });
        panel.Children.Add(DialogStyle.Label("Profilname", 12, true));
        name = new TextBox { Text = currentName, Margin = new Thickness(0, 6, 0, 18) }; panel.Children.Add(name);
        panel.Children.Add(DialogStyle.Label("Server-Dateiname ohne Endung", 12, true));
        baseName = new TextBox { Text = currentBase, Margin = new Thickness(0, 6, 0, 14) }; panel.Children.Add(baseName);
        panel.Children.Add(DialogStyle.Label("Erlaubt sind Buchstaben, Zahlen, Bindestriche und Unterstriche. Bekannte Kennwortfelder werden im Profil entfernt.", 12, true));
        var actions = new StackPanel { Orientation = Orientation.Horizontal, HorizontalAlignment = HorizontalAlignment.Right, Margin = new Thickness(0, 23, 0, 0) };
        var cancel = DialogStyle.Button("Abbrechen"); cancel.Click += (_, _) => DialogResult = false;
        var save = DialogStyle.Button("Speichern", true); save.Click += (_, _) =>
        {
            if (ProfileName.Length == 0 || ProfileName.Length > 160 || !System.Text.RegularExpressions.Regex.IsMatch(BaseName, "^[A-Za-z0-9_-]{1,64}$"))
                MessageBox.Show(this, "Bitte gültigen Profilnamen und Server-Dateinamen eingeben.", "Eingabe prüfen", MessageBoxButton.OK, MessageBoxImage.Warning);
            else DialogResult = true;
        };
        actions.Children.Add(cancel); actions.Children.Add(save); panel.Children.Add(actions); Content = panel;
        Loaded += (_, _) => { name.Focus(); name.SelectAll(); };
    }
}

internal sealed class RawEditor : Window
{
    private readonly TextBox editor;
    public string Source => editor.Text;
    public RawEditor(string title, string source, bool readOnly = false)
    {
        Title = title; Width = 900; Height = 720; MinWidth = 650; MinHeight = 500;
        WindowStartupLocation = WindowStartupLocation.CenterOwner; Background = DialogStyle.Color("#111625");
        var root = new Grid { Margin = new Thickness(25) };
        root.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        root.RowDefinitions.Add(new RowDefinition { Height = new GridLength(1, GridUnitType.Star) });
        root.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        var header = DialogStyle.Label(title, 22); header.Margin = new Thickness(0, 0, 0, 16); root.Children.Add(header);
        editor = new TextBox { Text = source, AcceptsReturn = true, AcceptsTab = true, IsReadOnly = readOnly, FontFamily = new FontFamily("Cascadia Mono, Consolas"), FontSize = 13, TextWrapping = TextWrapping.NoWrap, HorizontalScrollBarVisibility = ScrollBarVisibility.Auto, VerticalScrollBarVisibility = ScrollBarVisibility.Auto };
        Grid.SetRow(editor, 1); root.Children.Add(editor);
        var actions = new StackPanel { Orientation = Orientation.Horizontal, HorizontalAlignment = HorizontalAlignment.Right, Margin = new Thickness(0, 16, 0, 0) };
        var close = DialogStyle.Button(readOnly ? "Schließen" : "Abbrechen"); close.Click += (_, _) => DialogResult = false; actions.Children.Add(close);
        if (!readOnly) { var apply = DialogStyle.Button("Änderungen übernehmen", true); apply.Click += (_, _) => DialogResult = true; actions.Children.Add(apply); }
        Grid.SetRow(actions, 2); root.Children.Add(actions); Content = root;
    }
}
