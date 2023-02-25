/*
TransferableNode.java
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

//{{{ jsXe classes
import net.sourceforge.jsxe.dom.AdapterNode;
//}}}

//{{{ AWT classes
import java.awt.datatransfer.*;
//}}}

//{{{ Java classes
import java.io.IOException;
import java.util.*;
//}}}

//}}}

/**
 * A transferable class that manages the physical data that is transferred when
 * transferring an XML node. This class handles an DefaultViewTreeNode
 * object and either returning the object itself or some representation of
 * the node (such as a string) during transfer.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: TransferableNode.java,v 1.2 2005/04/27 05:07:20 ian_lewis Exp $
 * @see DefaultView
 * @see DefaultViewTree
 * @see DefaultViewTreeModel
 */
public class TransferableNode implements Transferable {
    
    public static final DataFlavor stringFlavor = DataFlavor.stringFlavor;
    /**
     * The flavor for transferring DefaultViewTreeNodes
     */
    public static final DataFlavor nodeFlavor;
    
    //{{{ TransferableNode constructor
    /**
     * Creates a new TransferableNode to handle the AdapterNode given.
     * @param node The node that is being transferred.
     */
    public TransferableNode(AdapterNode node) {
        m_node = node;
    }//}}}
    
    //{{{ Transferable methods
    
    //{{{ getTransferDataFlavors()
    public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }//}}}
    
    //{{{ isDataFlavorSupported()
    
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavorList.contains(flavor));
    }//}}}
    
    //{{{ getTransferData()
    
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (stringFlavor.equals(flavor)) {
            return m_node.serializeToString();
        } else {
            if (nodeFlavor.equals(flavor)) {
                return m_node;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }//}}}
    
    //}}}
    
    //{{{ Private static members
    static {
        DataFlavor flav = null;
        try {
            flav = new DataFlavor(Class.forName("net.sourceforge.jsxe.dom.AdapterNode"), "XML Node");
        } catch (ClassNotFoundException e) {}
        nodeFlavor = flav;
    }
    
    private static final DataFlavor[] flavors = {
        stringFlavor,
        nodeFlavor
    };
    
    private static final List flavorList = Arrays.asList( flavors );
    
    //}}}
    
    //{{{ Private members
    private AdapterNode m_node;
    //}}}    
}
