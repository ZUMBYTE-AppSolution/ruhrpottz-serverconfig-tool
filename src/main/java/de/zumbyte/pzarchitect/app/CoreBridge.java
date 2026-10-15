package de.zumbyte.pzarchitect.app;

import de.zumbyte.pzarchitect.update.Json;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Local, line-delimited JSON protocol for the native Windows UI. No socket or service. */
public final class CoreBridge {
    private final Catalog catalog;
    private final ProfileStore store;
    private Workspace workspace = new Workspace();

    private CoreBridge() throws IOException {
        catalog = new Catalog();
        store = new ProfileStore(ProfileStore.defaultRoot());
    }

    public static void main(String[] args) throws Exception {
        CoreBridge bridge = new CoreBridge();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
             PrintWriter output = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true)) {
            String line;
            while ((line = input.readLine()) != null) {
                try {
                    if (line.length() > 1_048_576) throw new IllegalArgumentException("Anfrage zu groß");
                    output.println(Json.write(Map.of("ok", true, "data", bridge.handle(Json.object(Json.parse(line))))));
                } catch (Exception error) {
                    output.println(Json.write(Map.of("ok", false, "error", Objects.toString(error.getMessage(), error.getClass().getSimpleName()))));
                }
            }
        }
    }

    private Object handle(Map<String,Object> q) throws Exception {
        String command = Json.string(q, "command");
        switch (command) {
            case "state": { return state(); }
            case "set": {
                Catalog.Setting s = setting(q);
                workspace.set(s, s.parse(Json.string(q, "value")));
                return state();
            }
            case "unset": { workspace.unset(setting(q)); return state(); }
            case "restore": { workspace.restore(setting(q)); return state(); }
            case "undo": { workspace.undo(); return state(); }
            case "redo": { workspace.redo(); return state(); }
            case "new": { workspace = new Workspace(); return state(); }
            case "import": {
                workspace.importPair(Path.of(Json.string(q, "ini")), Path.of(Json.string(q, "lua")));
                return state();
            }
            case "profiles": { return profiles(); }
            case "load": {
                workspace = store.load(profilePath(q));
                return state();
            }
            case "save": {
                workspace.name = Json.string(q, "name").strip();
                workspace.baseName = Json.string(q, "baseName").strip();
                if (workspace.name.isEmpty() || workspace.name.length() > 160 || !workspace.baseName.matches("[A-Za-z0-9_-]{1,64}"))
                    throw new IllegalArgumentException("Profilname oder Server-Dateiname ist ungültig");
                store.save(workspace);
                return state();
            }
            case "saveCopy": {
                workspace.id = UUID.randomUUID().toString();
                workspace.diskHash = null;
                workspace.name = Json.string(q, "name").strip();
                workspace.baseName = Json.string(q, "baseName").strip();
                if (workspace.name.isEmpty() || workspace.name.length() > 160 || !workspace.baseName.matches("[A-Za-z0-9_-]{1,64}"))
                    throw new IllegalArgumentException("Profilname oder Server-Dateiname ist ungültig");
                store.save(workspace);
                return state();
            }
            case "deleteProfile": { store.delete(profilePath(q)); return profiles(); }
            case "validate": { return review(); }
            case "export": {
                workspace.exportZip(Path.of(Json.string(q, "path")), catalog);
                return Map.of("path", Json.string(q, "path"));
            }
            case "raw": { return Map.of("ini", workspace.ini.source, "lua", workspace.lua.source); }
            case "editRaw": {
                workspace.editRaw(Json.string(q, "scope"), Json.string(q, "source"));
                return state();
            }
            case "presets": { return Presets.ALL.stream().map(p -> Map.of("name", p.name(), "description", p.description())).toList(); }
            case "previewWizard": { return wizard(q, false); }
            case "applyWizard": { return wizard(q, true); }
            default: throw new IllegalArgumentException("Unbekannter Befehl");
        }
    }

    private Catalog.Setting setting(Map<String,Object> q) {
        String id = Json.string(q, "id");
        Catalog.Setting s = catalog.lookup(id, workspace);
        if (s == null) throw new IllegalArgumentException("Einstellung nicht gefunden: " + id);
        return s;
    }

    private Path profilePath(Map<String,Object> q) {
        UUID id = UUID.fromString(Json.string(q, "id"));
        return store.profiles.resolve(id + ".json");
    }

    private Object profiles() throws IOException {
        List<Object> entries = new ArrayList<>();
        for (ProfileStore.Entry e : store.list(store.profiles)) {
            String filename = e.file().getFileName().toString();
            if (!filename.matches("[0-9a-fA-F-]{36}\\.json")) continue;
            entries.add(Map.of("id", filename.substring(0, 36), "name", e.name(), "updated", e.updated(), "error", e.error()));
        }
        return entries;
    }

    private Object state() {
        List<Object> categories = new ArrayList<>();
        for (Catalog.Category c : catalog.categories) categories.add(Map.of("id", c.id(), "title", c.title(), "guidance", c.guidance()));
        List<Object> settings = new ArrayList<>();
        int changed = 0;
        for (Catalog.Setting s : catalog.all(workspace)) {
            ConfigDocument.Field field = workspace.document(s.scope).fields.get(s.id);
            ConfigDocument.Field original = workspace.original(s.scope).fields.get(s.id);
            boolean modified = field == null ? original != null : original == null || !Catalog.equal(field.value(), original.value());
            if (modified) changed++;
            Map<String,Object> item = new LinkedHashMap<>();
            item.put("id", s.id); item.put("key", s.key); item.put("title", s.title); item.put("scope", s.scope);
            item.put("category", s.category); item.put("type", s.type); item.put("description", s.description);
            item.put("unit", s.unit); item.put("risk", s.risk); item.put("note", s.note);
            item.put("guide", s.valueGuide()); item.put("secret", s.secret()); item.put("binary", s.binary());
            item.put("present", field != null); item.put("value", field == null ? "" : Catalog.display(field.value()));
            item.put("original", original == null ? "" : Catalog.display(original.value())); item.put("modified", modified);
            List<Object> choices = new ArrayList<>();
            for (Catalog.Choice c : s.choices) choices.add(Map.of("value", Catalog.display(c.value()), "title", c.title(), "effect", c.effect()));
            item.put("choices", choices);
            settings.add(item);
        }
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("id", workspace.id); result.put("name", workspace.name); result.put("baseName", workspace.baseName);
        result.put("dirty", workspace.dirty); result.put("changed", changed);
        result.put("imported", !workspace.originalIni.source.startsWith("# PZ Server Architect:"));
        result.put("categories", categories); result.put("settings", settings);
        return result;
    }

    private Object review() {
        List<Object> issues = new ArrayList<>();
        for (Workspace.Issue issue : workspace.validate(catalog)) issues.add(Map.of("error", issue.error(), "text", issue.text()));
        return Map.of("issues", issues, "diff", workspace.diff(catalog));
    }

    private Object wizard(Map<String,Object> q, boolean apply) {
        List<?> selections = (List<?>)q.get("choices");
        if (selections == null || selections.size() != 9) throw new IllegalArgumentException("Neun Assistenten-Schritte erforderlich");
        int[] c = new int[9];
        int[] lengths = {8,5,5,4,3,3,4,2,4};
        for (int i=0; i<9; i++) {
            if (!(selections.get(i) instanceof BigDecimal n)) throw new IllegalArgumentException("Ungültige Auswahl");
            c[i]=n.intValueExact();
            if (c[i]<0 || c[i]>=lengths[i]) throw new IllegalArgumentException("Ungültige Auswahl");
        }
        Presets.Preset preset=Presets.ALL.get(c[0]);
        Map<String,Object> m=new LinkedHashMap<>(preset.values());
        if(c[1]>0){double n=new double[]{0,0,.35,1,2.5}[c[1]];Presets.population(m,n);}
        if(c[2]>0)Presets.put(m,"lua","DayLength",new int[]{0,3,5,7,9}[c[2]]);
        if(c[3]>0)for(String key:new ArrayList<>(m.keySet()))if(key.contains("LootNew"))m.put(key,new BigDecimal(new String[]{"0","0.4","1","2"}[c[3]]));
        boolean zero=Catalog.equal(m.get(ConfigDocument.id("lua","ZombieConfig.PopulationMultiplier")),BigDecimal.ZERO);
        if(c[4]>0||zero){Presets.put(m,"lua","ZombieConfig.RespawnHours",c[4]==2&&!zero?72:0);Presets.put(m,"lua","ZombieConfig.RespawnMultiplier",c[4]==2&&!zero?.1:0);}
        if(c[5]>0)Presets.put(m,"ini","PVP",c[5]==2);
        if(c[6]>0)Presets.put(m,"lua","MultiplierConfig.Global",new int[]{0,1,2,4}[c[6]]);
        if(c[7]==1){for(String k:List.of("WaterShut","ElecShut"))Presets.put(m,"lua",k,9);for(String k:List.of("WaterShutModifier","ElecShutModifier"))Presets.put(m,"lua",k,-1);}
        if(c[8]==1){for(String k:List.of("DayNightCycle","ClimateCycle","FogCycle"))Presets.put(m,"lua",k,1);}
        if(c[8]==2)Presets.put(m,"lua","FogCycle",2);
        if(c[8]==3){for(String k:List.of("DayNightCycle","ClimateCycle","FogCycle"))Presets.put(m,"lua",k,2);}
        boolean existingOnly = Boolean.TRUE.equals(q.get("existingOnly"));
        if (apply) { workspace.apply(m,catalog,existingOnly); return state(); }
        List<Object> preview = new ArrayList<>();
        for (var e:m.entrySet()) {
            Catalog.Setting s=catalog.settings.get(e.getKey());
            if(s==null)continue;
            ConfigDocument.Field old=workspace.document(s.scope).fields.get(s.id);
            if(existingOnly&&old==null)continue;
            preview.add(Map.of("title",s.title,"key",s.key,"before",old==null?"Nicht gesetzt":Catalog.display(old.value()),"after",Catalog.display(e.getValue())));
        }
        return Map.of("preset",preset.name(),"changes",preview);
    }
}
