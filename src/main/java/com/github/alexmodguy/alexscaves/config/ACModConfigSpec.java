package com.github.alexmodguy.alexscaves.config;

import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A small file-backed config.
 *
 * <p>This used to be a pure stub: {@code define(name, default)} threw the key away and handed back the
 * compiled-in default, and {@code configure()} never registered anything with a loader. The result was that no
 * config file was ever written and every option was permanently pinned to its default - which is why players
 * could not, for example, set {@code biome_sky_fog_overrides = false} to stop the fog flickering that happens
 * when another mod also drives fog. Upstream registers real NeoForge specs; this restores equivalent behaviour
 * for both loaders.
 *
 * <p>The format is a small TOML subset - {@code [section]} headers, {@code # comment} lines and
 * {@code key = value} - so the file matches upstream's {@code alexscaves-client.toml} layout closely enough
 * that existing advice applies, without pulling in a TOML parser (Fabric has no NightConfig by default).
 * Reading also accepts fully dotted keys, so either style works.
 *
 * <p>Two deliberate safety properties: loading is <b>lazy</b> (first {@code get()}, not class-init, because the
 * config objects are built in a static initialiser), and every file operation is wrapped so that any IO problem
 * falls back to the compiled defaults instead of breaking startup.
 */
public class ACModConfigSpec {

    public static class Builder {
        private final Store store;
        private final Deque<String> path = new ArrayDeque<>();
        private String[] pendingComment;

        public Builder(String fileName) {
            this.store = new Store(fileName);
        }

        public Builder push(String section) {
            path.addLast(section);
            return this;
        }

        public Builder pop() {
            if (!path.isEmpty()) {
                path.removeLast();
            }
            return this;
        }

        public Builder comment(String... comment) {
            pendingComment = comment;
            return this;
        }

        public Builder translation(String translationKey) {
            return this;
        }

        private String declare(String name, Object defaultValue) {
            String key = path.isEmpty() ? name : String.join(".", path) + "." + name;
            store.declare(key, defaultValue, pendingComment);
            pendingComment = null;
            return key;
        }

        public BooleanValue define(String name, boolean defaultValue) {
            return new BooleanValue(store, declare(name, defaultValue), defaultValue);
        }

        public IntValue defineInRange(String name, int defaultValue, int min, int max) {
            return new IntValue(store, declare(name, defaultValue), defaultValue, min, max);
        }

        public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
            return new DoubleValue(store, declare(name, defaultValue), defaultValue, min, max);
        }

        public <T> ConfigValue<T> define(String name, T defaultValue) {
            return new ConfigValue<>(store, declare(name, defaultValue), defaultValue);
        }

        public <T> ConfigValue<List<? extends T>> defineList(String name, List<? extends T> defaultValue, Predicate<Object> validator) {
            // No AC option needs a runtime-editable list; keep the default rather than round-tripping one.
            return new ConfigValue<>(null, null, defaultValue);
        }

        public <C> Pair<C, ACModConfigSpec> configure(Function<Builder, C> factory) {
            return Pair.of(factory.apply(this), new ACModConfigSpec());
        }
    }

    /** Holds the declared options for one file and lazily loads/creates that file. */
    private static final class Store {
        private final String fileName;
        private final Map<String, Object> defaults = new LinkedHashMap<>();
        private final Map<String, String[]> comments = new HashMap<>();
        private final List<String> order = new ArrayList<>();
        private Map<String, String> values;

        private Store(String fileName) {
            this.fileName = fileName;
        }

        void declare(String key, Object defaultValue, String[] comment) {
            if (!defaults.containsKey(key)) {
                order.add(key);
            }
            defaults.put(key, defaultValue);
            if (comment != null && comment.length > 0) {
                comments.put(key, comment);
            }
        }

        synchronized String raw(String key) {
            if (values == null) {
                values = new LinkedHashMap<>();
                try {
                    Path file = FMLPaths.CONFIGDIR.get().resolve(fileName);
                    if (Files.exists(file)) {
                        read(Files.readAllLines(file, StandardCharsets.UTF_8));
                    }
                    // Rewrite so a fresh install gets a documented file and an existing one picks up options
                    // added by a later version, without discarding anything the player already set.
                    write(file);
                } catch (Throwable ignored) {
                    // Config is never worth crashing over - fall through to the compiled defaults.
                }
            }
            return values.get(key);
        }

        private void read(List<String> lines) {
            String section = "";
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    section = trimmed.substring(1, trimmed.length() - 1).trim();
                    continue;
                }
                int equals = trimmed.indexOf('=');
                if (equals <= 0) {
                    continue;
                }
                String name = trimmed.substring(0, equals).trim();
                String value = trimmed.substring(equals + 1).trim();
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(section.isEmpty() || name.indexOf('.') >= 0 ? name : section + "." + name, value);
            }
        }

        private void write(Path file) throws Exception {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            StringBuilder out = new StringBuilder();
            out.append("# Alex's Caves configuration.\n");
            out.append("# Remove a line to restore its default value.\n");
            String currentSection = null;
            for (String key : order) {
                int dot = key.lastIndexOf('.');
                String section = dot < 0 ? "" : key.substring(0, dot);
                String name = dot < 0 ? key : key.substring(dot + 1);
                if (!section.equals(currentSection)) {
                    currentSection = section;
                    out.append('\n');
                    if (!section.isEmpty()) {
                        out.append('[').append(section).append("]\n");
                    }
                }
                String[] comment = comments.get(key);
                if (comment != null) {
                    for (String line : comment) {
                        out.append("\t# ").append(line).append('\n');
                    }
                }
                Object defaultValue = defaults.get(key);
                String value = values.containsKey(key) ? values.get(key) : String.valueOf(defaultValue);
                out.append('\t').append(name).append(" = ")
                        .append(defaultValue instanceof String ? "\"" + value.replace("\"", "") + "\"" : value)
                        .append('\n');
                values.putIfAbsent(key, String.valueOf(defaultValue));
            }
            Files.writeString(file, out.toString(), StandardCharsets.UTF_8);
        }
    }

    public static class ConfigValue<T> {
        private final Store store;
        private final String key;
        private final T defaultValue;

        ConfigValue(Store store, String key, T defaultValue) {
            this.store = store;
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public T get() {
            if (store == null || key == null) {
                return defaultValue;
            }
            String raw = store.raw(key);
            if (raw == null || raw.isEmpty()) {
                return defaultValue;
            }
            try {
                return parse(raw);
            } catch (Throwable ignored) {
                return defaultValue;
            }
        }

        protected T defaultValue() {
            return defaultValue;
        }

        @SuppressWarnings("unchecked")
        protected T parse(String raw) {
            return defaultValue instanceof String ? (T) raw : defaultValue;
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> {
        BooleanValue(Store store, String key, Boolean defaultValue) {
            super(store, key, defaultValue);
        }

        @Override
        protected Boolean parse(String raw) {
            String value = raw.trim();
            if (value.equalsIgnoreCase("true")) {
                return Boolean.TRUE;
            }
            if (value.equalsIgnoreCase("false")) {
                return Boolean.FALSE;
            }
            return defaultValue();
        }
    }

    public static class IntValue extends ConfigValue<Integer> {
        private final int min;
        private final int max;

        IntValue(Store store, String key, Integer defaultValue, int min, int max) {
            super(store, key, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        protected Integer parse(String raw) {
            return Math.max(min, Math.min(max, Integer.parseInt(raw.trim())));
        }
    }

    public static class DoubleValue extends ConfigValue<Double> {
        private final double min;
        private final double max;

        DoubleValue(Store store, String key, Double defaultValue, double min, double max) {
            super(store, key, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        protected Double parse(String raw) {
            return Math.max(min, Math.min(max, Double.parseDouble(raw.trim())));
        }
    }
}
