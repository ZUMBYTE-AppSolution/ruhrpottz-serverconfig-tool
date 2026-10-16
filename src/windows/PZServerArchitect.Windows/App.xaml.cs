using System.Configuration;
using System.Data;
using System.Windows;

namespace PZServerArchitect.Windows;

/// <summary>
/// Interaction logic for App.xaml
/// </summary>
public partial class App : Application
{
    public App()
    {
        // Commit popup choices before a focus change can close the menu.
        EventManager.RegisterClassHandler(typeof(System.Windows.Controls.ComboBoxItem),
            UIElement.PreviewMouseLeftButtonDownEvent,
            new System.Windows.Input.MouseButtonEventHandler(ChooseDropDownItem), true);
    }

    private static void ChooseDropDownItem(object sender, System.Windows.Input.MouseButtonEventArgs e)
    {
        if (sender is not System.Windows.Controls.ComboBoxItem item ||
            System.Windows.Controls.ItemsControl.ItemsControlFromItemContainer(item) is not System.Windows.Controls.ComboBox combo) return;
        combo.SelectedItem = combo.ItemContainerGenerator.ItemFromContainer(item);
        combo.IsDropDownOpen = false;
        e.Handled = true;
    }
}
