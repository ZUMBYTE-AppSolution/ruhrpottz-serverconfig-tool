package de.zumbyte.pzarchitect.app;

import de.zumbyte.pzarchitect.update.Json;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;

/** Strict data-only parser. Edits replace source spans, never evaluate Lua or rewrite unrelated fields. */
public final class ConfigDocument {
    public record Field(String id, List<Object> path, Object value, int start, int end, int entryStart, int entryEnd, String comment) {}
    private record Table(List<Object> path, int close, int count, boolean separator) {}
    public final String scope, source;
    public final LinkedHashMap<String,Field> fields = new LinkedHashMap<>();
    private final Map<List<Object>,Table> tables = new LinkedHashMap<>();
    private int pos;
    private static final Pattern NUMBER=Pattern.compile("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?");
    public ConfigDocument(String scope, String source) {
        if (!scope.equals("ini") && !scope.equals("lua")) throw new IllegalArgumentException("Unbekannter Dateityp");
        this.scope=scope; this.source=Objects.requireNonNull(source);
        if(source.length()>1_000_000)throw new IllegalArgumentException("Datei überschreitet 1 MB Textlimit");
        if(scope.equals("ini")) parseIni(); else parseLua();
    }
    public static String id(String scope,List<?> path){return scope.equals("ini")?"ini:"+path.getFirst():"lua:"+Json.write(path);}
    public static String id(String scope,String key){return id(scope,scope.equals("ini")?List.of(key):List.of(key.split("\\.")));}
    private void add(Field field) {
        if(fields.size()>10000 || fields.putIfAbsent(field.id(),field)!=null)throw error("Doppelter Schlüssel oder zu viele Felder: "+field.id());
    }
    private void parseIni() {
        int p=source.startsWith("\ufeff")?1:0;StringBuilder comment=new StringBuilder();
        while(p<source.length()) {
            int nl=source.indexOf('\n',p),end=nl<0?source.length():nl;
            int clean=end>p && source.charAt(end-1)=='\r'?end-1:end;
            String line=source.substring(p,clean),trim=line.strip();
            if(trim.isEmpty() || trim.startsWith("#") || trim.startsWith(";")){comment.append(trim).append('\n');}
            else {
                int equal=line.indexOf('=');
                if(equal<1)throw error("Nicht unterstützte INI-Zeile bei Zeichen "+p);
                String key=line.substring(0,equal).strip();
                if(!key.matches("[A-Za-z_][A-Za-z0-9_.-]*"))throw error("Ungültiger INI-Schlüssel");
                int start=p+equal+1;
                String raw=source.substring(start,clean);
                Object value=raw;
                if(raw.equals("true")||raw.equals("false"))value=Boolean.valueOf(raw);
                else if(NUMBER.matcher(raw).matches())value=number(raw);
                add(new Field(id(scope,List.of(key)),List.of(key),value,start,clean,p,nl<0?end:end+1,comment.toString()));comment.setLength(0);
            }
            p=nl<0?end:end+1;
        }
    }
    private void parseLua() {
        if(source.startsWith("\ufeff"))pos++;
        skip();if(!word().equals("SandboxVars"))throw error("Erwartet: SandboxVars = { ... }");
        skip();take('=');skip();table(List.of(),0);skip();if(peek(';')){pos++;skip();}
        if(pos!=source.length())throw error("Ausführbarer Lua-Code oder zusätzlicher Text wird nicht importiert");
    }
    private void table(List<Object> path,int depth) {
        if(depth>30 || tables.size()>2000)throw error("Lua-Verschachtelung zu tief");
        take('{');int count=0;boolean separator=false;int autoIndex=0;Set<Object> keys=new HashSet<>();
        while(true) {
            int before=pos;skip();String comment=source.substring(before,pos).strip();
            if(peek('}')) { tables.put(List.copyOf(path),new Table(List.copyOf(path),pos,count,separator));pos++;return; }
            if(count>0&&!separator)throw error("Komma zwischen Lua-Feldern fehlt");
            int entryStart=pos;Object key;
            if(peek('[')&&longLevel(pos)<0) {
                pos++;skip();key=scalar();
                if(!(key instanceof String) && !(key instanceof BigDecimal))throw error("Nur String- oder Zahlenschlüssel unterstützt");
                if(key instanceof BigDecimal n)key=n.stripTrailingZeros();
                skip();take(']');skip();take('=');skip();
            } else if(pos<source.length()&&(Character.isLetter(source.charAt(pos))||source.charAt(pos)=='_')) {
                int mark=pos;String ident=word();skip();
                if(peek('=')){pos++;skip();key=ident;}else{pos=mark;key=BigDecimal.valueOf(++autoIndex);}
            } else { key=BigDecimal.valueOf(++autoIndex); }
            if(!keys.add(key))throw error("Doppelter Lua-Schlüssel");
            ArrayList<Object> child=new ArrayList<>(path);child.add(key);int start=pos,end;Object value=null;boolean isTable=peek('{');
            if(isTable){table(child,depth+1);end=pos;}else{value=scalar();end=pos;}
            skip();separator=peek(',')||peek(';');if(separator)pos++;
            if(!isTable)add(new Field(id(scope,child),List.copyOf(child),value,start,end,entryStart,pos,comment));
            count++;
        }
    }
    private Object scalar() {
        if(pos>=source.length())throw error("Unerwartetes Dateiende");
        char c=source.charAt(pos);
        if(c=='\''||c=='"')return quoted();
        if(longLevel(pos)>=0)return longString();
        if(c=='-' || (c>='0'&&c<='9')) {
            Matcher m=NUMBER.matcher(source);m.region(pos,source.length());if(!m.lookingAt())throw error("Ungültige Lua-Zahl");
            String raw=m.group();pos=m.end();return number(raw);
        }
        String literal=word();
        return switch(literal){case "true"->true;case "false"->false;case "nil"->null;default->throw error("Nur Lua-Daten erlaubt, kein Ausdruck: "+literal);};
    }
    private static BigDecimal number(String raw) {
        if(raw.length()>80)throw new IllegalArgumentException("Zahl zu lang");
        BigDecimal n=new BigDecimal(raw);if(Math.abs(n.scale())>100)throw new IllegalArgumentException("Zahlenexponent zu groß");return n;
    }
    private String quoted() {
        char quote=source.charAt(pos++);StringBuilder s=new StringBuilder();
        while(pos<source.length()) {
            char c=source.charAt(pos++);if(c==quote)return s.toString();
            if(c=='\r'||c=='\n')throw error("Nicht maskierter Zeilenumbruch in Lua-String");
            if(c!='\\'){s.append(c);continue;}
            if(pos==source.length())throw error("Unvollständige Maskierung");
            char e=source.charAt(pos++);
            switch(e) {
                case 'n'->s.append('\n');case 'r'->s.append('\r');case 't'->s.append('\t');case 'b'->s.append('\b');case 'f'->s.append('\f');case 'a'->s.append((char)7);case 'v'->s.append((char)11);
                case '\\','\'','"'->s.append(e);
                case '\n'->s.append('\n');
                case '\r'->{if(peek('\n'))pos++;s.append('\n');}
                case 'z'->{while(pos<source.length()&&Character.isWhitespace(source.charAt(pos)))pos++;}
                default->{
                    if(e>='0'&&e<='9') {int n=e-'0';int digits=1;while(digits<3&&pos<source.length()&&source.charAt(pos)>='0'&&source.charAt(pos)<='9'){n=n*10+source.charAt(pos++)-'0';digits++;}if(n>255)throw error("Lua-Byte außerhalb 0..255");s.append((char)n);}
                    else throw error("Nicht unterstützte Lua-Maskierung");
                }
            }
        }
        throw error("Nicht geschlossener Lua-String");
    }
    private int longLevel(int at){if(at>=source.length()||source.charAt(at)!='[')return -1;int n=at+1;while(n<source.length()&&source.charAt(n)=='=')n++;return n<source.length()&&source.charAt(n)=='['?n-at-1:-1;}
    private String longString(){int level=longLevel(pos);if(level<0)throw error("Ungültiger Long-String");int start=pos+level+2;String close="]"+"=".repeat(level)+"]";int end=source.indexOf(close,start);if(end<0)throw error("Nicht geschlossener Lua-Long-String");pos=end+close.length();String value=source.substring(start,end);if(value.startsWith("\r\n"))return value.substring(2);if(value.startsWith("\n"))return value.substring(1);return value;}
    private void skip(){while(pos<source.length()){char c=source.charAt(pos);if(Character.isWhitespace(c)){pos++;continue;}if(source.startsWith("--",pos)){pos+=2;if(longLevel(pos)>=0)longString();else{int nl=source.indexOf('\n',pos);pos=nl<0?source.length():nl;}continue;}break;}}
    private String word(){int start=pos;while(pos<source.length()&&(source.charAt(pos)=='_'||Character.isLetterOrDigit(source.charAt(pos))))pos++;if(start==pos)throw error("Unerwartetes Zeichen");return source.substring(start,pos);}
    private boolean peek(char c){return pos<source.length()&&source.charAt(pos)==c;}
    private void take(char c){if(!peek(c))throw error("Erwartetes Zeichen: "+c);pos++;}
    private IllegalArgumentException error(String s){return new IllegalArgumentException(s+" (Position "+pos+")");}
    public ConfigDocument set(List<?> rawPath,Object value) {
        List<Object> path=new ArrayList<>(rawPath);String id=id(scope,path);Field old=fields.get(id);
        if(old!=null) return new ConfigDocument(scope,replace(old.start,old.end,encode(value,scope)));
        String nl=source.contains("\r\n")?"\r\n":"\n";
        if(scope.equals("ini")){if(path.size()!=1||!path.getFirst().toString().matches("[A-Za-z_][A-Za-z0-9_.-]*"))throw error("Ungültiger INI-Schlüssel");return new ConfigDocument(scope,source+(source.isEmpty()||source.endsWith("\n")?"":nl)+path.getFirst()+"="+encode(value,scope)+nl);}
        List<Object> parent=path.subList(0,path.size()-1);
        if(tables.containsKey(path))throw error("Eine vorhandene Tabelle wird nicht durch einen Einzelwert überschrieben");
        while(!tables.containsKey(parent)){if(fields.containsKey(id(scope,parent)))throw error("Ein Einzelwert kann nicht als Tabelle verwendet werden");parent=parent.subList(0,parent.size()-1);}
        Table table=tables.get(parent);String valueText=encode(value,scope);
        for(int i=path.size()-1;i>parent.size();i--)valueText="{ "+luaKey(path.get(i))+" = "+valueText+" }";
        String text=(table.count>0&&!table.separator?",":"")+nl+"    ".repeat(parent.size()+1)+luaKey(path.get(parent.size()))+" = "+valueText+","+nl+"    ".repeat(parent.size());
        return new ConfigDocument(scope,replace(table.close,table.close,text));
    }
    public ConfigDocument remove(String id){Field f=fields.get(id);return f==null?this:new ConfigDocument(scope,replace(f.entryStart,f.entryEnd,""));}
    private String replace(int start,int end,String text){return source.substring(0,start)+text+source.substring(end);}
    private static String luaKey(Object key){if(key instanceof String s && s.matches("[A-Za-z_][A-Za-z0-9_]*")&&!Set.of("true","false","nil","end","function","local","return","if","then","else","do","while","repeat","until","for","in","and","or","not","break","elseif").contains(s))return s;return "["+encode(key,"lua")+"]";}
    public static String encode(Object v,String scope){
        if(v==null){if(scope.equals("ini"))return "";return "nil";}
        if(v instanceof BigDecimal n)return n.stripTrailingZeros().toPlainString();
        if(v instanceof Number || v instanceof Boolean)return v.toString();
        if(!(v instanceof String s))throw new IllegalArgumentException("Nicht unterstützter Werttyp");
        if(scope.equals("ini")){if(s.indexOf('\r')>=0||s.indexOf('\n')>=0||s.indexOf('\0')>=0)throw new IllegalArgumentException("INI-Werte dürfen keine Zeilenumbrüche enthalten");return s;}
        StringBuilder out=new StringBuilder("\"");for(char c:s.toCharArray()){switch(c){case '"'->out.append("\\\"");case '\\'->out.append("\\\\");case '\n'->out.append("\\n");case '\r'->out.append("\\r");case '\t'->out.append("\\t");default->{if(c<32)out.append(String.format("\\%03d",(int)c));else out.append(c);}}}return out.append('"').toString();
    }
}
