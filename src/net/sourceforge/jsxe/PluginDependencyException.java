/*
PluginDependencyException.java
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

package net.sourceforge.jsxe;

/**
 * Signals that a dependency required by a plugin has not been met.
 * @author <a href="mailto:IanLewis at member dot fsf dot org">Ian Lewis</a>
 * @version $Id: PluginDependencyException.java,v 1.4 2005/04/15 20:00:50 ian_lewis Exp $
 * @see JARClassLoader
 */
public class PluginDependencyException extends RuntimeException {

    //{{{ PluginDependencyException constructor
    
    public PluginDependencyException(String pluginName, String requiredName, String versionRequired, String versionFound) {
        super(pluginName + " requires "+requiredName+" version "+versionRequired+" but only version "+versionFound+" was found.");
        m_pluginName = pluginName;
        m_requiredName = requiredName;
        m_versionRequired = versionRequired;
        m_versionFound = versionFound;
    }//}}}
    
    //{{{ PluginDependencyException constructor
    
    public PluginDependencyException(String pluginName, String message) {
        super(message);
        m_pluginName = pluginName;
    }//}}}

    //{{{ Private Members
    private String m_pluginName;
    private String m_requiredName;
    private String m_versionRequired;
    private String m_versionFound;
    //}}}
}
