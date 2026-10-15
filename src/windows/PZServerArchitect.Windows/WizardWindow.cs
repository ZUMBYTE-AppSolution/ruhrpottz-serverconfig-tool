using System.Text.Json;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace PZServerArchitect.Windows;

internal sealed class WizardWindow : Window
{
    private readonly CoreClient core;
    private readonly int[] answers = new int[9];
    private int step;
    private readonly Grid page;
    private readonly Button back, next;
    private bool existingOnly;
    public JsonElement? UpdatedState { get; private set; }
    private static readonly string[] Questions = [
        "Welche Spielrichtung passt zu deinem Server?", "Wie viele Zombies sollen unterwegs sein?", "Wie lang soll ein Spieltag dauern?",
        "Wie häufig soll Alltagsloot sein?", "Sollen Zombies wieder erscheinen?", "Dürfen Spieler gegeneinander kämpfen?",
        "Wie schnell lernen Charaktere?", "Wie lange funktionieren Strom und Wasser?", "Wie soll sich das Wetter verhalten?"
    ];
    private static readonly string[] Explanations = [
        "Die Spielrichtung legt einen ausgewogenen Ausgangspunkt fest. Jeder Wert lässt sich später einzeln ändern.",
        "Mehr Zombies verändern das Überleben deutlich. Bei „Wie Vorlage“ bleibt die Spielrichtung maßgeblich.",
        "Die echte Dauer eines Spieltags bestimmt das Tempo deiner Welt. Längere Tage lassen mehr Zeit für RP und Erkundung.",
        "Dieser Schritt passt Alltagsgegenstände wie Nahrung und Material an. Spezialbeute bleibt getrennt.",
        "Wiederkehr hält die Welt langfristig belebt. Ohne Wiederkehr bleiben geräumte Gebiete eher frei.",
        "PvP ermöglicht Konflikte zwischen Spielern. Weitere Schutzregeln kannst du danach im Editor abstimmen.",
        "Ein höherer Multiplikator beschleunigt den Fortschritt aller Fähigkeiten. Für langfristiges RP eignet sich ein gemäßigtes Tempo.",
        "Dauerhafte Versorgung verhindert den Ausfall von Strom und Wasser. Vor Einsatz mit deinem Build prüfen.",
        "Wähle natürliche Zyklen oder eine gezielte Abweichung. Ohne Auswahl bleibt die Vorlage unverändert."
    ];
    private static readonly string[][] Options = [
        ["Koop & Freunde", "PvE Community", "Survival ausgewogen", "Hardcore Survival", "PvP Survival", "Roleplay mit Zombies", "Ziviles Roleplay", "Bauen & Testen"],
        ["Wie Vorlage", "Aus", "Wenig (0,35)", "Normal (1)", "Hoch (2,5)"],
        ["Wie Vorlage", "1 Stunde", "2 Stunden", "4 Stunden", "6 Stunden"],
        ["Wie Vorlage", "Knapp (0,4)", "Ausgewogen (1)", "Reichlich (2)"],
        ["Wie Vorlage", "Keine Wiederkehr", "Alle 72 Spielstunden"],
        ["Wie Vorlage", "Aus", "Ein, mit Sicherheitssystem"],
        ["Wie Vorlage", "Normal (1×)", "Schneller (2×)", "Sehr schnell (4×)"],
        ["Unverändert lassen", "Strom und Wasser dauerhaft (B42.21 prüfen)"],
        ["Unverändert lassen", "Natürliche Zyklen", "Kein Nebel", "Dauer-Tag ohne Wetter"]
    ];

