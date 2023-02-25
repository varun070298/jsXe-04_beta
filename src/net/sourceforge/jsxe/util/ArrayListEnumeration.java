/*
ArrayListEnumeration.java
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

package net.sourceforge.jsxe.util;

//{{{ imports

import java.util.*;

//}}}

/**
 * A utility class that allows enumaration or iteration over
 * an ArrayList. One can use ArrayList.iterator() or ArrayList.ListIterator()
 * but this class allows passing a single object around as an Enumaration or
 * Iterator.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: ArrayListEnumeration.java,v 1.3 2005/04/15 20:01:09 ian_lewis Exp $
 */
public class ArrayListEnumeration implements Enumeration, ListIterator {
    
    private ArrayList m_list;
    private int m_index = 0;
    
    //{{{ ArrayListEnumeration constructor
    
    public ArrayListEnumeration(ArrayList list) {
        m_list = list;
    }//}}}
    
    //{{{ Enumeration methods
    
    //{{{ hasMoreElements()
    
    public boolean hasMoreElements() {
        return (m_list.size() > m_index);
    }//}}}
    
    //{{{ nextElement()
    
    public Object nextElement() throws NoSuchElementException {
        try {
            if (hasMoreElements()) {
                return m_list.get(m_index++);
            } else {
                throw new NoSuchElementException("No more elements");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //shouldn't happen
            throw new NoSuchElementException(e.getMessage());
        }
    }//}}}
    
    //}}}
    
    //{{{ ListIterator methods
    
    //{{{ add()
    public void add(Object o) {
        m_list.add(o);
    }//}}}
    
    //{{{ hasNext()
    public boolean hasNext() {
        return hasMoreElements();
    }//}}}
    
    //{{{ hasPrevious()
    public boolean hasPrevious() {
        return m_index != 0;
    }//}}}
    
    //{{{ next()
    public Object next() throws NoSuchElementException {
        return nextElement();
    }//}}}
    
    //{{{ nextIndex()
    public int nextIndex() {
        return m_index;
    }//}}}
    
    //{{{ previous()
    public Object previous() {
        try {
            if (hasPrevious()) {
                return m_list.get(--m_index);
            } else {
                throw new NoSuchElementException("No previous elements");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //shouldn't happen
            throw new NoSuchElementException(e.getMessage());
        }
    }//}}}
    
    //{{{ previousIndex()
    public int previousIndex() {
        return nextIndex() - 1;
    }//}}}
    
    //{{{ remove()
    public void remove() throws UnsupportedOperationException {
        //optional operation
        throw new UnsupportedOperationException("cannot call remove on this Iterator");
    }//}}}
    
    //{{{ set()
    public void set(Object o) throws UnsupportedOperationException {
        //optional operation
        throw new UnsupportedOperationException("cannot call set on this Iterator");
    }//}}}
    
    //}}}
}
