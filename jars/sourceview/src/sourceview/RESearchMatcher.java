/*
RESearchMatcher.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 1999, 2000, 2001 Slava Pestov
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

package sourceview;

//{{{ Imports
import gnu.regexp.*;
//}}}

/**
 * A regular expression string matcher using {@link gnu.regexp}.
 * @author Slava Pestov
 * @author Ian Lewis
 * @version $Id: RESearchMatcher.java,v 1.2 2005/05/12 06:57:23 ian_lewis Exp $
 */
public class RESearchMatcher {
    /**
     * Perl5 syntax with character classes enabled.
     */
    public static final RESyntax RE_SYNTAX_JEDIT
        = new RESyntax(RESyntax.RE_SYNTAX_PERL5)
        .set(RESyntax.RE_CHAR_CLASSES)
        .setLineSeparator("\n");

    //{{{ RESearchMatcher constructor
    /**
     * Creates a new regular expression string matcher.
     */
    public RESearchMatcher(String search, String replace, boolean ignoreCase) throws Exception {
        // gnu.regexp doesn't seem to support \n and \t in the replace
        // string, so implement it here
        
        
        m_replace = escapesToChars(replace);

        m_re = new RE(search,(ignoreCase ? RE.REG_ICASE : 0)
            | RE.REG_MULTILINE,RE_SYNTAX_JEDIT);

        m_returnValue = new int[2];
    } //}}}

    //{{{ nextMatch() method
    /**
     * Returns the offset of the first match of the specified text
     * within this matcher.
     * @param text The text to search in
     * @param start True if the start of the segment is the beginning of the
     * buffer
     * @param end True if the end of the segment is the end of the buffer
     * @param firstTime If false and the search string matched at the start
     * offset with length zero, automatically find next match
     * @param reverse If true, searching will be performed in a backward
     * direction.
     * @return an array where the first element is the start offset
     * of the match, and the second element is the end offset of
     * the match
     * @since jEdit 4.1pre7
     */
    public int[] nextMatch(CharIndexed text, boolean start, boolean end,
        boolean firstTime, boolean reverse)
    {
        int flags = 0;

        // unless we are matching from the start of the buffer,
        // ^ should not match on the beginning of the substring
        if(!start)
            flags |= RE.REG_NOTBOL;
        // unless we are matching to the end of the buffer,
        // $ should not match on the end of the substring
        if(!end)
            flags |= RE.REG_NOTEOL;

        REMatch match = m_re.getMatch(text,0,flags);
        if(match == null)
            return null;

        int _start = match.getStartIndex();
        int _end = match.getEndIndex();

        // some regexps (eg ^ by itself) have a length == 0, so we
        // implement this hack. if you don't understand what's going on
        // here, then go back to watching MTV
        if(!firstTime && _start == 0 && _end == 0)
        {
            text.move(1);

            if(text.charAt(0) == CharIndexed.OUT_OF_BOUNDS)
            {
                // never mind
                return null;
            }

            match = m_re.getMatch(text,0,flags | RE.REG_NOTBOL);
            if(match == null)
                return null;
            else
            {
                _start = match.getStartIndex() + 1;
                _end = match.getEndIndex() + 1;
            }
        }

        m_returnValue[0] = _start;
        m_returnValue[1] = _end;
        return m_returnValue;
    } //}}}

    //{{{ substitute() method
    /**
     * Returns the specified text, with any substitution specified
     * within this matcher performed.
     * @param text The text
     */
    public String substitute(String text) throws Exception
    {
        REMatch match = m_re.getMatch(text);
        if(match == null)
            return null;

        return match.substituteInto(m_replace);
    } //}}}

    //{{{ Private members
    
    //{{{ escapesToChars()
    
    private static String escapesToChars(String str) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch(c)
            {
            case '\\':
                if(i == str.length() - 1)
                {
                    buf.append('\\');
                    break;
                }
                c = str.charAt(++i);
                switch(c)
                {
                case 'n':
                    buf.append('\n');
                    break;
                case 't':
                    buf.append('\t');
                    break;
                default:
                    buf.append(c);
                    break;
                }
                break;
            default:
                buf.append(c);
            }
        }
        return buf.toString();
    } //}}}
    
    private String m_replace;
    private RE m_re;
    private int[] m_returnValue;
    //}}}
}
