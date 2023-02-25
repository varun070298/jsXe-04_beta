/*
BufferHistory.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2004 Ian Lewis (IanLewis@member.fsf.org)

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

//{{{ Java classes
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
//}}}

//{{{ SAX classes
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
//}}}

//{{{ DOM classes
import org.w3c.dom.*;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMError;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//}}}

//{{{ jsXe classes
import net.sourceforge.jsxe.dom.DOMOutput;
import net.sourceforge.jsxe.dom.DOMSerializer;
import net.sourceforge.jsxe.dom.DOMSerializerConfiguration;
import net.sourceforge.jsxe.util.Log;
//}}}

//}}}

/**
 * Maintains the files that have been used recently and their properties.
 * The recent.xml file in the .jsXe directory in the user's home directory
 * holds properties and info about how the user was using the document when
 * he or she closed it last. This way the user can resume work on a file
 * fairly seamlessly even though they closed the file previously or exited jsXe
 * entirely
 *
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: BufferHistory.java,v 1.13 2006/04/08 04:02:47 ian_lewis Exp $
 */
public class BufferHistory {

    //{{{ BufferHistory constructor
    /**
     * Creates a new empty buffer history
     */
    public BufferHistory() {}
    //}}}
    
    //{{{ getEntry()
    /**
     * Gets an entry in the buffer history for the path given.
     * @param path the path of the file in the buffer history
     * @return the BufferHistoryEntry object containing the info
     */
    public BufferHistoryEntry getEntry(String path) {
        //TODO: make this use a HashMap
        Iterator historyItr = m_history.iterator();
        while (historyItr.hasNext()) {
            BufferHistoryEntry nextEntry = (BufferHistoryEntry)historyItr.next();
            if (nextEntry.getPath().equals(path)) {
                return nextEntry;
            }
        }
        return null;
    }//}}}
    
    //{{{ getEntries()
    /**
     * Gets a list of all entries in the buffer history
     * @return an ArrayList of BufferHistoryEntry objects
     */
    public ArrayList getEntries() {
        return m_history;
    }//}}}
    
    //{{{ setEntry()
    /**
     * Adds or updates an entry in the buffer history for an open file
     * @param buffer the DocumentBuffer to update the history for
     * @param viewName the name of the document view that was being used
     */
    public void setEntry(DocumentBuffer buffer, String viewName) {
        if (!buffer.isUntitled()) {
            try {
                String path = buffer.getFile().getCanonicalPath();
                setEntry(path, viewName, buffer.getProperties());
            } catch (IOException ioe) {
                Log.log(Log.ERROR, this, ioe);
            }
        }
    }//}}}
    
    //{{{ setEntry()
    /**
     * Adds or updates an entry in the buffer history for the path given
     * @param the path of the file to update the history entry for
     * @param viewName the name of the document view that was being used
     * @param properties the properties to save to the history
     */
    public void setEntry(String path, String viewName, Properties properties) {
        BufferHistoryEntry entry = new BufferHistoryEntry(path, viewName, properties);
        setEntry(entry);
    }//}}}
    
    //{{{ setEntry()
    
    /**
     * Adds a new entry to the buffer history. If the entry is for a file that
     * is already in the history then the info in the history is updated with
     * the new info.
     * @param entry the BufferHistoryEntry to set to the history
     */
    public void setEntry(BufferHistoryEntry entry) {
        if (!m_history.contains(entry)) {
            String path = entry.getPath();
            BufferHistoryEntry previousEntry = getEntry(path);
            if (previousEntry != null) {
                m_history.remove(previousEntry);
            }
            m_history.add(0, entry);
            
            //remove entries from the bottom of the list if it's too big.
            int maxRecentFiles;
            try {
                maxRecentFiles = Integer.parseInt(jsXe.getProperty("max.recent.files"));
            } catch (NumberFormatException nfe) {
                try {
                    maxRecentFiles = Integer.parseInt(jsXe.getDefaultProperty("max.recent.files"));
                } catch (NumberFormatException nfe2) {}
            }
        }
    }//}}}
    
    //{{{ load()
    
