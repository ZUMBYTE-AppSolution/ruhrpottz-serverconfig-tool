package de.zumbyte.pzarchitect.update;

import java.awt.Desktop;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Integrate start() after the app window is visible. Events are dispatched on the supplied UI executor. */
public final class StartupUpdates implements AutoCloseable {
    public enum Mode { MANUAL, NOTIFY, AUTO_DOWNLOAD }
    public enum Phase { CHECKING, AVAILABLE, DOWNLOADING, READY, COMPLETE, FAILED }
    public record Event(Phase phase, String message, long downloadedBytes, long totalBytes,
                        UpdateClient.Candidate candidate, UpdateClient.Downloaded downloaded) {}
    private final UpdateClient client;
    private final Consumer<Runnable> ui;
    private final Consumer<Event> listener;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "pz-update-worker"); t.setDaemon(true); return t;
    });
    private Future<?> current;
    private boolean closed;
    public StartupUpdates(UpdateClient client, Consumer<Runnable> ui, Consumer<Event> listener) {
        this.client = Objects.requireNonNull(client); this.ui = Objects.requireNonNull(ui); this.listener = Objects.requireNonNull(listener);
    }
    public synchronized boolean start(Mode mode) {
        Objects.requireNonNull(mode);
        if (closed || mode == Mode.MANUAL || (current != null && !current.isDone())) return false;
        current = worker.submit(() -> run(mode));
        return true;
    }
    private void run(Mode mode) {
        try {
            send(new Event(Phase.CHECKING, "Suche nach Updates …", 0, 0, null, null));
            UpdateClient.Check check = client.check();
            if (check.state() != UpdateClient.State.UPDATE_AVAILABLE) {
                send(new Event(Phase.COMPLETE, check.message(), 0, 0, null, null)); return;
            }
            UpdateClient.Candidate c = check.candidate();
            send(new Event(Phase.AVAILABLE, check.message(), 0, c.bytes(), c, null));
            if (mode != Mode.AUTO_DOWNLOAD) return;
            long[] last = {0};
            UpdateClient.Downloaded downloaded = client.download(c, count -> {
                long now = System.nanoTime();
                if (now - last[0] >= 100_000_000L || count == c.bytes()) {
                    last[0] = now;
                    send(new Event(Phase.DOWNLOADING, "Update wird geladen und geprüft …", count, c.bytes(), c, null));
                }
            });
            send(new Event(Phase.READY, "Update geprüft. Installation nach dem Speichern starten.", c.bytes(), c.bytes(), c, downloaded));
        } catch (Exception e) {
            if (e instanceof InterruptedException || e instanceof java.io.InterruptedIOException) Thread.currentThread().interrupt();
            // Do not send exception text, URLs, tokens or local paths to the UI or telemetry.
            String message = e instanceof UpdateClient.RateLimitException rate
                ? "GitHub begrenzt die Abfragen. Erneute Prüfung ab " + rate.retryAt() + ". Die vorhandene App bleibt nutzbar."
                : "Updateprüfung oder Download fehlgeschlagen. Die vorhandene App bleibt nutzbar.";
            send(new Event(Phase.FAILED, message, 0, 0, null, null));
        }
    }
    private void send(Event event) {
        if (Thread.currentThread().isInterrupted()) return;
        ui.accept(() -> { synchronized (this) { if (closed) return; } listener.accept(event); });
    }
    /** Called only from an explicit Install action after saving/closing the current work. */
    public void openInstaller(UpdateClient.Downloaded downloaded, boolean workSaved) throws Exception {
        if (!workSaved) throw new IllegalStateException("Save current work before installing");
        client.verifyPackage(downloaded.candidate(), downloaded.file());
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) throw new IOException("No OS installer handler; open the verified package manually");
        Desktop.getDesktop().open(downloaded.file().toFile());
    }
    @Override public synchronized void close() { closed = true; if (current != null) current.cancel(true); worker.shutdownNow(); }
}
