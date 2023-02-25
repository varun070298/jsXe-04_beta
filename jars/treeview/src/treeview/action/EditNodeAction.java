/*
EditNodeAction.java
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
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.dom.AdapterNode;
import net.sourceforge.jsxe.dom.completion.ElementDecl;
//}}}

//}}}

/**
 * An action that edits the current selected node.
 *
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: EditNodeAction.java,v 1.3 2005/05/30 21:27:32 ian_lewis Exp $
 */
public class EditNodeAction extends AbstractAction {
    
    //{{{ EditNodeAction constructor
    /**
     * Creates a action that brings up an EditTagDialog for the selected
     * Node
     */
    public EditNodeAction() {
        putValue(Action.NAME, Messages.getMessage("TreeView.EditNode"));
    }//}}}
    
    //{{{ actionPerformed()
  
    public void actionPerformed(ActionEvent e) {
        DocumentView view = jsXe.getActiveView().getDocumentView();
        if (view instanceof DefaultView) {
            DefaultView defView = (DefaultView)view;
            DefaultViewTree tree = defView.getDefaultViewTree();
            AdapterNode selectedNode = tree.getSelectedNode();
            AdapterNode addedNode = null;
            if (selectedNode != null && selectedNode.getNodeType() == AdapterNode.ELEMENT_NODE) {
                try {
                    ElementDecl element = selectedNode.getOwnerDocument().getElementDecl(selectedNode.getNodeName());
                    if (element != null) {
                        //Edit tag dialog needs to be reworked
                        EditTagDialog dialog = new EditTagDialog(jsXe.getActiveView(),
                                                                 element,
                                                                 new HashMap(),
                                                                 element.empty,
                                                                 element.completionInfo.getEntityHash(),
                                                                 new ArrayList(), //don't support IDs for now.
                                                                 selectedNode.getOwnerDocument(),
                                                                 selectedNode);
                        dialog.show();
                        //The TreeModel doesn't automatically call treeNodesInserted() yet
                        tree.updateUI();
                    }
                
                } catch (DOMException dome) {
                    JOptionPane.showMessageDialog(tree, dome, "XML Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }//}}}
  
}

