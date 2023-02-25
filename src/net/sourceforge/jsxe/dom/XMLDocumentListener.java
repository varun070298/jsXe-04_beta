/*
XMLDocumentListener.java
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


package net.sourceforge.jsxe.dom;
/**
 * XMLDocumentListener is used to notify objects of a change to the XMLDocument.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: XMLDocumentListener.java,v 1.10 2005/04/15 20:00:53 ian_lewis Exp $
 * @see XMLDocument
 */
public interface XMLDocumentListener {
    
    //{{{ propertyChanged()
    
    /**
     * Called when a property associated with the XMLDocument has changed.
     * @param source The source XMLDocument
     * @param key the key to the property that changed
     * @param oldValue the previous value of this key, null if there was none
     */
    public void propertyChanged(XMLDocument source, String key, String oldValue);//}}}
    
    //{{{ structureChanged()
    
    /**
     * Called when the structure of the XMLDocument has changed.
     * @param source The source XMLDocument
     * @param location The AdapterNode location where the change occurred.
     *                 If a node was removed then this is the parent of the
     *                 node that was removed. This could be null if the location
     *                 is unknown.
     */
    public void structureChanged(XMLDocument source, AdapterNode location);//}}}
    
}
