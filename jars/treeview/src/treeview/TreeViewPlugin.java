/*
TreeViewPlugin.java
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
import net.sourceforge.jsxe.DocumentBuffer;
import net.sourceforge.jsxe.ViewPlugin;
import net.sourceforge.jsxe.dom.AdapterNode;
import net.sourceforge.jsxe.gui.DocumentView;
import net.sourceforge.jsxe.gui.OptionsPanel;
//}}}

//{{{ Java classes
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
//}}}

//}}}

public class TreeViewPlugin extends ViewPlugin {
    
    public static final String PLUGIN_NAME = "tree";
    
    //{{{ TreeViewPlugin constructor
    
    public TreeViewPlugin() {
        //add actions
        addAction("treeview.add.element.node", new AddNodeAction("Add Element Node", "new_element", "", AdapterNode.ELEMENT_NODE));
        addAction("treeview.add.text.node", new AddNodeAction("Add Text Node", "", "New Text Node", AdapterNode.TEXT_NODE));
        addAction("treeview.add.cdata.node", new AddNodeAction("Add CDATA Section", "", "New CDATA Section", AdapterNode.CDATA_SECTION_NODE));
        addAction("treeview.add.processing.instruction.node", new AddNodeAction("Add Processing Instruction", "Instruction", "New Processing Instruction", AdapterNode.PROCESSING_INSTRUCTION_NODE));
        addAction("treeview.add.comment.node", new AddNodeAction("Add Comment", "", "New Comment", AdapterNode.COMMENT_NODE));
        addAction("treeview.remove.node", new RemoveNodeAction());
        addAction("treeview.rename.node", new RenameNodeAction());
        addAction("treeview.add.attribute", new AddAttributeAction());
        addAction("treeview.remove.attribute", new RemoveAttributeAction());
        addAction("treeview.edit.node", new EditNodeAction());
        addAction("treeview.add.doctype.node", new AddDocTypeAction());
        addAction("treeview.cut.node", new CutNodeAction());
        addAction("treeview.copy.node", new CopyNodeAction());
        addAction("treeview.paste.node", new PasteNodeAction());
    }//}}}
    
    //{{{ newDocumentView()
    
    public DocumentView newDocumentView(DocumentBuffer document) throws IOException {
        return new DefaultView(document, this);
    }//}}}
    
    //{{{ getOptionsPanel()
    
    public OptionsPanel getOptionsPanel(DocumentBuffer buffer) {
        return new DefaultViewOptionsPanel(buffer);
    }//}}}
    
}
