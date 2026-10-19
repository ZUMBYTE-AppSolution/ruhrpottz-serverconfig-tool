using System.IO;
using System.Reflection;
using System.Text.Json;
using System.Windows;
using System.Windows.Automation;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Threading;
using PZServerArchitect.Windows;

internal static class Program
{
    private static MainWindow window = null!;
    private static int checks;
    private const BindingFlags PrivateInstance = BindingFlags.Instance | BindingFlags.NonPublic;

    [STAThread]
    private static int Main(string[] args)
    {
        if (!args.Contains("--offline")) throw new ArgumentException("Run this regression test with --offline.");
        Environment.SetEnvironmentVariable("PZ_ARCHITECT_DATA", Path.Combine(Path.GetTempPath(), "pz-wpf-editor-" + Guid.NewGuid()));
        var app = new App(); app.InitializeComponent();
        window = new MainWindow();
        int exit = 1;
        window.Loaded += async (_, _) =>
        {
            try
            {
                await ExerciseEditor();
                var report = $"PASS: {checks} WPF editor assertions. Real controls and local Java core; no network, installers or live server writes.";
                Console.WriteLine(report);
                var target = args.FirstOrDefault(a => a.EndsWith(".txt", StringComparison.OrdinalIgnoreCase));
                if (target is not null) { Directory.CreateDirectory(Path.GetDirectoryName(Path.GetFullPath(target))!); File.WriteAllText(target, report + Environment.NewLine); }
                exit = 0;
            }
            catch (Exception error) { Console.Error.WriteLine(error); }
            finally
            {
                Invoke("EndSettingEdit");
                typeof(MainWindow).GetField("state", PrivateInstance)!.SetValue(window, default(JsonElement));
                window.Close(); app.Shutdown(exit);
                Dispatcher.CurrentDispatcher.BeginInvokeShutdown(DispatcherPriority.Background);
            }
        };
        window.Show(); Dispatcher.Run();
        return exit;
    }

    private static async Task ExerciseEditor()
    {
        Invoke("Navigate", "settings"); await Task.Delay(60);
        Find<ComboBox>("SettingsCategory").SelectedIndex = 0;
        await Task.Delay(60);
        var list = Find<ListBox>("SettingsList");
        var initialChanged = State().GetProperty("changed").GetInt32();
        await Select("ini:PublicName");
        Check(State().GetProperty("changed").GetInt32() == initialChanged, "opening an editor does not create a change");
        var original = Value("ini:PublicName");
        var input = Find<TextBox>("SettingValue");
        input.Focus(); Keyboard.Focus(input); FocusManager.SetFocusedElement(window, input);
        var order = list.Items.Cast<ListBoxItem>().Select(Id).ToArray();
        var scroll = Descendants(list).OfType<ScrollViewer>().First();
        await Dispatcher.Yield(DispatcherPriority.ApplicationIdle);
        window.UpdateLayout();
        scroll.ScrollToVerticalOffset(Math.Min(20, scroll.ScrollableHeight));
        await Dispatcher.Yield(DispatcherPriority.ApplicationIdle);
        double offset = scroll.VerticalOffset;
        Check(offset > 0, "test edits a setting after scrolling down the list");
        Check(!Descendants(window).OfType<Button>().Any(b => Equals(b.Content, "Übernehmen")), "no apply button in the value editor");
        input.Text = "Automatic";
        await Task.Delay(100);
        input.Text = "Automatic server";
        input.CaretIndex = input.Text.Length;
        await Until(() => Value("ini:PublicName") == "Automatic server", "typing commits without pressing a button");
        Check(ReferenceEquals(input, Find<TextBox>("SettingValue")), "typing keeps the input control");
        Check(ReferenceEquals(FocusManager.GetFocusedElement(window), input), "typing keeps logical focus");
        Check(input.CaretIndex == input.Text.Length, "typing keeps caret position");
        StableList(list, scroll, offset, order, "ini:PublicName");
        Invoke("Mutate", "undo", null, null);
        Check(Value("ini:PublicName") == original, "one undo reverses a burst of typing");
        StableList(list, scroll, offset, order, "ini:PublicName");
        Invoke("Mutate", "redo", null, null);
        Check(Value("ini:PublicName") == "Automatic server", "redo restores the automatic change");
        StableList(list, scroll, offset, order, "ini:PublicName");

        await Select("ini:Public");
        var choice = Find<ComboBox>("SettingValue");
        choice.SelectedIndex = 1;
        await Until(() => Value("ini:Public") == "true", "dropdown choice commits immediately");
        choice.SelectedIndex = 0;
        await Until(() => !Setting("ini:Public").GetProperty("present").GetBoolean(), "not-set choice removes the key immediately");
        Check(ReferenceEquals(choice, Find<ComboBox>("SettingValue")), "dropdown keeps its control");

        await Select("ini:MaxPlayers");
        var number = Find<TextBox>("SettingValue");
        string before = Value("ini:MaxPlayers");
        number.Text = "-";
        for (int i = 0; i < 100 && Find<TextBlock>("SettingFeedback").Text == "Änderung wird übernommen …"; i++) await Task.Delay(50);
        Check(Value("ini:MaxPlayers") == before, "invalid intermediate number does not overwrite the last value");
        Check(Find<TextBlock>("SettingFeedback").Text != "Automatisch übernommen", "invalid value has inline feedback");
        Invoke("Navigate", "export");
        Check(ReferenceEquals(list, Find<ListBox>("SettingsList")), "invalid value keeps the editor open");
        var other = list.Items.Cast<ListBoxItem>().First(i => Id(i) == "ini:PublicName");
        list.SelectedItem = other;
        Check(Id((ListBoxItem)list.SelectedItem) == "ini:MaxPlayers", "invalid value keeps the current selection");
        Check(ReferenceEquals(number, Find<TextBox>("SettingValue")), "invalid input text is retained");
        number.Text = "42";
        await Until(() => Value("ini:MaxPlayers") == "42", "valid correction commits automatically");
        offset = scroll.VerticalOffset;
        number.Text = "43";
        Invoke("Navigate", "export");
        Check(Value("ini:MaxPlayers") == "43", "navigation flushes the pending final keystroke");
        Invoke("Navigate", "settings"); await Dispatcher.Yield(DispatcherPriority.ApplicationIdle);
        var restored = Find<ListBox>("SettingsList");
        var restoredScroll = Descendants(restored).OfType<ScrollViewer>().First();
        Check(Math.Abs(restoredScroll.VerticalOffset - offset) < .01, "returning to settings restores scroll position");
        Check(Id((ListBoxItem)restored.SelectedItem) == "ini:MaxPlayers", "returning to settings restores selection");
        Invoke("Mutate", "restore", new { id = "ini:MaxPlayers" }, null);
        Check(ReferenceEquals(restored, Find<ListBox>("SettingsList")), "restore keeps the list instance");
        Check(Math.Abs(restoredScroll.VerticalOffset - offset) < .01, "restore keeps scroll position");
    }

