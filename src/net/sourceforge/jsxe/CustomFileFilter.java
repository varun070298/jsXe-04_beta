/*
CustomFileFilter.java
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
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
Optionally, you may find a copy of the GNU General Public License
from http://www.fsf.org/copyleft/gpl.txt
*/

package net.sourceforge.jsxe;

//{{{ imports
/*
All classes are listed explicitly so
it is easy to see which package it
belongs to.
*/

//{{{ Java Base classes
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
//}}}

//{{{ Swing Classes
import javax.swing.filechooser.FileFilter;
//}}}

//}}}

/**
 * A custom class that implements the FileFilter interface. It simplifies
 * creating a file filter.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: CustomFileFilter.java,v 1.7 2005/04/15 20:00:50 ian_lewis Exp $
 */
public class CustomFileFilter extends FileFilter {
    
    //{{{ CustomFileFilter constructor
    /**
     * Creates a CustomFileFilter given a Collection containing the file extensions
     * that the file filter accepts. If the Collection does not contain strings
     * then the file filter will not accept any files.
     * @param exts A Collection of String objects containing the file
     *             extensions that this file filter accepts.
     * @param desc The description for this file filter.
     */
    public CustomFileFilter(Collection exts, String desc) {
        extensions = exts;
        description = desc;
    }//}}}
    
    //{{{ accept()
    
    public boolean accept(File f) {
        if(f != null) {
            if(f.isDirectory()) {
                return true;
            }
            String ext = getExtension(f);
            Iterator iterator = extensions.iterator();
            while (iterator.hasNext()) {
                if(iterator.next().toString().equals(ext)) {
                    return true;
                }
            }
        }
        return false;
    }//}}}
    
    //{{{ getDescription()
    
    public String getDescription() {
        return description;
    }//}}}
    
    //{{{ Private members
    
    //{{{ getExtension()
    
    private String getExtension(File f) {
        if(f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i>0 && i<filename.length()-1) {
                return filename.substring(i+1).toLowerCase();
            }
        }
        return null;
    }//}}}
    
    private Collection extensions;
    private String description;
    
    //}}}
}
