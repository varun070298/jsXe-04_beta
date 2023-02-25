/*
ActivityLogAction.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2005 Trish Harnett (trishah136@member.fsf.org)

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
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.gui.TabbedView;
import net.sourceforge.jsxe.gui.ActivityLogDialog;
import net.sourceforge.jsxe.util.Log;
//}}}

//{{{ Swing components
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
//}}}

//{{{ AWT components
import java.awt.event.ActionEvent;
//}}}

//}}}

/**
 * The action that is executed when the clicks on the Activity Log option under the Help Menu
 *
 * @author  Trish Hartnett
 * @version $Id: ActivityLogAction.java,v 1.6 2005/10/26 04:41:34 ian_lewis Exp $
 */
public class ActivityLogAction extends AbstractAction {
		
	//	{{{ Public members
    private TabbedView view;
   // private boolean activityLogUpdated = false;
    private ActivityLogDialog dialog = new ActivityLogDialog(view);      
    //}}}
    
    //{{{ ActivityLogAction 
	 /**
     * @param TabbedView parent view containing the JSXE editor.
     * Constructor for the ActivityLogActionclass
     * @since jsXe 0.3pre15
     */
    public ActivityLogAction(TabbedView parent) {
    	putValue(Action.NAME, Messages.getMessage("ActivityLogDialog.Open"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyStroke.getKeyStroke("L").getKeyCode()));
        view = parent;
    }//}}}
    
    //{{{ actionPerformed()   
    public void actionPerformed(ActionEvent e) {
        dialog.refreshContents();
        dialog.show();  
    }//}}}
    
    /*
	public void updateDialog(){
		dialog.refreshContents();
	}*/
}
