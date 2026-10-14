package de.zumbyte.pzarchitect.update;

import java.math.BigDecimal;
import java.util.*;

/** Small, bounded JSON reader for metadata. Never evaluates code. */
public final class Json {
    private final String text;
    private int pos;
    private Json(String text) { this.text = Objects.requireNonNull(text); }
    public static Object parse(String text) {
        if (text.length() > 1_048_576) throw new IllegalArgumentException("JSON too large");
        Json p = new Json(text);
        Object value = p.value(0);
        p.space();
        if (p.pos != text.length()) throw p.error("Trailing input");
        return value;
    }
    @SuppressWarnings("unchecked")
    public static Map<String, Object> object(Object value) {
        if (!(value instanceof Map<?, ?>)) throw new IllegalArgumentException("Expected object");
        return (Map<String, Object>) value;
    }
    public static String string(Map<String, Object> map, String key) {
        if (!(map.get(key) instanceof String s)) throw new IllegalArgumentException("Missing string: " + key);
        return s;
    }
    public static long integer(Map<String, Object> map, String key) {
        if (!(map.get(key) instanceof BigDecimal n)) throw new IllegalArgumentException("Missing integer: " + key);
        return n.longValueExact();
    }
    public static boolean bool(Map<String, Object> map, String key) {
        if (!(map.get(key) instanceof Boolean b)) throw new IllegalArgumentException("Missing boolean: " + key);
        return b;
    }
    /** JSON writer for test fixtures and local developer tooling. */
    public static String write(Object value) {
        if (value == null) return "null";
        if (value instanceof Boolean || value instanceof BigDecimal || value instanceof Integer || value instanceof Long) return value.toString();
        if (value instanceof String s) {
            StringBuilder b = new StringBuilder("\"");
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"' -> b.append("\\\"");
                    case '\\' -> b.append("\\\\");
                    case '\n' -> b.append("\\n");
                    case '\r' -> b.append("\\r");
                    case '\t' -> b.append("\\t");
                    default -> { if (c < 32) b.append(String.format("\\u%04x", (int)c)); else b.append(c); }
                }
            }
            return b.append('"').toString();
        }
        if (value instanceof Map<?, ?> m) {
            StringJoiner out = new StringJoiner(",", "{", "}");
            for (var entry : m.entrySet()) {
                if (!(entry.getKey() instanceof String)) throw new IllegalArgumentException("JSON object key must be a string");
                out.add(write(entry.getKey()) + ":" + write(entry.getValue()));
            }
            return out.toString();
        }
        if (value instanceof List<?> values) return values.stream().map(Json::write).collect(java.util.stream.Collectors.joining(",", "[", "]"));
        throw new IllegalArgumentException("Unsupported JSON value");
    }
    private Object value(int depth) {
        if (depth > 32) throw error("Nesting too deep");
        space();
        if (pos >= text.length()) throw error("Unexpected end");
        char c = text.charAt(pos);
        if (c == '"') return string();
        if (c == '{') {
            pos++;
            Map<String, Object> values = new LinkedHashMap<>();
            space();
            if (take('}')) return values;
            do {
                space();
                if (pos == text.length() || text.charAt(pos) != '"') throw error("Expected key");
                String key = string();
                space(); require(':');
                if (values.containsKey(key)) throw error("Duplicate key");
                values.put(key, value(depth + 1));
                space();
                if (take('}')) return values;
                require(',');
            } while (true);
        }
        if (c == '[') {
            pos++;
            List<Object> values = new ArrayList<>();
            space();
            if (take(']')) return values;
            do {
                values.add(value(depth + 1));
                space();
                if (take(']')) return values;
                require(',');
            } while (true);
        }
        for (String literal : List.of("true", "false", "null")) {
            if (text.startsWith(literal, pos)) {
                pos += literal.length();
                return literal.equals("null") ? null : Boolean.valueOf(literal);
            }
        }
        int start = pos;
        take('-');
        if (!take('0')) digits();
        if (take('.')) digits();
        if (take('e') || take('E')) {
            if (!take('+')) take('-');
            digits();
        }
        if (pos == start || pos - start > 128) throw error("Invalid number");
        try { return new BigDecimal(text.substring(start, pos)); }
        catch (NumberFormatException e) { throw error("Invalid number"); }
    }
    private String string() {
        require('"');
        StringBuilder out = new StringBuilder();
        while (pos < text.length()) {
            char c = text.charAt(pos++);
            if (c == '"') {
                String result = out.toString();
                for (int i = 0; i < result.length(); i++) {
                    char v = result.charAt(i);
                    if (Character.isHighSurrogate(v)) {
                        if (++i >= result.length() || !Character.isLowSurrogate(result.charAt(i))) throw error("Invalid surrogate");
                    } else if (Character.isLowSurrogate(v)) throw error("Invalid surrogate");
                }
                return result;
            }
            if (c < 32) throw error("Control character");
            if (c == '\\') {
                if (pos >= text.length()) throw error("Unfinished escape");
                c = text.charAt(pos++);
                switch (c) {
                    case '"', '\\', '/' -> out.append(c);
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'u' -> {
                        if (pos + 4 > text.length()) throw error("Unfinished unicode escape");
                        String hex = text.substring(pos, pos + 4);
                        if (!hex.matches("[0-9a-fA-F]{4}")) throw error("Invalid unicode escape");
                        out.append((char) Integer.parseInt(hex, 16)); pos += 4;
                    }
                    default -> throw error("Invalid escape");
                }
            } else out.append(c);
        }
        throw error("Unclosed string");
    }
    private void digits() {
        int start = pos;
        while (pos < text.length() && text.charAt(pos) >= '0' && text.charAt(pos) <= '9') pos++;
        if (start == pos) throw error("Expected digits");
    }
    private void space() { while (pos < text.length() && " \r\n\t".indexOf(text.charAt(pos)) >= 0) pos++; }
    private boolean take(char c) { if (pos < text.length() && text.charAt(pos) == c) { pos++; return true; } return false; }
    private void require(char c) { if (!take(c)) throw error("Expected " + c); }
    private IllegalArgumentException error(String message) { return new IllegalArgumentException(message + " at " + pos); }
}
