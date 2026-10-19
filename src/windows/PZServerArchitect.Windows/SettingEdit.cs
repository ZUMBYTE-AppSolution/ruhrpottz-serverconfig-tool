using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Threading;
using System.Text.Json;

namespace PZServerArchitect.Windows;

public partial class MainWindow
{
    private SettingEdit? settingEdit;
    private ListBox? settingsList;
    private Action<bool>? refreshSettingsView;
    private double settingsScrollOffset;

    private bool CommitSettingEdit() => settingEdit?.Flush(true) ?? true;
    private void EndSettingEdit() { settingEdit?.Dispose(); settingEdit = null; }
    private void RememberSettingsPosition()
    {
        if (route == "settings" && settingsList is not null && FindVisualChild<ScrollViewer>(settingsList) is { } scroll)
            settingsScrollOffset = scroll.VerticalOffset;
    }

    private static T? FindVisualChild<T>(DependencyObject parent) where T : DependencyObject
    {
        for (int i = 0; i < VisualTreeHelper.GetChildrenCount(parent); i++)
        {
            var child = VisualTreeHelper.GetChild(parent, i);
            if (child is T match) return match;
            if (FindVisualChild<T>(child) is { } nested) return nested;
        }
        return null;
    }

    private sealed record SettingCommit(JsonElement? State, string? Error);

    private Task<SettingCommit> ApplySettingValue(string id, string? value)
    {
        var current = settings.First(s => s.Id == id);
        if (value is null ? !current.Present : current.Present && current.Value == value)
            return Task.FromResult(new SettingCommit(null, null));
        return Task.Run(() =>
        {
            try { return new SettingCommit(core.Call(value is null ? "unset" : "set", new { id, value }), null); }
            catch (Exception ex) { return new SettingCommit(null, ex.Message); }
        });
    }

    private string? AcceptSettingValue(SettingCommit result)
    {
        if (result.Error is not null) return result.Error;
        if (result.State is { } updated)
        {
            state = updated; ReadState(); refreshSettingsView?.Invoke(false);
            Notice("Änderung automatisch übernommen");
        }
        return null;
    }

    /// <summary>Groups a typing burst into one validated workspace change.</summary>
    private sealed class SettingEdit : IDisposable
    {
        private readonly DispatcherTimer timer = new() { Interval = TimeSpan.FromMilliseconds(350) };
        private readonly Func<string?> read;
        private readonly Func<string?, Task<SettingCommit>> apply;
        private readonly Func<SettingCommit, string?> accept;
        private readonly Action<string, bool> feedback;
        private readonly System.Action focus;
        private bool pending, disposed;
        private Task<SettingCommit>? inFlight;

        public SettingEdit(Func<string?> read, Func<string?, Task<SettingCommit>> apply, Func<SettingCommit, string?> accept, Action<string, bool> feedback, System.Action focus)
        {
            this.read = read; this.apply = apply; this.accept = accept; this.feedback = feedback; this.focus = focus;
            timer.Tick += (_, _) => { timer.Stop(); _ = SaveAsync(); };
        }
        public void Changed(bool immediately = false)
        {
            if (disposed) return;
            pending = true; timer.Stop();
            feedback("Änderung wird übernommen …", false);
            if (immediately) _ = SaveAsync(); else timer.Start();
        }
        public bool Flush(bool focusError = false)
        {
            timer.Stop();
            while (!disposed && (pending || inFlight is not null))
            {
                var task = inFlight ?? Start();
                if (task is null) return true;
                if (!Finish(task, task.GetAwaiter().GetResult()))
                {
                    if (focusError) focus();
                    return false;
                }
            }
            return true;
        }
        private Task<SettingCommit>? Start()
        {
            if (disposed || !pending) return null;
            pending = false;
            return inFlight = apply(read());
        }
        private async Task SaveAsync()
        {
            while (!disposed)
            {
                var task = inFlight ?? Start();
                if (task is null) return;
                var result = await task;
                if (!Finish(task, result) || !pending || timer.IsEnabled) return;
            }
        }
        private bool Finish(Task<SettingCommit> task, SettingCommit result)
        {
            // A synchronous navigation flush may already have accepted this result.
            if (!ReferenceEquals(inFlight, task)) return true;
            inFlight = null;
            if (disposed) return true;
            var error = accept(result);
            if (error is null)
            {
                feedback(pending ? "Änderung wird übernommen …" : "Automatisch übernommen", false);
                return true;
            }
            pending = true;
            feedback(error, true);
            return false;
        }
        public void Dispose()
        {
            disposed = true; pending = false; timer.Stop();
            // A reset/profile change must not be overtaken by a queued old write.
            inFlight?.GetAwaiter().GetResult(); inFlight = null;
        }
    }
}
