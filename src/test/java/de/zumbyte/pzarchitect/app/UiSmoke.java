package de.zumbyte.pzarchitect.app;

import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public final class UiSmoke {
    private static App app;private static int checks;
    private UiSmoke(){}
    private static void ok(boolean v,String m){if(!v)throw new AssertionError(m);checks++;}
    private static java.util.List<Component> children(Container c){java.util.List<Component> r=new ArrayList<>();for(Component item:c.getComponents()){r.add(item);if(item instanceof Container child)r.addAll(children(child));}return r;}
    public static void main(String[] args)throws Exception{
        Path out=Path.of(args.length==0?"build/screenshots":args[0]);Files.createDirectories(out);Path data=Files.createTempDirectory("pz-ui-test-");System.setProperty("pz.data",data.toString());
        try{
            SwingUtilities.invokeAndWait(()->{try{app=new App(true);app.setLocation(20,20);app.setVisible(true);}catch(Exception ex){throw new RuntimeException(ex);}});Robot robot=new Robot();robot.setAutoDelay(120);robot.waitForIdle();Thread.sleep(700);
            capture(robot,out,"01-uebersicht");
            SwingUtilities.invokeAndWait(()->{JScrollPane home=children(app).stream().filter(c->c instanceof JScrollPane).map(c->(JScrollPane)c).findFirst().orElseThrow();ok(home.getViewport().getViewPosition().y==0,"home heading starts visible");});
            for(String route:new String[]{"all","rp","wizard","profiles","presets","export","server","about"}){SwingUtilities.invokeAndWait(()->app.navigate(route));robot.waitForIdle();Thread.sleep(250);ok(app.isShowing(),"page "+route);capture(robot,out,"page-"+route);}
            java.lang.reflect.Field timerField=App.class.getDeclaredField("statusTimer");timerField.setAccessible(true);javax.swing.Timer timer=(javax.swing.Timer)timerField.get(app);ok(timer!=null&&timer.getDelay()==15_000,"server status refresh interval is 15 seconds");ok(!timer.isRunning(),"status polling stops outside server page");
            SwingUtilities.invokeAndWait(()->{app.navigate("server");ok(timer.isRunning(),"server status refresh starts on community page");app.navigate("home");ok(!timer.isRunning(),"server status refresh stops after leaving");button(app,"Bereiche anzeigen").doClick();JButton category=button(app,"Multiplayer & PvP");ok(category.isShowing(),"sidebar categories expand");category.doClick();ok(children(app).stream().anyMatch(c->c instanceof JLabel l&&l.isShowing()&&Objects.equals(l.getText(),"Multiplayer & PvP")),"sidebar category opens matching settings");});capture(robot,out,"page-category");
            SwingUtilities.invokeAndWait(()->app.navigate("rp"));robot.waitForIdle();SwingUtilities.invokeAndWait(()->{
                Theme.Toggle toggle=(Theme.Toggle)children(app).stream().filter(c->c instanceof Theme.Toggle).findFirst().orElseThrow();ok(toggle.missing,"missing distinct from false");toggle.doClick();ok(app.workspace.is("ini","PVP",true),"UI toggle sets true");
                Theme.Toggle second=(Theme.Toggle)children(app).stream().filter(c->c instanceof Theme.Toggle).findFirst().orElseThrow();second.doClick();ok(app.workspace.is("ini","PVP",false),"UI toggle sets false");
            });capture(robot,out,"02-toggle");
            SwingUtilities.invokeAndWait(()->app.navigate("wizard"));
            SwingUtilities.invokeLater(()->button(app,"Assistent starten").doClick());
            JDialog wizard=waitDialog("Quick Start");ok(wizard.isShowing(),"wizard opens in separate window");Thread.sleep(700);capture(robot,out,"03-wizard",wizard);
            SwingUtilities.invokeAndWait(()->{button(wizard,"Weiter").doClick();button(wizard,"Zurück").doClick();ok(button(wizard,"Zurück").isEnabled()==false,"wizard back disabled on first step");for(int i=0;i<8;i++)button(wizard,"Weiter").doClick();ok(button(wizard,"Vorschau öffnen").isShowing(),"wizard reaches final step");});
            SwingUtilities.invokeLater(()->button(wizard,"Vorschau öffnen").doClick());
            JDialog preview=waitDialog("Änderungsvorschau");ok(preview.isShowing(),"wizard change preview opens");
            SwingUtilities.invokeAndWait(()->button(preview,"Als einen Bearbeitungsschritt übernehmen").doClick());robot.waitForIdle();ok(app.workspace.lua.fields.size()>10,"wizard actually produces settings");ok(app.workspace.is("ini","PVP",false),"wizard PvE suggestion applied");capture(robot,out,"04-export");
            SwingUtilities.invokeLater(()->button(app,"Speichern").doClick());JDialog save=waitDialog();
            SwingUtilities.invokeAndWait(()->{java.util.List<JTextField> inputs=children(save).stream().filter(c->c instanceof JTextField).map(c->(JTextField)c).toList();ok(inputs.size()>=2,"profile save fields visible");inputs.getFirst().setText("UI-Testprofil");button(save,"OK").doClick();});robot.waitForIdle();
            ok(app.store.list(app.store.profiles).size()==1,"profile actually saved through UI");ok(!app.workspace.dirty,"saved workspace is clean");
            SwingUtilities.invokeAndWait(()->{JComboBox<?> profiles=children(app).stream().filter(c->c instanceof JComboBox<?> box&&box.getItemCount()>0&&Objects.toString(box.getItemAt(box.getItemCount()-1)).contains("Neues Profil")).map(c->(JComboBox<?>)c).findFirst().orElseThrow();profiles.setSelectedIndex(profiles.getItemCount()-1);ok(app.workspace.name.equals("Neues Profil"),"profile dropdown creates a new workspace");ok(profiles.getItemCount()>=2,"saved profile available for switching");profiles.setSelectedIndex(1);ok(app.workspace.name.equals("UI-Testprofil"),"profile dropdown opens a saved profile");});
            SwingUtilities.invokeAndWait(()->app.navigate("profiles"));capture(robot,out,"06-profile-saved");

            SwingUtilities.invokeAndWait(()->{app.setSize(1040,760);app.navigate("wizard");});robot.waitForIdle();Thread.sleep(200);capture(robot,out,"05-small-window");ok(app.getWidth()==1040,"small window");
            System.out.println("PASS: "+checks+" UI assertions; real Swing window rendered and toggles operated in offline mode.");
        }finally{if(app!=null)SwingUtilities.invokeAndWait(app::closeForTest);try(var paths=Files.walk(data)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);}}
    }
    private static JButton button(Container root,String text){return children(root).stream().filter(c->c instanceof JButton b&&text.equals(b.getText())).map(c->(JButton)c).findFirst().orElseThrow(()->new AssertionError("Button missing: "+text));}
    private static JDialog waitDialog()throws Exception{return waitDialog("");}
    private static JDialog waitDialog(String title)throws Exception{for(int i=0;i<80;i++){JDialog[] result={null};SwingUtilities.invokeAndWait(()->{for(Window w:Window.getWindows())if(w instanceof JDialog d&&d.isShowing()&&d.getTitle().contains(title))result[0]=d;});if(result[0]!=null)return result[0];Thread.sleep(100);}throw new AssertionError("Dialog not shown: "+title);}
    private static void capture(Robot robot,Path out,String name)throws Exception{capture(robot,out,name,app);}
    private static void capture(Robot robot,Path out,String name,Window window)throws Exception{BufferedImage[] image={null};SwingUtilities.invokeAndWait(()->{Container content=((RootPaneContainer)window).getContentPane();image[0]=new BufferedImage(content.getWidth(),content.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D graphics=image[0].createGraphics();content.printAll(graphics);graphics.dispose();});ImageIO.write(image[0],"png",out.resolve(name+".png").toFile());checks++;}
}