    private static void StableList(ListBox list, ScrollViewer scroll, double offset, string[] order, string selected)
    {
        Check(ReferenceEquals(list, Find<ListBox>("SettingsList")), "edit keeps the settings list instance");
        Check(Math.Abs(scroll.VerticalOffset - offset) < .01, "edit keeps scroll position");
        Check(order.SequenceEqual(list.Items.Cast<ListBoxItem>().Select(Id)), "edit keeps the row order");
        Check(Id((ListBoxItem)list.SelectedItem) == selected, "edit keeps the selected setting");
    }
    private static async Task Select(string id)
    {
        var list = Find<ListBox>("SettingsList");
        var item = list.Items.Cast<ListBoxItem>().Single(i => Id(i) == id);
        await Dispatcher.Yield(DispatcherPriority.ApplicationIdle);
        list.SelectedItem = item; window.UpdateLayout(); list.ScrollIntoView(item);
        await Dispatcher.Yield(DispatcherPriority.ApplicationIdle);
    }
    private static string Id(ListBoxItem item) => (string)item.Tag.GetType().GetProperty("Id")!.GetValue(item.Tag)!;
    private static JsonElement State() => (JsonElement)typeof(MainWindow).GetField("state", PrivateInstance)!.GetValue(window)!;
    private static JsonElement Setting(string id) => State().GetProperty("settings").EnumerateArray().Single(e => e.GetProperty("id").GetString() == id);
    private static string Value(string id) => Setting(id).GetProperty("value").GetString()!;
    private static void Invoke(string name, params object?[] args) => typeof(MainWindow).GetMethod(name, PrivateInstance)!.Invoke(window, args);
    private static void Check(bool condition, string message) { if (!condition) throw new InvalidOperationException(message); checks++; }
    private static async Task Until(Func<bool> condition, string message)
    {
        for (int i = 0; i < 100 && !condition(); i++) await Task.Delay(50);
        Check(condition(), message);
    }
    private static T Find<T>(string id) where T : DependencyObject => Descendants(window).OfType<T>().Single(e => AutomationProperties.GetAutomationId(e) == id);
    private static IEnumerable<DependencyObject> Descendants(DependencyObject root)
    {
        for (int i = 0; i < VisualTreeHelper.GetChildrenCount(root); i++)
        {
            var child = VisualTreeHelper.GetChild(root, i); yield return child;
            foreach (var nested in Descendants(child)) yield return nested;
        }
    }
}
