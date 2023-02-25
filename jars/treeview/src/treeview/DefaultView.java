/*
DefaultView.java
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

package treeview;

//{{{ imports

import treeview.action.*;

//{{{ jsXe classes
import net.sourceforge.jsxe.dom.*;
import net.sourceforge.jsxe.dom.completion.ElementDecl;
import net.sourceforge.jsxe.jsXe;
import net.sourceforge.jsxe.DocumentBuffer;
import net.sourceforge.jsxe.ViewPlugin;
import net.sourceforge.jsxe.gui.OptionsPanel;
import net.sourceforge.jsxe.gui.DocumentView;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.util.Log;
//}}}

//{{{ Swing components
import javax.swing.*;
import javax.swing.text.PlainDocument;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
//}}}

//{{{ AWT components
import java.awt.*;
import java.awt.event.*;
//}}}

//{{{ Java base classes
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
//}}}

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

//}}}

/**
 * This is the default tree-like view in jsXe. It has a tree panel in the
 * upper-left, a table in the lower-left to display attributes of the currently
 * selected node, and a text panel on the right to display the value of the
 * currently selected node.
 *
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: DefaultView.java,v 1.25 2006/02/16 23:13:03 ian_lewis Exp $
 */
public class DefaultView extends JPanel implements DocumentView {
    
    //{{{ Private static members
    private static final Properties m_defaultProperties;
    //}}}
    
    //{{{ Public static members
    public static final String CONTINUOUS_LAYOUT = TreeViewPlugin.PLUGIN_NAME+".continuous.layout";
    public static final String VERT_SPLIT_LOCATION = TreeViewPlugin.PLUGIN_NAME+".splitpane.vert.loc";
    public static final String HORIZ_SPLIT_LOCATION = TreeViewPlugin.PLUGIN_NAME+".splitpane.horiz.loc";
    public static final String SHOW_COMMENTS = TreeViewPlugin.PLUGIN_NAME+".show.comment.nodes";
    public static final String SHOW_EMPTY_NODES = TreeViewPlugin.PLUGIN_NAME+".show.empty.nodes";
    public static final String SHOW_ATTRIBUTES  = TreeViewPlugin.PLUGIN_NAME+".show.attributes";
    //}}}
    
    static {
        InputStream viewinputstream = DefaultView.class.getResourceAsStream("/treeview/treeview.props");
        m_defaultProperties = new Properties();
        try {
            m_defaultProperties.load(viewinputstream);
        } catch (IOException ioe) {}
    }
    
