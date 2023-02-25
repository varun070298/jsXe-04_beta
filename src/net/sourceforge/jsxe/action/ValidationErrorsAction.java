/*
ValidationErrorsAction.java
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

package net.sourceforge.jsxe.action;

//{{{ imports
/*
All classes are listed explicitly so
it is easy to see which package it
belongs to.
*/

//{{{ jsXe classes
import net.sourceforge.jsxe.DocumentBuffer;
import net.sourceforge.jsxe.gui.ErrorListDialog;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.gui.TabbedView;
import net.sourceforge.jsxe.gui.ActivityLogDialog;
import net.sourceforge.jsxe.util.Log;
//}}}

//{{{ SAX
import org.xml.sax.SAXParseException;
//}}}

//{{{ Java classes
import java.util.Vector;
import java.util.Iterator;
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
 * The action shows the Validation Errors Dialog
 *
 * @author Ian Lewis
 * @version $Id: ValidationErrorsAction.java,v 1.2 2006/02/16 21:27:34 ian_lewis Exp $
 */
public class ValidationErrorsAction extends AbstractAction {
		
	//	{{{ Public members
    private TabbedView view;      
    //}}}
    
    //{{{ ValidationErrorsAction 
	 /**
     * @param TabbedView parent view containing the JSXE editor.
     * Constructor for the ActivityLogActionclass
     * @since jsXe 0.3pre15
     */
    public ValidationErrorsAction(TabbedView parent) {
    	putValue(Action.NAME, Messages.getMessage("Tools.ValidationErrors"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyStroke.getKeyStroke("V").getKeyCode()));
        view = parent;
    }//}}}
    
    //{{{ actionPerformed()   
    public void actionPerformed(ActionEvent e) {
        DocumentBuffer document = view.getDocumentView().getDocumentBuffer();
        Vector errors = new Vector();
        Iterator itr = document.getErrors().iterator();
        while (itr.hasNext()) {
            errors.add(formatError(document.getName(), (Exception)itr.next()));
        }
        
        ErrorListDialog dialog = new ErrorListDialog(view,
                                                     Messages.getMessage("ValidationErrors.title"),
                                                     Messages.getMessage("ValidationErrors.message"),
                                                     errors,
                                                     false);
    }//}}}
    
    //{{{ formatError
    private String formatError(String name, Exception e) {
        String message;
        if (e instanceof SAXParseException) {
            SAXParseException saxError = (SAXParseException)e;
            int line = saxError.getLineNumber();
            int column = saxError.getColumnNumber();
            message = name+":"+line+":"+column+":"+e.getMessage();
        } else {
            message = name+":"+e.getMessage();
        }
        return message;
    }//}}}
    
}
