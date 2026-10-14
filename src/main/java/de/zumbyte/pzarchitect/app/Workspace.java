package de.zumbyte.pzarchitect.app;

import de.zumbyte.pzarchitect.update.Json;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.time.Instant;
import java.util.*;
import java.util.zip.*;

public final class Workspace {
    public ConfigDocument ini,lua,originalIni,originalLua;
    public String name="Neues Profil",baseName="servertest",notes="",id=UUID.randomUUID().toString();
    public String diskHash=null;
    public boolean dirty;
    public final Set<String> requiredSecrets=new LinkedHashSet<>();
    private final Deque<Snapshot> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    private record Snapshot(String ini,String lua,Set<String> required) {}
    public Workspace(){
        ini=new ConfigDocument("ini","# PZ Server Architect: eigene Ausgangsbasis, kein offizieller Preset\nPublicName=Mein Zomboid Server\nPublic=false\nOpen=true\nPassword=\nMaxPlayers=16\nPauseEmpty=true\nMods=\nWorkshopItems=\nMap=Muldraugh, KY\n");
        lua=new ConfigDocument("lua","-- Build-42-Dateischema; vor Einsatz mit dem Server abgleichen\nSandboxVars = {\n    VERSION = 6,\n}\n");originalIni=ini;originalLua=lua;
    }
    public ConfigDocument document(String scope){return scope.equals("ini")?ini:lua;}
    public ConfigDocument original(String scope){return scope.equals("ini")?originalIni:originalLua;}
    public void set(Catalog.Setting s,Object value){checkpoint();try{setDirect(s.scope,s.path,value);requiredSecrets.remove(s.id);}catch(RuntimeException ex){undo.pop();throw ex;}dirty=true;}
    public void unset(Catalog.Setting s){checkpoint();if(s.scope.equals("ini"))ini=ini.remove(s.id);else lua=lua.remove(s.id);dirty=true;}
    private void setDirect(String scope,List<?> path,Object value){if(scope.equals("ini"))ini=ini.set(path,value);else lua=lua.set(path,value);}
    public void restore(Catalog.Setting s){checkpoint();ConfigDocument.Field old=original(s.scope).fields.get(s.id);if(old==null){if(s.scope.equals("ini"))ini=ini.remove(s.id);else lua=lua.remove(s.id);}else setDirect(s.scope,s.path,old.value());dirty=true;}
    public void apply(Map<String,Object> changes,Catalog catalog,boolean existingOnly){
        ConfigDocument a=ini,b=lua;for(var e:changes.entrySet()){
            Catalog.Setting s=catalog.settings.get(e.getKey());if(s==null)throw new IllegalArgumentException("Unbekannter Vorlagenschlüssel: "+e.getKey());
            if(existingOnly&&!document(s.scope).fields.containsKey(s.id))continue;
            Object validated=s.parse(Catalog.display(e.getValue()));if(s.scope.equals("ini"))a=a.set(s.path,validated);else b=b.set(s.path,validated);
        }
        checkpoint();ini=a;lua=b;dirty=true;
    }
    public void editRaw(String scope,String text){ConfigDocument parsed=new ConfigDocument(scope,text);checkpoint();if(scope.equals("ini"))ini=parsed;else lua=parsed;dirty=true;}
    private void checkpoint(){undo.push(new Snapshot(ini.source,lua.source,Set.copyOf(requiredSecrets)));while(undo.size()>40)undo.removeLast();redo.clear();}
    public boolean undo(){return history(undo,redo);}public boolean redo(){return history(redo,undo);}
    private boolean history(Deque<Snapshot> from,Deque<Snapshot> to){if(from.isEmpty())return false;to.push(new Snapshot(ini.source,lua.source,Set.copyOf(requiredSecrets)));Snapshot s=from.pop();ini=new ConfigDocument("ini",s.ini);lua=new ConfigDocument("lua",s.lua);requiredSecrets.clear();requiredSecrets.addAll(s.required);dirty=true;return true;}
    public void importPair(Path a,Path b) throws IOException {
        String an=a.getFileName().toString(),bn=b.getFileName().toString();
        if(!an.endsWith(".ini")||!bn.equals(an.substring(0,an.length()-4)+"_SandboxVars.lua"))throw new IOException("Dateinamen passen nicht zusammen. Erwartet: name.ini und name_SandboxVars.lua");
        ConfigDocument x=new ConfigDocument("ini",read(a)),y=new ConfigDocument("lua",read(b));
        ini=x;lua=y;originalIni=x;originalLua=y;baseName=an.substring(0,an.length()-4);name=baseName;id=UUID.randomUUID().toString();diskHash=null;requiredSecrets.clear();undo.clear();redo.clear();dirty=true;
    }
    public record Issue(boolean error,String text) {}
    public List<Issue> validate(Catalog c){
        List<Issue> issues=new ArrayList<>();
        for(String secret:requiredSecrets)issues.add(new Issue(true,"Gespeichertes Geheimnis wurde entfernt und muss bewusst ergänzt oder geleert werden: "+secret));
        if(!baseName.matches("[A-Za-z0-9_-]{1,64}"))issues.add(new Issue(true,"Server-Dateiname: 1 bis 64 Buchstaben, Zahlen, Unterstrich oder Bindestrich."));
        for(Catalog.Setting s:c.settings.values()){
            ConfigDocument.Field f=document(s.scope).fields.get(s.id);if(f==null)continue;
            try{s.parse(Catalog.display(f.value()));}catch(IllegalArgumentException ex){issues.add(new Issue(false,s.key+": "+ex.getMessage()+". Importierter Wert bleibt erhalten; Build prüfen."));}
        }
        if(is("ini","Public",true)&&is("ini","Open",true)&&text("ini","Password").isBlank())issues.add(new Issue(false,"Öffentlicher Server mit offenem Beitritt und ohne Join-Passwort."));
        if(is("ini","SleepNeeded",true)&&is("ini","SleepAllowed",false))issues.add(new Issue(false,"Schlaf wird benötigt, ist aber nicht erlaubt."));
        if(num("ini","VoiceMinDistance",0)>num("ini","VoiceMaxDistance",Double.MAX_VALUE))issues.add(new Issue(true,"Voice-Minimaldistanz ist größer als die Maximaldistanz."));
        if(num("lua","ZombieConfig.PopulationMultiplier",1)==0 && num("lua","ZombieConfig.RespawnHours",0)>0)issues.add(new Issue(false,"Population ist 0, ein Respawn-Intervall bleibt gesetzt. Eigene Spawn-Mods separat prüfen."));
        if(is("lua","ZombieRespawn",new BigDecimal("4"))&&num("lua","ZombieConfig.RespawnHours",0)>0)issues.add(new Issue(false,"Ein importierter grober ZombieRespawn-Wert steht auf 4, während Wiederkehr-Stunden aktiv sind. Priorität und Auswahlwerte im Zielbuild prüfen."));
        if(num("ini","MaxPlayers",16)>64)issues.add(new Issue(false,"Mehr als 64 Slots: Hardware, Spielversion und Mods unter Last testen."));
        if(!lua.fields.containsKey(ConfigDocument.id("lua","VERSION")))issues.add(new Issue(false,"VERSION des Sandbox-Dateischemas fehlt; bitte Originaldatei des Builds verwenden."));
        return issues;
    }
    public boolean is(String scope,String key,Object value){ConfigDocument.Field f=document(scope).fields.get(ConfigDocument.id(scope,key));return f!=null&&Catalog.equal(f.value(),value);}
    public String text(String scope,String key){ConfigDocument.Field f=document(scope).fields.get(ConfigDocument.id(scope,key));return f==null||f.value()==null?"":Catalog.display(f.value());}
    public double num(String scope,String key,double fallback){try{return Double.parseDouble(text(scope,key));}catch(NumberFormatException e){return fallback;}}
    public String diff(Catalog c){StringBuilder out=new StringBuilder();for(String scope:List.of("ini","lua")){
        ConfigDocument old=original(scope),now=document(scope);Set<String> keys=new LinkedHashSet<>(old.fields.keySet());keys.addAll(now.fields.keySet());
        for(String key:keys){ConfigDocument.Field a=old.fields.get(key),b=now.fields.get(key);if(a!=null&&b!=null&&Catalog.equal(a.value(),b.value()))continue;
            Catalog.Setting s=c.lookup(key,this);boolean secret=s!=null?s.secret():secretKey(key);
            out.append(key).append("\n  Original: ").append(a==null?"Nicht gesetzt":secret?"[geschützt]":Catalog.display(a.value())).append("\n  Aktuell:  ").append(b==null?"Nicht gesetzt":secret?"[geschützt]":Catalog.display(b.value())).append("\n\n");
        }
    }return out.isEmpty()?"Keine Wertänderungen gegenüber dem Original.":out.toString();}
    public void exportZip(Path file,Catalog c)throws IOException{
        if(validate(c).stream().anyMatch(Issue::error))throw new IOException("Export wegen Fehlern gesperrt. Prüfliste beachten.");
        Path absolute=file.toAbsolutePath();if(Files.exists(absolute))throw new IOException("Zieldatei existiert bereits. Einen neuen Dateinamen verwenden.");
        Path temp=Files.createTempFile(absolute.getParent(),".pz-export-",".tmp");
        try{
            try(ZipOutputStream zip=new ZipOutputStream(Files.newOutputStream(temp),StandardCharsets.UTF_8)){
                put(zip,baseName+".ini",ini.source);put(zip,baseName+"_SandboxVars.lua",lua.source);
                put(zip,"LIES-MICH.txt","PZ Server Architect\nDieses Archiv enthält echte Serverwerte und gegebenenfalls Kennwörter. Nicht öffentlich teilen.\nServer vor Übernahme stoppen und sichern. Spielbuild/Mods prüfen. AMP kann Einstellungen überschreiben.\nEs wurde kein Savegame verändert und kein Server neu gestartet.\n");
            }Files.move(temp,absolute);
        }finally{Files.deleteIfExists(temp);}
    }
    private static void put(ZipOutputStream zip,String name,String text)throws IOException{zip.putNextEntry(new ZipEntry(name));zip.write(text.getBytes(StandardCharsets.UTF_8));zip.closeEntry();}
    public Map<String,Object> snapshot(boolean scrub){
        Set<String> required=new LinkedHashSet<>(requiredSecrets);ConfigDocument a=ini,b=lua,oa=originalIni,ob=originalLua;
        if(scrub){for(ConfigDocument d:List.of(ini,lua))for(ConfigDocument.Field f:d.fields.values())if(secretKey(f.id())&&f.value()!=null&&!Catalog.display(f.value()).isEmpty()){
            required.add(f.id());if(d.scope.equals("ini"))a=a.set(f.path(),"");else b=b.set(f.path(),"");}
            for(ConfigDocument d:List.of(originalIni,originalLua))for(ConfigDocument.Field f:d.fields.values())if(secretKey(f.id())){if(d.scope.equals("ini"))oa=oa.set(f.path(),"");else ob=ob.set(f.path(),"");}
        }
        Map<String,Object> m=new LinkedHashMap<>();m.put("format","pz-architect-profile");m.put("schema",1);m.put("id",id);m.put("name",name);m.put("baseName",baseName);m.put("notes",notes);m.put("updatedAt",Instant.now().toString());m.put("ini",a.source);m.put("lua",b.source);m.put("originalIni",oa.source);m.put("originalLua",ob.source);m.put("requiredSecrets",new ArrayList<>(required));return m;
    }
    public static Workspace from(String json){Map<String,Object> m=Json.object(Json.parse(json));if(!"pz-architect-profile".equals(m.get("format"))||Json.integer(m,"schema")!=1)throw new IllegalArgumentException("Nicht unterstütztes Profilformat. HTML-Profile werden nicht automatisch migriert.");
        Workspace w=new Workspace();w.id=UUID.fromString(Json.string(m,"id")).toString();w.name=limited(Json.string(m,"name"),160);w.baseName=limited(Json.string(m,"baseName"),64);w.notes=limited(Json.string(m,"notes"),10000);
        w.ini=new ConfigDocument("ini",Json.string(m,"ini"));w.lua=new ConfigDocument("lua",Json.string(m,"lua"));w.originalIni=new ConfigDocument("ini",Json.string(m,"originalIni"));w.originalLua=new ConfigDocument("lua",Json.string(m,"originalLua"));
        if(m.get("requiredSecrets") instanceof List<?> list)for(Object id:list){if(!(id instanceof String s)||s.length()>1000)throw new IllegalArgumentException("Ungültige Geheimnismarkierung");w.requiredSecrets.add(s);}return w;
    }
    private static String limited(String s,int max){if(s.length()>max)throw new IllegalArgumentException("Profilmetadaten zu lang");return s;}
    public static boolean secretKey(String key){return key.toLowerCase(Locale.ROOT).matches(".*(password|passwd|token|secret|apikey|api_key|privatekey).* ".strip());}
    public static String read(Path file)throws IOException{if(Files.size(file)>1_000_000)throw new IOException("Datei überschreitet 1 MB");return Files.readString(file,StandardCharsets.UTF_8);}
    public static String digest(String text){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}}
    public static void atomic(Path file,String text)throws IOException{
        Path absolute=file.toAbsolutePath();Files.createDirectories(absolute.getParent());if(Files.isSymbolicLink(absolute))throw new IOException("Symbolische Links als Schreibziel werden nicht unterstützt");
        Path temp=Files.createTempFile(absolute.getParent(),".pz-save-",".tmp");try{
            try{Files.setPosixFilePermissions(temp,PosixFilePermissions.fromString("rw-------"));}catch(UnsupportedOperationException ignored){}
            Files.writeString(temp,text,StandardCharsets.UTF_8);try{Files.move(temp,absolute,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException ex){Files.move(temp,absolute,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(temp);}
    }
}