    /**
     * Loads the buffer history from a file on disk.
     * @param file the file from which to load the buffer history
     */
    public void load(File file) throws IOException, SAXException, ParserConfigurationException {
        // if file doesn't exist then the history is empty
        Log.log(Log.NOTICE, this, "Loading buffer history: "+file.getName());
        m_history = new ArrayList();
        if (file.exists()) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                parser.parse(file, new BufferHistoryHandler());
                
            } catch (IllegalArgumentException iae) {}
        }
    }//}}}

   // //{{{ save()
   // /**
   //  * Saves the buffer history to a file on disk
   //  * @param file the file to save the buffer history to
   //  */
   // public void save(File file) throws IOException {
   //     Log.log(Log.NOTICE, this, "Saving buffer history: "+file.getName());
   //     String lineSep = System.getProperty("line.separator");
   //     
   //     BufferedWriter out = new BufferedWriter(new FileWriter(file));
   //     
   //     out.write("<?xml version=\"1.0\"?>");
   //     out.write(lineSep);
   //     out.write("<recent>");
   //     out.write(lineSep);
   //     
   //     int maxRecentFiles = 20;
   //     try {
   //         maxRecentFiles = Integer.parseInt(jsXe.getProperty("max.recent.files"));
   //     } catch (NumberFormatException nfe) {
   //         try {
   //             maxRecentFiles = Integer.parseInt(jsXe.getDefaultProperty("max.recent.files"));
   //         } catch (NumberFormatException nfe2) {}
   //     }
   //     
   //     int index = 0;
   //     Iterator historyItr = m_history.iterator();
   //     while (historyItr.hasNext() && index < maxRecentFiles) {
   //         
   //         ++index;
   //         
   //         BufferHistoryEntry entry = (BufferHistoryEntry) historyItr.next();
   //         out.write("<entry>");
   //         out.write(lineSep);
   //         
   //         String path = entry.getPath();
   //         out.write("<path><![CDATA[");
   //         out.write(path);
   //         out.write("]]></path>");
   //         out.write(lineSep);
   //         
   //         String viewName = entry.getViewName();
   //         out.write("<view>");
   //         out.write(viewName);
   //         out.write("</view>");
   //         out.write(lineSep);
   //         
   //         Properties props = entry.getProperties();
   //         Enumeration propertyItr = props.keys();
   //         while (propertyItr.hasMoreElements()) {
   //             String key = propertyItr.nextElement().toString();
   //             if (!m_excludeKeys.contains(key)) {
   //                 String value = props.getProperty(key);
   //                 out.write("<property name=\"");
   //                 out.write(key);
   //                 out.write("\" value=\"");
   //                 out.write(value);
   //                 out.write("\"/>");
   //                 out.write(lineSep);
   //             }
   //         }
   //         
   //         out.write("</entry>");
   //         out.write(lineSep);
   //     }
   //     
   //     out.write("</recent>");
   //     out.close();
   //     
   // }//}}}
    
    //{{{ save()
    /**
     * Saves the buffer history to a file on disk
     * @param file the file to save the buffer history to
     */
    public void save(File file) throws IOException {
        try {
            Log.log(Log.NOTICE, this, "Saving buffer history: "+file.getName());
            String lineSep = System.getProperty("line.separator");
            
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Node recent = document.appendChild(document.createElement("recent"));
            
            int maxRecentFiles = 20;
            try {
                maxRecentFiles = Integer.parseInt(jsXe.getProperty("max.recent.files"));
            } catch (NumberFormatException nfe) {
                try {
                    maxRecentFiles = Integer.parseInt(jsXe.getDefaultProperty("max.recent.files"));
                } catch (NumberFormatException nfe2) {}
            }
            
            int index = 0;
            Iterator historyItr = m_history.iterator();
            while (historyItr.hasNext() && index < maxRecentFiles) {
                
                ++index;
                
                BufferHistoryEntry entry = (BufferHistoryEntry) historyItr.next();
                Node entryNode = recent.appendChild(document.createElement("entry"));
                
                String path = entry.getPath();
                Node pathNode = entryNode.appendChild(document.createElement("path"));
                pathNode.appendChild(document.createCDATASection(path));
                
                String viewName = entry.getViewName();
                Node view = entryNode.appendChild(document.createElement("view"));
                view.appendChild(document.createTextNode(viewName));
                
                Properties props = entry.getProperties();
                Enumeration propertyItr = props.keys();
                while (propertyItr.hasMoreElements()) {
                    String key = propertyItr.nextElement().toString();
                    if (!m_excludeKeys.contains(key)) {
                        try {
                            String value = props.getProperty(key);
                            Element property = (Element)entryNode.appendChild(document.createElement("property"));
                            property.setAttribute("name", key);
                            property.setAttribute("value", value);
                        } catch (ClassCastException e) {
                            Log.log(Log.ERROR, this, e);
                        }
                    }
                }
            }
            
            //write the recent files
            DOMSerializerConfiguration config = new DOMSerializerConfiguration();
            config.setFeature(DOMSerializerConfiguration.SOFT_TABS, true);
            config.setParameter(DOMSerializerConfiguration.INDENT,  new Integer(2));
            config.setFeature(DOMSerializerConfiguration.FORMAT_XML, true);
            config.setParameter(DOMSerializerConfiguration.ERROR_HANDLER, new DOMErrorHandler() {
                public boolean handleError(DOMError error) {               
                    Log.log(Log.ERROR, this, error.getMessage());
                    return true;
                }
            });
            
            
            DOMSerializer serializer = new DOMSerializer(config);
            serializer.setNewLine(lineSep);
            serializer.write(document, new DOMOutput(out));
            
            out.close();
        } catch (ParserConfigurationException e) {
            Log.log(Log.ERROR, this, e);
        }
    }//}}}
    
    //{{{ BufferHistoryEntry class
    
    /**
     * Represents an entry in the buffer history. Entries are comprised of
     * the path of the file in the entry, the name of the document view that
     * was used to edit the document, and the properties that are associated
     * with that document.
     */
    public static class BufferHistoryEntry {
        
        //{{{ BufferHistoryEntry constructor
        /**
         * Creates a new Buffer history entry for the path given
         * @param path the path of the file for this entry
         * @param viewName the name of the view that was used
         * @param properties the properties associated with the document
         */
        public BufferHistoryEntry(String path, String viewName, Properties properties) {
            m_path = path;
            m_viewName = viewName;
            m_properties = properties;
        }//}}}
        
        //{{{ getPath()
        /**
         * Gets the path of the file for this entry
         * @return the path to the file
         */
        public String getPath() {
            return m_path;
        }//}}}
        
        //{{{ getViewName()
        /**
         * Gets the name of the document view used to edit the file. This is
         * the name normally returned by the method DocumentView.getViewName();
         * @return the view name
         * @see net.sourceforge.jsxe.gui.view.DocumentView#getViewName()
         */
        public String getViewName() {
            return m_viewName;
        }//}}}
        
        //{{{ getProperties()
        /**
         * Gets the properties associated with the file
         * @return the properties associated with the file
         */
        public Properties getProperties() {
            return m_properties;
        }//}}}
        
        //{{{ Private members
        
        private String m_path;
        private String m_viewName;
        private Properties m_properties;
        //}}}
    }//}}}
    
    //{{{ Private members
    
    //{{{ BufferHistoryHandler class
    /**
     * Used to load the buffer history
     */
    private class BufferHistoryHandler extends DefaultHandler {
        
        //{{{ characters()
        
        public void characters(char[] ch, int start, int length) {
            m_m_charData = new String(ch, start, length);
        }//}}}
        
        //{{{ endElement()
        
        public void endElement(String uri, String localName, String qName) {
            if (qName.equals("entry")) {
                Log.log(Log.MESSAGE, this, "Loading buffer history entry: "+m_m_path);
                m_history.add(new BufferHistoryEntry(m_m_path, m_m_viewName, m_m_properties));
            }
            
            if (qName.equals("path")) {
                m_m_path = m_m_charData;
            }
            
            if (qName.equals("view")) {
                m_m_viewName = m_m_charData;
            }
            
        }//}}}
        
        //{{{ startElement()
        
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            
            if (qName.equals("entry")) {
                m_m_properties=new Properties();
            }
            
            if (qName.equals("property")) {
                int length = attributes.getLength();
                String propName = null;
                String propValue = null;
                for (int i=0; i<length; i++) {
                    String name = attributes.getQName(i);
                    if (name.equals("name")) {
                        propName = attributes.getValue(i);
                    }
                    if (name.equals("value")) {
                        propValue = attributes.getValue(i);
                    }
                }
                if (!m_excludeKeys.contains(propName) && propName != null && propValue != null) {
                    m_m_properties.setProperty(propName, propValue);
                }
            }
        }//}}}
        
        //{{{ Private members
        Properties m_m_properties;;
        String m_m_charData;
        String m_m_path;
        String m_m_viewName;
        //}}}
        
    }//}}}
    
    private ArrayList m_history = new ArrayList();
    private static ArrayList m_excludeKeys;
    
    static {
        m_excludeKeys = new ArrayList();
        m_excludeKeys.add(DocumentBuffer.LINE_SEPARATOR);
    }
    //}}}
    
}
