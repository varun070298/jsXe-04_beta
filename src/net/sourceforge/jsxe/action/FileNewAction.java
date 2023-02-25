/*
FileNewAction.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2002 Ian Lewis (IanLewis@member.fsf.org)
Portions Copyright (C) 2005 Trish Harnett (trishah136@member.fsf.org)

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

package net.sourceforge.jsxe.action;

//{{{ imports
/*
All classes are listed explicitly so
it is easy to see which package it
belongs to.
*/

//{{{ jsXe classes
import net.sourceforge.jsxe.jsXe;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.gui.TabbedView;
//}}}

//{{{ Swing components
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
//}}}

//{{{ AWT components
import java.awt.event.ActionEvent;
//}}}

//{{{ Java Base classes
import java.io.IOException;
//}}}

//}}}

/**
 * The action that is executed when the user selects 'new' in the file menu.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @author Trish Hartnett (<a href="mailto:trishah136@member.fsf.org">trishah136@member.fsf.org</a>)
 * @version $Id: FileNewAction.java,v 1.10 2005/04/26 20:25:12 ian_lewis Exp $
 */
public class FileNewAction extends AbstractAction {
    
    //{{{ FileNewAction constructor
    
    public FileNewAction(TabbedView parent) {
        //putValue(Action.NAME, "New");
    	putValue(Action.NAME, Messages.getMessage("File.New"));	
        putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke("ctrl N"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyStroke.getKeyStroke("N").getKeyCode()));
        view = parent;
    }//}}}
    
    //{{{ actionPerformed()
    
    public void actionPerformed(ActionEvent e) {
        try {
            boolean success = jsXe.openXMLDocument(view, jsXe.getDefaultDocument());
            if (!success) {
                jsXe.exiterror(view, "Could not open default document.",1);
            }
        } catch (IOException ioe) {
            jsXe.exiterror(view, "Could not open default document: " + ioe.toString(),1);
        }
    }//}}}
    
    //{{{ Private members
    private TabbedView view;
    //}}}
}
