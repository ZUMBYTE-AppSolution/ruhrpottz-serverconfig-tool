using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Text.Json;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Threading;

namespace PZServerArchitect.Windows;

public partial class MainWindow : Window
{
    private readonly CoreClient core;
    private JsonElement state;
    private readonly List<SettingItem> settings = [];
    private readonly List<ProfileItem> profiles = [];
    private bool updatingPicker;
    private string route = "home", category = "server", searchText = "";
    private string? selectedSetting = "ini:PublicName";
    private readonly DispatcherTimer statusTimer = new() { Interval = TimeSpan.FromSeconds(15) };
    private readonly HttpClient statusHttp = new() { Timeout = TimeSpan.FromSeconds(9) };
    private bool statusBusy;
    private ServerSnapshot? serverSnapshot;
    private DateTime lastStatusAttempt;
    private TextBlock? liveStatus;
    private TextBlock? liveStatusInfo;
    private TextBlock? liveAddress;
    private TextBlock? livePlayers;
    private TextBlock? liveMap;
    private TextBlock? liveVersion;
    private TextBlock? liveMods;
    private TextBlock? liveSecurity;
    private Border? liveStatusDot;
    private TextBlock? liveUpdate;
    private string updateStatus = "Noch nicht geprüft";
    private string updateState = "";
    private string updateNotes = "";
    private string? updatePath;
    private bool updateBusy;

