/*
DefaultDOMLocator.java
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
import org.w3c.dom.Node;
import org.w3c.dom.DOMLocator;
//}}}

//}}}

public class DefaultDOMLocator implements DOMLocator {
    
    //{{{ DefaultDOMLocator constructor
    
    public DefaultDOMLocator() {
        m_column = -1;
        m_line   = -1;
        m_offset = -1;
    }//}}}
    
    //{{{ DefaultDOMLocator constructor
    
    public DefaultDOMLocator(Node node, int lineno, int col, int byteOffset, String uri) {
        m_line = lineno;
        m_column = col;
        m_errorNode = node;
        m_offset = byteOffset;
        m_uri = uri;
    }//}}}
    
    //{{{ Implemented DOMLocator methods
    
    //{{{ getByteOffset()
    
    public int getByteOffset() {
        return m_offset;
    }//}}}
    
    //{{{ getColumnNumber()
    
    public int getColumnNumber() {
        return m_column;
    }//}}}
    
    //{{{ getRelatedNode()
    
    public Node getRelatedNode() {
        return m_errorNode;
    }//}}}
    
    //{{{ getLineNumber()
    
    public int getLineNumber() {
        return m_line;
    }//}}}
    
    //{{{ getUri()
    
    public String getUri() {
        return m_uri;
    }//}}}
    
    //{{{ getUtf16Offset()
    
    public int getUtf16Offset() {
        //not sure how to approach the byte/UTF-16 offset stuff yet.
        return -1;
    }//}}}
    
    //}}}
    
    //{{{ Private members
    
    private int m_column;
    private int m_line;
    private int m_offset;
    private Node m_errorNode;
    private String m_uri;
    
    //}}}
}
