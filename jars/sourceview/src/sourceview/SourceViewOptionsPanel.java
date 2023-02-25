/*
SourceViewOptionsPanel.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2002 Ian Lewis (IanLewis@member.fsf.org)
Portions Copyright (C) 1999, 2000, 2001, 2002 Slava Pestov

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
Optionally, you may find a copy of the GNU General Public License
from http://www.fsf.org/copyleft/gpl.txt
*/

package sourceview;

//{{{ Imports

import org.syntax.jedit.*;

//{{{ jsXe classes
import net.sourceforge.jsxe.*;
import net.sourceforge.jsxe.gui.*;
import net.sourceforge.jsxe.util.MiscUtilities;
//}}}

//{{{ Java Classes
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.EmptyBorder;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Iterator;
//}}}

//}}}

public class SourceViewOptionsPanel extends OptionsPanel {
    
    //{{{ SourceViewOptionsPanel constructor
    
    public SourceViewOptionsPanel(DocumentBuffer buffer) {
        
        m_document = buffer;
        
        setLayout(new BorderLayout(6,6));

        add(BorderLayout.CENTER,createStyleTableScroller());
        
        JPanel panel = new JPanel();
        
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        panel.setLayout(layout);
        
        int gridY = 0;
        
        m_endOfLineMarkCheckBox = new JCheckBox(Messages.getMessage("SourceView.Options.EndOfLineMarker"),jsXe.getBooleanProperty("source.end-of-line-markers",true));
        
        constraints.gridy      = gridY++;
        constraints.gridx      = 1;
        constraints.gridheight = 1;
        constraints.gridwidth  = 2;
        constraints.weightx    = 1.0f;
        constraints.fill       = GridBagConstraints.BOTH;
        constraints.insets     = new Insets(1,0,1,0);
        
        layout.setConstraints(m_endOfLineMarkCheckBox, constraints);
        panel.add(m_endOfLineMarkCheckBox);
        
        add(BorderLayout.SOUTH, panel);
        
    }//}}}
    
    //{{{ getName()
    
    public String getName() {
        return "sourceview";
    }//}}}
    
    //{{{ save()
    
    public void save() {
        styleModel.save();
        jsXe.setBooleanProperty("source.end-of-line-markers",m_endOfLineMarkCheckBox.isSelected());
        
        Iterator itr = SourceView.m_sourceviews.iterator();
        while (itr.hasNext()) {
            TextAreaPainter painter = ((SourceView)itr.next()).getTextArea().getPainter();
            painter.setEOLMarkersPainted(m_endOfLineMarkCheckBox.isSelected());
            painter.setStyles(
                new SyntaxStyle[] { parseStyle(jsXe.getProperty("source.text.color")),
                                    parseStyle(jsXe.getProperty("source.comment.color")),
                                    parseStyle(jsXe.getProperty("source.doctype.color")),
                                    parseStyle(jsXe.getProperty("source.attribute.value.color")),
                                    parseStyle(jsXe.getProperty("source.attribute.value.color")),
                                    parseStyle(jsXe.getProperty("source.cdata.color")),
                                    parseStyle(jsXe.getProperty("source.entity.reference.color")),
                                    parseStyle(jsXe.getProperty("source.element.color")),
                                    parseStyle(jsXe.getProperty("source.attribute.color")),
                                    parseStyle(jsXe.getProperty("source.processing.instruction.color")),
                                    parseStyle(jsXe.getProperty("source.namespace.prefix.color")),
                                    parseStyle(jsXe.getProperty("source.markup.color")),
                                    parseStyle(jsXe.getProperty("source.invalid.color")),
                                  });
        }
        
        
    }//}}}
    
    //{{{ getTitle()
    
    public String getTitle() {
        return Messages.getMessage("SourceView.Options.Title");
    }//}}}
    