    public MainWindow()
    {
        InitializeComponent();
        core = new CoreClient();
        state = core.Call("state");
        ReadState(); RefreshProfiles(); SetupNavigation();
        var initial = Environment.GetCommandLineArgs().FirstOrDefault(a => a.StartsWith("--page=", StringComparison.Ordinal));
        Navigate(initial is null ? "home" : initial[7..]);
        statusTimer.Tick += async (_, _) => await RefreshServerStatus();
        if (Environment.GetCommandLineArgs().Contains("--smoke")) Loaded += (_, _) => Close();
        else { statusTimer.Start(); _ = RefreshServerStatus(); Loaded += async (_, _) => await CheckUpdate(false); }
        PreviewKeyDown += (_, e) =>
        {
            bool control = (System.Windows.Input.Keyboard.Modifiers & System.Windows.Input.ModifierKeys.Control) != 0;
            if (!control) return;
            if (e.Key == System.Windows.Input.Key.S) SaveProfile();
            else if (e.Key == System.Windows.Input.Key.Z) Mutate("undo");
            else if (e.Key == System.Windows.Input.Key.Y) Mutate("redo");
            else if (e.Key == System.Windows.Input.Key.E) Navigate("export");
            else if (e.Key == System.Windows.Input.Key.F) Navigate("settings");
            else return;
            e.Handled = true;
        };
    }
    private void SetupNavigation()
    {
        SetNav(HomeNav, "home", "Übersicht"); SetNav(WizardNav, "wizard", "Quick Start");
        SetNav(SettingsNav, "settings", "Einstellungen"); SetNav(ProfilesNav, "profiles", "Profile");
        SetNav(ExportNav, "export", "Prüfen & Export"); SetNav(CommunityNav, "server", "Community");
        SetNav(HelpNav, "about", "Hilfe & Updates");
    }
    private static void SetNav(Button button, string icon, string title)
    {
        System.Windows.Automation.AutomationProperties.SetName(button, title);
        var row = new StackPanel { Orientation = Orientation.Horizontal };
        row.Children.Add(Icon(icon, 18));
        row.Children.Add(new TextBlock { Text = title, VerticalAlignment = VerticalAlignment.Center, Margin = new Thickness(12, 0, 0, 0), FontSize = 13 });
        button.Content = row;
    }
    private static new Image Icon(string name, double size)
    {
        var image = new Image { Width = size, Height = size, Stretch = Stretch.Uniform, VerticalAlignment = VerticalAlignment.Center };
        try { image.Source = new BitmapImage(new Uri($"pack://application:,,,/Assets/icons/{name}.png", UriKind.Absolute)); } catch { }
        return image;
    }
    private void Navigate_Click(object sender, RoutedEventArgs e) => Navigate((string)((Button)sender).Tag);
    private void Navigate(string next)
    {
        route = next;
        PageTitle.Text = next switch { "home" => "Übersicht", "wizard" => "Quick Start", "settings" => "Einstellungen", "profiles" => "Profile", "export" => "Prüfen & Export", "community" => "Community", _ => "Hilfe & Updates" };
        PageSubtitle.Text = next switch { "settings" => "Serverwerte bearbeiten, ohne die Orientierung zu verlieren", "community" => "Serverstatus und Discord an einem eigenen Ort", "wizard" => "Neun Entscheidungen, Schritt für Schritt", _ => $"Aktives Profil: {state.S("name")}{(state.B("dirty") ? " · ungespeichert" : "")}" };
        foreach (var button in new[] { HomeNav, WizardNav, SettingsNav, ProfilesNav, ExportNav, CommunityNav, HelpNav })
            button.Background = (string)button.Tag == next ? Brush("#423460") : (Brush)FindResource("Surface2");
        PageHost.Content = next switch { "home" => HomePage(), "wizard" => WizardPage(), "settings" => SettingsPage(), "profiles" => ProfilesPage(), "export" => ExportPage(), "community" => CommunityPage(), _ => HelpPage() };
    }
    private void ReadState()
    {
        settings.Clear(); foreach (var item in state.GetProperty("settings").EnumerateArray()) settings.Add(new SettingItem(item));
        Title = "PZ Server Architect · " + state.S("name") + (state.B("dirty") ? " *" : "");
    }
    private void RefreshProfiles()
    {
        profiles.Clear(); foreach (var entry in core.Call("profiles").EnumerateArray()) profiles.Add(new ProfileItem(entry.S("id"), entry.S("name"), entry.S("updated")));
        updatingPicker = true; ProfilePicker.Items.Clear();
        ProfilePicker.Items.Add(new ProfileItem("__current__", "Aktiv: " + state.S("name"), ""));
        foreach (var item in profiles.Where(p => p.Id != state.S("id"))) ProfilePicker.Items.Add(item);
        ProfilePicker.Items.Add(new ProfileItem("__new__", "+ Neues Profil", ""));
        ProfilePicker.SelectedIndex = 0; updatingPicker = false;
    }
    private void ProfilePicker_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        if (updatingPicker || ProfilePicker.SelectedItem is not ProfileItem item || item.Id == "__current__") return;
        if (!CanReplace()) { updatingPicker = true; ProfilePicker.SelectedIndex = 0; updatingPicker = false; return; }
        if (item.Id == "__new__") Mutate("new"); else Mutate("load", new { id = item.Id });
    }
    private bool CanReplace() => !state.B("dirty") || MessageBox.Show(this, "Im aktuellen Profil gibt es ungespeicherte Änderungen. Zum Wechseln verwerfen?", "Profil wechseln", MessageBoxButton.YesNo, MessageBoxImage.Warning) == MessageBoxResult.Yes;
    private void Mutate(string command, object? args = null, string? next = null)
    {
        try { state = core.Call(command, args); ReadState(); RefreshProfiles(); Navigate(next ?? route); Notice("Änderung übernommen"); }
        catch (Exception ex) { Error(ex); }
    }
    private void Save_Click(object sender, RoutedEventArgs e) => SaveProfile();
    private bool SaveProfile(bool copy = false)
    {
        var prompt = new ProfilePrompt(state.S("name") + (copy ? " (Kopie)" : ""), state.S("baseName")) { Owner = this };
        if (prompt.ShowDialog() != true) return false;
        try { state = core.Call(copy ? "saveCopy" : "save", new { name = prompt.ProfileName, baseName = prompt.BaseName }); ReadState(); RefreshProfiles(); Navigate(route); Notice("Profil lokal gespeichert"); return true; }
        catch (Exception ex) { Error(ex); return false; }
    }
    private void Notice(string message) => StatusText.Text = message;
    private void Error(Exception ex) { StatusText.Text = ex.Message; MessageBox.Show(this, ex.Message, "PZ Server Architect", MessageBoxButton.OK, MessageBoxImage.Warning); }
    private static Brush Brush(string hex) => (Brush)new BrushConverter().ConvertFrom(hex)!;
    private static TextBlock Text(string value, int size = 14, bool muted = false, bool bold = false) => new() { Text = value, FontSize = size, FontWeight = bold ? FontWeights.SemiBold : FontWeights.Normal, Foreground = muted ? Brush("#B9C4D9") : Brush("#F2F5FC"), LineHeight = size * 1.45 };
    private static Border Card(UIElement content, Thickness? padding = null) => new() { Background = Brush("#181F30"), BorderBrush = Brush("#344056"), BorderThickness = new Thickness(1), CornerRadius = new CornerRadius(14), Padding = padding ?? new Thickness(24), Child = content };
    private static StackPanel Column() => new() { Orientation = Orientation.Vertical };
    private static Button Action(string title, Action action, bool primary = false, string? icon = null)
    {
        var button = new Button { Style = (Style)Application.Current.FindResource(primary ? "PrimaryButton" : "SoftButton") };
        System.Windows.Automation.AutomationProperties.SetName(button, title);
        if (icon is null) button.Content = title;
        else { var row = new StackPanel { Orientation = Orientation.Horizontal }; row.Children.Add(Icon(icon, 17)); row.Children.Add(new TextBlock { Text = title, Margin = new Thickness(9, 0, 0, 0) }); button.Content = row; }
        button.Click += (_, _) => action(); return button;
    }
    private static StackPanel Row(params UIElement[] items)
    { var row = new StackPanel { Orientation = Orientation.Horizontal }; foreach (var item in items) { if (item is FrameworkElement f) f.Margin = new Thickness(0, 0, 10, 0); row.Children.Add(item); } return row; }
    private static ScrollViewer Scroll(UIElement child) => new() { Content = child, VerticalScrollBarVisibility = ScrollBarVisibility.Auto, HorizontalScrollBarVisibility = ScrollBarVisibility.Disabled };
    protected override void OnClosing(CancelEventArgs e)
    {
        if (state.ValueKind != JsonValueKind.Undefined && state.B("dirty") && MessageBox.Show(this, "Ungespeicherte Änderungen verwerfen und App schließen?", "App schließen", MessageBoxButton.YesNo, MessageBoxImage.Warning) != MessageBoxResult.Yes) { e.Cancel = true; return; }
        statusTimer.Stop(); statusHttp.Dispose(); core.Dispose(); base.OnClosing(e);
    }
    private sealed record ProfileItem(string Id, string Name, string Updated) { public override string ToString() => Name; }
    private sealed record ServerSnapshot(string Status, string Address, string Players, string Map,
        string Version, string Mods, string Vac, string Password, string Whitelist, DateTime CheckedAt);
    private sealed class SettingItem(JsonElement e)
    {
        public string Id { get; } = e.S("id"); public string Title { get; } = e.S("title"); public string Key { get; } = e.S("key");
        public string Category { get; } = e.S("category"); public string Type { get; } = e.S("type"); public string Description { get; } = e.S("description");
        public string Guide { get; } = e.S("guide"); public string Unit { get; } = e.S("unit"); public string Risk { get; } = e.S("risk");
        public string Value { get; } = e.S("value"); public string Original { get; } = e.S("original");
        public bool Present { get; } = e.B("present"); public bool Modified { get; } = e.B("modified"); public bool Secret { get; } = e.B("secret");
        public List<(string Value, string Title)> Choices { get; } = e.GetProperty("choices").EnumerateArray().Select(c => (c.S("value"), c.S("title"))).ToList();
        public override string ToString() => Title;
    }
}
