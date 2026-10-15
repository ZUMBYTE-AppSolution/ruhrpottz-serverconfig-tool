package de.zumbyte.pzarchitect.app;

import de.zumbyte.pzarchitect.update.Json;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/** Metadata from the prior editor. Original file comments take precedence over unverified catalog ranges. */
public final class Catalog {
    public record Category(String id,String title,String guidance) {}
    public record Choice(Object value,String title,String effect){@Override public String toString(){return value+" = "+title;}}
    public static final class Setting {
        public String id,scope,key,title,type,description,unit,category,note,risk;
        public List<Object> path;
        public BigDecimal min,max;
        public boolean known;
        public final List<Choice> choices=new ArrayList<>();
        public boolean binary(){return type.equals("boolean")||(type.equals("enum")&&choices.size()==2&&choices.stream().anyMatch(c->equal(c.value,BigDecimal.ZERO)&&c.title.toLowerCase(Locale.ROOT).matches("aus|false|off|deaktiviert"))&&choices.stream().anyMatch(c->equal(c.value,BigDecimal.ONE)&&c.title.toLowerCase(Locale.ROOT).matches("ein|an|true|on|aktiviert")))||(type.equals("integer")&&min!=null&&max!=null&&min.compareTo(BigDecimal.ZERO)==0&&max.compareTo(BigDecimal.ONE)==0);}
        public boolean secret(){return type.equals("password")||key.toLowerCase(Locale.ROOT).matches(".*(password|passwd|secret|token|apikey|api_key|privatekey).* ".strip());}
        public Object parse(String text){
            if(type.equals("boolean")){if(text.equals("true")||text.equals("false"))return Boolean.valueOf(text);if(text.equals("0")||text.equals("1"))return new BigDecimal(text);throw new IllegalArgumentException("Erlaubt: true/false, bei numerischer Originalkodierung 0/1");}
            if(type.equals("integer")||type.equals("number")||type.equals("enum")){
                if(text.length()>80)throw new IllegalArgumentException("Zahl zu lang");
                BigDecimal n;try{n=new BigDecimal(text.strip());}catch(NumberFormatException ex){throw new IllegalArgumentException("Bitte eine Zahl eingeben, Dezimaltrennzeichen ist ein Punkt.");}
                if(Math.abs(n.scale())>100)throw new IllegalArgumentException("Zahlenexponent zu groß");
                if(!type.equals("number")&&n.stripTrailingZeros().scale()>0)throw new IllegalArgumentException("Nur ganze Zahlen erlaubt");
                if(min!=null&&n.compareTo(min)<0)throw new IllegalArgumentException("Minimum: "+min);
                if(max!=null&&n.compareTo(max)>0)throw new IllegalArgumentException("Maximum: "+max);
                if(!choices.isEmpty()&&choices.stream().noneMatch(c->equal(c.value,n)))throw new IllegalArgumentException("Bitte einen dokumentierten Auswahlwert verwenden");
                return n;
            }
            if(scope.equals("ini")&&(text.contains("\n")||text.contains("\r")||text.indexOf('\0')>=0))throw new IllegalArgumentException("Kein Zeilenumbruch in INI-Werten erlaubt");
            return text;
        }
        public String valueGuide(){
            if(binary()){
                String off=type.equals("boolean")?"false":"0",on=type.equals("boolean")?"true":"1";
                String[] effects=BOOL_HELP.getOrDefault(key,new String[]{"Die beschriebene Funktion ist ausgeschaltet. Andere Einstellungen und Mods bleiben unabhängig davon wirksam.","Die beschriebene Funktion ist eingeschaltet. "+description});
                return off+" = Aus\n"+effects[0]+"\n\n"+on+" = Ein\n"+effects[1]+"\n\nEin fehlender Schlüssel ist weder Aus noch Ein. Bei importierten 0/1-Schaltern wird die numerische Schreibweise erhalten.";
            }
            if(!choices.isEmpty()){StringBuilder b=new StringBuilder();for(Choice c:choices)b.append(c).append('\n').append(c.effect).append("\n\n");return b.toString().strip();}
            if(type.equals("integer")||type.equals("number")||type.equals("enum")){
                String t=type.equals("number")?"Dezimalzahl (z. B. 0.5, 1 oder 2).":"Ganze Zahl bzw. Auswahlindex, nicht automatisch Ein/Aus.";
                t+="\nEinheit: "+(unit.isBlank()?"Im Originalkommentar prüfen":unit)+"\n";
                t+="Grenzen: "+(min==null?"Minimum nicht dokumentiert":min)+" / "+(max==null?"Maximum nicht dokumentiert":max)+"\n\n"+description;
                if(key.contains("Multiplier")||unit.equals("Faktor"))t+="\n\nBei einem echten Multiplikator: 0.5 = halber Faktor, 1 = unverändert, 2 = doppelter Faktor. Ob 0 eine Funktion abschaltet, ist optionabhängig.";
                return t+"\n\nNicht belegte Auswahlstufen werden nicht erfunden. Importiere die vom Server erzeugte Datei für die Kommentare deines Builds.";
            }
            if(type.equals("list"))return "Einträge in ihrer vorhandenen Reihenfolge, in der Regel durch Semikolon getrennt. Kein automatisches Sortieren. Workshop-IDs sind nicht mit Mod-IDs identisch.\n\n"+description;
            return "Textwert. "+description+(type.equals("password")?"\nDieser Wert wird standardmäßig nicht in Profilen gespeichert.":"");
        }
    }
    public final List<Category> categories=new ArrayList<>();
    public final LinkedHashMap<String,Setting> settings=new LinkedHashMap<>();
    public Catalog() throws IOException {
        try(InputStream in=Catalog.class.getResourceAsStream("/catalog.json")){
            if(in==null)throw new IOException("Einstellungskatalog fehlt im Programm");
            Map<String,Object> root=Json.object(Json.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)));
            for(Object item:(List<?>)root.get("categories")){Map<String,Object> c=Json.object(item);categories.add(new Category(str(c,"id"),str(c,"title"),str(c,"guidance")));}
            for(Object item:(List<?>)root.get("settings")){
                Map<String,Object> c=Json.object(item);Setting s=new Setting();s.scope=str(c,"scope");s.key=str(c,"key");s.path=new ArrayList<>((List<?>)c.get("path"));s.id=ConfigDocument.id(s.scope,s.path);
                s.title=str(c,"title");s.type=str(c,"type");s.category=str(c,"category");s.description=str(c,"description");s.unit=str(c,"unit");s.note=str(c,"note");s.risk=str(c,"risk");s.known=true;
                s.min=c.get("min") instanceof BigDecimal n?n:null;s.max=c.get("max") instanceof BigDecimal n?n:null;
                if(c.get("options") instanceof List<?> options)for(Object o:options){List<?> v=(List<?>)o;String label=v.get(1).toString();s.choices.add(new Choice(v.getFirst(),label,effect(s.key,label)));}
                settings.put(s.id,s);
            }
        }
    }
    public List<Setting> all(Workspace w){LinkedHashMap<String,Setting> out=new LinkedHashMap<>(settings);for(ConfigDocument d:List.of(w.ini,w.lua))for(ConfigDocument.Field f:d.fields.values())if(!out.containsKey(f.id()))out.put(f.id(),unknown(d.scope,f));List<Setting> result=new ArrayList<>();for(Setting original:out.values()){ConfigDocument.Field field=w.document(original.scope).fields.get(original.id);result.add(field==null?original:withComment(original,field.comment()));}return result;}
    private Setting withComment(Setting src,String comment){
        if(comment.isBlank() || !(src.type.equals("integer")||src.type.equals("number")||src.type.equals("enum")))return src;
        Setting s=new Setting();s.id=src.id;s.scope=src.scope;s.key=src.key;s.path=src.path;s.title=src.title;s.type=src.type;s.category=src.category;s.description=src.description;s.unit=src.unit;s.note=src.note;s.risk=src.risk;s.min=src.min;s.max=src.max;s.known=src.known;s.choices.addAll(src.choices);
        Matcher min=Pattern.compile("(?i)min(?:imum)?\\s*[=:]\\s*(-?[0-9]+(?:\\.[0-9]+)?)").matcher(comment), max=Pattern.compile("(?i)max(?:imum)?\\s*[=:]\\s*(-?[0-9]+(?:\\.[0-9]+)?)").matcher(comment);
        if(min.find())s.min=new BigDecimal(min.group(1));if(max.find())s.max=new BigDecimal(max.group(1));
        Matcher option=Pattern.compile("(?m)^\\s*(?:--|#|;)?\\s*(-?[0-9]+)\\s*=\\s*(.+)$").matcher(comment);List<Choice> choices=new ArrayList<>();Set<BigDecimal> seen=new HashSet<>();
        while(option.find()){BigDecimal n=new BigDecimal(option.group(1));if(seen.add(n))choices.add(new Choice(n,option.group(2).strip(),"Bedeutung laut Kommentar deiner importierten Datei: "+option.group(2).strip()));}
        if(choices.size()>=2){s.choices.clear();s.choices.addAll(choices);s.type="enum";s.note="Auswahlwerte aus dem Originalkommentar deiner Datei übernommen.";}
        return s;
    }
    public Setting lookup(String id,Workspace w){if(settings.containsKey(id))return settings.get(id);ConfigDocument d=id.startsWith("ini:")?w.ini:w.lua;ConfigDocument.Field f=d.fields.get(id);return f==null?null:unknown(d.scope,f);}
    private Setting unknown(String scope,ConfigDocument.Field f){
        Setting s=new Setting();s.id=f.id();s.scope=scope;s.path=f.path();s.key=String.join(".",f.path().stream().map(Object::toString).toList());s.title=s.key;s.category="custom";
        s.type=f.value() instanceof Boolean?"boolean":f.value() instanceof BigDecimal n?(n.stripTrailingZeros().scale()>0?"number":"integer"):"text";
        s.description="Zusätzliche Einstellung aus deiner Datei. Eine konkrete Spielwirkung ist im Katalog nicht dokumentiert.";s.unit="";s.note="";s.risk="Originalkommentar und Mod-Dokumentation beachten.";
        Matcher binary=Pattern.compile("(?is).*min(?:imum)?\\s*[=:]\\s*0(?:\\.0+)?\\b.*max(?:imum)?\\s*[=:]\\s*1(?:\\.0+)?\\b.*").matcher(f.comment());
        if(s.type.equals("integer")&&binary.matches()){s.min=BigDecimal.ZERO;s.max=BigDecimal.ONE;}
        return s;
    }
    public static boolean equal(Object a,Object b){if(a instanceof BigDecimal x&&b instanceof BigDecimal y)return x.compareTo(y)==0;return Objects.equals(a,b);}
    public static String display(Object v){if(v==null)return "nil";return v instanceof BigDecimal n?n.stripTrailingZeros().toPlainString():v.toString();}
    private static String str(Map<String,Object> map,String key){return Objects.toString(map.get(key),"");}
    private static String effect(String key,String label){return switch(key){
        case "DayLength"->"Ein vollständiger Spieltag dauert "+label+" in echter Zeit. Längere Tage verlangsamen den Kalenderfortschritt.";
        case "MapRemotePlayerVisibility"->"Auf der Karte erscheinen: "+label+". Das verändert nicht die Sichtlinien in der Spielwelt.";
        case "StartMonth"->"Neue Welt startet im Monat "+label+". Ein bestehender Kalender wird dadurch nicht zurückgesetzt.";
        case "DayNightCycle"->"Beleuchtungszyklus: "+label+". Datum und andere Zeitmechaniken bleiben eigenständige Systeme.";
        case "FogCycle"->"Nebelregel: "+label+". Keine Änderung an Kamerazoom, Gebäudeverdeckung oder Renderdistanz.";
        case "NightDarkness"->"Helligkeit während der Nacht: "+label+". Der Tag-Nacht-Wechsel wird dadurch nicht abgeschaltet.";
        case "FoodRotSpeed"->"Lebensmittel verderben "+label.toLowerCase(Locale.ROOT)+". Kühlung wird zusätzlich über den Kühlfaktor geregelt.";
        case "FridgeFactor"->"Schutz gegen Verderb durch Kühlung: "+label+". Größere Wirkung hält Lebensmittel länger frisch.";
        case "CarSpawnRate"->"Verfügbarkeit neu erzeugter Fahrzeuge: "+label+". Bestehende Fahrzeuge werden nicht gelöscht.";
        case "CarGeneralCondition"->"Ausgangszustand neu erzeugter Fahrzeuge: "+label+". Bereits beschädigte Fahrzeuge werden nicht repariert.";
        case "ChanceHasGas"->"Wahrscheinlichkeit, Fahrzeuge mit Kraftstoff vorzufinden: "+label+". Menge und Verbrauch sind getrennte Einstellungen.";
        case "InjurySeverity"->"Schwere neu entstehender Verletzungen: "+label+". Heilt keine bereits bestehenden Verletzungen.";
        case "DamageToPlayerFromHitByACar"->"Anfahrschaden an Charakteren: "+label+". Fahrzeugschäden und PvP-Regeln sind getrennt.";
        default->"Für diese Option gilt die Stufe „"+label+"“. Beachte Wirkung, Einheit und Originalkommentar dieser Einstellung.";
    };}
    private static final Map<String,String[]> BOOL_HELP=Map.ofEntries(
        Map.entry("PVP",new String[]{"Spieler-gegen-Spieler-Schaden ist ausgeschaltet. Zombies und andere Gefahren bleiben davon unabhängig.","Spieler können anderen Spielern Schaden zufügen. SafetySystem und weitere Schutzregeln beeinflussen den Ablauf. Kein Ersatz für RP-Zustimmung."}),
        Map.entry("Public",new String[]{"Nicht öffentlich in der Serverliste sichtbar. Direkter Beitritt ist ohne weitere Sperren weiterhin möglich.","Der Server darf in der öffentlichen Serverliste erscheinen. Ein Passwort wird dadurch nicht entfernt."}),
        Map.entry("Open",new String[]{"Zugang setzt eine passende Freigabe in der Whitelist/Serverdatenbank voraus.","Beitritt ohne vorab angelegten Whitelist-Eintrag möglich. Ein gesetztes Serverpasswort gilt weiterhin."}),
        Map.entry("PauseEmpty",new String[]{"Die Spielwelt läuft bei leerem Server weiter.","Die Spielwelt pausiert, wenn kein Spieler verbunden ist."}),
        Map.entry("SleepNeeded",new String[]{"Kein verpflichtender Schlafbedarf im Multiplayer.","Spieler müssen ihren Schlafbedarf im Multiplayer beachten."}),
        Map.entry("SleepAllowed",new String[]{"Schlafen ist im Multiplayer nicht verfügbar.","Schlafen ist möglich; die Verpflichtung dazu wird separat über SleepNeeded geregelt."}),
        Map.entry("FireSpread",new String[]{"Native Brandausbreitung wird unterbunden, nicht jede einzelne Feuerquelle.","Feuer kann sich ausbreiten und weitere Flächen oder Gebäude beschädigen."}),
        Map.entry("BoneFracture",new String[]{"Die native Entstehung von Knochenbrüchen ist deaktiviert.","Knochenbrüche können entstehen; bestehende Verletzungen werden nicht automatisch verändert."}),
        Map.entry("EnableVehicles",new String[]{"Das native Fahrzeugsystem ist deaktiviert.","Das native Fahrzeugsystem ist aktiviert. Spawnmenge, Zustand und Kraftstoff bleiben separat."}),
        Map.entry("VoiceEnable",new String[]{"Nativer Voicechat ist abgeschaltet.","Nativer Voicechat ist aktiviert. Distanzen und fremde Voice-Mods sind separat zu prüfen."}),
        Map.entry("NoFire",new String[]{"Feuer wird nicht durch diesen Schalter verhindert. Brandausbreitung ist separat.","Der Server verhindert Feuer über diese Option. Der Name NoFire ist absichtlich negativ formuliert."}),
        Map.entry("ConstructionPreventsLootRespawn",new String[]{"Spielerbauten sind kein zusätzliches Ausschlusskriterium für Loot-Nachschub.","Spielerbauten können Loot-Nachschub in betroffenen Bereichen verhindern."}),
        Map.entry("SafetySystem",new String[]{"Das native PvP-Sicherheitssystem ist nicht aktiv.","Das native Sicherheitssystem ist aktiv. Es erzwingt keine beidseitige RP-Zustimmung."})
    );
}
