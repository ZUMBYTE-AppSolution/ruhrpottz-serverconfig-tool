package de.zumbyte.pzarchitect.update;

import java.io.*;
import java.net.URI;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.*;

/** Repository-bound update checker and verified download staging. Does not replace a running app. */
public final class UpdateClient {
    public static final String PRODUCT = "pz-server-architect";
    public static final String REPOSITORY = "ZUMBYTE-AppSolution/ruhrpottz-serverconfig-tool";
    public static final String UPDATER_VERSION = "0.2.0";
    public static final URI RELEASES = URI.create("https://api.github.com/repos/" + REPOSITORY + "/releases?per_page=100");
    public static final int MAX_RELEASE_PAGES = 5;
    public static final long MAX_PACKAGE_BYTES = 2L * 1024 * 1024 * 1024;
    private static final int MAX_METADATA_BYTES = 1_048_576;

    public enum Target {
        WINDOWS_X64("windows-x64", ".msi"), WINDOWS_ARM64("windows-arm64", ".msi"),
        MACOS_X64("macos-x64", ".dmg"), MACOS_ARM64("macos-arm64", ".dmg"),
        LINUX_X64("linux-x64", ".deb"), LINUX_ARM64("linux-arm64", ".deb");
        public final String id, extension;
        Target(String id, String extension) { this.id = id; this.extension = extension; }
        public static Target detect(String os, String architecture) {
            String a = architecture.toLowerCase(Locale.ROOT);
            String arch = switch (a) { case "amd64", "x86_64", "x64" -> "x64"; case "aarch64", "arm64" -> "arm64"; default -> throw new IllegalArgumentException("Unsupported architecture: " + architecture); };
            String n = os.toLowerCase(Locale.ROOT);
            String family = n.contains("mac") || n.contains("darwin") ? "macos" : n.startsWith("windows") ? "windows" : n.equals("linux") ? "linux" : "unsupported";
            return Arrays.stream(values()).filter(t -> t.id.equals(family + "-" + arch)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unsupported system: " + os));
        }
    }
    public record Config(Version current, Target target, Path cache) {
        public Config {
            Objects.requireNonNull(current); Objects.requireNonNull(target); Objects.requireNonNull(cache);
        }
        public static Config github(String current, Path cache) {
            return new Config(Version.parse(current), Target.detect(System.getProperty("os.name"), System.getProperty("os.arch")), cache);
        }
    }
    public enum State { UPDATE_AVAILABLE, UP_TO_DATE, NO_RELEASE, NO_PLATFORM_PACKAGE }
    public record Check(State state, Candidate candidate, String message) {}
    public record Candidate(Version version, Target target, URI url, String fileName, long bytes, String sha256,
                            String notes) {}
    public record Downloaded(Candidate candidate, Path file) {}
    public interface Transport { Response open(URI uri, boolean asset) throws Exception; }
    public record Response(int status, Map<String, List<String>> headers, InputStream body) implements AutoCloseable {
        public Response { Objects.requireNonNull(headers); Objects.requireNonNull(body); }
        @Override public void close() throws IOException { body.close(); }
    }
    private final Config config;
    private final Transport transport;
    private final Clock clock;
    private Instant retryAfter = Instant.MIN;
    /** A temporary GitHub throttle, not an indication that the installed version is current. */
    public static final class RateLimitException extends IOException {
        private static final long serialVersionUID = 1L;
        private final Instant retryAt;
        RateLimitException(Instant retryAt) {
            super("GitHub request limit reached; retry after " + retryAt);
            this.retryAt = retryAt;
        }
        public Instant retryAt() { return retryAt; }
    }
    public UpdateClient(Config config, Transport transport, Clock clock) {
        this.config = Objects.requireNonNull(config); this.transport = Objects.requireNonNull(transport); this.clock = Objects.requireNonNull(clock);
    }
    public UpdateClient(Config config) { this(config, new HttpsTransport(), Clock.systemUTC()); }

