/*
jsxeAboutDialog.java
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

package net.sourceforge.jsxe.gui;

//{{{ imports
/*
All classes are listed explicitly so
it is easy to see which package it
belongs to.
*/

//{{{ jsXe classes
import net.sourceforge.jsxe.jsXe;
//}}}

//{{{ Swing components
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
//}}}

//{{{ AWT components
import java.awt.Component;
import java.awt.event.ActionEvent;
//}}}

//}}}

/**
 * Displays the jsXe about dialog.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @author Trish Hartnett (<a href="mailto:trishah136@member.fsf.org">trishah136@member.fsf.org</a>)
 * @version $Id: jsxeAboutDialog.java,v 1.24 2006/02/17 20:23:49 ian_lewis Exp $
 */
public class jsxeAboutDialog extends AbstractAction {
    
    //{{{ jsxeAboutDialog constructor
    
    public jsxeAboutDialog(Component parent) {
	    //putValue(Action.NAME, "About jsXe...");
	    putValue(Action.NAME, Messages.getMessage("Help.About"));	    	
        putValue(Action.MNEMONIC_KEY, new Integer(KeyStroke.getKeyStroke("A").getKeyCode()));
        view = parent;
    }//}}}
    
    //{{{ actionPerformed()
    /**
     * Displays jsXe's About dialog
     */
    public void actionPerformed(ActionEvent e) {
        String aboutMsg = 
        jsXe.getAppTitle() + " " + jsXe.getVersion()+" "+"\n"+
        "Java Simple XML Editor\n"+
        "Copyright (C) 2002 Ian Lewis\n"+
        "Build Date:  "+jsXe.getBuildDate().toString()+"\n\n"+ 
        Messages.getMessage("about.message");
        
        Object[] okButton = {Messages.getMessage("common.close")};
        JOptionPane.showOptionDialog(
            view,
            aboutMsg,
            Messages.getMessage("about.title"),
            0,
            JOptionPane.INFORMATION_MESSAGE,
            jsXe.getIcon(),
            okButton,
            okButton[0]);
    }//}}}
    
    //{{{ Private Members
    private Component view;
    //}}}
}
