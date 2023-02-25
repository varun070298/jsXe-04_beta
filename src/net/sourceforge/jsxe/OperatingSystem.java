/*
OperatingSystem.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2002 Slava Pestov
Portions Copyright (C) 2002 Ian Lewis (IanLewis@member.fsf.org)

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

import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.UIManager;
import java.io.File;

/**
 * Operating system detection routines.
 * @author Slava Pestov
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: OperatingSystem.java,v 1.3 2005/04/15 20:00:50 ian_lewis Exp $
 * @since jsXe 0.4 pre1
 */
public class OperatingSystem {
    
    //{{{ getScreenBounds()
    
    public static final Rectangle getScreenBounds() {
        int screenX = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int screenY = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        int x, y, w, h;
        
        if (isMacOS()) {
            x = 0;
            y = 22;
            w = screenX;
            h = screenY - y - 4;//shadow size
        } else {
            if (isWindows()) {
                x = -4;
                y = -4;
                w = screenX - 2*x;
                h = screenY - 2*y;
            } else {
                x = 0;
                y = 0;
                w = screenX;
                h = screenY;
            }
        }
        
        return new Rectangle(x,y,w,h);
    }//}}}
    
    //{{{ isDOSDerived() method
    /**
     * Returns if we're running Windows 95/98/ME/NT/2000/XP, or OS/2.
     */
    public static final boolean isDOSDerived() {
        return isWindows() || isOS2();
    } //}}}

    //{{{ isWindows() method
    /**
     * Returns if we're running Windows 95/98/ME/NT/2000/XP.
     */
    public static final boolean isWindows() {
        return os == WINDOWS_9x || os == WINDOWS_NT;
    } //}}}

    //{{{ isWindows9x() method
    /**
     * Returns if we're running Windows 95/98/ME.
     */
    public static final boolean isWindows9x() {
        return os == WINDOWS_9x;
    } //}}}

    //{{{ isWindowsNT() method
    /**
     * Returns if we're running Windows NT/2000/XP.
     */
    public static final boolean isWindowsNT() {
        return os == WINDOWS_NT;
    } //}}}

    //{{{ isOS2() method
    /**
     * Returns if we're running OS/2.
     */
    public static final boolean isOS2() {
        return os == OS2;
    } //}}}

    //{{{ isUnix() method
    /**
     * Returns if we're running Unix (this includes MacOS X).
     */
    public static final boolean isUnix() {
        return os == UNIX || os == MAC_OS_X;
    } //}}}

    //{{{ isMacOS() method
    /**
     * Returns if we're running MacOS X.
     */
    public static final boolean isMacOS() {
        return os == MAC_OS_X;
    } //}}}

    //{{{ isMacOSLF() method
    /**
     * Returns if we're running MacOS X and using the native look and feel.
     */
    public static final boolean isMacOSLF() {
        return (isMacOS() && UIManager.getLookAndFeel().isNativeLookAndFeel());
    }//}}}

    //{{{ isJava14() method
    /**
     * Returns if Java 2 version 1.4 is in use.
     */
    public static final boolean hasJava14() {
        return java14;
    } //}}}

    //{{{ Private members
    private static final int UNIX = 0x31337;
    private static final int WINDOWS_9x = 0x640;
    private static final int WINDOWS_NT = 0x666;
    private static final int OS2 = 0xDEAD;
    private static final int MAC_OS_X = 0xABC;
    private static final int UNKNOWN = 0xBAD;

    private static int os;
    private static boolean java14;

    //{{{ Class initializer
    static {
        if (System.getProperty("mrj.version") != null) {
            os = MAC_OS_X;
        } else {
            String osName = System.getProperty("os.name");
            if (osName.indexOf("Windows 9") != -1 || osName.indexOf("Windows M") != -1) {
                os = WINDOWS_9x;
            } else {
                if (osName.indexOf("Windows") != -1) {
                    os = WINDOWS_NT;
                } else {
                    if (osName.indexOf("OS/2") != -1) {
                        os = OS2;
                    } else {
                        if(File.separatorChar == '/') {
                            os = UNIX;
                        } else {
                            os = UNKNOWN;
                        }
                    }
                }
            }
        }

        if (System.getProperty("java.version").compareTo("1.4") >= 0) {
            java14 = true;
        }
    } //}}}

    //}}}
}
