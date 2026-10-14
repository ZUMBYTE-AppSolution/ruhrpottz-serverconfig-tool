package de.zumbyte.pzarchitect.app;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;

@SuppressWarnings("serial")
public final class Theme {
    public static final Color BG=new Color(15,16,22),SIDE=new Color(19,20,29),CARD=new Color(26,27,38),LINE=new Color(52,52,71),TEXT=new Color(237,232,247),MUTED=new Color(163,155,181),PURPLE=new Color(166,117,242),MINT=new Color(111,217,188);
    public static float scale=1;
    private Theme(){}
    public static int px(int p){return Math.round(p*scale);}
    public static Font font(int p,int style){return new Font(Font.DIALOG,style,px(p));}
    public static void install(){
        UIManager.put("defaultFont",font(13,0));
        for(String key:new String[]{"Label","Button","ToggleButton","CheckBox","RadioButton","ComboBox","TextField","PasswordField","TextArea","TextPane","List","Table","Tree","Menu","MenuItem","OptionPane","TabbedPane","Spinner"}){UIManager.put(key+".font",font(13,0));UIManager.put(key+".foreground",TEXT);UIManager.put(key+".background",CARD);}
        UIManager.put("Panel.background",BG);UIManager.put("OptionPane.background",BG);UIManager.put("OptionPane.messageForeground",TEXT);UIManager.put("ScrollPane.background",BG);UIManager.put("Viewport.background",BG);UIManager.put("List.selectionBackground",new Color(59,43,83));UIManager.put("List.selectionForeground",TEXT);UIManager.put("TextField.caretForeground",TEXT);UIManager.put("TextArea.caretForeground",TEXT);UIManager.put("TextField.selectionBackground",new Color(80,49,120));UIManager.put("ComboBox.selectionBackground",new Color(59,43,83));UIManager.put("ScrollBar.thumb",LINE);UIManager.put("ScrollBar.track",BG);UIManager.put("ScrollBar.width",px(12));
    }
    public static JPanel column(){JPanel p=new Column();p.setAlignmentX(Component.LEFT_ALIGNMENT);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setOpaque(false);return p;}
    public static JPanel row(Component... components){JPanel p=new JPanel(new Wrap(FlowLayout.LEFT,px(8),px(5)));p.setOpaque(false);p.setAlignmentX(Component.LEFT_ALIGNMENT);for(Component c:components)p.add(c);return p;}
    public static JLabel label(String text,int size,Color color){JLabel l=new JLabel(text);l.setFont(font(size,size>=18?Font.BOLD:Font.PLAIN));l.setForeground(color);l.setAlignmentX(Component.LEFT_ALIGNMENT);return l;}
    public static JTextArea paragraph(String text){JTextArea a=new JTextArea(text){@Override public Dimension getPreferredSize(){Container p=getParent();int w=p==null?700:p.getWidth()-p.getInsets().left-p.getInsets().right;if(w<80)w=700;FontMetrics fm=getFontMetrics(getFont());int lines=0;for(String line:getText().split("\\n",-1)){int length=0;lines++;for(String word:line.split(" ")){int width=fm.stringWidth(word+" ");if(length>0&&length+width>w){lines++;length=0;}length+=width;}}return new Dimension(w,lines*fm.getHeight()+6);}@Override public Dimension getMaximumSize(){return new Dimension(getPreferredSize().width,getPreferredSize().height);}@Override public Dimension getMinimumSize(){return new Dimension(1,getFontMetrics(getFont()).getHeight()+6);}};a.setMargin(new Insets(0,0,0,0));a.setFont(font(13,0));a.setForeground(MUTED);a.setOpaque(false);a.setLineWrap(true);a.setWrapStyleWord(true);a.setEditable(false);a.setFocusable(true);int rows=0;for(String line:text.split("\n",-1))rows+=Math.max(1,(line.length()+84)/85);a.setRows(Math.min(rows,80));a.setMaximumSize(new Dimension(Integer.MAX_VALUE,px(Math.min(rows,80)*21+8)));a.setAlignmentX(Component.LEFT_ALIGNMENT);return a;}
    public static JPanel card(){JPanel p=column();p.setOpaque(true);p.setBackground(CARD);p.setBorder(new CompoundBorder(new LineBorder(LINE,1,true),new EmptyBorder(px(18),px(18),px(18),px(18))));p.setAlignmentX(Component.LEFT_ALIGNMENT);return p;}
    public static void gap(JPanel p,int height){p.add(Box.createVerticalStrut(px(height)));}
    public static JScrollPane scroll(Component content){JScrollPane s=new JScrollPane(content);s.setBorder(BorderFactory.createEmptyBorder());s.getViewport().setBackground(BG);s.setHorizontalScrollBarPolicy(content instanceof JTextArea a&&!a.getLineWrap()?ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED:ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);s.getVerticalScrollBar().setUnitIncrement(px(24));s.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI(){@Override protected void configureScrollBarColors(){thumbColor=LINE;trackColor=BG;}@Override protected JButton createDecreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}@Override protected JButton createIncreaseButton(int o){return createDecreaseButton(o);}});return s;}
    public static JButton button(String title,boolean accent,Runnable action){JButton b=new JButton(title);b.setFont(font(13,Font.BOLD));b.setForeground(accent?new Color(22,11,38):TEXT);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setContentAreaFilled(false);b.setFocusPainted(false);b.setBorder(new EmptyBorder(px(10),px(13),px(10),px(13)));b.setUI(new BasicButtonUI(){@Override public void paint(Graphics g,JComponent c){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int w=c.getWidth(),h=c.getHeight();Color start=accent?new Color(183,142,251):new Color(44,43,60),end=accent?new Color(144,94,214):new Color(31,31,44);if(b.getModel().isRollover())start=start.brighter();g2.setPaint(new GradientPaint(0,0,start,w,h,end));g2.fillRoundRect(1,1,w-3,h-3,px(8),px(8));g2.setColor(b.hasFocus()?PURPLE:LINE);g2.drawRoundRect(1,1,w-3,h-3,px(8),px(8));g2.dispose();super.paint(g,c);}});b.addActionListener(e->action.run());return b;}

    private static final class Column extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
        public int getScrollableUnitIncrement(Rectangle r,int orientation,int direction){return px(24);}
        public int getScrollableBlockIncrement(Rectangle r,int orientation,int direction){return Math.max(px(24),r.height-px(24));}
        public boolean getScrollableTracksViewportWidth(){return true;}
        public boolean getScrollableTracksViewportHeight(){return false;}
    }
    private static final class Wrap extends FlowLayout {
        Wrap(int align,int h,int v){super(align,h,v);}
        @Override public Dimension preferredLayoutSize(Container target){
            int width=target.getWidth();if(width<=0&&target.getParent()!=null)width=target.getParent().getWidth();if(width<=0)width=850;
            Insets in=target.getInsets();int max=width-in.left-in.right-getHgap()*2,x=0,y=0,line=0,used=0;
            for(Component c:target.getComponents())if(c.isVisible()){Dimension d=c.getPreferredSize();if(x>0&&x+getHgap()+d.width>max){y+=line+getVgap();used=Math.max(used,x);x=0;line=0;}if(x>0)x+=getHgap();x+=d.width;line=Math.max(line,d.height);}
            return new Dimension(Math.max(used,x)+in.left+in.right+getHgap()*2,y+line+in.top+in.bottom+getVgap()*2);
        }
        @Override public Dimension minimumLayoutSize(Container t){Dimension d=preferredLayoutSize(t);return new Dimension(100,d.height);}
    }
    public static final class Toggle extends JToggleButton {
        public boolean missing;
        public Toggle(boolean selected,boolean missing){super();setSelected(selected);this.missing=missing;setPreferredSize(new Dimension(px(175),px(42)));setMinimumSize(getPreferredSize());setMaximumSize(getPreferredSize());setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));getAccessibleContext().setAccessibleName("Ein/Aus-Schalter");}
        @Override protected void paintComponent(Graphics g){Graphics2D a=(Graphics2D)g.create();a.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int y=px(8);a.setColor(missing?LINE:isSelected()?PURPLE:new Color(70,68,85));a.fillRoundRect(px(2),y,px(57),px(28),px(28),px(28));a.setColor(TEXT);a.fillOval(px(isSelected()&&!missing?33:6),y+px(4),px(20),px(20));a.setFont(font(13,Font.BOLD));a.setColor(missing?MUTED:TEXT);a.drawString(missing?"Nicht gesetzt":isSelected()?"Ein":"Aus",px(70),y+px(19));if(hasFocus()){a.setColor(PURPLE);a.drawRoundRect(0,0,getWidth()-1,getHeight()-1,px(6),px(6));}a.dispose();}
    }
}