    //{{{ DefaultView constructor
    /**
     * Constructs a new DefaultView for the provided document.
     * @param document the document that this view shows
     * @throws IOException if the document cannot be viewed using this view.
     */
    public DefaultView(DocumentBuffer document, TreeViewPlugin plugin) throws IOException {
        
        m_plugin = plugin;
        
        setLayout(new BorderLayout());
        
        //{{{ init html editor pane
        m_valueTextArea.setName("TreeViewTextArea");
        m_valueTextArea.setEditable(false);
        //use hard coded font for right now
        //style: 0=plain, 1=bold, 2=italic, 3=boldItalic
        m_valueTextArea.setFont(new Font("Monospaced", 0, 12));
        m_valueTextArea.setLineWrap(false);
        JScrollPane htmlView = new JScrollPane(m_valueTextArea);
        //}}}
        
        //{{{ init attributes table
        JScrollPane attrView = new JScrollPane(attributesTable);
        
        attributesTable.setName("TreeViewAttributesTable");
        attributesTable.setColumnSelectionAllowed(false);
        attributesTable.setRowSelectionAllowed(false);
        attributesTable.addMouseListener(new TablePopupListener());
        attributesTable.setRowHeight(new JComboBox(new String[] { "template" })
                .getPreferredSize().height);
        //}}}
        
        //{{{ init tree
        tree.setName("TreeViewTree");
        JScrollPane treeView = new JScrollPane(tree);
        tree.addTreeSelectionListener(new DefaultTreeSelectionListener(this));
        
        //starts editing if the user start typing on one of the nodes
        //seems to catch user shortcuts too. Not sure how to resolve that.
        
       // tree.addKeyListener(new KeyListener() {//{{{
       //     
       //     public void keyPressed(KeyEvent e) {}
       //     
       //     public void keyReleased(KeyEvent e) {}
       //     
       //     public void keyTyped(KeyEvent e) {
       //         if (!e.isActionKey()) {
       //             AdapterNode node = (AdapterNode)tree.getLastSelectedPathComponent();
       //             if (node != null) {
       //                 if (tree.isEditable(node)) {
       //                     tree.startEditingAtPath(tree.getLeadSelectionPath());
       //                 } else {
       //                     if (canEditInJEditorPane(node)) {
       //                         m_valueTextArea.requestFocus();
       //                     }
       //                 }
       //             }
       //         }
       //     }
       //     
       // });//}}}
        //}}}
        
        //{{{ Construct Edit Menu
        //TODO: get the keyboard shortcuts to work,
        //TODO: get cut/copy/paste to work in the right hand text window
        m_editMenu = new JMenu(Messages.getMessage("Edit.Menu"));
        m_editMenu.setMnemonic('E');
        JMenuItem menuItem = new JMenuItem(jsXe.getAction("treeview.cut.node"));
        m_editMenu.add(menuItem);
        menuItem = new JMenuItem(jsXe.getAction("treeview.copy.node"));
        m_editMenu.add(menuItem);
        menuItem = new JMenuItem(jsXe.getAction("treeview.paste.node"));
        m_editMenu.add(menuItem);
        //}}}
        
        //{{{ Create and set up the splitpanes
        vertSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeView, attrView);
        vertSplitPane.setContinuousLayout(false);
        vertSplitPane.setOneTouchExpandable(true);
        
        horizSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, vertSplitPane, htmlView);
        horizSplitPane.setContinuousLayout(false);
        horizSplitPane.setOneTouchExpandable(true);
        
        add(horizSplitPane, BorderLayout.CENTER);
        
        //}}}
        
        //focus on the tree the first time the view is shown
        addComponentListener(new ComponentListener() {//{{{
            
            public void componentHidden(ComponentEvent e) {}
            
            public void componentMoved(ComponentEvent e) {}
            
            public void componentResized(ComponentEvent e) {}
            
            public void componentShown(ComponentEvent e) {
                tree.requestFocus();
                Container parent = getParent();
                if (parent != null) {
                    Dimension size = parent.getSize();
                    float vertPercent = Integer.valueOf(m_document.getProperty(VERT_SPLIT_LOCATION)).floatValue();
                    float horizPercent = Integer.valueOf(m_document.getProperty(HORIZ_SPLIT_LOCATION)).floatValue();
                    
                    int vertLoc = (int)((vertPercent/100.0)*size.getHeight());
                    int horizLoc = (int)((horizPercent/100.0)*size.getWidth());
                    
                    vertSplitPane.setDividerLocation(vertLoc);
                    horizSplitPane.setDividerLocation(horizLoc);
                    m_viewShown = true;
                    removeComponentListener(this);
                }
            }
            
        });//}}}
        
        setDocumentBuffer(document);
    }//}}}
    
    //{{{ DocumentView methods

    //{{{ close()
    
    public boolean close() {
        
        //m_document should only be null if setDocumentBuffer was never called.
        if (m_document != null) {
            //only update the dimension settings if the view has been shown
            if (m_viewShown) {
                Dimension size = getSize();
            
                String vert = Integer.toString((int)(vertSplitPane.getDividerLocation()/size.getHeight()*100));
                String horiz = Integer.toString((int)(horizSplitPane.getDividerLocation()/size.getWidth()*100));
                
                m_document.setProperty(VERT_SPLIT_LOCATION,vert);
                m_document.setProperty(HORIZ_SPLIT_LOCATION,horiz);
                
                m_document.removeXMLDocumentListener(m_documentListener);
            }
        }
        
        return true;
    }//}}}

    //{{{ getDocumentViewComponent
    
    public Component getDocumentViewComponent() {
        return this;
    }//}}}

    //{{{ getViewPlugin()
    
    public ViewPlugin getViewPlugin() {
        return m_plugin;
    }//}}}
    
    //{{{ getMenus()
    
    public JMenu[] getMenus() {
        return new JMenu[] { m_editMenu };
    }//}}}
    
    //{{{ getDocumentBuffer()
    
    public DocumentBuffer getDocumentBuffer() {
        return m_document;
    }//}}}
    
    //{{{ setDocumentBuffer()
    
    public void setDocumentBuffer(DocumentBuffer document) throws IOException {
        
        try {
            document.checkWellFormedness();
        } catch (SAXException e) {
            String errormsg = "The tree view requires XML documents to be well-formed.\n\n"+
            e.toString();
            throw new IOException(errormsg);
        } catch (ParserConfigurationException e) {
            throw new IOException(e.toString());
        }
        
        ensureDefaultProps(document);
        
        AdapterNode adapter = document.getAdapterNode();
        
        DefaultViewTreeModel treeModel = new DefaultViewTreeModel(this, document);
        DefaultViewTableModel tableModel = new DefaultViewTableModel(this, adapter);
        DefaultViewDocument styledDoc = new DefaultViewDocument(adapter);
        
        //This adapter may have the listener already.
        //addTreeModelListener does not add the listener
        //again if it is already added.
        tree.setModel(treeModel);
        attributesTable.setModel(tableModel);
        treeModel.addTreeModelListener(treeListener);
        tableModel.addTableModelListener(tableListener);
        
        m_valueTextArea.setDocument(styledDoc);
        styledDoc.addDocumentListener(docListener);
        
        //get the splitpane layout options
        boolean layout = Boolean.valueOf(document.getProperty(CONTINUOUS_LAYOUT)).booleanValue();
        vertSplitPane.setContinuousLayout(layout);
        horizSplitPane.setContinuousLayout(layout);
        
        //update the UI so that the components
        //are redrawn.
        attributesTable.updateUI();
        tree.updateUI();
        updateUI();
        
        //Make root element node expanded.
       // TreePath path = new TreePath(new Object[] { , document.getRootElementNode() });
       // tree.expandPath(path);
        
        m_document = document;
        m_document.addXMLDocumentListener(m_documentListener);
    } //}}}
    
    //{{{ goToLine()
    public boolean goToLine(int lineNo) {
        //not supported in this view... yet.
        return false;
    }//}}}
    
    //}}}
    
    //{{{ getDefaultViewTree()
    /**
     * Gets the tree component for this DefaultView.
     * @return the tree component
     */
    public DefaultViewTree getDefaultViewTree() {
        return tree;
    }//}}}
    
    //{{{ getDefaultViewTextPane()
    /**
     * Gets the component used for editing node values
     * @return the editor pane
     */
    public JTextArea getDefaultViewTextPane() {
        return m_valueTextArea;
    }//}}}
    
    //{{{ getDefaultViewAttributeTable()
    /**
     * Gets the table component used to display attributes
     * for this DefaultView.
     * @return the table component
     */
    public JTable getDefaultViewAttributeTable() {
        return attributesTable;
    }//}}}
    
    //{{{ Private Members
    
    //{{{ canEditInJEditorPane()
    
    private boolean canEditInJEditorPane(AdapterNode node) {
        return (node.getNodeValue() != null);
    }//}}}
    
    //{{{ ensureDefaultProps()
    
    private void ensureDefaultProps(XMLDocument document) {
        //get default properties from jsXe
        document.setProperty(CONTINUOUS_LAYOUT, document.getProperty(CONTINUOUS_LAYOUT, m_defaultProperties.getProperty(CONTINUOUS_LAYOUT)));
        document.setProperty(HORIZ_SPLIT_LOCATION, document.getProperty(HORIZ_SPLIT_LOCATION, m_defaultProperties.getProperty(HORIZ_SPLIT_LOCATION)));
        document.setProperty(VERT_SPLIT_LOCATION, document.getProperty(VERT_SPLIT_LOCATION, m_defaultProperties.getProperty(VERT_SPLIT_LOCATION)));
        document.setProperty(SHOW_COMMENTS, document.getProperty(SHOW_COMMENTS, m_defaultProperties.getProperty(SHOW_COMMENTS)));
        document.setProperty(SHOW_ATTRIBUTES, document.getProperty(SHOW_ATTRIBUTES, m_defaultProperties.getProperty(SHOW_ATTRIBUTES)));
       // document.setProperty(SHOW_EMPTY_NODES, document.getProperty(SHOW_EMPTY_NODES, m_defaultProperties.getProperty(SHOW_EMPTY_NODES)));
    }//}}}
    
    //{{{ TablePopupListener class
    
    private class TablePopupListener extends MouseAdapter {
        
        //{{{ mousePressed()
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }//}}}

        //{{{ mouseReleased()
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }//}}}

        //{{{ maybeShowPopup
        
        private void maybeShowPopup(MouseEvent e) {
            Point point = new Point(e.getX(), e.getY());
            int row = attributesTable.rowAtPoint(point);
            int column = attributesTable.columnAtPoint(point);
            
            attributesTable.setColumnSelectionInterval(column, column);
            attributesTable.setRowSelectionInterval(row, row);
            
            if (e.isPopupTrigger() && row != -1) {
                
                DefaultViewTableModel model = (DefaultViewTableModel)attributesTable.getModel();
                JPopupMenu popup = new JPopupMenu();
                JMenuItem popupMenuItem;
                
                popupMenuItem = new JMenuItem("Add Attribute");
                popupMenuItem.addActionListener(jsXe.getAction("treeview.add.attribute"));
                popup.add(popupMenuItem);
                
                if (row != attributesTable.getRowCount()-1) {
                    popupMenuItem = new JMenuItem("Remove Attribute");
                    popupMenuItem.addActionListener(jsXe.getAction("treeview.remove.attribute"));
                    popup.add(popupMenuItem);
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }//}}}
        
    }//}}}

    //{{{ DefaultTreeSelectionListener class
    
    private class DefaultTreeSelectionListener implements TreeSelectionListener {
        
        //{{{ DefaultTreeSelectionListener constructor
        
        DefaultTreeSelectionListener(Component p) {
            parent=p;
        }//}}}
        
        //{{{ valueChanged()
        
        public void valueChanged(TreeSelectionEvent e) {
            TreePath selPath = e.getPath();
            AdapterNode selectedNode = (AdapterNode)selPath.getLastPathComponent();
            if ( selectedNode != null ) {
                
                //if the selected node can be edited in the text pane
                m_valueTextArea.setEditable(canEditInJEditorPane(selectedNode));
                
                //if the table is editing we want to cancel it.
                if (attributesTable.isEditing()) {
                    int row = attributesTable.getEditingRow();
                    int column = attributesTable.getEditingColumn();
                    attributesTable.getCellEditor(row, column).cancelCellEditing();
                }
                
                //update the attributes table with the current info.
                ((DefaultViewTableModel)attributesTable.getModel()).setAdapterNode(selectedNode);
                
                //update the text pane with the current info
                DefaultViewDocument styledDoc = new DefaultViewDocument(selectedNode);
                m_valueTextArea.setDocument(styledDoc);
                styledDoc.addDocumentListener(docListener);
                m_valueTextArea.updateUI();
                
            } else {
                m_valueTextArea.setDocument(new PlainDocument());
                m_valueTextArea.setEditable(false);
            }
        }//}}}
        
        //{{{ Private members
        private Component parent;
        //}}}
        
    }//}}}
    
    //{{{ DefaultViewTable class
    
    public class DefaultViewTable extends JTable {
    
        //{{{ DefaultViewTable constructor
        
        public DefaultViewTable() {
            super();
        }//}}}
        
        //{{{ editingStopped()
        
        public void editingStopped(ChangeEvent e) {
            int editRow = getEditingRow();
            int editColumn = getEditingColumn();
            
            super.editingStopped(e);
            
            if (editRow==getRowCount()-1) {
                //users can switch the columns around in the ui
                if (editColumn == 0) {
                    editColumn = 1;
                } else {
                    editColumn = 0;
                }
                editCellAt(editRow, editColumn);
            }
        }//}}}
        
        //{{{ getCellEditor() method
        public TableCellEditor getCellEditor(int row, int column) {
            Object value = getModel().getValueAt(row,column);
            if (value instanceof EditTagDialog.Attribute.Value) {
                return m_comboRenderer;
            }

            return super.getCellEditor(row,column);
        } //}}}

        //{{{ getCellRenderer() method
        public TableCellRenderer getCellRenderer(int row, int column) {
            Object value = getModel().getValueAt(row,column);
            if (value instanceof EditTagDialog.Attribute.Value) {
                return m_comboRenderer;
            }

            return super.getCellRenderer(row,column);
        } //}}}
        
    }//}}}
    
    private DefaultViewTree tree = new DefaultViewTree();
    private JTextArea m_valueTextArea = new JTextArea("");
    private DefaultViewTable attributesTable = new DefaultViewTable();
    private JSplitPane vertSplitPane;
    private JSplitPane horizSplitPane;
    private DocumentBuffer m_document;
    private boolean m_viewShown = false;
    private TreeViewPlugin m_plugin;
    private JMenu m_editMenu;
    
    private EditTagDialog.ComboValueRenderer m_comboRenderer = new EditTagDialog.ComboValueRenderer();
    
    private TableModelListener tableListener = new TableModelListener() {//{{{
        public void tableChanged(TableModelEvent e) {
           attributesTable.updateUI();
        }
    };//}}}
    private TreeModelListener treeListener = new TreeModelListener() {//{{{
        
        public void treeNodesChanged(TreeModelEvent e) {
           // updateTree();
        }
        
        //These aren't called yet.
        public void treeNodesInserted(TreeModelEvent e) {
           // updateComponents();
            tree.updateUI();
        }
        public void treeNodesRemoved(TreeModelEvent e) {
           // updateComponents();
            tree.updateUI();
        }
        public void treeStructureChanged(TreeModelEvent e) {
           // updateComponents();
            tree.updateUI();
        }
        
    };//}}}
    private DocumentListener docListener = new DocumentListener() {//{{{
        
        public void changedUpdate(DocumentEvent e) {
           // updateTree();
        }
        public void insertUpdate(DocumentEvent e) {
           // updateTree();
        }
        public void removeUpdate(DocumentEvent e) {
           // updateTree();
        };
        
    };//}}}
    private XMLDocumentListener m_documentListener = new XMLDocumentListener() {///{{{
        
        //{{{ propertiesChanged
        
        public void propertyChanged(XMLDocument source, String key, String oldValue) {
            if (CONTINUOUS_LAYOUT.equals(key)) {
                boolean layout = Boolean.valueOf(source.getProperty(CONTINUOUS_LAYOUT)).booleanValue();
                vertSplitPane.setContinuousLayout(layout);
                horizSplitPane.setContinuousLayout(layout);
            }
            if (CONTINUOUS_LAYOUT.equals(key) || SHOW_COMMENTS.equals(key) || SHOW_ATTRIBUTES.equals(key)) {
                tree.updateUI();
            }
        }//}}}
        
        //{{{ structureChanged()
        
        public void structureChanged(XMLDocument source, AdapterNode location) {
            /*
            need to reload since saving can change the structure,
            like when splitting cdata sections
            */
            tree.updateUI();
            //TODO: update the attributes table to handle this
            ((DefaultViewTableModel)attributesTable.getModel()).updateTable();
            attributesTable.updateUI();
        }//}}}
        
    };//}}}

    //}}}

}
