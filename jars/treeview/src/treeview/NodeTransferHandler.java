/*
NodeTransferHandler.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

jsXe is the Java Simple XML Editorh
jsXe is a gui application that can edit an XML document and create a tree view.
The user can then edit this tree and the content in the tree and save the
document.

This file contains the data transfer handler for TransferableNode objects.

This file written by Aaron Flatten (aflatten@users.sourceforge.net)
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

//{{{ jsXe classes
import net.sourceforge.jsxe.dom.*;
//}}}

//{{{ Swing components
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
//}}}

//{{{ AWT components
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.dnd.*;
//}}}

//{{{ DOM classes
import org.w3c.dom.DOMException;
//}}}

//{{{ Java base classes
import java.io.IOException;
//}}}

//}}}


/**
 * Created on Apr 29, 2005
 *
 * @author Aaron Flatten
 *
 * This class manages data transfer for <code>TransferableNode</code>
 * objects.
 */
public class NodeTransferHandler extends TransferHandler {

//-----------------------------------------------------------------------------
// In order to use this custom TransferHandler in DefaultTreeView.java,
// the following lines of code need to be added to the default constructor:
//
//        //Create the node transfer handler
//        m_nodeHandler = new NodeTransferHandler();
//        setDragEnabled(true);
//        setTransferHandler(m_nodeHandler);
//
// Additionally, a new instance variable m_nodeHandler needs to be added (which
// should have type NodeTransferHandler.  Finally, the old
// DragGestureRecognizer and DropTarget objects should no longer be initialized,
// since we now let Swing handle the drag and drop behavior.
//
//
// This will set up treeview to use the default Swing DnD behavior.  We may
// decide to patch in the old custom drag/drop handler.
//-----------------------------------------------------------------------------

    DataFlavor m_nodeFlavor = TransferableNode.nodeFlavor;
    DataFlavor m_stringFlavor = TransferableNode.stringFlavor;

    /* Returns <code>true</code> if a data import is allowed for the specified
     * flavors.
     * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
     */
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        for (int i=0; i<transferFlavors.length; i++) {
            //The nodeFlavor and stringFavor are the only supported flavors
            if (transferFlavors[i].equals(m_nodeFlavor) ||
                    transferFlavors[i].equals(m_stringFlavor))
                return true;
        }
        //None of the flavors were supported
        return false;
    }


    /* Creates a <code>TransferableNode</code> object from the source for a data
     * transfer or <code>null</code> if no data is available.
     * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
     */
    protected Transferable createTransferable(JComponent c) {
        Transferable result = null;

        DefaultViewTree dvt = (DefaultViewTree)c;
        TreePath path = dvt.getSelectionPath();

        //If a node is selected
        if (path != null) {
            AdapterNode node = (AdapterNode)path.getLastPathComponent();
        	result = new TransferableNode(node);
        }
        return result;
    }

    /* Returns the actions allowed -- in this case, a TransferableNode
     * can only be moved.
     *
     * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
     */
    public int getSourceActions(JComponent c) {
        return MOVE;
    }


    /* Called when the export is complete.  If this was a move action,
     * then the original node is deleted.
     * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
     */
    protected void exportDone(JComponent source, Transferable data, int action) {
        DefaultViewTree dvt = (DefaultViewTree)source;
        if (action == MOVE) {
            //XXX Delete the old source node?  Probably not necessary since
            //the addAdapterNodeAt method (see importData) is actually a
            //move operation.
        }
    }



    /* Imports transferable data into the specified component.
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
     */
    public boolean importData(JComponent comp, Transferable t) {
        //If the import is not supported
        if (!canImport(comp, t.getTransferDataFlavors())) { return false; }

        Object data = null;
        try { data = t.getTransferData(m_nodeFlavor); }
        //Do nothing if the import is an unsupported flavor
        catch (UnsupportedFlavorException ufe) { return false; }
        catch (IOException ioe) { return false; }

        DefaultViewTree dvt = (DefaultViewTree)comp;
        TreePath path = dvt.getSelectionPath();

        //If nothing is selected, do not import
        if (path == null) { return false; }

        AdapterNode anode = (AdapterNode)data; //Node to be imported
        AdapterNode selected = (AdapterNode)path.getLastPathComponent();

        try {
        	if (selected == null) throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "HIERARCHY_REQUEST_ERR: An attempt was made to insert a node where it is not permitted");
        	AdapterNode parent = (AdapterNode)selected.getParentNode();

            //If there is no parent (i.e. the root node is selected)
        	if (parent == null) throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "HIERARCHY_REQUEST_ERR: An attempt was made to insert a node where it is not permitted");

        	parent.addAdapterNodeAt(anode, parent.index(selected));
        	dvt.makeVisible(path);
        	TreePath droppedPath = path.getParentPath();

        	//Ensure that new path of the drop node is in selection model
        	dvt.addSelectionPath(droppedPath.pathByAddingChild(selected));
        } catch (DOMException dome) {
            //Show a dialog box if an illegal import was attempted
            JOptionPane.showMessageDialog(dvt, dome, "XML Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
