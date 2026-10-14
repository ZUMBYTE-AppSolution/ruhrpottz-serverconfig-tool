package de.zumbyte.pzarchitect.update;

import java.net.URI;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.*;

/** Integration utility, not the complete PZ Server Architect desktop app. */
public final class UpdaterCli {
    private UpdaterCli() {}
    public static void main(String[] args) {
        if (args.length == 0 || Arrays.asList(args).contains("--help")) {
            System.out.println("PZ Server Architect: Updater integration utility (Java 21)");
            System.out.println("--current VERSION --cache DIRECTORY [--download] [--master-manifest HTTPS_URI --master-key BASE64_SPKI]");
            System.out.println("No argument: no network access. This is not the finished configurator.");
            return;
        }
        try {
            Map<String, String> options = new HashMap<>(); boolean download = false;
            Set<String> valid = Set.of("--current", "--cache", "--master-manifest", "--master-key");
            for (int i = 0; i < args.length; i++) {
                String key = args[i];
                if (key.equals("--download")) { download = true; continue; }
                if (!valid.contains(key) || ++i >= args.length || options.putIfAbsent(key, args[i]) != null) throw new IllegalArgumentException("Invalid arguments");
            }
            String current = Objects.requireNonNull(options.get("--current"));
            Path cache = Path.of(Objects.requireNonNull(options.get("--cache")));
            URI master = options.containsKey("--master-manifest") ? URI.create(options.get("--master-manifest")) : null;
            PublicKey key = options.containsKey("--master-key") ? UpdateClient.readPinnedKey(options.get("--master-key")) : null;
            UpdateClient.Config config = new UpdateClient.Config(Version.parse(current), UpdateClient.Target.detect(System.getProperty("os.name"), System.getProperty("os.arch")), cache, master, key);
            UpdateClient client = new UpdateClient(config);
            UpdateClient.Check result = client.check();
            System.out.println(result.state() + ": " + result.message());
            if (result.candidate() != null) {
                System.out.println("Version: " + result.candidate().version().text());
                // Print release notes as text, never as executable HTML or shell commands.
                System.out.println(result.candidate().notes().replaceAll("[\\p{Cntrl}&&[^\\n\\r\\t]]", ""));
                if (download) {
                    UpdateClient.Downloaded file = client.download(result.candidate(), bytes -> {});
                    System.out.println("Verified download: " + file.file());
                    System.out.println("No installer was started.");
                }
            }
        } catch (Exception e) {
            System.err.println("Update operation failed (" + e.getClass().getSimpleName() + "). No package was installed.");
            System.exit(1);
        }
    }
}
