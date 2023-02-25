/*
SourceView.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2002 Ian Lewis (IanLewis@member.fsf.org)

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

//{{{ imports
/*
All classes are listed explicitly so
it is easy to see which package it
belongs to.
*/

//{{{ jsXe classes
import net.sourceforge.jsxe.*;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.gui.OptionsPanel;
import net.sourceforge.jsxe.gui.DocumentView;
import net.sourceforge.jsxe.dom.AdapterNode;
import net.sourceforge.jsxe.dom.XMLDocument;
import net.sourceforge.jsxe.dom.XMLDocumentListener;
import net.sourceforge.jsxe.util.Log;
import net.sourceforge.jsxe.util.MiscUtilities;
//}}}

//{{{ jEdit Syntax classes
import org.syntax.jedit.*;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;
//}}}

//{{{ Swing components
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
//}}}

//{{{ AWT components
import java.awt.*;
import java.awt.event.*;
//}}}

//{{{ DOM Classes
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.ParserConfigurationException;
//}}}

//{{{ Java base classes
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ArrayList;
//}}}

//}}}

/**
 * The SourceView class allows users to view and edit an XML document in raw
 * text form.
 *
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: SourceView.java,v 1.19 2006/04/09 01:38:07 ian_lewis Exp $
 */
public class SourceView extends JPanel implements DocumentView {
    
    // Temporary Hack
    protected static ArrayList m_sourceviews = new ArrayList();
    
    //{{{ Private static members
    private static final String _VIEWNAME = "source";
    //}}}
    
    //{{{ Public static members
    public static final String SOFT_TABS = _VIEWNAME+".soft.tabs";
    public static final String LAST_FIND_STRING = _VIEWNAME+".last.find.string";
    public static final String END_OF_LINE_MARKS = _VIEWNAME+".end-of-line-markers";
    //}}}
    
    //{{{ SourceView constructor
    /**
     * Creates a new SourceView for the XMLDocument specified.
     * Parent
     * @param document the document to open.
     * @throws IOException if the document cannot be viewed using this view
     */
    public SourceView(DocumentBuffer document, SourceViewPlugin plugin) throws IOException {
        
        m_plugin = plugin;
        
        m_textarea = new JEditTextArea();
        
        InputHandler handler = m_textarea.getInputHandler();
        handler.addKeyBinding("ENTER",new SourceViewEnter());
        handler.addKeyBinding("TAB",new SourceViewTab());
        m_textarea.setInputHandler(handler);
        
        TextAreaPainter painter = m_textarea.getPainter();
        painter.setEOLMarkersPainted(jsXe.getBooleanProperty(SourceView.END_OF_LINE_MARKS, true));
        painter.setStyles(
            new SyntaxStyle[] { SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.text.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.comment.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.doctype.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.attribute.value.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.attribute.value.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.cdata.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.entity.reference.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.element.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.attribute.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.processing.instruction.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.namespace.prefix.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.markup.color")),
                                SourceViewOptionsPanel.parseStyle(jsXe.getProperty("source.invalid.color")),
                                });
       // textarea.setFont(new Font("Monospaced", 0, 12));
        m_textarea.setCaretPosition(0);
        //for test scripts
        m_textarea.setName("SourceTextArea");
        
        m_textarea.putClientProperty(InputHandler.SMART_HOME_END_PROPERTY, Boolean.TRUE);
        
        //{{{ create popup menu
        
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(jsXe.getAction("sourceview.cut"));
        popup.add(menuItem);
        menuItem = new JMenuItem(jsXe.getAction("sourceview.copy"));
        popup.add(menuItem);
        menuItem = new JMenuItem(jsXe.getAction("sourceview.paste"));
        popup.add(menuItem);
        popup.addSeparator();
        menuItem = new JMenuItem(jsXe.getAction("sourceview.find"));
        popup.add(menuItem);
        
        m_textarea.setRightClickPopup(popup);
        //}}}
        
        setLayout(new BorderLayout());
        add(m_textarea, BorderLayout.CENTER);
        
        //{{{ Construct Edit Menu
        m_editMenu = new JMenu(Messages.getMessage("Edit.Menu"));
        m_editMenu.setMnemonic('E');
       // These don't do anything yet.
       // JMenuItem menuItem = new JMenuItem("Undo");
       // menuItem.addActionListener( new EditUndoAction() );
       // menu.add( menuItem );
       // menuItem = new JMenuItem("Redo");
       // menuItem.addActionListener( new EditRedoAction() );
       // menu.add(menuItem);
       // menu.addSeparator();
        menuItem = new JMenuItem(jsXe.getAction("sourceview.cut"));
        m_editMenu.add(menuItem);
        menuItem = new JMenuItem(jsXe.getAction("sourceview.copy"));
        m_editMenu.add(menuItem);
        menuItem = new JMenuItem(jsXe.getAction("sourceview.paste"));
        m_editMenu.add(menuItem);
        m_editMenu.addSeparator();
        menuItem = new JMenuItem(jsXe.getAction("sourceview.find"));
        m_editMenu.add(menuItem);
       // menuItem = new JMenuItem(new EditFindNextAction());
       // menu.add(menuItem);
        //}}}
        
        m_sourceviews.add(this);
        
        setDocumentBuffer(document);
        
        //focus on the text area the first time the view is shown
        addComponentListener(new ComponentListener() {//{{{
            
            public void componentHidden(ComponentEvent e) {}
            
            public void componentMoved(ComponentEvent e) {}
            
            public void componentResized(ComponentEvent e) {}
            
            public void componentShown(ComponentEvent e) {
                m_textarea.requestFocus();
                removeComponentListener(this);
            }
            
        });//}}}
        
    }//}}}
    