    //{{{ parseStyle() method
    /**
     * Converts a style string to a style object.
     * @param str The style string
     * @param family Style strings only specify font style, not font family
     * @param size Style strings only specify font style, not font family
     * @param color If false, the styles will be monochrome
     * @exception IllegalArgumentException if the style is invalid
     */
    public static SyntaxStyle parseStyle(String str) throws IllegalArgumentException {
        Color fgColor = Color.black;
        Color bgColor = null;
        boolean italic = false;
        boolean bold = false;
        StringTokenizer st = new StringTokenizer(str);
        while(st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.startsWith("color:")) {
                fgColor = parseColor(s.substring(6), Color.black);
            }
            else if (s.startsWith("style:")) {
                for (int i = 6; i < s.length(); i++) {
                    if (s.charAt(i) == 'i')
                        italic = true;
                    else if(s.charAt(i) == 'b')
                        bold = true;
                    else
                        throw new IllegalArgumentException("Invalid style: " + s);
                }
            }
            else
                throw new IllegalArgumentException("Invalid directive: " + s);
        }
        return new SyntaxStyle(fgColor, italic, bold);
    } //}}}
    
    //{{{ parseColor() method
    public static Color parseColor(String name, Color defaultColor) {
        if (name == null)
            return defaultColor;
        else if(name.startsWith("#")) {
            try
            {
                return Color.decode(name);
            }
            catch(NumberFormatException nf)
            {
                return defaultColor;
            }
        }
        else if("red".equals(name))
            return Color.red;
        else if("green".equals(name))
            return Color.green;
        else if("blue".equals(name))
            return Color.blue;
        else if("yellow".equals(name))
            return Color.yellow;
        else if("orange".equals(name))
            return Color.orange;
        else if("white".equals(name))
            return Color.white;
        else if("lightGray".equals(name))
            return Color.lightGray;
        else if("gray".equals(name))
            return Color.gray;
        else if("darkGray".equals(name))
            return Color.darkGray;
        else if("black".equals(name))
            return Color.black;
        else if("cyan".equals(name))
            return Color.cyan;
        else if("magenta".equals(name))
            return Color.magenta;
        else if("pink".equals(name))
            return Color.pink;
        else
            return defaultColor;
    } //}}}
    
    //{{{ Private Members
    
    //{{{ getParentDialog() method
    /**
     * Traverses the given component's parent tree looking for an
     * instance of JDialog, and return it. If not found, return null.
     * @param c The component
     */
    private static JDialog getParentDialog(Component c) {
        Component p = c.getParent();
        while (p != null && !(p instanceof JDialog)) {
            p = p.getParent();
        }

        return (p instanceof JDialog) ? (JDialog) p : null;
    } //}}}
    
    //{{{ getStyleString() method
    /**
     * Converts a style into it's string representation.
     * @param style The style
     */
    private static String getStyleString(SyntaxStyle style) {
        StringBuffer buf = new StringBuffer();

        if (style.getColor() != null) {
            buf.append("color:" + getColorHexString(style.getColor()));
        }

        if (!style.isPlain()) {
            buf.append(" style:" + (style.isItalic() ? "i" : "")
                + (style.isBold() ? "b" : ""));
        }

        return buf.toString();
    }//}}}
    
    //{{{ createStyleTableScroller() method
    
    private JScrollPane createStyleTableScroller() {
        styleModel = createStyleTableModel();
        styleTable = new JTable(styleModel);
        styleTable.setRowSelectionAllowed(false);
        styleTable.setColumnSelectionAllowed(false);
        styleTable.setCellSelectionEnabled(false);
        styleTable.getTableHeader().setReorderingAllowed(false);
        styleTable.addMouseListener(new MouseHandler());
        TableColumnModel tcm = styleTable.getColumnModel();
        TableColumn styleColumn = tcm.getColumn(1);
        styleColumn.setCellRenderer(new StyleTableModel.StyleRenderer());
        Dimension d = styleTable.getPreferredSize();
        d.height = Math.min(d.height,100);
        JScrollPane scroller = new JScrollPane(styleTable);
        scroller.setPreferredSize(d);
        return scroller;
    } //}}}

    //{{{ createStyleTableModel() method
    
    private StyleTableModel createStyleTableModel() {
        return new StyleTableModel();
    } //}}}
    
    //{{{ getColorHexString() method
    /**
     * Converts a color object to its hex value. The hex value
     * prefixed is with `#', for example `#ff0088'.
     * @param c The color object
     */
    private static String getColorHexString(Color c) {
        String colString = Integer.toHexString(c.getRGB() & 0xffffff);
        return "#000000".substring(0,7 - colString.length()).concat(colString);
    } //}}}
    
    //{{{ MouseHandler class
    
    private class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            int row = styleTable.rowAtPoint(evt.getPoint());
            if(row == -1)
                return;

            SyntaxStyle style = new StyleEditor(
                SourceViewOptionsPanel.this,
                (SyntaxStyle)styleModel.getValueAt(
                row,1)).getStyle();
            if(style != null)
                styleModel.setValueAt(style,row,1);
        }
    } //}}}
    
    //{{{ StyleEditor class
    private static class StyleEditor extends EnhancedDialog implements ActionListener {
        //{{{ StyleEditor constructor
        public StyleEditor(Component comp, SyntaxStyle style) {
            super(getParentDialog(comp),
                Messages.getMessage("SourceView.StyleEditor.Title"),true);
    
            JPanel content = new JPanel(new BorderLayout(12,12));
            content.setBorder(new EmptyBorder(12,12,12,12));
            setContentPane(content);
    
            GridBagLayout layout = new GridBagLayout();
            JPanel panel = new JPanel(layout);
    
            GridBagConstraints cons = new GridBagConstraints();
            cons.gridx = cons.gridy = 0;
            cons.gridwidth = 2;
            cons.gridheight = 1;
            cons.fill = GridBagConstraints.BOTH;
            cons.weightx = 0.0f;
    
            italics = new JCheckBox(Messages.getMessage("SourceView.StyleEditor.Italics"));
            italics.setSelected(style.isItalic());
            italics.setToolTipText(Messages.getMessage("SourceView.StyleEditor.Italics.ToolTip"));
            layout.setConstraints(italics,cons);
            panel.add(italics);
    
            cons.gridy++;
            bold = new JCheckBox(Messages.getMessage("SourceView.StyleEditor.Bold"));
            bold.setSelected(style.isBold());
            bold.setToolTipText(Messages.getMessage("SourceView.StyleEditor.Bold.ToolTip"));
            layout.setConstraints(bold,cons);
            panel.add(bold);
    
            cons.gridy++;
            cons.gridwidth = 1;
            Color fg = style.getColor();
    
            fgColorCheckBox = new JCheckBox(Messages.getMessage("SourceView.StyleEditor.Color"));
            fgColorCheckBox.setSelected(fg != null);
            fgColorCheckBox.addActionListener(this);
            fgColorCheckBox.setBorder(new EmptyBorder(0,0,0,12));
            fgColorCheckBox.setToolTipText(Messages.getMessage("SourceView.StyleEditor.Color.ToolTip"));
            layout.setConstraints(fgColorCheckBox,cons);
            panel.add(fgColorCheckBox);
    
            cons.gridx++;
            fgColor = new ColorWellButton(fg);
            fgColor.setEnabled(fg != null);
            layout.setConstraints(fgColor,cons);
            panel.add(fgColor);
    
            content.add(BorderLayout.CENTER,panel);
    
            Box box = new Box(BoxLayout.X_AXIS);
            box.add(Box.createGlue());
            box.add(ok = new JButton(Messages.getMessage("common.ok")));
            getRootPane().setDefaultButton(ok);
            ok.addActionListener(this);
            box.add(Box.createHorizontalStrut(6));
            box.add(cancel = new JButton(Messages.getMessage("common.cancel")));
            cancel.addActionListener(this);
            box.add(Box.createGlue());
    
            content.add(BorderLayout.SOUTH,box);
    
            pack();
            setLocationRelativeTo(getParentDialog(comp));
    
            setResizable(false);
            show();
        } //}}}
    
        //{{{ actionPerformed() method
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if(source == ok)
                ok();
            else if(source == cancel)
                cancel();
            else if(source == fgColorCheckBox)
                fgColor.setEnabled(fgColorCheckBox.isSelected());
        } //}}}
    
        //{{{ ok() method
        public void ok() {
            okClicked = true;
            dispose();
        } //}}}
    
        //{{{ cancel() method
        public void cancel() {
            dispose();
        } //}}}
    
        //{{{ getStyle() method
        public SyntaxStyle getStyle() {
            if(!okClicked)
                return null;
    
            Color foreground = (fgColorCheckBox.isSelected()
                ? fgColor.getSelectedColor()
                : null);
    
            return new SyntaxStyle(foreground, italics.isSelected(), bold.isSelected());
        } //}}}
    
        //{{{ Private members
        private JCheckBox italics;
        private JCheckBox bold;
        private JCheckBox fgColorCheckBox;
        private ColorWellButton fgColor;
        private JButton ok;
        private JButton cancel;
        private boolean okClicked;
        //}}}
    } //}}}
    
    //{{{ StyleTableModel class
    private static class StyleTableModel extends AbstractTableModel {
        private Vector styleChoices;
    
        //{{{ StyleTableModel constructor
        public StyleTableModel() {
            styleChoices = new Vector(9);
            addStyleChoice("SourceView.Markup", "source.markup.color");
            addStyleChoice("xml.namespace.prefix", "source.namespace.prefix.color");
            addStyleChoice("xml.text","source.text.color");
            addStyleChoice("xml.element","source.element.color");
            addStyleChoice("xml.attribute","source.attribute.color");
            addStyleChoice("xml.attribute.value", "source.attribute.value.color");
            addStyleChoice("xml.cdata","source.cdata.color");
            addStyleChoice("xml.processing.instruction","source.processing.instruction.color");
            addStyleChoice("xml.entity.reference","source.entity.reference.color");
            addStyleChoice("xml.comment","source.comment.color");
            addStyleChoice("xml.doctype","source.doctype.color");
            addStyleChoice("SourceView.Invalid", "source.invalid.color");
            MiscUtilities.quicksort(styleChoices, new MiscUtilities.StringCompare());
        } //}}}
    
        //{{{ getColumnCount() method
        public int getColumnCount()
        {
            return 2;
        } //}}}
    
        //{{{ getRowCount() method
        public int getRowCount()
        {
            return styleChoices.size();
        } //}}}
    
        //{{{ getValueAt() method
        public Object getValueAt(int row, int col)
        {
            StyleChoice ch = (StyleChoice)styleChoices.elementAt(row);
            switch(col)
            {
            case 0:
                return ch.label;
            case 1:
                return ch.style;
            default:
                return null;
            }
        } //}}}
    
        //{{{ setValueAt() method
        public void setValueAt(Object value, int row, int col)
        {
            StyleChoice ch = (StyleChoice)styleChoices.elementAt(row);
            if(col == 1)
                ch.style = (SyntaxStyle)value;
            fireTableRowsUpdated(row,row);
        } //}}}
    
        //{{{ getColumnName() method
        public String getColumnName(int index)
        {
            switch(index)
            {
            case 0:
                return Messages.getMessage("SourceView.Syntax.Object");
            case 1:
                return Messages.getMessage("SourceView.Syntax.Style");
            default:
                return null;
            }
        } //}}}
    
        //{{{ save() method
        public void save() {
            for (int i = 0; i < styleChoices.size(); i++) {
                StyleChoice ch = (StyleChoice)styleChoices.elementAt(i);
                jsXe.setProperty(ch.property, getStyleString(ch.style));
            }
        } //}}}
    
        //{{{ addStyleChoice() method
        private void addStyleChoice(String label, String property) {
            styleChoices.addElement(new StyleChoice(Messages.getMessage(label), property, parseStyle(jsXe.getProperty(property, "color:#000000"))));
        } //}}}
    
        //{{{ StyleChoice class
        private class StyleChoice {
            String label;
            String property;
            SyntaxStyle style;
    
            StyleChoice(String label, String property, SyntaxStyle style) {
                this.label = label;
                this.property = property;
                this.style = style;
            }
    
            // for sorting
            public String toString() {
                return label;
            }
        } //}}}
    
        //{{{ StyleRenderer class
        static class StyleRenderer extends JLabel implements TableCellRenderer {
            //{{{ StyleRenderer constructor
            public StyleRenderer() {
                setOpaque(true);
               // setBorder();
                setText("Hello World");
                setToolTipText(Messages.getMessage("SourceView.Syntax.ToolTip"));
            } //}}}
    
            //{{{ getTableCellRendererComponent() method
            public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean cellHasFocus,
                int row,
                int col) 
            {
                if (value != null) {
                    SyntaxStyle style = (SyntaxStyle)value;
                    setForeground(style.getColor());
                    setBackground(Color.WHITE);
                    setFont(new Font("Monospaced", (style.isItalic() ? Font.ITALIC : 0) | (style.isBold() ? Font.BOLD : 0), 12));
                }
    
                //Set a border if focused?
               // setBorder();
                return this;
            } //}}}
        } //}}}
    } //}}}
    
    //{{{ ColorWellButton class
    
    /**
     * A button that, when clicked, shows a color chooser.
     *
     * You can get and set the currently selected color using
     * {@link #getSelectedColor()} and {@link #setSelectedColor(Color)}.
     * @author Slava Pestov
     * @version $Id: SourceViewOptionsPanel.java,v 1.8 2006/04/09 01:38:07 ian_lewis Exp $
     */
    public static class ColorWellButton extends JButton {
    
        //{{{ ColorWellButton constructor
        public ColorWellButton(Color color)
        {
            setIcon(new ColorWell(color));
            setMargin(new Insets(2,2,2,2));
            addActionListener(new ActionHandler());
    
            // according to krisk this looks better on OS X...
            if(OperatingSystem.isMacOSLF())
                putClientProperty("JButton.buttonType","toolbar");
        } //}}}
    
        //{{{ getSelectedColor() method
        public Color getSelectedColor()
        {
            return ((ColorWell)getIcon()).color;
        } //}}}
    
        //{{{ setSelectedColor() method
        public void setSelectedColor(Color color)
        {
            ((ColorWell)getIcon()).color = color;
            repaint();
        } //}}}
    
        //{{{ ColorWell class
        private class ColorWell implements Icon {
        
            Color color;
    
            ColorWell(Color color)
            {
                this.color = color;
            }
    
            public int getIconWidth()
            {
                return 35;
            }
    
            public int getIconHeight()
            {
                return 10;
            }
    
            public void paintIcon(Component c, Graphics g, int x, int y)
            {
                if(color == null)
                    return;
    
                g.setColor(color);
                g.fillRect(x,y,getIconWidth(),getIconHeight());
                g.setColor(color.darker());
                g.drawRect(x,y,getIconWidth()-1,getIconHeight()-1);
            }
        } //}}}
    
        //{{{ ActionHandler class
        class ActionHandler implements ActionListener
        {
            public void actionPerformed(ActionEvent evt)
            {
                JDialog parent = getParentDialog(ColorWellButton.this);
                JDialog dialog;
                if (parent != null)
                {
                    dialog = new ColorPickerDialog(parent,
                        Messages.getMessage("SourceView.ColorChooser.Title"),
                        true);
                }
                else
                {
                    dialog = new ColorPickerDialog(
                        JOptionPane.getFrameForComponent(
                        ColorWellButton.this),
                        Messages.getMessage("SourceView.ColorChooser.Title"),
                        true);
                }
                dialog.pack();
                dialog.show();
            }
        } //}}}
    
        //{{{ ColorPickerDialog class
        /**
         * Replacement for the color picker dialog provided with Swing. This version
         * supports dialog as well as frame parents.
         */
        private class ColorPickerDialog extends EnhancedDialog implements ActionListener
        {
            public ColorPickerDialog(Frame parent, String title, boolean modal)
            {
                super(parent,title,modal);
    
                init();
            }
    
            public ColorPickerDialog(Dialog parent, String title, boolean modal)
            {
                super(parent,title,modal);
    
                getContentPane().setLayout(new BorderLayout());
    
                init();
            }
    
            public void ok()
            {
                Color c = chooser.getColor();
                if (c != null)
                    setSelectedColor(c);
                setVisible(false);
            }
    
            public void cancel()
            {
                setVisible(false);
            }
    
            public void actionPerformed(ActionEvent evt)
            {
                if (evt.getSource() == ok)
                    ok();
                else
                    cancel();
            }
    
            //{{{ Private members
            private JColorChooser chooser;
            private JButton ok;
            private JButton cancel;
    
            private void init()
            {
                Color c = getSelectedColor();
                if(c == null)
                    chooser = new JColorChooser();
                else
                    chooser = new JColorChooser(c);
    
                getContentPane().add(BorderLayout.CENTER, chooser);
    
                Box buttons = new Box(BoxLayout.X_AXIS);
                buttons.add(Box.createGlue());
    
                ok = new JButton(Messages.getMessage("common.ok"));
                ok.addActionListener(this);
                buttons.add(ok);
                buttons.add(Box.createHorizontalStrut(6));
                getRootPane().setDefaultButton(ok);
                cancel = new JButton(Messages.getMessage("common.cancel"));
                cancel.addActionListener(this);
                buttons.add(cancel);
                buttons.add(Box.createGlue());
    
                getContentPane().add(BorderLayout.SOUTH, buttons);
                pack();
                setLocationRelativeTo(getParent());
            } //}}}
        } //}}}
    }//}}}
    
    private StyleTableModel styleModel;
    private JTable styleTable;
    private DocumentBuffer m_document;
    private JCheckBox m_endOfLineMarkCheckBox;
    //}}}
    
}//}}}

