using System.Diagnostics;
using System.IO;
using System.Text;
using System.Text.Json;

namespace PZServerArchitect.Windows;

internal sealed class CoreClient : IDisposable
{
    private readonly Process process;
    private readonly object gate = new();

    public CoreClient()
    {
        var baseDir = AppContext.BaseDirectory;
        var jar = Environment.GetEnvironmentVariable("PZ_ARCHITECT_CORE_JAR");
        if (string.IsNullOrWhiteSpace(jar))
        {
            jar = Path.Combine(baseDir, "engine", "PZ-Server-Architect-0.4.1.jar");
            if (!File.Exists(jar))
            {
                var dir = new DirectoryInfo(baseDir);
                while (dir is not null && !File.Exists(Path.Combine(dir.FullName, "tools", "build.py"))) dir = dir.Parent;
                if (dir is not null) jar = Path.Combine(dir.FullName, "build", "PZ-Server-Architect-0.4.1.jar");
            }
        }
        if (!File.Exists(jar)) throw new FileNotFoundException("Die Konfigurationslogik fehlt. Bitte die App vollständig entpacken.", jar);
        var java = Environment.GetEnvironmentVariable("PZ_ARCHITECT_JAVA");
        if (string.IsNullOrWhiteSpace(java))
        {
            var bundled = Path.Combine(baseDir, "runtime", "bin", "java.exe");
            java = File.Exists(bundled) ? bundled : "java";
        }
        var start = new ProcessStartInfo(java)
        {
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardInput = true,
            RedirectStandardOutput = true,
            StandardInputEncoding = new UTF8Encoding(false),
            StandardOutputEncoding = Encoding.UTF8
        };
        if (Environment.GetEnvironmentVariable("PZ_ARCHITECT_DATA") is { Length: > 0 } data)
            start.ArgumentList.Add("-Dpz.data=" + data);
        start.ArgumentList.Add("-cp");
        start.ArgumentList.Add(jar);
        start.ArgumentList.Add("de.zumbyte.pzarchitect.app.CoreBridge");
        process = Process.Start(start) ?? throw new InvalidOperationException("Konfigurationslogik konnte nicht gestartet werden.");
    }

    public JsonElement Call(string command, object? arguments = null)
    {
        lock (gate)
        {
            if (process.HasExited) throw new InvalidOperationException("Konfigurationslogik wurde unerwartet beendet.");
            var request = new Dictionary<string, object?> { ["command"] = command };
            if (arguments is not null)
                foreach (var property in JsonSerializer.SerializeToElement(arguments).EnumerateObject()) request[property.Name] = property.Value;
            process.StandardInput.WriteLine(JsonSerializer.Serialize(request));
            process.StandardInput.Flush();
            var line = process.StandardOutput.ReadLine() ?? throw new InvalidOperationException("Keine Antwort von der Konfigurationslogik.");
            using var response = JsonDocument.Parse(line);
            var root = response.RootElement;
            if (!root.GetProperty("ok").GetBoolean()) throw new InvalidOperationException(root.GetProperty("error").GetString());
            return root.GetProperty("data").Clone();
        }
    }

    public void Dispose()
    {
        try { process.StandardInput.Close(); if (!process.WaitForExit(1500)) process.Kill(); } catch { /* closing */ }
        process.Dispose();
    }
}

internal static class JsonExt
{
    public static string S(this JsonElement e, string key) => e.GetProperty(key).GetString() ?? "";
    public static bool B(this JsonElement e, string key) => e.GetProperty(key).GetBoolean();
    public static int I(this JsonElement e, string key) => e.GetProperty(key).GetInt32();
}