    //{{{ getTextArea()
    
    public JEditTextArea getTextArea() {
        return m_textarea;
    }//}}}
    
    //{{{ getHumanReadableName()
    
    public static String getHumanReadableName() {
        return "Source View";
    }//}}}
    
    //{{{ DocumentView methods
    
    //{{{ close()
    
    public boolean close() {
        SourceViewSearchDialog dialog = SourceViewSearchDialog.getSearchDialog();
        if (dialog != null) {
            dialog.dispose();
        }
        m_document.removeXMLDocumentListener(docListener);
        m_sourceviews.remove(this);
        return true;
    }//}}}
    
    //{{{ getDocumentViewComponent
    
    public Component getDocumentViewComponent() {
        return this;
    }//}}}

    //{{{ getMenus()
    
    public JMenu[] getMenus() {
        return new JMenu[] { m_editMenu };
    }//}}}
    
    //{{{ getDocumentBuffer()
    
    public DocumentBuffer getDocumentBuffer() {
        return m_document;
    }//}}}
    
    //{{{ getViewName()
    
    public ViewPlugin getViewPlugin() {
        return m_plugin;
    }//}}}
    
    //{{{ setDocumentBuffer()
    
    public void setDocumentBuffer(DocumentBuffer document) throws IOException {
        
        if (m_document != null) {
            m_document.removeXMLDocumentListener(docListener);
        }
        
        ensureDefaultProps(document);
        
        m_document = document;
        m_textarea.setDocument(new SourceViewDocument(m_document));
        m_textarea.setTokenMarker(new XMLTokenMarker());
        try {
            m_textarea.getDocument().putProperty(PlainDocument.tabSizeAttribute, Integer.valueOf(document.getProperty(XMLDocument.INDENT, "4")));
        } catch (NumberFormatException e) {
            Log.log(Log.WARNING, this, e.getMessage());
        }
        m_document.addXMLDocumentListener(docListener);
    }//}}}
    
    //{{{ goToLine()
    public boolean goToLine(int lineNo) {
        //not supported yet.
        return false;
    }//}}}
    
    //}}}
    
    //{{{ Private members
    
    //{{{ EditUndoAction class
    
    private class EditUndoAction implements ActionListener {
        
        //{{{ actionPerformed()
        
        public void actionPerformed(ActionEvent e) {
            //undo does nothing for now
        }//}}}
        
    }//}}}
    
    //{{{ EditRedoAction class
    
    private class EditRedoAction implements ActionListener {
        
        //{{{ actionPerformed()
        
        public void actionPerformed(ActionEvent e) {
            //redo action does nothing for now.
        }//}}}
        
    }//}}}
    
    //{{{ EditFindNextAction class
    
    private class EditFindNextAction extends AbstractAction {
        
