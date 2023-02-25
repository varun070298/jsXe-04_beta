/*
RemoveAttributeAction.java
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

//{{{ jsXe classes

import net.sourceforge.jsxe.jsXe;
import net.sourceforge.jsxe.dom.AdapterNode;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.gui.DocumentView;

//}}}

//{{{ AWT classes
import java.awt.event.ActionEvent;
//}}}

//{{{ Swing classes
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
//}}}

//}}}

/**
 * This action removes the currently selected attribute if the curretly selected
 * node is an element.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: RemoveAttributeAction.java,v 1.1 2005/05/06 23:28:49 ian_lewis Exp $
 */
public class RemoveAttributeAction extends AbstractAction {

    //{{{ RemoveAttributeAction constructor
    
    public RemoveAttributeAction() {
        putValue(Action.NAME, Messages.getMessage("TreeView.RemoveAttribute"));
    }//}}}
    
    //{{{ actionPerformed()
    
    public void actionPerformed(ActionEvent e) {
        DocumentView view = jsXe.getActiveView().getDocumentView();
        if (view instanceof DefaultView) {
            DefaultView defView = (DefaultView)view;
            DefaultViewTree tree = defView.getDefaultViewTree();
            AdapterNode selectedNode = tree.getSelectedNode();
            if (selectedNode != null && selectedNode.getNodeType() == AdapterNode.ELEMENT_NODE) {
                JTable table = defView.getDefaultViewAttributeTable();
                DefaultViewTableModel model = (DefaultViewTableModel)table.getModel();
                int row = table.getSelectedRow();
                //if there is as selected row and it's not the last row.
                if (row != -1 && row+1 != model.getRowCount()) {
                    model.removeRow(row);
                    table.updateUI();
                }
            }
        }
        
        
    }//}}}
    
}
