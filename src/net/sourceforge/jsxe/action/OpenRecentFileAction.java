/*
OpenRecentFileAction.java
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
import net.sourceforge.jsxe.jsXe;
import net.sourceforge.jsxe.BufferHistory;
import net.sourceforge.jsxe.gui.TabbedView;
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

//{{{ Java Base classes
import java.io.IOException;
import java.io.File;
//}}}

//}}}

/**
 * An action that opens a specific file based on a BufferHistoryEntry
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: OpenRecentFileAction.java,v 1.4 2005/05/09 01:24:42 ian_lewis Exp $
 * @see net.sourceforge.jsxe.BufferHistory
 */
public class OpenRecentFileAction extends AbstractAction {
    
    //{{{ OpenRecentFileAction constructor
    
    public OpenRecentFileAction(TabbedView parent, BufferHistory.BufferHistoryEntry entry) {
        String path = entry.getPath();
        String fileName = path.substring(path.lastIndexOf(System.getProperty("file.separator"))+1);
        if (fileName.equals("")) {
            fileName = path;
        }
        putValue(Action.NAME, fileName);
        m_view = parent;
        m_entry = entry;
    }//}}}
    
    //{{{ actionPerformed()
    
    public void actionPerformed(ActionEvent e) {
        try {
            File file = new File(m_entry.getPath());
            jsXe.openXMLDocument(m_view, file, m_entry.getProperties(), m_entry.getViewName());
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(m_view, ioe, "IO Error", JOptionPane.WARNING_MESSAGE);
        }
        
    }//}}}
    
    //{{{ Public members
    private TabbedView m_view;
    private BufferHistory.BufferHistoryEntry m_entry;
    //}}}
}
