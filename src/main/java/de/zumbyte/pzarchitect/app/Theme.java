package de.zumbyte.pzarchitect.app;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;

@SuppressWarnings("serial")
public final class Theme {
    public static final Color BG=new Color(13,15,24),SIDE=new Color(19,22,34),CARD=new Color(27,31,46),LINE=new Color(59,65,88),TEXT=new Color(246,246,252),MUTED=new Color(181,187,205),PURPLE=new Color(178,137,255),MINT=new Color(111,226,195);
    public static float scale=1;
    private Theme(){}
    public static int px(int p){return Math.round(p*scale);}
    public static Font font(int p,int style){return new Font(Font.DIALOG,style,px(p));}
    public static void install(){
        UIManager.put("defaultFont",font(14,0));
        for(String key:new String[]{"Label","Button","ToggleButton","CheckBox","RadioButton","ComboBox","TextField","PasswordField","TextArea","TextPane","List","Table","Tree","Menu","MenuItem","OptionPane","TabbedPane","Spinner"}){UIManager.put(key+".font",font(14,0));UIManager.put(key+".foreground",TEXT);UIManager.put(key+".background",CARD);}
        UIManager.put("Panel.background",BG);UIManager.put("OptionPane.background",BG);UIManager.put("OptionPane.messageForeground",TEXT);UIManager.put("ScrollPane.background",BG);UIManager.put("Viewport.background",BG);UIManager.put("List.selectionBackground",new Color(59,43,83));UIManager.put("List.selectionForeground",TEXT);UIManager.put("TextField.caretForeground",TEXT);UIManager.put("TextArea.caretForeground",TEXT);UIManager.put("TextField.selectionBackground",new Color(80,49,120));UIManager.put("ComboBox.selectionBackground",new Color(59,43,83));UIManager.put("ScrollBar.thumb",LINE);UIManager.put("ScrollBar.track",BG);UIManager.put("ScrollBar.width",px(12));
    }
    public static JPanel column(){JPanel p=new Column();p.setAlignmentX(Component.LEFT_ALIGNMENT);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setOpaque(false);return p;}
    public static JPanel row(Component... components){JPanel p=new JPanel(new Wrap(FlowLayout.LEFT,px(8),px(5)));p.setOpaque(false);p.setAlignmentX(Component.LEFT_ALIGNMENT);for(Component c:components)p.add(c);return p;}
    public static JLabel label(String text,int size,Color color){JLabel l=new JLabel(text);l.setFont(font(size,size>=18?Font.BOLD:Font.PLAIN));l.setForeground(color);l.setAlignmentX(Component.LEFT_ALIGNMENT);return l;}
    public static JTextArea paragraph(String text){JTextArea a=new JTextArea(text){@Override public Dimension getPreferredSize(){Container p=getParent();int w=p==null?700:p.getWidth()-p.getInsets().left-p.getInsets().right;if(w<80)w=700;FontMetrics fm=getFontMetrics(getFont());int lines=0;for(String line:getText().split("\\n",-1)){int length=0;lines++;for(String word:line.split(" ")){int width=fm.stringWidth(word+" ");if(length>0&&length+width>w){lines++;length=0;}length+=width;}}return new Dimension(w,lines*fm.getHeight()+6);}@Override public Dimension getMaximumSize(){return new Dimension(getPreferredSize().width,getPreferredSize().height);}@Override public Dimension getMinimumSize(){return new Dimension(1,getFontMetrics(getFont()).getHeight()+6);}};a.setMargin(new Insets(0,0,0,0));a.setFont(font(14,0));a.setForeground(MUTED);a.setOpaque(false);a.setLineWrap(true);a.setWrapStyleWord(true);a.setEditable(false);a.setFocusable(false);a.setCaret(new javax.swing.text.DefaultCaret(){@Override protected void adjustVisibility(Rectangle r){}});int rows=0;for(String line:text.split("\n",-1))rows+=Math.max(1,(line.length()+84)/85);a.setRows(Math.min(rows,80));a.setMaximumSize(new Dimension(Integer.MAX_VALUE,px(Math.min(rows,80)*23+8)));a.setAlignmentX(Component.LEFT_ALIGNMENT);return a;}
    public static JPanel card(){JPanel p=new RoundedCard();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setBorder(new EmptyBorder(px(23),px(25),px(23),px(25)));p.setAlignmentX(Component.LEFT_ALIGNMENT);return p;}
    public static void gap(JPanel p,int height){p.add(Box.createVerticalStrut(px(height)));}
    public static JScrollPane scroll(Component content){JScrollPane s=new JScrollPane(content);s.setBorder(BorderFactory.createEmptyBorder());s.getViewport().setBackground(BG);s.setHorizontalScrollBarPolicy(content instanceof JTextArea a&&!a.getLineWrap()?ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED:ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);s.getVerticalScrollBar().setUnitIncrement(px(24));s.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI(){@Override protected void configureScrollBarColors(){thumbColor=LINE;trackColor=BG;}@Override protected JButton createDecreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}@Override protected JButton createIncreaseButton(int o){return createDecreaseButton(o);}});SwingUtilities.invokeLater(()->s.getViewport().setViewPosition(new Point(0,0)));return s;}
    public static JButton button(String title,boolean accent,Runnable action){
        JButton b=new JButton(title);b.setFont(font(14,Font.BOLD));b.setForeground(accent?new Color(25,16,43):TEXT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setContentAreaFilled(false);b.setRolloverEnabled(true);b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(px(13),px(20),px(13),px(20)));
        b.setUI(new BasicButtonUI(){@Override public void paint(Graphics g,JComponent c){
            Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=c.getWidth(),h=c.getHeight(),r=px(14);boolean hover=b.getModel().isRollover(),press=b.getModel().isPressed();
            boolean selected=Boolean.TRUE.equals(b.getClientProperty("selectedNav"));
            if(accent||hover||selected||b.hasFocus())for(int i=px(7);i>=1;i--){
                int alpha=(accent?11:5)+(hover?7:0)+(selected?4:0);
                g2.setColor(new Color(156,103,247,alpha));g2.fillRoundRect(i,i,w-i*2,h-i*2,r,r);
            }
            Color start=accent?new Color(200,166,255):selected?new Color(63,53,89):hover?new Color(62,58,87):new Color(42,45,65);
            Color end=accent?new Color(157,107,235):selected?new Color(43,39,67):hover?new Color(44,42,68):new Color(34,37,55);
            if(press){start=start.darker();end=end.darker();}
            g2.setPaint(new GradientPaint(0,0,start,w,h,end));g2.fillRoundRect(px(3),px(3),w-px(6),h-px(6),r,r);
            g2.setColor(b.hasFocus()?MINT:accent?new Color(222,194,255):selected?PURPLE:LINE);
            g2.drawRoundRect(px(3),px(3),w-px(7),h-px(7),r,r);g2.dispose();super.paint(g,c);
        }});
        b.addActionListener(e->action.run());return b;
    }

    private static final class RoundedCard extends JPanel {
        RoundedCard(){setOpaque(false);}
        @Override protected void paintComponent(Graphics g){Graphics2D a=(Graphics2D)g.create();a.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int w=getWidth(),h=getHeight(),r=px(20);a.setPaint(new GradientPaint(0,0,new Color(32,37,55),w,h,CARD));a.fillRoundRect(0,0,w-1,h-1,r,r);a.setColor(LINE);a.drawRoundRect(0,0,w-1,h-1,r,r);a.dispose();super.paintComponent(g);}
    }

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