    public synchronized Check check() throws Exception {
        if (clock.instant().isBefore(retryAfter)) throw new RateLimitException(retryAfter);
        List<?> list = readReleases();
        Map<String, Object> best = null;
        Version newest = null;
        for (Object item : list) {
            Map<String, Object> release = Json.object(item);
            if (Json.bool(release, "draft") || Json.bool(release, "prerelease")) continue;
            Version v;
            try { v = Version.parse(Json.string(release, "tag_name")); } catch (IllegalArgumentException e) { continue; }
            if (v.pre() != null) continue;
            if (newest == null || v.compareTo(newest) > 0) { newest = v; best = release; }
        }
        if (newest == null) return new Check(State.NO_RELEASE, null, "Noch keine stabile Version veröffentlicht.");
        if (newest.compareTo(config.current()) <= 0) return new Check(State.UP_TO_DATE, null, "Keine neuere stabile Version gefunden.");
        if (!(best.get("assets") instanceof List<?> assets)) throw new IOException("Release has no asset list");
        String expected = assetName(newest, config.target());
        Map<String, Object> selected = null;
        for (Object item : assets) {
            Map<String, Object> asset = Json.object(item);
            if (!expected.equals(Json.string(asset, "name"))) continue;
            if (selected != null) throw new IOException("Duplicate release package");
            selected = asset;
        }
        if (selected == null) return new Check(State.NO_PLATFORM_PACKAGE, null, "Neue Version vorhanden, aber kein passendes Paket für " + config.target().id + ".");
        if (!"uploaded".equals(Json.string(selected, "state"))) throw new IOException("Release package is incomplete");
        String digest = Json.string(selected, "digest");
        if (!digest.matches("sha256:[0-9a-fA-F]{64}")) throw new IOException("GitHub SHA-256 digest missing or invalid");
        String tag = Json.string(best, "tag_name");
        URI url = URI.create(Json.string(selected, "browser_download_url"));
        requireAssetUrl(url, tag, expected);
        Object body = best.get("body");
        String notes = body instanceof String s ? s : "Kein Changelog hinterlegt.";
        Candidate candidate = new Candidate(newest, config.target(), url, expected, Json.integer(selected, "size"), digest.substring(7).toLowerCase(Locale.ROOT), notes);
        validateCandidate(candidate);
        return new Check(State.UPDATE_AVAILABLE, candidate, "Version " + newest.text() + " verfügbar.");
    }
    public Downloaded download(Candidate candidate, LongConsumer progress) throws Exception {
        validateCandidate(candidate);
        Objects.requireNonNull(progress);
        Path root = config.cache().toAbsolutePath().normalize();
        if (Files.isSymbolicLink(root)) throw new SecurityException("Cache must not be a symbolic link");
        Files.createDirectories(root);
        Path target = root.resolve(candidate.fileName());
        Path lock = root.resolve(".download.lock");
        if (Files.isSymbolicLink(lock)) throw new SecurityException("Invalid lock path");
        try (FileChannel channel = FileChannel.open(lock, StandardOpenOption.CREATE, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
             FileLock held = channel.tryLock()) {
            if (held == null) throw new IOException("Another download is active");
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                verifyPackage(candidate, target);
                progress.accept(candidate.bytes());
                return new Downloaded(candidate, target);
            }
            Path part = Files.createTempFile(root, ".download-", ".part");
            try {
                MessageDigest hash = MessageDigest.getInstance("SHA-256");
                long count = 0;
                try (Response response = transport.open(candidate.url(), true);
                     OutputStream out = Files.newOutputStream(part, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
                    if (response.status() != 200) throw httpError(response);
                    String length = header(response, "Content-Length");
                    if (length != null && Long.parseLong(length) != candidate.bytes()) throw new IOException("HTTP package size mismatch");
                    byte[] buffer = new byte[64 * 1024];
                    int read;
                    while ((read = response.body().read(buffer)) != -1) {
                        if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Download cancelled");
                        if (read == 0) continue;
                        count += read;
                        if (count > candidate.bytes()) throw new IOException("Package exceeds declared size");
                        hash.update(buffer, 0, read); out.write(buffer, 0, read);
                        progress.accept(count);
                    }
                }
                if (count != candidate.bytes() || !HexFormat.of().formatHex(hash.digest()).equalsIgnoreCase(candidate.sha256())) throw new SecurityException("Downloaded package failed size or SHA-256 verification");
                validateCandidate(candidate);
                try (FileChannel file = FileChannel.open(part, StandardOpenOption.WRITE)) { file.force(true); }
                try { Files.move(part, target, StandardCopyOption.ATOMIC_MOVE); }
                catch (AtomicMoveNotSupportedException e) { Files.move(part, target); }
                return new Downloaded(candidate, target);
            } finally { Files.deleteIfExists(part); }
        }
    }
    /** Recheck just before handing a package to the OS installer; never disables OS protections. */
    public void verifyPackage(Candidate candidate, Path file) throws Exception {
        validateCandidate(candidate);
        if (Files.isSymbolicLink(file) || !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) || Files.size(file) != candidate.bytes()) throw new SecurityException("Staged package missing or wrong size");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
            byte[] bytes = new byte[64 * 1024]; int read;
            while ((read = in.read(bytes)) != -1) {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Verification cancelled");
                digest.update(bytes, 0, read);
            }
        }
        if (!HexFormat.of().formatHex(digest.digest()).equalsIgnoreCase(candidate.sha256())) throw new SecurityException("Staged package was changed");
    }
    private void validateCandidate(Candidate c) {
        Objects.requireNonNull(c);
        if (c.target() != config.target() || c.version().compareTo(config.current()) <= 0 || c.version().pre() != null) throw new SecurityException("Incompatible or old package");
        if (!assetName(c.version(), c.target()).equals(c.fileName()) || !c.fileName().matches("[A-Za-z0-9._+-]{1,200}")) throw new SecurityException("Invalid package name");
        if (c.bytes() <= 0 || c.bytes() > MAX_PACKAGE_BYTES || !c.sha256().matches("[0-9a-fA-F]{64}")) throw new SecurityException("Invalid package checksum/size");
        if (c.notes().length() > 131_072) throw new SecurityException("Changelog too large");
        requireHttps(c.url());
        String prefix = "https://github.com/" + REPOSITORY + "/releases/download/";
        if (!c.url().toString().startsWith(prefix)) throw new SecurityException("Unapproved download repository");
        String rest = c.url().toString().substring(prefix.length());
        int slash = rest.indexOf('/');
        if (slash <= 0 || !rest.substring(slash + 1).equals(c.fileName()) || !Version.parse(rest.substring(0, slash)).text().equals(c.version().text())) throw new SecurityException("Download tag/file mismatch");
        requireAssetUrl(c.url(), rest.substring(0, slash), c.fileName());
    }
    public static String assetName(Version v, Target target) { return "PZ-Server-Architect-" + v.text() + "-" + target.id + target.extension; }
    static void requireHttps(URI uri) {
        if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getFragment() != null || (uri.getPort() != -1 && uri.getPort() != 443)) throw new SecurityException("Only plain HTTPS endpoints are allowed");
    }
    static void requireAssetUrl(URI url, String tag, String file) {
        requireHttps(url);
        if (!url.toString().equals("https://github.com/" + REPOSITORY + "/releases/download/" + tag + "/" + file)) throw new SecurityException("Unexpected release asset URL");
    }
    static URI releasePage(int page) {
        if (page < 1 || page > MAX_RELEASE_PAGES) throw new IllegalArgumentException("Invalid release page");
        return page == 1 ? RELEASES : URI.create(RELEASES + "&page=" + page);
    }
    static void requireMetadataUrl(URI uri) {
        requireHttps(uri);
        for (int page = 1; page <= MAX_RELEASE_PAGES; page++) {
            if (uri.equals(releasePage(page))) return;
        }
        throw new SecurityException("Only this repository's GitHub release API is allowed");
    }
    private List<Object> readReleases() throws Exception {
        List<Object> releases = new ArrayList<>();
        for (int page = 1; page <= MAX_RELEASE_PAGES; page++) {
            try (Response response = transport.open(releasePage(page), false)) {
                if (response.status() != 200) throw httpError(response);
                byte[] bytes = response.body().readNBytes(MAX_METADATA_BYTES + 1);
                if (bytes.length > MAX_METADATA_BYTES) throw new IOException("Metadata exceeds size limit");
                Object parsed = Json.parse(utf8(bytes));
                if (!(parsed instanceof List<?> values) || values.size() > 100) throw new IOException("Invalid GitHub release page");
                releases.addAll(values);
                // Do not follow arbitrary Link URLs: construct the next repository-bound page ourselves.
                boolean hasNext = response.headers().entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase("Link"))
                    .flatMap(e -> e.getValue().stream())
                    .anyMatch(value -> java.util.regex.Pattern.compile("(?:^|;)\\s*rel\\s*=\\s*\"next\"(?:\\s*[,;]|\\s*$)").matcher(value).find());
                if (!hasNext) return releases;
                if (page == MAX_RELEASE_PAGES) throw new IOException("Release scan limit reached; version status is unknown");
            }
        }
        throw new IOException("Release scan incomplete");
    }
    static String utf8(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
    }
    private IOException httpError(Response response) {
        int status = response.status();
        String wait = header(response, "Retry-After"), remaining = header(response, "X-RateLimit-Remaining");
        if (status == 429 || (status == 403 && ("0".equals(remaining) || wait != null))) {
            Instant now = clock.instant(), retry = now.plusSeconds(60);
            try {
                if (wait != null) {
                    retry = wait.matches("[0-9]{1,9}") ? now.plusSeconds(Long.parseLong(wait))
                        : ZonedDateTime.parse(wait, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                } else if ("0".equals(remaining)) {
                    String reset = header(response, "X-RateLimit-Reset");
                    if (reset != null && reset.matches("[0-9]{1,12}")) retry = Instant.ofEpochSecond(Long.parseLong(reset));
                }
            } catch (IllegalArgumentException | DateTimeException ignored) { retry = now.plusSeconds(60); }
            if (!retry.isAfter(now)) retry = now.plusSeconds(60);
            synchronized (this) { retryAfter = retry; }
            return new RateLimitException(retry);
        }
        return new IOException("Update source returned HTTP " + status);
    }
    static String header(Response r, String name) {
        return r.headers().entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(name)).flatMap(e -> e.getValue().stream()).findFirst().orElse(null);
    }
}