    public WizardWindow(CoreClient core, bool imported)
    {
        this.core = core;
        Title = "Quick Start · PZ Server Architect"; Width = 850; Height = 730; MinWidth = 700; MinHeight = 600;
        WindowStartupLocation = WindowStartupLocation.CenterOwner; Background = DialogStyle.Color("#0D111B");
        var shell = new Grid { Margin = new Thickness(32, 27, 32, 27) };
        shell.RowDefinitions.Add(new RowDefinition { Height = new GridLength(1, GridUnitType.Star) });
        shell.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        page = new Grid(); shell.Children.Add(page);
        var footer = new Grid { Margin = new Thickness(0, 20, 0, 0) }; footer.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) }); footer.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
        var cancel = DialogStyle.Button("Abbrechen"); cancel.Click += (_, _) => DialogResult = false; footer.Children.Add(cancel);
        var actions = new StackPanel { Orientation = Orientation.Horizontal }; Grid.SetColumn(actions, 1); footer.Children.Add(actions);
        back = DialogStyle.Button("Zurück"); back.Click += (_, _) => { if (step > 0) { step--; Render(); } }; actions.Children.Add(back);
        next = DialogStyle.Button("Weiter", true); next.Click += (_, _) => { if (step < 9) { step++; Render(); } else Apply(); }; actions.Children.Add(next);
        existingOnly = imported;
        Grid.SetRow(footer, 1); shell.Children.Add(footer); Content = shell; Render();
    }

    private void Render()
    {
        page.Children.Clear(); page.RowDefinitions.Clear();
        page.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        page.RowDefinitions.Add(new RowDefinition { Height = new GridLength(1, GridUnitType.Star) });
        var head = new StackPanel();
        head.Children.Add(DialogStyle.Label(step == 9 ? "VORSCHAU" : $"SCHRITT {step + 1} VON 9", 12, true));
        var title = DialogStyle.Label(step == 9 ? "Deine Konfiguration prüfen" : Questions[step], 25); title.FontWeight = FontWeights.SemiBold; title.Margin = new Thickness(0, 8, 0, 7); head.Children.Add(title);
        head.Children.Add(DialogStyle.Label(step == 9 ? "Diese Werte werden nach Bestätigung als ein Bearbeitungsschritt übernommen." : Explanations[step], 14, true));
        var progress = new ProgressBar { Minimum = 0, Maximum = 9, Value = step + 1, Height = 6, Margin = new Thickness(0, 24, 0, 18), Foreground = DialogStyle.Color("#B58CFF"), Background = DialogStyle.Color("#30394D") }; head.Children.Add(progress);
        page.Children.Add(head);
        var body = new StackPanel(); Grid.SetRow(body, 1); page.Children.Add(body);
        if (step < 9)
        {
            var options = new ListBox();
            for (int i = 0; i < Options[step].Length; i++) options.Items.Add(Options[step][i]);
            options.SelectedIndex = answers[step];
            options.SelectionChanged += (_, _) => { if (options.SelectedIndex >= 0) answers[step] = options.SelectedIndex; };
            body.Children.Add(new ScrollViewer { Content = options, VerticalScrollBarVisibility = ScrollBarVisibility.Auto });
        }
        else
        {
            var only = new CheckBox { Content = "Nur bereits vorhandene Schlüssel ändern", IsChecked = existingOnly, Foreground = DialogStyle.Color("#F2F5FC"), FontSize = 13, Margin = new Thickness(0, 0, 0, 15) };
            only.Checked += (_, _) => { existingOnly = true; Render(); }; only.Unchecked += (_, _) => { existingOnly = false; Render(); };
            body.Children.Add(only);
            try
            {
                var preview = core.Call("previewWizard", new { choices = answers, existingOnly });
                body.Children.Add(DialogStyle.Label("Ausgangspunkt: " + preview.S("preset"), 13, true));
                var changes = preview.GetProperty("changes").EnumerateArray().ToList();
                body.Children.Add(DialogStyle.Label($"{changes.Count} vorgeschlagene Änderungen", 16));
                var list = new StackPanel { Margin = new Thickness(0, 12, 0, 0) };
                foreach (var change in changes)
                {
                    var entry = new StackPanel { Margin = new Thickness(0, 0, 0, 13) };
                    entry.Children.Add(DialogStyle.Label(change.S("title"), 13));
                    entry.Children.Add(DialogStyle.Label(change.S("before") + "  →  " + change.S("after"), 12, true)); list.Children.Add(entry);
                }
                body.Children.Add(new ScrollViewer { Content = list, VerticalScrollBarVisibility = ScrollBarVisibility.Auto });
            }
            catch (Exception ex) { body.Children.Add(DialogStyle.Label(ex.Message, 14)); next.IsEnabled = false; }
        }
        back.IsEnabled = step > 0; next.Content = step == 9 ? "Änderungen übernehmen" : step == 8 ? "Vorschau öffnen" : "Weiter";
    }

    private void Apply()
    {
        try { UpdatedState = core.Call("applyWizard", new { choices = answers, existingOnly }); DialogResult = true; }
        catch (Exception ex) { MessageBox.Show(this, ex.Message, "Quick Start", MessageBoxButton.OK, MessageBoxImage.Warning); }
    }
}
