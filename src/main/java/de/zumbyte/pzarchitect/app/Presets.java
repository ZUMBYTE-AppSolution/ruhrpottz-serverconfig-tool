package de.zumbyte.pzarchitect.app;

import java.math.BigDecimal;
import java.util.*;

/** Curated examples, not official game defaults. No identities, secrets, mods or map order are changed. */
public final class Presets {
    public record Preset(String name,String description,Map<String,Object> values){@Override public String toString(){return name;}}
    public static final List<Preset> ALL=List.of(
        make("Koop & Freunde","Zwei Stunden pro Spieltag, moderate Population, kein PvP und etwas schnelleres Lernen.",0.65,5,false,1.5,1.5,0),
        make("PvE Community","Gemeinsames Überleben mit normaler Population und kontrolliertem Nachschub.",1,5,false,1,1,168),
        make("Survival ausgewogen","Überleben mit normaler Population, knapperem Loot und ohne Nachschub.",1,3,false,0.8,1,0),
        make("Hardcore Survival","Mehr Zombies, weniger Loot und langsamerer Fortschritt. Verletzungen bleiben relevant.",2.5,3,false,0.35,0.75,0),
        make("PvP Survival","Spielerkonflikte und Zombies. Sicherheitssystem aktiv; kein Ersatz für Serverregeln.",1,3,true,0.8,1,0),
        make("Roleplay mit Zombies","Vier Stunden pro Tag, reduzierte Zombiepopulation und schnelleres Lesen.",0.45,7,false,1,2,168),
        make("Ziviles Roleplay","Keine reguläre Population oder Wiederkehr. Vorhandene Zombies werden nicht gelöscht.",0,7,false,1.5,2,168),
        make("Bauen & Testen","Zombiefreie Testwelt mit reichlichem Loot. Kein Godmode, kein automatischer Adminzugang.",0,7,false,3,4,0)
    );
    private Presets(){}
    private static Preset make(String name,String description,double population,int day,boolean pvp,double loot,double xp,int respawn){
        LinkedHashMap<String,Object> m=new LinkedHashMap<>();put(m,"ini","PVP",pvp);put(m,"ini","SafetySystem",true);put(m,"ini","PauseEmpty",true);put(m,"ini","SleepAllowed",true);put(m,"ini","SleepNeeded",false);
        put(m,"lua","DayLength",day);population(m,population);put(m,"lua","ZombieConfig.RespawnHours",population==0?0:72);put(m,"lua","ZombieConfig.RespawnMultiplier",population==0?0:0.1);
        for(String k:List.of("FoodLootNew","CannedFoodLootNew","MedicalLootNew","LiteratureLootNew","ToolLootNew","MaterialLootNew","ClothingLootNew","MechanicsLootNew","OtherLootNew"))put(m,"lua",k,loot);
        put(m,"lua","HoursForLootRespawn",respawn);put(m,"lua","SeenHoursPreventLootRespawn",72);put(m,"lua","ConstructionPreventsLootRespawn",true);put(m,"lua","MultiplierConfig.GlobalToggle",true);put(m,"lua","MultiplierConfig.Global",xp);
        put(m,"lua","MinutesPerPage",xp>=2?0.2:1);return new Preset(name,description,Collections.unmodifiableMap(m));
    }
    public static void population(Map<String,Object> m,double n){for(String k:List.of("PopulationMultiplier","PopulationStartMultiplier","PopulationPeakMultiplier"))put(m,"lua","ZombieConfig."+k,n);if(n==0){put(m,"lua","Zombies",6);put(m,"lua","ZombieMigrate",false);put(m,"lua","ZombieConfig.RedistributeHours",0);}else{put(m,"lua","Zombies",4);put(m,"lua","ZombieMigrate",true);put(m,"lua","ZombieConfig.RedistributeHours",12);}}
    public static void put(Map<String,Object> m,String scope,String key,Object value){if(value instanceof Number n)value=new BigDecimal(n.toString());m.put(ConfigDocument.id(scope,key),value);}
}
