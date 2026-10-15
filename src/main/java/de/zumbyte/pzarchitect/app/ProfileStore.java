package de.zumbyte.pzarchitect.app;

import de.zumbyte.pzarchitect.update.Json;
import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.*;

public final class ProfileStore {
    public record Entry(Path file,String name,String updated,String error){@Override public String toString(){return name+"  ·  "+updated+(error.isEmpty()?"":"  [beschädigt]");}}
    public final Path root,profiles,trash,history;
    public ProfileStore(Path root)throws IOException{this.root=root;profiles=root.resolve("profiles");trash=root.resolve("trash");history=root.resolve("history");for(Path d:List.of(root,profiles,trash,history)){if(Files.isSymbolicLink(d))throw new IOException("Profilordner darf kein symbolischer Link sein");Files.createDirectories(d);try{Files.setPosixFilePermissions(d,PosixFilePermissions.fromString("rwx------"));}catch(UnsupportedOperationException ignored){}}}
    public static Path defaultRoot(){String custom=System.getProperty("pz.data");if(custom!=null)return Path.of(custom);String os=System.getProperty("os.name").toLowerCase(Locale.ROOT),home=System.getProperty("user.home");if(os.startsWith("windows"))return Path.of(System.getenv().getOrDefault("LOCALAPPDATA",home),"PZServerArchitect");if(os.contains("mac"))return Path.of(home,"Library","Application Support","PZServerArchitect");return Path.of(System.getenv().getOrDefault("XDG_DATA_HOME",home+"/.local/share"),"pz-server-architect");}
    public List<Entry> list(Path dir)throws IOException{List<Entry> out=new ArrayList<>();try(var stream=Files.list(dir)){for(Path p:stream.filter(f->f.getFileName().toString().endsWith(".json")&&!Files.isSymbolicLink(f)).sorted().toList()){try{Map<String,Object> m=Json.object(Json.parse(Workspace.read(p)));out.add(new Entry(p,Objects.toString(m.get("name"),p.getFileName().toString()),Objects.toString(m.get("updatedAt"),""),""));}catch(Exception ex){out.add(new Entry(p,p.getFileName().toString(),"", "Nicht lesbar"));}}}return out;}
    public Workspace load(Path file)throws IOException{String text=Workspace.read(file);Workspace w=Workspace.from(text);w.diskHash=file.toAbsolutePath().normalize().getParent().equals(profiles.toAbsolutePath().normalize())?Workspace.digest(text):null;return w;}
    public Path save(Workspace w)throws IOException{
        Path file=profiles.resolve(UUID.fromString(w.id)+".json");String text=Json.write(w.snapshot(true));if(text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>950_000)throw new IOException("Profil ist zu groß (inklusive Originalen)");
        try(FileChannel ch=FileChannel.open(root.resolve(".write.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE,LinkOption.NOFOLLOW_LINKS);FileLock lock=ch.tryLock()){
            if(lock==null)throw new IOException("Ein anderes Fenster schreibt gerade Profile");
            if(Files.exists(file)){
                String previous=Workspace.read(file);if(w.diskHash==null||!Workspace.digest(previous).equals(w.diskHash))throw new IOException("Profil wurde außerhalb dieses Fensters geändert. Neu laden oder als Kopie speichern.");
                Path dir=history.resolve(w.id);Files.createDirectories(dir);Workspace.atomic(dir.resolve(System.currentTimeMillis()+"-"+UUID.randomUUID()+".json"),previous);
                try(var files=Files.list(dir)){List<Path> items=files.sorted(Comparator.comparing(Path::toString).reversed()).toList();for(int i=10;i<items.size();i++)Files.delete(items.get(i));}
            }
            Workspace.atomic(file,text);w.diskHash=Workspace.digest(text);w.dirty=false;return file;
        }catch(OverlappingFileLockException ex){throw new IOException("Profil wird gerade geschrieben",ex);}
    }
    public Path delete(Path file)throws IOException{if(!file.toAbsolutePath().normalize().getParent().equals(profiles.toAbsolutePath().normalize()))throw new IOException("Kein Profilpfad");Path target=trash.resolve(Instant.now().toEpochMilli()+"-"+file.getFileName());Files.move(file,target);return target;}
    public Workspace restore(Path file)throws IOException{Workspace w=load(file);w.id=UUID.randomUUID().toString();w.name+=" (wiederhergestellt)";w.diskHash=null;w.dirty=true;save(w);return w;}
}
