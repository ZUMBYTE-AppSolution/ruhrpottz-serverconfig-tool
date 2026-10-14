package de.zumbyte.pzarchitect.update;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Offline test harness. All installer bytes are inert fixtures and are NEVER executed. */
public final class UpdateSelfTest {
    private static int passed;
    private static final byte[] INSTALLER = "inert test fixture, not an installer".getBytes(StandardCharsets.UTF_8);
    private static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final URI MASTER = URI.create("https://master.invalid/explicitly-test-only");
    private static Path root;
    private UpdateSelfTest() {}
    @FunctionalInterface private interface Test { void run() throws Exception; }
    private static void ok(boolean value, String name) { if (!value) throw new AssertionError(name); passed++; }
    private static void rejects(Test test, String name) throws Exception {
        try { test.run(); } catch (Exception e) { passed++; return; }
        throw new AssertionError("Expected rejection: " + name);
    }
    private static String hash(byte[] value) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value)); }
    private static Map<String, Object> asset() throws Exception {
        String name = UpdateClient.assetName(Version.parse("1.2.0"), UpdateClient.Target.WINDOWS_X64);
        return new LinkedHashMap<>(Map.of("name", name, "size", (long) INSTALLER.length, "digest", "sha256:" + hash(INSTALLER), "state", "uploaded", "browser_download_url", "https://github.com/" + UpdateClient.REPOSITORY + "/releases/download/v1.2.0/" + name));
    }
    private static Map<String, Object> release() throws Exception {
        return new LinkedHashMap<>(Map.of("draft", false, "prerelease", false, "tag_name", "v1.2.0", "body", "Neue Version: Größe, Umlaute und Profile bleiben erhalten.", "assets", List.of(asset())));
    }
    private static UpdateClient.Config config(Path cache) { return new UpdateClient.Config(Version.parse("1.0.0"), UpdateClient.Target.WINDOWS_X64, cache, null, null); }
    private static UpdateClient.Response response(int status, byte[] body) { return new UpdateClient.Response(status, Map.of(), new ByteArrayInputStream(body)); }
    private static UpdateClient client(Object metadata, byte[] installer, Path cache) {
        return new UpdateClient(config(cache), (uri, a) -> response(200, a ? installer : Json.write(metadata).getBytes(StandardCharsets.UTF_8)), CLOCK);
    }
    private static Path cache() throws IOException { return Files.createTempDirectory(root, "case-"); }
    private static Map<String, Object> manifest() throws Exception {
        Map<String, Object> a = asset();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("schema", 1); m.put("product", UpdateClient.PRODUCT); m.put("repository", UpdateClient.REPOSITORY); m.put("channel", "stable"); m.put("target", "windows-x64");
        m.put("minimumUpdater", "0.1.0"); m.put("issuedAt", NOW.minusSeconds(60).toString()); m.put("expiresAt", NOW.plusSeconds(600).toString()); m.put("available", true);
        m.put("version", "1.2.0"); m.put("tag", "v1.2.0"); m.put("notes", "Signed test notes");
        m.put("artifact", Map.of("name", a.get("name"), "url", a.get("browser_download_url"), "size", a.get("size"), "sha256", hash(INSTALLER)));
        return m;
    }
    private static byte[] envelope(Map<String, Object> manifest, PrivateKey key) throws Exception {
        byte[] payload = Json.write(manifest).getBytes(StandardCharsets.UTF_8);
        Signature signer = Signature.getInstance("Ed25519"); signer.initSign(key); signer.update(payload);
        return Json.write(Map.of("payload", Base64.getEncoder().encodeToString(payload), "signature", Base64.getEncoder().encodeToString(signer.sign()))).getBytes(StandardCharsets.UTF_8);
    }
    private static UpdateClient master(byte[] envelope, PublicKey key) throws Exception {
        return new UpdateClient(new UpdateClient.Config(Version.parse("1.0.0"), UpdateClient.Target.WINDOWS_X64, cache(), MASTER, key), (uri, a) -> response(200, a ? INSTALLER : envelope), CLOCK);
    }
    public static void main(String[] args) throws Exception {
        root = Files.createTempDirectory("pz-updater-tests-");
        try {
            jsonTests(); versionTests(); platformTests(); githubTests(); signatureTests(); asyncTests();
            System.out.println("PASS: " + passed + " offline assertions. No live service queried. No installer executed.");
        } finally {
            try (var paths = Files.walk(root)) { for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(p); }
        }
    }
    private static void jsonTests() throws Exception {
        ok(Json.string(Json.object(Json.parse("{\"a\":\"Größe\\nTest\"}")), "a").equals("Größe\nTest"), "UTF-8");
        ok(Json.parse("[true,false,null,1.5]") instanceof List<?>, "JSON values");
        for (String s : List.of("{\"a\":1,\"a\":2}", "{\"a\":null,\"a\":2}", "01", "1.", "+1", "NaN", "{} x", "[1,]", "{\"a\":1,}", "\"\\uD800\"", "\"\\uDC00\"", "\"bad\ntext\"", "\"\\q\"", "-", "1e", "[").stream().toList()) rejects(() -> Json.parse(s), "bad JSON " + s);
        rejects(() -> Json.parse("[".repeat(40) + "0" + "]".repeat(40)), "deep JSON");
        rejects(() -> Json.integer(Json.object(Json.parse("{\"x\":1.2}")), "x"), "fractional integer");
        rejects(() -> Json.parse(" ".repeat(1_048_577)), "JSON limit");
        ok(Json.parse("\"\\ud83d\\ude80\"").equals("🚀"), "valid surrogate pair");
        ok(Json.parse(Json.write(Map.of("text", "a\nb\tc\\d\""))) instanceof Map<?, ?>, "writer roundtrip");
    }
    private static void versionTests() throws Exception {
        String[] ascending = {"1.0.0-alpha", "1.0.0-alpha.1", "1.0.0-alpha.beta", "1.0.0-beta", "1.0.0-beta.2", "1.0.0-beta.11", "1.0.0-rc.1", "1.0.0", "1.1.0", "1.10.0", "2.0.0"};
        for (int i = 1; i < ascending.length; i++) ok(Version.parse(ascending[i - 1]).compareTo(Version.parse(ascending[i])) < 0, "SemVer order");
        ok(Version.parse("v1.2.3+build.2").compareTo(Version.parse("1.2.3+other")) == 0, "build metadata");
        for (String s : List.of("1.2", "01.2.3", "1.02.3", "1.2.03", "1.2.3-01", "1.2.3-", " 1.2.3", "1.2.3/evil", "1.2.3\n")) rejects(() -> Version.parse(s), "bad SemVer");
    }
    private static void platformTests() throws Exception {
        ok(UpdateClient.Target.detect("Darwin", "arm64") == UpdateClient.Target.MACOS_ARM64, "Darwin is not Windows");
        ok(UpdateClient.Target.detect("Windows 11", "amd64") == UpdateClient.Target.WINDOWS_X64, "Windows");
        ok(UpdateClient.Target.detect("Linux", "aarch64") == UpdateClient.Target.LINUX_ARM64, "Linux ARM");
        rejects(() -> UpdateClient.Target.detect("FreeBSD", "amd64"), "unsupported OS");
        rejects(() -> UpdateClient.Target.detect("Linux", "x86"), "unsupported CPU");
        rejects(() -> UpdateClient.requireHttps(URI.create("http://github.com/test")), "HTTP");
        rejects(() -> UpdateClient.requireHttps(URI.create("https://user@github.com/test")), "credentials in URL");
        rejects(() -> UpdateClient.requireHttps(URI.create("https://github.com:444/test")), "unexpected port");
        rejects(() -> HttpsTransport.validateRedirect(MASTER, URI.create("https://other.invalid/test"), false), "cross-origin metadata");
        rejects(() -> HttpsTransport.validateRedirect(MASTER, URI.create("https://github.com.evil.invalid/test"), true), "host suffix attack");
        rejects(() -> HttpsTransport.validateRedirect(MASTER, URI.create("https://github.com/other/repo/releases/download/v1/a"), true), "other repo redirect");
        HttpsTransport.validateRedirect(MASTER, URI.create("https://release-assets.githubusercontent.com/example?signature=example"), true); passed++;
        rejects(() -> new UpdateClient.Config(Version.parse("1.0.0"), UpdateClient.Target.WINDOWS_X64, root, MASTER, null), "untrusted master config");
    }
    private static void githubTests() throws Exception {
        UpdateClient c = client(List.of(release()), INSTALLER, cache());
        var check = c.check(); ok(check.state() == UpdateClient.State.UPDATE_AVAILABLE, "find update");
        ok(check.candidate().notes().contains("Größe"), "notes kept");
        var downloaded = c.download(check.candidate(), n -> {});
        ok(Arrays.equals(Files.readAllBytes(downloaded.file()), INSTALLER), "verified bytes");
        ok(c.download(check.candidate(), n -> {}).file().equals(downloaded.file()), "reuse verified cache");
        Files.writeString(downloaded.file(), "corrupt");
        rejects(() -> c.verifyPackage(check.candidate(), downloaded.file()), "changed cached file");
        rejects(() -> c.download(check.candidate(), n -> {}), "do not trust corrupt cache");
        var no = client(List.of(), INSTALLER, cache()).check(); ok(no.state() == UpdateClient.State.NO_RELEASE, "empty repo");
        var draft = release(); draft.put("draft", true); ok(client(List.of(draft), INSTALLER, cache()).check().state() == UpdateClient.State.NO_RELEASE, "draft not installed");
        var pre = release(); pre.put("prerelease", true); ok(client(List.of(pre), INSTALLER, cache()).check().state() == UpdateClient.State.NO_RELEASE, "prerelease flag");
        pre.put("prerelease", false); pre.put("tag_name", "v1.3.0-beta.1"); ok(client(List.of(pre), INSTALLER, cache()).check().state() == UpdateClient.State.NO_RELEASE, "prerelease tag");
        var old = release(); old.put("tag_name", "v0.9.0"); ok(client(List.of(old), INSTALLER, cache()).check().state() == UpdateClient.State.UP_TO_DATE, "no downgrade");
        var none = release(); none.put("assets", List.of()); ok(client(List.of(none), INSTALLER, cache()).check().state() == UpdateClient.State.NO_PLATFORM_PACKAGE, "no package");
        for (var change : Map.of("digest", "", "browser_download_url", "https://github.com/attacker/repo/releases/download/v1/x.msi", "state", "starter").entrySet()) {
            var a = asset(); a.put(change.getKey(), change.getValue()); var r = release(); r.put("assets", List.of(a));
            rejects(() -> client(List.of(r), INSTALLER, cache()).check(), "bad asset " + change.getKey());
        }
        var tooLarge = asset(); tooLarge.put("size", UpdateClient.MAX_PACKAGE_BYTES + 1); var big = release(); big.put("assets", List.of(tooLarge));
        rejects(() -> client(List.of(big), INSTALLER, cache()).check(), "package size cap");
        var dup = release(); dup.put("assets", List.of(asset(), asset())); rejects(() -> client(List.of(dup), INSTALLER, cache()).check(), "duplicate package");
        for (byte[] body : List.of(new byte[INSTALLER.length], new byte[INSTALLER.length - 1], new byte[INSTALLER.length + 1])) {
            Path path = cache(); UpdateClient bad = client(List.of(release()), body, path);
            rejects(() -> bad.download(bad.check().candidate(), n -> {}), "corrupt truncated or oversized download");
            try (var files = Files.list(path)) { ok(files.noneMatch(p -> p.toString().endsWith(".part") || p.toString().endsWith(".msi")), "failed download cleanup"); }
        }
        for (int status : List.of(403, 429, 500)) {
            UpdateClient fail = new UpdateClient(config(cache()), (u, a) -> response(status, new byte[0]), CLOCK);
            rejects(fail::check, "HTTP failure is not current");
        }
        UpdateClient large = new UpdateClient(config(cache()), (u, a) -> response(200, new byte[1_048_577]), CLOCK);
        rejects(large::check, "metadata byte limit");
        UpdateClient utf = new UpdateClient(config(cache()), (u, a) -> response(200, new byte[]{(byte)0xc3, 0x28}), CLOCK);
        rejects(utf::check, "invalid UTF-8");
    }
    private static void signatureTests() throws Exception {
        KeyPair key = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        UpdateClient signed = master(envelope(manifest(), key.getPrivate()), key.getPublic());
        ok(signed.check().state() == UpdateClient.State.UPDATE_AVAILABLE, "signed manifest");
        ok(signed.download(signed.check().candidate(), n -> {}).candidate().source().equals("Masterserver"), "signed package download");
        rejects(() -> master(envelope(manifest(), key.getPrivate()), KeyPairGenerator.getInstance("Ed25519").generateKeyPair().getPublic()).check(), "wrong pinned key");
        for (var change : Map.of("product", "velishell", "repository", "attacker/repo", "target", "linux-x64", "channel", "preview", "expiresAt", NOW.minusSeconds(1).toString(), "issuedAt", NOW.plusSeconds(301).toString(), "minimumUpdater", "99.0.0", "tag", "v1.2.1", "version", "1.2.0-beta").entrySet()) {
            var m = manifest(); m.put(change.getKey(), change.getValue());
            rejects(() -> master(envelope(m, key.getPrivate()), key.getPublic()).check(), "signed but invalid " + change.getKey());
        }
        var longLife = manifest(); longLife.put("expiresAt", NOW.plus(Duration.ofDays(8)).toString());
        rejects(() -> master(envelope(longLife, key.getPrivate()), key.getPublic()).check(), "maximum manifest validity");
        var none = manifest(); none.put("available", false);
        ok(master(envelope(none, key.getPrivate()), key.getPublic()).check().state() == UpdateClient.State.NO_RELEASE, "signed withdrawal");
        Map<String, Object> e = Json.object(Json.parse(new String(envelope(manifest(), key.getPrivate()), StandardCharsets.UTF_8)));
        var changed = manifest(); changed.put("notes", "tampered"); e.put("payload", Base64.getEncoder().encodeToString(Json.write(changed).getBytes(StandardCharsets.UTF_8)));
        rejects(() -> master(Json.write(e).getBytes(StandardCharsets.UTF_8), key.getPublic()).check(), "tampered payload");
        ok(UpdateClient.readPinnedKey(Base64.getEncoder().encodeToString(key.getPublic().getEncoded())).equals(key.getPublic()), "SPKI key roundtrip");
        // Key material exists only in memory and temporary test files, never in the repository.
        Path p = cache(), source = p.resolve("manifest.json"), privateKey = p.resolve("private.pem"), out = p.resolve("signed.json");
        Files.writeString(source, Json.write(manifest()));
        Files.writeString(privateKey, "-----BEGIN PRIVATE KEY-----\n" + Base64.getEncoder().encodeToString(key.getPrivate().getEncoded()) + "\n-----END PRIVATE KEY-----\n");
        ManifestSigner.main(new String[]{source.toString(), privateKey.toString(), out.toString()});
        ok(master(Files.readAllBytes(out), key.getPublic()).check().state() == UpdateClient.State.UPDATE_AVAILABLE, "offline signer interoperability");
        rejects(() -> ManifestSigner.main(new String[]{source.toString(), privateKey.toString(), out.toString()}), "signer refuses overwrite");
    }
    private static void asyncTests() throws Exception {
        UpdateClient c = client(List.of(release()), INSTALLER, cache());
        CountDownLatch ready = new CountDownLatch(1); List<StartupUpdates.Phase> phases = new CopyOnWriteArrayList<>();
        try (StartupUpdates startup = new StartupUpdates(c, Runnable::run, event -> { phases.add(event.phase()); if (event.phase() == StartupUpdates.Phase.READY) ready.countDown(); })) {
            ok(!startup.start(StartupUpdates.Mode.MANUAL), "manual mode no check");
            ok(startup.start(StartupUpdates.Mode.AUTO_DOWNLOAD), "startup begins");
            ok(ready.await(5, TimeUnit.SECONDS), "automatic verified download finishes");
            ok(phases.containsAll(List.of(StartupUpdates.Phase.CHECKING, StartupUpdates.Phase.AVAILABLE, StartupUpdates.Phase.DOWNLOADING, StartupUpdates.Phase.READY)), "startup phases");
            rejects(() -> startup.openInstaller(c.download(c.check().candidate(), n -> {}), false), "unsaved work prevents installer");
        }
        AtomicInteger calls = new AtomicInteger(); CountDownLatch failed = new CountDownLatch(1);
        UpdateClient bad = new UpdateClient(config(cache()), (u, a) -> { calls.incrementAndGet(); throw new IOException("PRIVATE-SECRET-NOT-FOR-UI"); }, CLOCK);
        List<String> messages = new CopyOnWriteArrayList<>();
        try (StartupUpdates startup = new StartupUpdates(bad, Runnable::run, e -> { messages.add(e.message()); if (e.phase() == StartupUpdates.Phase.FAILED) failed.countDown(); })) {
            startup.start(StartupUpdates.Mode.AUTO_DOWNLOAD);
            ok(failed.await(5, TimeUnit.SECONDS), "failure notification");
            ok(messages.stream().noneMatch(s -> s.contains("PRIVATE-SECRET")), "no secret error leak");
            ok(calls.get() == 1, "no uncontrolled fallback or retry");
        }
    }
}
