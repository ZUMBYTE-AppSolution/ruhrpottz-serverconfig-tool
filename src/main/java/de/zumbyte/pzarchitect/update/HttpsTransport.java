package de.zumbyte.pzarchitect.update;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;

/** No automatic redirects: metadata stays on its origin; asset redirects use a fixed CDN allowlist. */
public final class HttpsTransport implements UpdateClient.Transport {
    private static final Set<String> ASSET_HOSTS = Set.of("github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com");
    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "pz-update-deadlines"); t.setDaemon(true); return t;
    });
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(10)).build();
    @Override public UpdateClient.Response open(URI uri, boolean asset) throws Exception {
        UpdateClient.requireHttps(uri);
        URI original = uri;
        for (int hop = 0; hop < 6; hop++) {
            validateRedirect(original, uri, asset);
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20))
                .header("User-Agent", "PZ-Server-Architect-Updater/" + UpdateClient.UPDATER_VERSION)
                .header("Accept", asset ? "application/octet-stream" : "application/json").GET().build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int status = response.statusCode();
            if (Set.of(301, 302, 303, 307, 308).contains(status)) {
                response.body().close();
                String next = response.headers().firstValue("Location").orElseThrow(() -> new IOException("Redirect without location"));
                uri = uri.resolve(next);
                continue;
            }
            InputStream body = response.body();
            // The response-header timeout alone does not bound a streaming body.
            ScheduledFuture<?> deadline = TIMER.schedule(() -> {
                try { body.close(); } catch (IOException ignored) { }
            }, asset ? 600 : 20, TimeUnit.SECONDS);
            InputStream bounded = new FilterInputStream(body) {
                @Override public void close() throws IOException { deadline.cancel(false); super.close(); }
            };
            return new UpdateClient.Response(status, response.headers().map(), bounded);
        }
        throw new IOException("Too many update redirects");
    }
    static void validateRedirect(URI original, URI next, boolean asset) {
        UpdateClient.requireHttps(next);
        if (asset) {
            if (!ASSET_HOSTS.contains(next.getHost())) throw new SecurityException("Unapproved update download host");
            if (next.getHost().equals("github.com") && !next.getRawPath().startsWith("/" + UpdateClient.REPOSITORY + "/releases/download/")) throw new SecurityException("Unapproved GitHub path");
        } else if (!original.getHost().equals(next.getHost()) || original.getPort() != next.getPort()) {
            throw new SecurityException("Metadata redirected to a different origin");
        }
    }
}
