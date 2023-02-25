/*
DefaultViewTree.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

jsXe is the Java Simple XML Editorh
jsXe is a gui application that can edit an XML document and create a tree view.
The user can then edit this tree and the content in the tree and save the
document.

This file contains the tree class that is used in the default view.

This file written by Ian Lewis (IanLewis@member.fsf.org)
Copyright (C) 2002 Ian Lewis

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

import treeview.action.AddNodeAction;

//{{{ jsXe classes
import net.sourceforge.jsxe.jsXe;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.gui.EnhancedMenu;
import net.sourceforge.jsxe.dom.*;
import net.sourceforge.jsxe.dom.completion.*;
import net.sourceforge.jsxe.util.Log;
//}}}

//{{{ Swing components
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
//}}}

//{{{ AWT components
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
//}}}

//{{{ DOM classes
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
//}}}

//{{{ Java base classes
import java.io.IOException;
import java.util.*;
//}}}

//}}}

/**
 * The DefaultViewTree is the tree that is displayed in the upper-left of
 * the DefaultView in jsXe. This class defines methods specific to the tree
 * display.
 *
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: DefaultViewTree.java,v 1.25 2006/02/16 23:13:03 ian_lewis Exp $
 * @see DefaultView
 */
public class DefaultViewTree extends JTree implements Autoscroll, ClipboardOwner {
    
    //{{{ Properties
    private final String NODE_EXPANDED = "tree.expandedstate";
    //}}}
    
    //{{{ DefaultViewTree constructor
    /**
     * Creates a new DefaultViewTree with the default TreeModel
     */
    public DefaultViewTree() {
        
        //{{{ intitalize Drag n Drop
        m_dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, m_treeDGListener);
        m_dropTarget = new DropTarget(this, m_acceptableActions, m_treeDTListener, true);
        //}}}
        
        addMouseListener(new TreePopupListener());
        setEditable(false);
        addTreeExpansionListener(new TreeExpansionListener() {//{{{
            
            //{{{ treeExpanded()
            
            public void treeExpanded(TreeExpansionEvent event) {
                try {
                    AdapterNode node = (AdapterNode)event.getPath().getLastPathComponent();
                    node.setProperty(NODE_EXPANDED, "true");
                } catch (ClassCastException e) {}
            }//}}}
            
            //{{{ treeCollapsed()
            
            public void treeCollapsed(TreeExpansionEvent event) {
                try {
                    AdapterNode node = (AdapterNode)event.getPath().getLastPathComponent();
                    node.setProperty(NODE_EXPANDED, "false");
                } catch (ClassCastException e) {}
            }//}}}
            
        });//}}}
        
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        DefaultViewTreeCellRenderer renderer = new DefaultViewTreeCellRenderer();
        setCellRenderer(renderer);
        
