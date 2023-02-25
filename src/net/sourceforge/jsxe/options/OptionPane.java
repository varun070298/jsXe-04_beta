/*
OptionPane.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 1999 Slava Pestov
Portions Copyright (C) 2004 Ian Lewis (IanLewis@member.fsf.org)

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

package net.sourceforge.jsxe.options;

import java.awt.Component;

/**
 * The interface all option panes must implement.<p>
 *
 * The <i>name</i> of an option pane is returned by the <code>getName()</code>
 * method. The label displayed in the option pane's tab is obtained from the
 * <code>getTitle()</code> method.
 *
 * Note that you should extend the {@link net.sourceforge.jsxe.gui.OptionPanel}
 * if creating an OptionPane for use with jsXe.
 *
 * @see OptionGroup
 * @see net.sourceforge.jsxe.gui.OptionsDialog#addOptionPane(OptionPane)
 *
 * @author Slava Pestov
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: OptionPane.java,v 1.3 2005/04/21 17:57:45 ian_lewis Exp $
 */
public interface OptionPane {
    
    //{{{ getName()
    
    /**
     * Returns the internal name of this option pane.
     */
    public String getName();
    //}}}
    
    //{{{ getTitle()
    public String getTitle();
    //}}}

    //{{{ getComponent()
    /**
     * Returns the component that should be displayed for this option pane.
     */
    public Component getComponent();
    //}}}

    //{{{ init()
    /**
     * This method is called every time the option pane is displayed.
     */
    public void init();
    //}}}

    //{{{ save()
    /**
     * Called when the options dialog's "ok" button is clicked.
     * This should save any properties being edited in this option
     * pane.
     */
    public void save();
    //}}}
}
