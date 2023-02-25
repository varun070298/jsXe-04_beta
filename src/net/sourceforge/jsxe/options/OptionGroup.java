/*
OptionGroup.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2000 Mike Dillon
Portions Copyright (C) 2000 John Gellene
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

import java.util.Enumeration;
import java.util.Vector;
import net.sourceforge.jsxe.util.Log;

/**
 * A set of option panes shown in one branch in the options dialog.<p>
 *
 * In those cases where a single option pane is inadequate to present all
 * of a plugin's configuration options, this class can be used to create a
 * group of options panes. The group will appear as a single node in the
 * options dialog tree. The member option panes will appear as
 * leaf nodes under the group's node.
 *
 * @see OptionPane
 * @see net.sourceforge.jsxe.gui.OptionsDialog#addOptionGroup(OptionGroup)
 *
 * @author Mike Dillon
 * @author John Gellene (API documentation)
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: OptionGroup.java,v 1.5 2005/10/27 00:09:06 ian_lewis Exp $
 */
public class OptionGroup {
    
    //{{{ OptionGroup constructor
    /**
     * Creates an option group.
     * @param name The internal name of the option group, used to key a
     * property <code>options.<i>name</i>.label</code> which is the
     * label displayed in the options dialog.
     */
    public OptionGroup(String name, String title) {
        this.name = name;
        members = new Vector();
        m_title = title;
    }//}}}

    //{{{ getName()
    /**
     * Gets the internal name of the option group
     */
    public String getName() {
        return name;
    }//}}}
    
    //{{{ getTitle()
    /**
     * Gets the title for the option group
     */
    public String getTitle() {
        return m_title;
    }//}}}
    
    //{{{ addOptionGroup()
    /**
     * Adds a group as a child in the option tree.
     * @param group the group to add
     */
    public void addOptionGroup(OptionGroup group) {
        if (members.indexOf(group) != -1) return;

        members.addElement(group);
    }//}}}

    //{{{ addOptionPane()
    /**
     * Adds an OptionPane as a leaf node in the option tree.
     * @param pane the OptionPane to add to this group
     */
    public void addOptionPane(OptionPane pane) {
        if (members.indexOf(pane) != -1)  {
            return;
        }

        members.addElement(pane);
    }//}}}

    //{{{ getMembers()
    /**
     * Gets the members of this option group
     */
    public Enumeration getMembers() {
        return members.elements();
    }//}}}

    //{{{ getMember()
    /**
     * Gets a specific member of the option group
     */
    public Object getMember(int index) {
        return (index >= 0 && index < members.size())
            ? members.elementAt(index) : null;
    }//}}}

    //{{{ getMemberIndex()
    /**
     * Gets the index of a member
     */
    public int getMemberIndex(Object member) {
        return members.indexOf(member);
    }//}}}

    //{{{ getMemberCount()
    /**
     * Gets the number of members in this group
     */
    public int getMemberCount() {
        return members.size();
    }//}}}

    //{{{ save()
    /**
     * Saves the options in all options panes
     */
    public void save() {
        Enumeration elements = members.elements();

        while (elements.hasMoreElements()) {
            Object elem = elements.nextElement();
            try {
                if (elem instanceof OptionPane) {
                    ((OptionPane)elem).save();
                } else {
                    if (elem instanceof OptionGroup) {
                        ((OptionGroup)elem).save();
                    }
                }
            /*
            We want to catch all Throwables here so that plugins don't
            crash jsXe since they can add options to the dialogs.
            */
            } catch(Throwable t) {
                Log.log(Log.ERROR, this, t);
            }
        }
    }//}}}
   
    //{{{ toString()
    
    public String toString() {
       return getName();
    }//}}}
    
    //{{{ Private members
    private String name;
    private String m_title;
    private Vector members;
    //}}}
}
