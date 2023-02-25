/*
AddNodeAction.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2005 Ian Lewis (IanLewis@member.fsf.org)

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

package treeview.action;

//{{{ imports

import treeview.*;

//{{{ AWT classes
import java.awt.event.ActionEvent;
//}}}

//{{{ Java base classes
import java.util.HashMap;
import java.util.ArrayList;
//}}}

//{{{ Swing classes
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.Action;
//}}}

//{{{ DOM classes
import org.w3c.dom.DOMException;
//}}}

//{{{ jsXe classes
import net.sourceforge.jsxe.jsXe;
import net.sourceforge.jsxe.gui.DocumentView;
import net.sourceforge.jsxe.dom.AdapterNode;
import net.sourceforge.jsxe.dom.completion.ElementDecl;
//}}}

//}}}

/**
 * An action that adds a node to the tree at the current selected node.
 *
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: AddNodeAction.java,v 1.5 2005/05/30 21:27:32 ian_lewis Exp $
 */
public class AddNodeAction extends AbstractAction {
    
    //{{{ AddNodeAction constructor
    /**
     * Creates a action that adds a new node of the specified type and values
     * to the current selected node in the tree.
     * @param displayName the human readable name that is displayed for this action
     * @param nodeName the name of the node. This can be null if applicable
     * @param nodeValue the value of the node. This can be null if applicable
     * @param nodeType the type of the node. Use the values specified by the org.w3c.dom.Node class
     */
    public AddNodeAction(String displayName, String nodeName, String nodeValue, short nodeType) {
        init(nodeName, nodeValue, displayName, nodeType);
    }//}}}
  
    //{{{ AddNodeAction constructor
    /**
     * Crates an action that adds an element node based on the specified
     * element declaration. When adding a node using an ElementDecl
     * an EditTagDialog will be displayed.
     * @param element the declaration specifying the information for this node.
     * @param 
     */
    public AddNodeAction(ElementDecl element, String displayName) {
        init(element.name, "", displayName, AdapterNode.ELEMENT_NODE);
        m_m_element = element;
    }//}}}
  
    //{{{ actionPerformed()
  
    public void actionPerformed(ActionEvent e) {
        DocumentView view = jsXe.getActiveView().getDocumentView();
        if (view instanceof DefaultView) {
            DefaultView defView = (DefaultView)view;
            DefaultViewTree tree = defView.getDefaultViewTree();
            AdapterNode selectedNode = tree.getSelectedNode();
            AdapterNode addedNode = null;
            if (selectedNode != null) {
                try {
                    if (m_m_element != null) {
                        EditTagDialog dialog = new EditTagDialog(jsXe.getActiveView(),
                                                                 m_m_element,
                                                                 new HashMap(),
                                                                 m_m_element.empty,
                                                                 m_m_element.completionInfo.getEntityHash(),
                                                                 new ArrayList(), //don't support IDs for now.
                                                                 selectedNode.getOwnerDocument());
                        dialog.show();
                        addedNode = selectedNode.addAdapterNode(dialog.getNewNode());
                    } else {
                        //add the node of the correct type to the end of the children of this node
                        addedNode = selectedNode.addAdapterNode(m_name, m_value, m_nodeType, selectedNode.childCount());
                    }
                    tree.expandPath(tree.getLeadSelectionPath());
                    //The TreeModel doesn't automatically call treeNodesInserted() yet
                    tree.updateUI();
                
                } catch (DOMException dome) {
                    JOptionPane.showMessageDialog(tree, dome, "XML Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }//}}}
  
    //{{{ Private members
  
    //{{{ init()
  
    private void init(String qualifiedName, String value,  String actionTitle, short nodeType) {
        m_name     = qualifiedName;
        m_value    = value;
        m_nodeType = nodeType;
        putValue(Action.NAME, actionTitle);
    }//}}}
  
    private short m_nodeType;
    private String m_name;
    private String m_value;
    private ElementDecl m_m_element;
    //}}}
  
}

