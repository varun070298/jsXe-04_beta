/*
GlobalOptionsDialog.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2004 Ian Lewis (IanLewis@member.fsf.org)

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
import net.sourceforge.jsxe.*;
import net.sourceforge.jsxe.options.*;

import java.util.Iterator;

//}}}

/**
 * jsXe's global options dialog.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: GlobalOptionsDialog.java,v 1.9 2005/05/12 21:32:18 ian_lewis Exp $
 */
public class GlobalOptionsDialog extends OptionsDialog {
    
    private DocumentBuffer m_buffer;
    private OptionGroup m_defaultGroup;
    
    //{{{ GlobalOptionsDialog constructor
    
    public GlobalOptionsDialog(TabbedView view) {
        super(view, "global", Messages.getMessage("Global.Options.Dialog.Title"), jsXe.getProperty("global.last"));
    }//}}}
    
    //{{{ createOptionTreeModel()
    
    protected OptionTreeModel createOptionTreeModel() {
        m_defaultGroup = new OptionGroup("jsxe", "jsXe");
        OptionTreeModel paneTreeModel = new OptionTreeModel();
        OptionGroup rootGroup = (OptionGroup) paneTreeModel.getRoot();

        TabbedView view = (TabbedView)getOwner();
        
        DocumentBuffer buffer = view.getDocumentView().getDocumentBuffer();
        
        OptionPane pane = jsXe.getOptionsPanel();
        if (pane != null) {
            addOptionPane(pane);
        }
        
        Iterator pluginItr = jsXe.getPluginLoader().getAllPlugins().iterator();
        while (pluginItr.hasNext()) {
            ActionPlugin plugin = (ActionPlugin)pluginItr.next();
            pane = plugin.getOptionsPanel(buffer);
            if (pane != null) {
                addOptionPane(pane);
            }
        }
        
        pane = buffer.getOptionsPanel();
        if (pane != null) {
            addOptionPane(pane);
        }
        addOptionGroup(m_defaultGroup, rootGroup);
        
        return paneTreeModel;
    }//}}}
    
    //{{{ getDefaultGroup()
    
	protected OptionGroup getDefaultGroup() {
        return m_defaultGroup;
    }//}}}
    
}
