/*
AdapterNodeListener.java
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

//{{{ Java Base Classes
import java.util.ArrayList;
//}}}

//{{{ DOM classes
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//}}}

//}}}

public interface AdapterNodeListener {
    
    //{{{ nodeAdded()
    
    public void nodeAdded(AdapterNode source, AdapterNode added);//}}}
    
    //{{{ nodeRemoved()
    
    public void nodeRemoved(AdapterNode source, AdapterNode removed);//}}}
    
    //{{{ localNameChanged()
    
    public void localNameChanged(AdapterNode source);//}}}
    
    //{{{ namespaceChanged()
    
    public void namespaceChanged(AdapterNode source);//}}}
    
    //{{{ nodeValueChanged()
    
    public void nodeValueChanged(AdapterNode source);//}}}
    
    //{{{ attributeChanged()
    
    public void attributeChanged(AdapterNode source, String attr);//}}}
    
}
