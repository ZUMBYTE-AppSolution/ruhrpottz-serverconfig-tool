package de.zumbyte.pzarchitect.update;

import java.nio.file.Path;
import java.util.*;

/** Integration utility, not the complete PZ Server Architect desktop app. */
public final class UpdaterCli {
    private UpdaterCli() {}
    public static void main(String[] args) {
        if (args.length == 0 || Arrays.asList(args).contains("--help")) {
            System.out.println("PZ Server Architect: Updater integration utility (Java 21)");
            System.out.println("--current VERSION --cache DIRECTORY [--download]");
            System.out.println("No argument: no network access. This is not the finished configurator.");
            return;
        }
        try {
            Map<String, String> options = new HashMap<>(); boolean download = false;
            Set<String> valid = Set.of("--current", "--cache");
            for (int i = 0; i < args.length; i++) {
                String key = args[i];
                if (key.equals("--download")) { download = true; continue; }
                if (!valid.contains(key) || ++i >= args.length || options.putIfAbsent(key, args[i]) != null) throw new IllegalArgumentException("Invalid arguments");
            }
            String current = Objects.requireNonNull(options.get("--current"));
            Path cache = Path.of(Objects.requireNonNull(options.get("--cache")));
            UpdateClient.Config config = UpdateClient.Config.github(current, cache);
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
        } catch (UpdateClient.RateLimitException e) {
            System.err.println("GitHub request limit reached. Retry after " + e.retryAt() + ". Version status is unknown.");
            System.exit(2);
        } catch (Exception e) {
            System.err.println("Update operation failed (" + e.getClass().getSimpleName() + "). No package was installed.");
            System.exit(1);
        }
    }
}