        //{{{ EditFindNextAction constructor
        
        public EditFindNextAction() {
            //putValue(Action.NAME, "Find Next");
        	putValue(Action.NAME,  Messages.getMessage("SourceView.FindNext"));
            putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke("ctrl G"));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyStroke.getKeyStroke("N").getKeyCode()));
        }//}}}
        
        //{{{ actionPerformed()
        
        public void actionPerformed(ActionEvent e) {
            //use previous find string
            
        }//}}}
        
    }//}}}
    
    //{{{ SourceViewXMLDocumentListener class
    
    private class SourceViewXMLDocumentListener implements XMLDocumentListener {
        
        //{{{ propertyChanged()
        
        public void propertyChanged(XMLDocument source, String key, String oldValue) {
            if (key.equals(XMLDocument.INDENT)) {
                try {
                    m_textarea.getDocument().putProperty(PlainDocument.tabSizeAttribute, Integer.valueOf(source.getProperty(XMLDocument.INDENT, "4")));
                    m_textarea.updateUI();
                } catch (NumberFormatException e) {
                    Log.log(Log.WARNING, this, e.getMessage());
                }
            }
        }//}}}
        
        //{{{ structureChanged
        
        public void structureChanged(XMLDocument source, AdapterNode location) {}//}}}
        
    }//}}}
    
    //{{{ SourceViewEnter
    
    private class SourceViewEnter implements ActionListener {
        
        //{{{ actionPerformed()
        
        public void actionPerformed(ActionEvent evt) {
            
            if (!m_textarea.isEditable()) {
                m_textarea.getToolkit().beep();
                return;
            }
            
            m_textarea.setSelectedText("\n"+getLastIndent());
        }//}}}
        
        //{{{ getLastIndent()
        
        private String getLastIndent() {
            boolean softTabs = Boolean.valueOf(m_document.getProperty(XMLDocument.IS_USING_SOFT_TABS, "false")).booleanValue();
            int tabWidth = Integer.parseInt(m_document.getProperty(XMLDocument.INDENT));
            
            int line = m_textarea.getCaretLine();
			StringBuffer indent = new StringBuffer();
            while (line >= 1) {
                String text = m_textarea.getLineText(line);
                for (int i=0; i<text.length(); i++) {
                    char current = text.charAt(i);
                    if (!(current == ' ' ||
                          current == '\t' ||
                          current == '\n'))
                    {
                        int ws = MiscUtilities.getLeadingWhiteSpaceWidth(text, tabWidth);
                        return MiscUtilities.createWhiteSpace(ws, softTabs ? 0 : tabWidth);
                    }
                }
                line--;
            }
            return "";
        }//}}}
        
    }//}}}
    
    //{{{ SourceViewTab
    
    private class SourceViewTab implements ActionListener {
        
        //{{{ actionPerformed()
        
        public void actionPerformed(ActionEvent evt) {
            
            if (!m_textarea.isEditable())  {
                m_textarea.getToolkit().beep();
                return;
            }
            
            
            boolean softTabs = Boolean.valueOf(m_document.getProperty(XMLDocument.IS_USING_SOFT_TABS, "false")).booleanValue();
            if (softTabs) {
                try {
                    int indent = Integer.parseInt(m_document.getProperty(XMLDocument.INDENT));
                    StringBuffer tab = new StringBuffer();
                    for (int i=0; i<indent; i++) {
                        tab.append(" ");
                    }
                    m_textarea.overwriteSetSelectedText(tab.toString());
                } catch (NumberFormatException nfe) {
                    Log.log(Log.ERROR, this, nfe);
                }
            } else {
                m_textarea.overwriteSetSelectedText("\t");
            }
        }//}}}
        
    }//}}}
    
    //{{{ ensureDefaultProps()
    
    private void ensureDefaultProps(DocumentBuffer document) {
        
    }//}}}
    
    private SourceViewXMLDocumentListener docListener = new SourceViewXMLDocumentListener();
    
    private DocumentBuffer m_document;
    private JEditTextArea m_textarea;
    
    private String m_searchString;
    private String m_replaceString;
    private SourceViewPlugin m_plugin;
    
    private JMenu m_editMenu;
    
    //}}}
    
}
