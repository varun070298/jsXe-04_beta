/*
EditFindAction.java
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

package sourceview.action;

//{{{ imports

import sourceview.*;

//{{{ jsXe classes

import net.sourceforge.jsxe.jsXe;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.gui.DocumentView;

//}}}

//{{{ AWT classes
import java.awt.event.ActionEvent;
//}}}

//{{{ Swing classes
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
//}}}

//}}}

/**
 * This action that opens the find dialog.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: EditFindAction.java,v 1.2 2006/02/16 21:29:21 ian_lewis Exp $
 */

public class EditFindAction extends AbstractAction {
    
    //{{{ EditFindAction constructor
    
    public EditFindAction() {
        //putValue(Action.NAME, "Find...");
        putValue(Action.NAME, Messages.getMessage("common.find"));
        putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke("ctrl F"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyStroke.getKeyStroke("F").getKeyCode()));
    }//}}}
    
    //{{{ actionPerformed()
    
    public void actionPerformed(ActionEvent e) {
        DocumentView view = jsXe.getActiveView().getDocumentView();
        if (view instanceof SourceView) {
            SourceView sourceView = (SourceView)view;
            SourceViewSearchDialog.showSearchDialog(sourceView);
        }
        
        
    }//}}}
    
}
