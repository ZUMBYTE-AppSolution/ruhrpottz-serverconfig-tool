package de.zumbyte.pzarchitect.update;

import java.math.BigInteger;
import java.util.regex.*;

/** SemVer ordering; build metadata never makes a version newer. */
public record Version(String text, BigInteger major, BigInteger minor, BigInteger patch, String pre) implements Comparable<Version> {
    private static final Pattern FORMAT = Pattern.compile("v?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?");
    public static Version parse(String text) {
        if (text == null || text.length() > 128) throw new IllegalArgumentException("Invalid version");
        Matcher m = FORMAT.matcher(text);
        if (!m.matches()) throw new IllegalArgumentException("Invalid SemVer: " + text);
        String pre = m.group(4);
        if (pre != null) for (String part : pre.split("\\."))
            if (part.matches("[0-9]+") && part.length() > 1 && part.startsWith("0")) throw new IllegalArgumentException("Leading zero");
        return new Version(text.startsWith("v") ? text.substring(1) : text,
            new BigInteger(m.group(1)), new BigInteger(m.group(2)), new BigInteger(m.group(3)), pre);
    }
    public int compareTo(Version other) {
        int n = major.compareTo(other.major); if (n != 0) return n;
        n = minor.compareTo(other.minor); if (n != 0) return n;
        n = patch.compareTo(other.patch); if (n != 0) return n;
        if (pre == null || other.pre == null) return pre == other.pre ? 0 : pre == null ? 1 : -1;
        String[] a = pre.split("\\."), b = other.pre.split("\\.");
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            boolean an = a[i].matches("[0-9]+"), bn = b[i].matches("[0-9]+");
            n = an && bn ? new BigInteger(a[i]).compareTo(new BigInteger(b[i])) : an != bn ? (an ? -1 : 1) : a[i].compareTo(b[i]);
            if (n != 0) return n;
        }
        return Integer.compare(a.length, b.length);
    }
}