        //Since elements are the only editable nodes...
        renderer = new DefaultViewTreeCellRenderer();
        setCellEditor(new DefaultTreeCellEditor(this, renderer, new ElementCellEditor(this, renderer)));
        
        
        ToolTipManager.sharedInstance().registerComponent(this);
        
    }//}}}
    
    //{{{ isEditable()
    /**
     * Indicates if a node is capable of being edited in
     * this tree.
     * @param node the node to check
     * @return true if the node can be edited in this tree
     */
    public boolean isEditable(AdapterNode node) {
        if (node != null) {
            int nodeType = node.getNodeType();
            return (nodeType == Node.ELEMENT_NODE || nodeType == Node.PROCESSING_INSTRUCTION_NODE);
        } else {
            return false;
        }
    }//}}}
    
    //{{{ isExpanded()
    
    public boolean isExpanded(AdapterNode node) {
        return Boolean.valueOf(node.getProperty(NODE_EXPANDED)).booleanValue();
    }//}}}
    
    //{{{ startEditingAtPath()
    
    public void startEditingAtPath(TreePath path) {
        if (path != null && isEditable((AdapterNode)path.getLastPathComponent())) {
            //When editing is finished go back to uneditable
            getCellEditor().addCellEditorListener(new CellEditorListener() {//{{{
                public void editingCanceled(ChangeEvent e) {
                    setEditable(false);
                    getCellEditor().removeCellEditorListener(this);
                }
                public void editingStopped(ChangeEvent e) {
                    setEditable(false);
                    getCellEditor().removeCellEditorListener(this);
                }
            });//}}}
            setEditable(true);
            super.startEditingAtPath(path);
        }
    }//}}}
    
    //{{{ getSelectedNode()
    /**
     * Gets the current node that is selected for editing.
     * @return the current selected node or null if no node is selected.
     */
    public AdapterNode getSelectedNode() {
        TreePath selPath = getLeadSelectionPath();
        AdapterNode selectedNode = null;
        if (selPath != null) {
            selectedNode = (AdapterNode)selPath.getLastPathComponent();
        }
        return selectedNode;
    }//}}}
    
    //{{{ cut()
    /**
     * Cuts the currently selected node out of the tree and places it in the
     * clipboard.
     * @return true if the node was successfully cut
     * @throws DOMException if the node cannot be removed from the tree.
     */
    public boolean cut() throws DOMException {
        AdapterNode selectedNode = getSelectedNode();
        Clipboard clipBoard = getToolkit().getSystemClipboard();
        if (selectedNode != null) {
            try {
                clipBoard.setContents(new TransferableNode(selectedNode), this);
                selectedNode.getParentNode().remove(selectedNode);
                updateUI();
                return true;
            } catch (IllegalStateException e) {
                Log.log(Log.ERROR, this, e);
            } catch (HeadlessException e) {
                Log.log(Log.ERROR, this, e);
            } catch (DOMException e) {
                clipBoard.setContents(null, this);
                throw e;
            }
        }
        return false;
    }//}}}
    
    //{{{ copy()
    /**
     * Copies the currently selected node to the clipboard.
     * @return true if the node was copied successfully
     */
    public boolean copy() {
        AdapterNode selectedNode = getSelectedNode();
        if (selectedNode != null) {
            try {
                Clipboard clipBoard = getToolkit().getSystemClipboard();
                AdapterNode newNode = selectedNode.copy(true);
                clipBoard.setContents(new TransferableNode(newNode), this);
                return true;
            } catch (IllegalStateException e) {
                Log.log(Log.ERROR, this, e);
            } catch (HeadlessException e) {
                Log.log(Log.ERROR, this, e);
            }
        }
        return false;
    }//}}}
    
    //{{{ paste()
    /**
     * Pastes the contents of the clipboard into the currently selected node
     * in the document tree.
     * @return true if the node was pasted successfully
     * @throws DOMException if the node cannot be inserted in the current
     *                      location.
     */
    public boolean paste() throws DOMException {
        AdapterNode selectedNode = getSelectedNode();
        if (selectedNode != null) {
            try {
                Clipboard clipBoard = getToolkit().getSystemClipboard();
                Transferable contents = clipBoard.getContents(this);
                if (contents != null) {
                    if (contents.isDataFlavorSupported(TransferableNode.nodeFlavor)) {
                        AdapterNode node = (AdapterNode)contents.getTransferData(TransferableNode.nodeFlavor);
                        TreePath pastePath = getLeadSelectionPath();
                        selectedNode.addAdapterNode(node);
                        
                        //reset the clipboard
                        StringSelection sel = new StringSelection("");
                        clipBoard.setContents(sel, sel);
                        
                        refreshExpandedStates(pastePath);
                        addSelectionPath(pastePath.pathByAddingChild(node));
                        updateUI();
                        return true;
                    }
                }
            } catch (IllegalStateException e) {
                Log.log(Log.ERROR, this, e);
            } catch (HeadlessException e) {
                Log.log(Log.ERROR, this, e);
            } catch (UnsupportedFlavorException e) {
                Log.log(Log.ERROR, this, e);
            } catch (IOException e) {
                Log.log(Log.ERROR, this, e);
            }
        }
        return false;
    }//}}}
    
    //{{{ Autoscroll methods
    
    //{{{ autoscroll()
    
    public void autoscroll(Point cursorLocn) {
        int row = getClosestRowForLocation(cursorLocn.x, cursorLocn.y);
        
        if (row < 0) {
            return;
        }
        
        Rectangle bounds = getBounds();
        
        if (cursorLocn.y + bounds.y <= m_AUTOSCROLL_MARGIN) {
            if (row <= 0) {
                row = 0;
            } else {
                row -=1;
            }
        } else {
            if (row < getRowCount() - 1) {
                row += 1;
            }
        }
        scrollRowToVisible(row);
    }//}}}
    
    //{{{ getAutoscrollInsets()
    
    public Insets getAutoscrollInsets() {
        
        Rectangle outerBounds = getBounds();
        Rectangle innerBounds = getParent().getBounds();
        int top = innerBounds.y - outerBounds.y + m_AUTOSCROLL_MARGIN;
        int left = innerBounds.x - outerBounds.x + m_AUTOSCROLL_MARGIN;
        int bottom = outerBounds.height - innerBounds.height - innerBounds.y + outerBounds.y + m_AUTOSCROLL_MARGIN;
        int right = outerBounds.width - innerBounds.width - innerBounds.x + outerBounds.y + m_AUTOSCROLL_MARGIN;
        return new Insets(top, left, bottom, right);
    }//}}}
    
    //}}}
    
    //{{{ ClipboardOwner methods
    
    //{{{ lostOwnership()
    
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // Nothing right now.
    }//}}}
    
    //}}}
    
    //{{{ Private static members
    private static final ImageIcon m_elementIcon = new ImageIcon(DefaultView.class.getResource("/net/sourceforge/jsxe/icons/Element.png"), "Element");
    private static final ImageIcon m_textIcon = new ImageIcon(DefaultView.class.getResource("/net/sourceforge/jsxe/icons/Text.png"), "Text");
    private static final ImageIcon m_CDATAIcon = new ImageIcon(DefaultView.class.getResource("/net/sourceforge/jsxe/icons/CDATA.png"), "CDATA");
    private static final ImageIcon m_commentIcon = new ImageIcon(DefaultView.class.getResource("/net/sourceforge/jsxe/icons/Comment.png"), "Comment");
   // private static final ImageIcon m_externalEntityIcon = new ImageIcon(DefaultView.class.getResource("/net/sourceforge/jsxe/icons/ExternalEntity.png"), "External Entity");
    private static final ImageIcon m_internalEntityIcon = new ImageIcon(DefaultView.class.getResource("/net/sourceforge/jsxe/icons/InternalEntity.png"), "Internal Entity");
    
    private static final int m_AUTOSCROLL_MARGIN = 12;
    //}}}
    
    //{{{ Private members
    
    //{{{ refreshExpandedStates()
    /**
     * Refreshes the expanded states of all the node pointed to by
     * the treepath and all nodes below it. Used after a drag and
     * drop is done because the JTree uses TreePaths to keep track
     * of expanded states. When a drag and drop is done the
     * path is broken and the expanded states are lost.
     */
    private void refreshExpandedStates(TreePath path) {
        AdapterNode node = (AdapterNode)path.getLastPathComponent();
        boolean expandedState = isExpanded(node);
        if (node.childCount() > 0) {
            expandPath(path); //expand all nodes out
            //still have to set expanded states
            int children = node.childCount();
            for (int i=0;i < children; i++) {
                TreePath newPath = path.pathByAddingChild(node.child(i));
                refreshExpandedStates(newPath);
            }
            if (expandedState) { //close non-expanded nodes
                expandPath(path);
            } else {
                collapsePath(path);
            }
        }
    }//}}}
    
    //{{{ toString()
    /**
     * Creates the string that will be displayed in the tree node
     * @param showattrs "ID only", "All" or "None"
     */
    private static String toString(AdapterNode node) {
        StringBuffer s = new StringBuffer();
        if (node.getNodeType() == Node.DOCUMENT_NODE)
            return "Document Root";
        String nodeName = node.getNodeName();
        if (! nodeName.startsWith("#")) {   
            s.append(nodeName);
            if (node.getNodeType() == AdapterNode.ELEMENT_NODE) {
                NamedNodeMap attributes = node.getAttributes();
                ElementDecl decl = node.getElementDecl();
                XMLDocument document = node.getOwnerDocument();
                String showAttrs = document.getProperty(DefaultView.SHOW_ATTRIBUTES);
                for (int i=0; i<attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    ElementDecl.AttributeDecl attrDecl = (decl != null) ? decl.getAttribute(attr.getNodeName()) : null;
                    if (showAttrs.equals("All") ||
                    (showAttrs.equals("ID only") && 
                        ((attr.getNodeName().equalsIgnoreCase("id") || 
                        (attrDecl != null && attrDecl.type.equals("ID"))))))
                    {
                        s.append(' '+attr.getNodeName() + "=\"" + attr.getNodeValue() + '"');
                    }
                }
            }
        }
        if (s.length()==0) {
            if (node.getNodeValue() != null) {
                String t = node.getNodeValue().trim();
                int x = t.indexOf("\n");
                if (x >= 0) {
                    t = t.substring(0, x);
                }
                if (t.length() > 50) {
                    t = t.substring(0, 50) + "...";
                }
                s.append(t);
            }
        }
        return s.toString();
    }//}}}
    
    //{{{ TreePopupListener class
    //TODO: rework this class
    private class TreePopupListener extends MouseAdapter {
        
        //{{{ mousePressed()
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }//}}}
        
        //{{{ mouseReleased()
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }//}}}
        
        //{{{ maybeShowPopup()
        
        private void maybeShowPopup(MouseEvent e) {
            TreePath selPath = getPathForLocation(e.getX(), e.getY());
            if (e.isPopupTrigger() && selPath != null) {
                setSelectionPath(selPath);
                
                //Don't want to interact with AdapterNodes too much. Maybe change this.
                AdapterNode selectedNode = ((AdapterNode)selPath.getLastPathComponent());
                XMLDocument ownerDocument = selectedNode.getOwnerDocument();
                
                JMenuItem popupMenuItem;
                JMenu addNodeItem = new JMenu(Messages.getMessage("common.add"));
                JPopupMenu popup = new JPopupMenu();
                boolean showpopup = false;
                boolean addNodeShown = false;
                
                if (selectedNode.getNodeType() == Node.ELEMENT_NODE) {
                    
                    JMenu addElement = new EnhancedMenu(Messages.getMessage("xml.element"), 20);
                    addNodeItem.add(addElement);
                    
                    addElement.add(jsXe.getAction("treeview.add.element.node"));
                    Iterator allowedElements = selectedNode.getAllowedElements().iterator();
                    while (allowedElements.hasNext()) {
                        ElementDecl decl = (ElementDecl)allowedElements.next();
                        addElement.add(new AddNodeAction(decl, decl.name));
                    }
                    
                    //Add the allowed entities even if no matter what
                    
                    JMenu addEntity = new EnhancedMenu(Messages.getMessage("xml.entity.reference"), 20);
                    addNodeItem.add(addEntity);
                    
                    Iterator allowedEntities = ownerDocument.getAllowedEntities().iterator();
                    while (allowedEntities.hasNext()) {
                        EntityDecl decl = (EntityDecl)allowedEntities.next();
                        popupMenuItem = new JMenuItem(new AddNodeAction(decl.name, decl.name, decl.value, AdapterNode.ENTITY_REFERENCE_NODE));
                        addEntity.add(popupMenuItem);
                    }
                    
                    popupMenuItem = new JMenuItem(jsXe.getAction("treeview.add.text.node"));
                    popupMenuItem.setText(Messages.getMessage("xml.text"));
                    addNodeItem.add(popupMenuItem);
                    popupMenuItem = new JMenuItem(jsXe.getAction("treeview.add.cdata.node"));
                    popupMenuItem.setText(Messages.getMessage("xml.cdata"));
                    addNodeItem.add(popupMenuItem);
                    
                   // popupMenuItem = new JMenuItem(new AddNodeAction("New_Entity", "", Node.ENTITY_REFERENCE_NODE));
                   // addNodeItem.add(popupMenuItem);
                    addNodeShown = true;
                    showpopup = true;
                }
                
                if (selectedNode.getNodeType() == Node.DOCUMENT_NODE) {
                    if (ownerDocument.getDocType() == null) {
                        popupMenuItem = new JMenuItem(jsXe.getAction("treeview.add.doctype.node"));
                        popupMenuItem.setText(Messages.getMessage("xml.doctypedef"));
                        addNodeItem.add(popupMenuItem);
                        showpopup = true;
                    }
                }
                
                if (selectedNode.getNodeType() == Node.DOCUMENT_NODE || selectedNode.getNodeType() == Node.ELEMENT_NODE) {
                    popupMenuItem = new JMenuItem(jsXe.getAction("treeview.add.processing.instruction.node"));
                    popupMenuItem.setText(Messages.getMessage("xml.processing.instruction"));
                    addNodeItem.add(popupMenuItem);
                    popupMenuItem = new JMenuItem(jsXe.getAction("treeview.add.comment.node"));
                    popupMenuItem.setText(Messages.getMessage("xml.comment"));
                    addNodeItem.add(popupMenuItem);
                    addNodeShown = true;
                    showpopup = true;
                }
                
                if (addNodeShown) {
                    popup.add(addNodeItem);
                }
                
                //Add the edit node action
                if (selectedNode.getNodeType() == Node.ELEMENT_NODE && ownerDocument.getElementDecl(selectedNode.getNodeName()) != null) {
                    popupMenuItem = new JMenuItem(jsXe.getAction("treeview.edit.node"));
                    popup.add(popupMenuItem);
                    showpopup = true;
                }
                
                if (selectedNode.getNodeType() == Node.ELEMENT_NODE || selectedNode.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                    popupMenuItem = new JMenuItem(jsXe.getAction("treeview.rename.node"));
                    popup.add(popupMenuItem);
                    showpopup = true;
                }
                //if the node is not the document, document type, or the document root.
                if (selectedNode.getNodeType() != Node.DOCUMENT_NODE && 
                    selectedNode.getNodeType() != Node.DOCUMENT_TYPE_NODE &&
                    !(selectedNode.getNodeType() == Node.ELEMENT_NODE &&
                    selectedNode.getParentNode().getNodeType() == Node.DOCUMENT_NODE))
                {
                    popupMenuItem = new JMenuItem(jsXe.getAction("treeview.remove.node"));
                    popup.add(popupMenuItem);
                    showpopup = true;
                }
                
                if (selectedNode.getNodeType() != Node.DOCUMENT_NODE && 
                    selectedNode.getNodeType() != Node.DOCUMENT_TYPE_NODE)
                {
                    popup.addSeparator();
                    if(!(selectedNode.getNodeType() == Node.ELEMENT_NODE &&
                       selectedNode.getParentNode().getNodeType() == Node.DOCUMENT_NODE))
                    {
                        popupMenuItem = new JMenuItem(jsXe.getAction("treeview.cut.node"));
                        popup.add(popupMenuItem);
                    }
                    
                    popupMenuItem = new JMenuItem(jsXe.getAction("treeview.copy.node"));
                    popup.add(popupMenuItem);
                    
                    if (selectedNode.getNodeType() == Node.ELEMENT_NODE) {
                        popupMenuItem = new JMenuItem(jsXe.getAction("treeview.paste.node"));
                        popup.add(popupMenuItem);
                    }
                    showpopup = true;
                }
                
                if (showpopup) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }//}}}
    
    }//}}}

    //{{{ DefaultViewTreeCellRenderer class
    
    private class DefaultViewTreeCellRenderer extends DefaultTreeCellRenderer {
        
        //{{{ DefaultViewTreeCellRenderer constructor
        
        DefaultViewTreeCellRenderer() {
            m_defaultLeafIcon = getLeafIcon();
            m_defaultOpenIcon = getOpenIcon();
            m_defaultClosedIcon = getClosedIcon();
            m_defaultBackgroundSelectionColor = this.backgroundSelectionColor;
        }//}}}
        
        //{{{ getTreeCellRendererComponent()
        
        public Component getTreeCellRendererComponent(JTree tree, 
            Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus)
        {
            
            int type = -1;
            try {
                AdapterNode node = (AdapterNode)value;
                type = node.getNodeType();
                setText(DefaultViewTree.toString(node));
            } catch (ClassCastException e) {}
            
            this.selected = selected;
            if (value != null && m_dragOverTarget == value) {
                this.selected = true;
                backgroundSelectionColor = m_dragSelectionColor;
            } else {
                backgroundSelectionColor = m_defaultBackgroundSelectionColor;
            }
            
            switch (type) {
                case Node.ELEMENT_NODE:
                    setIcon(m_elementIcon);
                    setLeafIcon(m_elementIcon);
                    setOpenIcon(m_elementIcon);
                    setClosedIcon(m_elementIcon);
                    setToolTipText(Messages.getMessage("xml.element"));
                    break;
                case Node.TEXT_NODE:
                    setIcon(m_textIcon);
                    setLeafIcon(m_textIcon);
                    setOpenIcon(m_textIcon);
                    setClosedIcon(m_textIcon);
                    setToolTipText(Messages.getMessage("xml.text"));
                    break;
                case Node.CDATA_SECTION_NODE:
                    setIcon(m_CDATAIcon);
                    setLeafIcon(m_CDATAIcon);
                    setOpenIcon(m_CDATAIcon);
                    setClosedIcon(m_CDATAIcon);
                    setToolTipText(Messages.getMessage("xml.cdata"));
                    break;
                case Node.COMMENT_NODE:
                    setIcon(m_commentIcon);
                    setLeafIcon(m_commentIcon);
                    setOpenIcon(m_commentIcon);
                    setClosedIcon(m_commentIcon);
                    setToolTipText(Messages.getMessage("xml.comment"));
                    break;
                case Node.ENTITY_REFERENCE_NODE:
                    setIcon(m_internalEntityIcon);
                    setLeafIcon(m_internalEntityIcon);
                    setOpenIcon(m_internalEntityIcon);
                    setClosedIcon(m_internalEntityIcon);
                    setToolTipText(Messages.getMessage("xml.entity.reference"));
                    break;
                case Node.DOCUMENT_NODE:
                    setIcon(m_defaultClosedIcon);
                    setLeafIcon(m_defaultLeafIcon);
                    setOpenIcon(m_defaultOpenIcon);
                    setClosedIcon(m_defaultClosedIcon);
                    setToolTipText(Messages.getMessage("xml.document"));
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    setIcon(m_defaultLeafIcon);
                    setLeafIcon(m_defaultLeafIcon);
                    setOpenIcon(m_defaultLeafIcon);
                    setClosedIcon(m_defaultLeafIcon);
                    setToolTipText(Messages.getMessage("xml.processing.instruction"));
                    break;
                case Node.DOCUMENT_TYPE_NODE:
                    setIcon(m_defaultLeafIcon);
                    setLeafIcon(m_defaultLeafIcon);
                    setOpenIcon(m_defaultLeafIcon);
                    setClosedIcon(m_defaultLeafIcon);
                    setToolTipText(Messages.getMessage("xml.doctypedef"));
                    break;
                default:
                    if (leaf) {
                        setIcon(m_defaultLeafIcon);
                    } else {
                        if (expanded) {
                            setIcon(m_defaultOpenIcon);
                        } else {
                            setIcon(m_defaultClosedIcon);
                        }
                    }
                    
                    setLeafIcon(m_defaultLeafIcon);
                    setOpenIcon(m_defaultOpenIcon);
                    setClosedIcon(m_defaultClosedIcon);
                    setToolTipText("Unknown node");
                    break;
            }
            
            return this;
        }//}}}
        
        //{{{ Private members
        
        private Icon m_defaultLeafIcon;
        private Icon m_defaultOpenIcon;
        private Icon m_defaultClosedIcon;
        private Color m_defaultBackgroundSelectionColor;
        //}}}
        
    }//}}}
    
    //{{{ ElementTreeCellRenderer class
    
    private class ElementTreeCellRenderer extends DefaultTreeCellRenderer {
        
        //{{{ ElementTreeCellRenderer constructor
        
        public ElementTreeCellRenderer() {
            m_defaultLeafIcon = getLeafIcon();
        }//}}}
        
        //{{{ getTreeCellRendererComponent
        
        public Component getTreeCellRendererComponent(JTree tree, 
            Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus)
        {
            AdapterNode node = (AdapterNode)value;
            
            // We can rename processing instruction nodes here too.
            if (node.getNodeType() == AdapterNode.PROCESSING_INSTRUCTION_NODE) {
                setIcon(m_defaultLeafIcon);
                setLeafIcon(m_defaultLeafIcon);
                setOpenIcon(m_defaultLeafIcon);
                setClosedIcon(m_defaultLeafIcon);
                setToolTipText(Messages.getMessage("xml.processing.instruction"));
            } else {
                setIcon(m_elementIcon);
                setLeafIcon(m_elementIcon);
                setOpenIcon(m_elementIcon);
                setClosedIcon(m_elementIcon);
                setToolTipText(Messages.getMessage("xml.element"));
            }

            //just use the node name, we don't want attributes and such.
            setText(((AdapterNode)value).getNodeName());
            return this;
            
        }//}}}
        
        private Icon m_defaultLeafIcon;
        
    }//}}}

    //{{{ ElementCellEditor class
    
    public class ElementCellEditor extends DefaultTreeCellEditor {
        
        //{{{ ElementCellEditor constructor
        
        public ElementCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
            super(tree, renderer);
        }//}}}
        
        //{{{ prepareForEditing()
        
        protected void prepareForEditing() {
            //HACK
            //Use prepareForEditing to initialize the renderer
            TreePath path = getLeadSelectionPath();
            JTree tree = DefaultViewTree.this;
            Object value = getLastSelectedPathComponent();
            boolean isSelected = isPathSelected(path);
            boolean expanded = isExpanded(path);
            boolean leaf = getModel().isLeaf(value);
            int row = getLeadSelectionRow();
            renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, true);
            super.prepareForEditing();
        }//}}}
        
        //{{{ getTreeCellEditorComponent
        
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            /*
            This code depends on the implementation of DefaultTreeCellEditor
            a little.
            */
            
            //DefaultTreeCellEditor.EditorContainer
            Container container = (Container)super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
            JTextField field = (JTextField)container.getComponent(0);
            
            // We know this is an element so just use the node name.
            field.setText(((AdapterNode)value).getNodeName());
            return field;
        }//}}}

    }//}}}

    //{{{ Drag n Drop classes
    
    //{{{ TreeDragGestureListener class
    
    private class TreeDragGestureListener implements DragGestureListener {
        
        //{{{ dragGestureRecognized()
        
        public void dragGestureRecognized(DragGestureEvent dge) {
            try {
                Point origin = dge.getDragOrigin();
                TreePath path = getPathForLocation(origin.x, origin.y);
                //ignore dragging the Document root.
                if (path != null && !(isRootVisible() && getRowForPath(path) == 0)) {
                    AdapterNode node = (AdapterNode)path.getLastPathComponent();
                    Transferable transferable = new TransferableNode(node);
                    m_dragSource.startDrag(dge, DragSource.DefaultCopyNoDrop, transferable, m_treeDSListener);
                }
            } catch( InvalidDnDOperationException idoe) {}
        }//}}}
        
    }//}}}
    
    //{{{ DefaultViewDragSourceListener class
    
    private class DefaultViewDragSourceListener implements DragSourceListener {
        
        //{{{ dragEnter()
        
        public void dragEnter(DragSourceDragEvent dsde) {
            DragSourceContext context = dsde.getDragSourceContext();
            
            int myaction = dsde.getDropAction();
            if ((myaction & DnDConstants.ACTION_MOVE) != 0) {
                context.setCursor(DragSource.DefaultMoveDrop);   
            } else {
                context.setCursor(DragSource.DefaultMoveNoDrop);
            }
        }//}}}
        
        //{{{ dragDropEnd()
        
        public void dragDropEnd(DragSourceDropEvent dsde) {
            //paint over the cue line no matter what.
            paintImmediately(m_cueLine);
            m_dragOverTarget = null;
            if ( dsde.getDropSuccess() == false ) {
                return;
            }
            
            int dropAction = dsde.getDropAction();
            if ( dropAction == DnDConstants.ACTION_MOVE ) {
                //***** Do stuff *****
            }
            
        }//}}}
        
        //{{{ dragExit()
        
        public void dragExit(DragSourceEvent dse) {
            paintImmediately(m_cueLine);
            DragSourceContext context = dse.getDragSourceContext();
            context.setCursor(DragSource.DefaultMoveNoDrop);
            m_dragOverTarget = null;
        }//}}}
        
        //{{{ dragOver()
        
        public void dragOver(DragSourceDragEvent dsde) {
            
        }//}}}
        
        //{{{ dropActionChanged()
        
        public void dropActionChanged(DragSourceDragEvent dsde) {
            
        }//}}}
        
    }//}}}
    
    //{{{ DefaultViewDropTargetListener class
    
    private class DefaultViewDropTargetListener implements DropTargetListener {
        
        //{{{ DefaultViewDropTargetListener constructor
        
        public DefaultViewDropTargetListener() {
            m_m_timerHover = new javax.swing.Timer(m_HOVER_TIME_BEFORE_EXPAND, new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (isRootVisible() && getRowForPath(m_m_lastPath) == 0) {
						return;	// Do nothing if we are hovering over the root node
					}
                    if (isExpanded(m_m_lastPath)) {
						collapsePath(m_m_lastPath);
                    } else {
						expandPath(m_m_lastPath);
                    }
				}
			});
			m_m_timerHover.setRepeats(false);	// Set timer to one-shot mode
        }//}}}
        
        //{{{ dragEnter()
        
        public void dragEnter(DropTargetDragEvent dtde) {
            if (!isDragOk(dtde)) {
                dtde.rejectDrag();
                return;
            }
            dtde.acceptDrag(dtde.getDropAction());
        }//}}}
        
        //{{{ drop()
        
        public void drop(DropTargetDropEvent dtde) {
            m_m_timerHover.stop(); // prevent timer from collapsing/expanding node
            
            if (!dtde.isDataFlavorSupported(TransferableNode.nodeFlavor)) {
                dtde.rejectDrop();
                return;
            }
            
            if ((dtde.getSourceActions() & m_acceptableActions ) == 0 ) {
                dtde.rejectDrop();
                return;
            }
            
            //We only support the nodeFlavor. Always chose that
            DataFlavor chosen = TransferableNode.nodeFlavor;
            
            Object data = null;
            try {
                data = dtde.getTransferable().getTransferData(chosen);
            } catch (UnsupportedFlavorException ufe) {}
              catch (IOException ioe) {}
            
            if (data == null)
                throw new NullPointerException();
            
            AdapterNode node = (AdapterNode)data;
            Point loc = dtde.getLocation();
            
            TreePath path = getClosestPathForLocation(loc.x, loc.y);
            if (path == null) {
                dtde.rejectDrop();
                return;
            }
            
            AdapterNode parentNode = (AdapterNode)path.getLastPathComponent();
            TreePath droppedPath;
            try {
                //Find out the relative location where I dropped.
                Rectangle bounds = getPathBounds(path);
                if (loc.y < bounds.y + (int)(bounds.height * 0.25)) {
                    //Insert before the node dropped on
                    if (parentNode != null) {
                        AdapterNode trueParent = (AdapterNode)parentNode.getParentNode();
                        if (trueParent != null) {
                            trueParent.addAdapterNodeAt(node, trueParent.index(parentNode));
                            makeVisible(path);
                            droppedPath = path.getParentPath();
                        } else {
                            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "HIERARCHY_REQUEST_ERR: An attempt was made to insert a node where it is not permitted");
                        }
                    } else {
                        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "HIERARCHY_REQUEST_ERR: An attempt was made to insert a node where it is not permitted");
                    }
                } else {
                    if (loc.y < bounds.y + (int)(bounds.height * 0.75)) {
                        
                        //insert in the node inside the parent at the end of its children
                        parentNode.addAdapterNode(node);
                        droppedPath = path;
                        //Make sure the node we just dropped is viewable
                        if (isCollapsed(path)) {
                            expandPath(path);
                        }
                        
                    } else {
                        if (parentNode != null) {
                            //insert after the node dropped on
                            AdapterNode trueParent = (AdapterNode)parentNode.getParentNode();
                            if (trueParent != null) {
                                trueParent.addAdapterNodeAt(node, trueParent.index(parentNode)+1);
                                droppedPath = path.getParentPath();
                                makeVisible(path);
                            } else {
                                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "HIERARCHY_REQUEST_ERR: An attempt was made to insert a node where it is not permitted");
                            }
                        } else {
                            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "HIERARCHY_REQUEST_ERR: An attempt was made to insert a node where it is not permitted");
                        }
                    }
                }
                refreshExpandedStates(droppedPath);
                /*
                need to make sure that the new path of the dropped node
                is in the selection model
                */
                addSelectionPath(droppedPath.pathByAddingChild(node));
                dtde.acceptDrop(m_acceptableActions);
            } catch (DOMException dome) {
                dtde.rejectDrop();
                JOptionPane.showMessageDialog(DefaultViewTree.this, dome, "XML Error", JOptionPane.WARNING_MESSAGE);
            }
            m_dragOverTarget = null;
            paintImmediately(m_cueLine);
            dtde.dropComplete(true);
            updateUI();
        }//}}}
        
        //{{{ dragOver()
        
        public void dragOver(DropTargetDragEvent dtde) {
            if (!isDragOk(dtde)) {
                dtde.rejectDrag();      
                return;
            }
            
            Point loc = dtde.getLocation();
           // TreePath path = getPathForLocation(loc.x, loc.y);
            TreePath path = getClosestPathForLocation(loc.x, loc.y);
            if (path != m_m_lastPath) {
                m_m_lastPath = path;
                m_m_timerHover.restart();
            }
            m_dragOverTarget = null;
            paintImmediately(m_cueLine);
            if (path != null) {
                Rectangle bounds = getPathBounds(path);
                //erase old cue line
                Graphics g = getGraphics();
                
                int x = bounds.x;
                int y = bounds.y;
                int width = bounds.width;
                int height = 2;
                
                g.setColor(m_dragSelectionColor);
                
                if (loc.y < bounds.y + (int)(bounds.height * 0.25)) {
                    //no change
                    g.fillRect(x, y, width, height);
                } else {
                    if (loc.y < bounds.y + (int)(bounds.height * 0.75)) {
                        //don't do anything right now
                        //Want to highlight the node we're dragging over
                        //in the future.
                       // x = 0;
                       // y = 0;
                       // width = 0;
                       // height = 0;
                        height=bounds.height;
                        /*
                        set the node that is being dragged over so that
                        the cell renderer knows that you are dragging over
                        */
                        m_dragOverTarget = path.getLastPathComponent();
                        paintImmediately(x,y,width,height);
                    } else {
                        y += bounds.height;
                        g.fillRect(x, y, width, height);
                    }
                }
                m_cueLine.setRect(x,y,width,height);
            }
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);      
        }//}}}
        
        //{{{ dropActionChanged()
        
        public void dropActionChanged(DropTargetDragEvent dtde) {
            if(isDragOk(dtde) == false) {
                dtde.rejectDrag();
                return;
            }
            dtde.acceptDrag(dtde.getDropAction());
        }//}}}
        
        //{{{ dragExit()
        
        public void dragExit(DropTargetEvent dte) {
            //Set the node that is dragged over to null
            m_dragOverTarget = null;
        }//}}}

        //{{{ Private static members
        private static final int m_HOVER_TIME_BEFORE_EXPAND = 1500;
        //}}}
        
        //{{{ Private Members
        
        //{{{ isDragOk()
        
        private boolean isDragOk(DropTargetDragEvent dtde) {
            //maybe someday I can accept text
            if (!dtde.isDataFlavorSupported(TransferableNode.nodeFlavor)) {
                return false;
            }
            
            // we're saying that these actions are necessary      
            if ((dtde.getDropAction() & m_acceptableActions) == 0) {
                return false;
            }
            return true;
        }//}}}
        
        private javax.swing.Timer m_m_timerHover;
        private TreePath m_m_lastPath;
        
        //}}}
        
    }//}}}
    
    //}}}

    //{{{ Drag and Drop instance variables
    private DragSource m_dragSource = DragSource.getDefaultDragSource();
    private DragGestureListener m_treeDGListener = new TreeDragGestureListener();
    private DragSourceListener m_treeDSListener = new DefaultViewDragSourceListener();
    private DropTarget m_dropTarget;
    private DropTargetListener m_treeDTListener = new DefaultViewDropTargetListener();
    private int m_acceptableActions = DnDConstants.ACTION_MOVE;
    
    private Rectangle m_cueLine = new Rectangle();
    
    //the node that is being dragged over.
    private Object m_dragOverTarget = null;
    //the color used to highlight the drop target when dragging
    //Use light grey for now. 
    // TODO: This should probably be based on the
    //current look and feel or customizable in an option.
    private Color m_dragSelectionColor = Color.lightGray;
    //}}}

    //}}}

}
