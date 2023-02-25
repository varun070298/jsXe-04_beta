/*
DocumentBufferListener.java
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

package net.sourceforge.jsxe;

import net.sourceforge.jsxe.dom.XMLDocument;
import net.sourceforge.jsxe.dom.AdapterNode;

/**
 * Defines an interface for objects that are notified when DocumentBuffers have changed.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: DocumentBufferListener.java,v 1.6 2005/04/15 20:00:50 ian_lewis Exp $
 * @see DocumentBuffer
 */
public interface DocumentBufferListener {
    
    //{{{ nameChanged()
    
    /**
     * Called whenever the buffers name has changed
     * @param source the source DocumentBuffer whose name has changed
     * @param newName the new name of the buffer
     */
    public void nameChanged(DocumentBuffer source, String newName);//}}}
    
    //{{{ bufferSaved()
    
    /**
     * Called whenever the buffer is saved.
     * @param source source buffer that was saved
     */
    public void bufferSaved(DocumentBuffer source);//}}}
    
    //{{{ statusChanged()
    
    public void statusChanged(DocumentBuffer source, int statusType, boolean oldStatus);//}}}
}
