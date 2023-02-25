/*
DOMSerializerException.java
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

//{{{ imports
/*
All classes are listed explicitly so
it is easy to see which package it
belongs to.
*/

//{{{ DOM classes
import org.w3c.dom.DOMError;
//}}}

//}}}

/**
 * Signals that a serialization error of some kind has occurred.
 * @author <a href="mailto:IanLewis at member dot fsf dot org">Ian Lewis</a>
 * @version $Id: DOMSerializerException.java,v 1.5 2006/03/06 22:04:31 ian_lewis Exp $
 * @see DOMSerializer
 */
public class DOMSerializerException extends Exception {
    
    //{{{ DOMSerializerException constructor
    
    public DOMSerializerException(DOMError err) {
        super(((Throwable)err.getRelatedException()).getMessage());
        error = err;
    }//}}}
    
    //{{{ getError()
    
    public DOMError getError() {
        return error;
    }//}}}

    //{{{ Private members
    private DOMError error;
    //}}}

}
