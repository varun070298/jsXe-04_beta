/*
DOMSerializer.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

This attempts to conform to the DOM3 implementation in Xerces. It tries to
conform to DOM3 as of Xerces 2.6.0. I'm not one to stay on the bleeding edge
but it is as close to a standard interface for load & save as you can get
and I didn't want to work around the fact that current serializers aren't
very good.

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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
//}}}

//{{{ Java base classes
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
//}}}

//}}}

/**
 * An implementation of the DOM3 LSSerializer interface. This class supports
 * everything that is supported by the DOMSerializerConfiguration class. Clients
 *  can check if a feature is supported by calling canSetParameter() on the
 * appropriate DOMSerializerConfiguration object.
 *
 * @author <a href="mailto:IanLewis at member dot fsf dot org">Ian Lewis</a>
 * @version $Id: DOMSerializer.java,v 1.50 2006/04/18 21:45:20 ian_lewis Exp $
 */
public class DOMSerializer implements LSSerializer {
    
    //{{{ DOMSerializer constructor
    /**
     * Creates a default DOMSerializer using the default options.
     */
    public DOMSerializer() {
        config = new DOMSerializerConfiguration();
        m_newLine = System.getProperty("line.separator");
    }//}}}
    
    //{{{ DOMSerializer constructor
    /**
     * Creates a DOMSerializer that uses the configuration specified.
     * @param config The configuration to be used by this DOMSerializer object
     */
    public DOMSerializer(DOMSerializerConfiguration config) {
        this.config = config;
        m_newLine = System.getProperty("line.separator");
    }//}}}
    
    //{{{ Implemented LSSerializer methods
    
    //{{{ getConfig()
    
    public DOMConfiguration getDomConfig() {
        return config;
    }//}}}
    
    //{{{ getFilter()
    
    public LSSerializerFilter getFilter() {
        return m_filter;
    }//}}}
    
    //{{{ getNewLine()
    
    public String getNewLine() {
        return m_newLine;
    }//}}}
    
    //{{{ setFilter()
    
    public void setFilter(LSSerializerFilter filter) {
        m_filter=filter;
    }//}}}
    
    //{{{ setNewLine()
    
    public void setNewLine(String newLine) {
        m_newLine=newLine;
    }//}}}
    
    //{{{ write()
    
    public boolean write(Node nodeArg, LSOutput destination) {
        if (m_filter == null || m_filter.acceptNode(nodeArg) == 1) {
            
            //{{{ try to get the Writer object for our destination
            Writer writer = destination.getCharacterStream();
            String encoding = null;
            
            if (writer == null) {
                //no character stream specified, try the byte stream.
                OutputStream out = destination.getByteStream();
                if (out != null) {
                    
                    try {
                        writer = new OutputStreamWriter(out, destination.getEncoding());
                        encoding = destination.getEncoding();
                    } catch (UnsupportedEncodingException uee) {
                        DefaultDOMLocator loc = new DefaultDOMLocator(nodeArg, 1, 1, 0, null);
                        try {
                            throwError(loc, "unsupported-encoding", DOMError.SEVERITY_FATAL_ERROR, uee);
                        } catch (DOMSerializerException e) {/*we know this will happen*/}
                        //This is a fatal error, quit.
                        return false;
                    }
                } else {
                    //no char stream or byte stream, try the uri
                    String id = destination.getSystemId();
                    if (id != null) {
                        
                        try {
                            //We use URL since outputing to any other type of URI
                            //is not possible.
                            URL uri = new URL(id);
                            URLConnection con = uri.openConnection();
                            
                            try  {
                                //We want to try to output to the URI
                                con.setDoOutput(true);
                                //I don't see a problem with using caches
                                //do you?
                                con.setUseCaches(true);
                            } catch (IllegalStateException ise) {
                                //we are guaranteed to not be connected
                            }
                            
                            con.connect();
                            
                            writer = new OutputStreamWriter(con.getOutputStream(), destination.getEncoding());
                            
                        } catch (MalformedURLException mue) {
                            DefaultDOMLocator loc = new DefaultDOMLocator(nodeArg, 1, 1, 0, null);
                            try {
                                throwError(loc, "bad-uri", DOMError.SEVERITY_FATAL_ERROR, mue);
                            } catch (DOMSerializerException e) {/*we know this will happen*/}
                            //this is a fatal error
                            return false;
                        } catch (IOException ioe) {
                            DefaultDOMLocator loc = new DefaultDOMLocator(nodeArg, 1, 1, 0, null);
                            try {
                                throwError(loc, "io-error", DOMError.SEVERITY_FATAL_ERROR, ioe);
                            } catch (DOMSerializerException e) {/*we know this will happen*/}
                            //this is a fatal error
                            return false;
                        }
                        
                    } else {
                        DefaultDOMLocator loc = new DefaultDOMLocator(nodeArg, 1, 1, 0, null);
                        try {
                            throwError(loc, "no-output-specified", DOMError.SEVERITY_FATAL_ERROR, null);
                        } catch (DOMSerializerException e) {/*we know this will happen*/}
                        //this is a fatal error
                        return false;
                    }
                }
            }//}}}
            
            BufferedWriter bufWriter = new BufferedWriter(writer, IO_BUFFER_SIZE);
            
            try {
                serializeNode(bufWriter, nodeArg, encoding);
                bufWriter.close();
                return true;
            } catch (IOException ioe) {
                Object rawHandler = config.getParameter(DOMSerializerConfiguration.ERROR_HANDLER);
                if (rawHandler != null) {
                    DOMErrorHandler handler = (DOMErrorHandler)rawHandler;
                    DefaultDOMLocator loc = new DefaultDOMLocator(nodeArg, 1, 1, 0, null);
                    DOMSerializerError error = new DOMSerializerError(loc, ioe, DOMError.SEVERITY_FATAL_ERROR, "io-error");
                    handler.handleError(error);
                }
            } catch (DOMSerializerException dse) {
                Object rawHandler = config.getParameter(DOMSerializerConfiguration.ERROR_HANDLER);
                if (rawHandler != null) {
                    DOMErrorHandler handler = (DOMErrorHandler)rawHandler;
                    DOMError error = dse.getError();
                    handler.handleError(error);
                }
                //This is a fatal error, quit.
            }
        }
        return false;
    }//}}}
    
    //{{{ writeToString()
    
    public String writeToString(Node nodeArg) throws DOMException {
        StringWriter writer = new StringWriter();
        try {
            serializeNode(writer, nodeArg);
            //flush the output-stream. Without this
            //files are sometimes not written at all.
            writer.flush();
        } catch (DOMSerializerException dse) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, dse.getMessage());
        }
        return writer.toString();
    }//}}}

    //{{{ writeToURI()
    
    public boolean writeToURI(Node nodeArg, java.lang.String uri) {
        return write(nodeArg, new DOMOutput(uri, "UTF-8"));
    }//}}}
    
    //}}}
    
    //{{{ Private static members
    private static final int IO_BUFFER_SIZE = 32768;
    //}}}
    
    //{{{ Private members
    
    //{{{ DOMSerializerError class
    
    private static class DOMSerializerError implements DOMError {
        
        //{{{ DOMSerializerError constructor
        
        public DOMSerializerError(DOMLocator locator, Exception e, short s, String type) {
            m_exception = e;
            m_location = locator;
            m_severity = s;
            m_type = type;
        }//}}}
        
        //{{{ getLocation()
        
        public DOMLocator getLocation() {
            return m_location;
        }//}}}
        
        //{{{ getMessage()
        
        public String getMessage() {
            return m_exception.getMessage();
        }//}}}
        
        //{{{ getRelatedData()
        
        public Object getRelatedData() {
            return m_location.getRelatedNode();
        }//}}}
        
        //{{{ getRelatedException()
        
        public Object getRelatedException() {
            return m_exception;
        }//}}}
        
        //{{{ getSeverity()
        
        public short getSeverity() {
            return m_severity;
        }//}}}
        
        //{{{ getType()
        
        public String getType() {
            return m_type;
        }//}}}
        
        //{{{ Private members
                
        private Exception m_exception;
        private DOMLocator m_location;
        private short m_severity;
        private String m_type;
        //}}}

    }//}}}
    
    //{{{ serializeNode()
    
    private void serializeNode(Writer writer, Node node) throws DOMSerializerException {
        serializeNode(writer, node, null);
    }//}}}
    
    //{{{ serializeNode()
    /**
     * Serializes the node to the writer specified
     */
    private void serializeNode(Writer writer, Node node, String encoding) throws DOMSerializerException {
        rSerializeNode(writer, node, encoding, "", 1, 1, 0);
    }//}}}
    
    //{{{ rSerializeNode()
    /**
     * Designed to be called recursively and maintain the state of the
     * serialization.
     */
    private void rSerializeNode(Writer writer, Node node, String encoding, String currentIndent, int line, int column, int offset) throws DOMSerializerException {
        
        boolean formatting = config.getFeature(DOMSerializerConfiguration.FORMAT_XML);
       // boolean whitespace = config.getFeature(DOMSerializerConfiguration.WS_IN_ELEMENT_CONTENT);
        
        //This is used many times below as a temporary variable.
        String str = "";
        
        if (m_filter == null || m_filter.acceptNode(node) == 1) {
            switch (node.getNodeType()) {
                case Node.DOCUMENT_NODE://{{{
                    if (config.getFeature(DOMSerializerConfiguration.XML_DECLARATION)) {
                        String header = "<?xml version=\"1.0\"";
                        String realEncoding = (String)config.getParameter(DOMSerializerConfiguration.XML_ENCODING);
                        if (realEncoding == null) {
                            realEncoding = encoding;
                        }
                        if (realEncoding != null)
                            header += " encoding=\""+realEncoding+"\"";
                        header +="?>";
                        doWrite(writer, header, node, line, column, offset);
                        offset += header.length();
                        column += header.length();
                        
                        
                        //if not formatting write newLine here.
                        if (!formatting) {
                            column = 0;
                            line += 1;
                            doWrite(writer, m_newLine, node, line, column, offset);
                            offset += m_newLine.length();
                        }
                    }
                    
                    NodeList nodes = node.getChildNodes();
                    if (nodes != null) {
                        for (int i=0; i<nodes.getLength(); i++) {
                            rSerializeNode(writer, nodes.item(i), encoding, currentIndent, line, column, offset);
                        }
                    }
                    
                    break;//}}}
                case Node.ELEMENT_NODE://{{{
                    String nodeName   = node.getLocalName();
                    String nodePrefix = node.getPrefix();
                    if (nodeName == null) {
                        nodeName = node.getNodeName();
                    }
                    
                    if (formatting) {
                        //set to zero here for error handling (if doWrite throws exception).
                        column = 0;
                        str = m_newLine + currentIndent;
                        doWrite(writer, str, node, line, column, offset);
                        column += currentIndent.length();
                        offset += str.length();
                    }
                    
                    if (config.getFeature(DOMSerializerConfiguration.NAMESPACES) && nodePrefix != null) {
                        str = "<" + nodePrefix + ":" + nodeName;
                    } else {
                        str = "<" + nodeName;
                    }
                    
                    doWrite(writer, str, node, line, column, offset);
                    column += str.length();
                    offset += str.length();
                    
                    NamedNodeMap attr = node.getAttributes();
                    for (int i=0; i<attr.getLength(); i++) {
                        Attr currentAttr = (Attr)attr.item(i);
                        boolean writeAttr = false;
                        
                        /*
                        if we discard default content check if the attribute
                        was specified in the original document.
                        */
                        if (config.getFeature(DOMSerializerConfiguration.DISCARD_DEFAULT_CONTENT)) {
                            if (currentAttr.getSpecified()) {
                                writeAttr = true;
                            }
                        } else {
                            writeAttr = true;
                        }
                        
                        if (writeAttr) {
                            str = " " + currentAttr.getNodeName() + "=\"" + normalizeCharacters(currentAttr.getNodeValue()) + "\"";
                            doWrite(writer, str, node, line, column, offset);
                            column += str.length();
                            offset += str.length();
                        }
                    }
                    
                    NodeList children = node.getChildNodes();
                    if (children != null) {
                        
                        //check if element is empty or has
                        //only whitespace-only nodes
                        boolean elementEmpty = false;
                        if (children.getLength() <= 0) {
                            elementEmpty = true;
                        } else {
                            if (!config.getFeature(DOMSerializerConfiguration.WS_IN_ELEMENT_CONTENT)) {
                                boolean hasWSOnlyElements = true;
                                for(int i=0; i<children.getLength();i++) {
                                    hasWSOnlyElements = hasWSOnlyElements &&
                                        children.item(i).getNodeType()==Node.TEXT_NODE &&
                                        children.item(i).getNodeValue().trim().equals("");
                                }
                                elementEmpty = formatting && hasWSOnlyElements;
                            }
                        }
                        if (!elementEmpty) {
                            
                            str = ">";
                            doWrite(writer, str, node, line, column, offset);
                            column += str.length();
                            offset += str.length();
                            
                            String indentUnit = "";
                            
                            if (formatting) {
                                if (config.getFeature(DOMSerializerConfiguration.SOFT_TABS)) {
                                    //get the indent size and use it when serializing the children nodes.
                                    Integer indentSize = (Integer)config.getParameter("indent");
                                    if (indentSize != null) {
                                        int size = indentSize.intValue();
                                        StringBuffer buf = new StringBuffer();
                                        for (int i=0; i < size; i++) {
                                            buf.append(" ");
                                        }
                                        indentUnit = buf.toString();
                                    }
                                } else {
                                    indentUnit = "\t";
                                }
                            }
                            
                            
                            for(int i=0; i<children.getLength();i++) {
                                rSerializeNode(writer, children.item(i), encoding, currentIndent + indentUnit, line, column, offset);
                            }
                            
                            //don't add a new line if there is only text node children
                            if (formatting) {
                                
                                boolean allText = true;
                                for(int i=0; i<children.getLength();i++) {
                                    if (!(children.item(i).getNodeType()==Node.TEXT_NODE) &&
                                        !(children.item(i).getNodeType()==Node.CDATA_SECTION_NODE))
                                    {
                                        allText = false;
                                        break;
                                    }
                                }
                                
                                if (!allText) {
                                    //set to zero here for error handling (if doWrite throws exception).
                                    column = 0;
                                    str = m_newLine + currentIndent;
                                    doWrite(writer, str, node, line, column, offset);
                                    column += currentIndent.length();
                                    offset += str.length();
                                }
                            }
                            if (config.getFeature(DOMSerializerConfiguration.NAMESPACES) && nodePrefix != null) {
                                str = "</" + nodePrefix + ":" +nodeName + ">";
                            } else {
                                str = "</" + nodeName + ">";
                            }
                            doWrite(writer, str, node, line, column, offset);
                            column += str.length();
                            offset += str.length();
                            
                        } else {
                            str = "/>";
                            doWrite(writer, str, node, line, column, offset);
                            column += str.length();
                            offset += str.length();
                        }
                    }
                    break;//}}}
                case Node.TEXT_NODE://{{{
                    String text = node.getNodeValue();
                    //formatting implies no whitespace
                    //but to be explicit...
                   // if (!whitespace || formatting) {
                   //     text = text.trim();
                   // }
                    if (!text.equals("")) {
                        if (formatting) {
                            if (text.trim().equals("")) {
                                //ignore this whitespace only text if formatting
                                return;
                            }
                            
                            /*
                            don't format text nodes
                            if (node.getNextSibling() != null || node.getPreviousSibling() != null) {
                                line++;
                                column=0;
                                doWrite(writer, m_newLine, node, line, column, offset);
                                offset += m_newLine.length();
                            }
                            */
                        }
                        
                        //TODO: This is a dumb quick fix and should probably be changed.
                        for (int i=0; i<text.length();i++) {
                            //this must be first or it picks up the other
                            //entities.
                            str = text.substring(i, i+1);
                            if (str.equals("&")) {
                                str = "&amp;";
                            }
                            if (str.equals(">")) {
                                str = "&gt;";
                            }
                            if (str.equals("<")) {
                                str = "&lt;";
                            }
                            if (str.equals("\'")) {
                                str = "&apos;";
                            }
                            if (str.equals("\"")) {
                                str = "&quot;";
                            }
                            if (str.equals(m_newLine)) {
                                line++;
                                column=0;
                                doWrite(writer, m_newLine, node, line, column, offset);
                                offset += m_newLine.length();
                            } else {
                                doWrite(writer, str, node, line, column, offset);
                                column += str.length();
                                offset += str.length();
                            }
                        }
                    }
                    break;//}}}
                case Node.CDATA_SECTION_NODE://{{{
                    if (config.getFeature(DOMSerializerConfiguration.CDATA_SECTIONS)) {
                        //shouldn't add newlines for 
                       // if (formatting) {
                       //     //set to zero here for error handling (if doWrite throws exception)
                       //     column = 0;
                       //     str = m_newLine + currentIndent;
                       //     doWrite(writer, str, node, line, column, offset);
                       //     column += currentIndent.length();
                       //     offset += str.length();
                       // }
                        str = "<![CDATA[";
                        doWrite(writer, str, node, line, column, offset);
                        column += str.length();
                        offset += str.length();
                        
                        String cdata = node.getNodeValue();
                        for (int i=0; i<cdata.length(); i++) {
                            
                            str = cdata.substring(i, i+1);
                            if (str.equals("]") && i+3 < cdata.length() && cdata.substring(i, i+3).equals("]]>")) {
                                //split the cdata
                                DefaultDOMLocator loc = new DefaultDOMLocator(node, line, column, offset, null);
                                if (config.getFeature(DOMSerializerConfiguration.SPLIT_CDATA)) {
                                    i+=2;
                                    str = "]]]]>";
                                    /*
                                    if (formatting) {
                                        str += (m_newLine + currentIndent);
                                        column = currentIndent.length();
                                        offset += (m_newLine.length() + currentIndent.length());
                                    }
                                    */
                                    str += "<![CDATA[>";
                                    throwError(loc, "cdata-sections-splitted", DOMError.SEVERITY_WARNING, null);
                                } else {
                                    throwError(loc, "invalid-data-in-cdata-section", DOMError.SEVERITY_FATAL_ERROR, null);
                                }
                            }
                            if (str.equals(m_newLine)) {
                                line++;
                                column=0;
                                doWrite(writer, m_newLine, node, line, column, offset);
                                offset += m_newLine.length();
                            } else {
                                doWrite(writer, str, node, line, column, offset);
                                column += str.length();
                                offset += str.length();
                            }
                            
                        }
                        
                        str = "]]>";
                        doWrite(writer, str, node, line, column, offset);
                        column += str.length();
                        offset += str.length();
                    } else {
                        //transform to text node.
                        Node textNode = node.getOwnerDocument().createTextNode(node.getNodeValue());
                        rSerializeNode(writer, textNode, encoding, currentIndent, line, column, offset);
                    }
                    break;//}}}
                case Node.COMMENT_NODE://{{{
                    if (config.getFeature("comments")) {
                        if (formatting) {
                            //set to zero here for error handling (if doWrite throws exception)
                            column = 0;
                            str = m_newLine + currentIndent;
                            doWrite(writer, str, node, line, column, offset);
                            column += currentIndent.length();
                            offset += str.length();
                        }
                        str = "<!--"+node.getNodeValue()+"-->";
                        doWrite(writer, str, node, line, column, offset);
                        column += str.length();
                        offset += str.length();
                    }
                    break;//}}}
                case Node.PROCESSING_INSTRUCTION_NODE://{{{
                    
                    if (formatting) {
                        //set to zero here for error handling (if doWrite throws exception)
                        column = 0;
                        str = m_newLine + currentIndent;
                        doWrite(writer, str, node, line, column, offset);
                        column += currentIndent.length();
                        offset += str.length();
                    }
                    
                    str = "<?" + node.getNodeName() + " " + node.getNodeValue() + "?>";
                    doWrite(writer, str, node, line, column, offset);
                    column += str.length();
                    offset += str.length();
                    
                    break;//}}}
                case Node.ENTITY_REFERENCE_NODE://{{{
                    str = "&" + node.getNodeName() + ";";
                    doWrite(writer, str, node, line, column, offset);
                    column += str.length();
                    offset += str.length();
                    break;//}}}
                case Node.DOCUMENT_TYPE_NODE://{{{
                    DocumentType docType = (DocumentType)node;
                    
                    if (formatting) {
                        //set to zero here for error handling (if doWrite throws exception).
                        column = 0;
                        str = m_newLine + currentIndent;
                        doWrite(writer, str, node, line, column, offset);
                        column += currentIndent.length();
                        offset += str.length();
                    }
                    
                    str = "<!DOCTYPE " + docType.getName();
                    doWrite(writer, str, node, line, column, offset);
                    column += str.length();
                    offset += str.length();
                    if (docType.getPublicId() != null) {
                        str = " PUBLIC \"" + docType.getPublicId() + "\" ";
                        doWrite(writer, str, node, line, column, offset);
                        column += str.length();
                        offset += str.length();
                    }
                    if (docType.getSystemId() != null) {
                        if (docType.getPublicId() == null) {
                            str = " SYSTEM ";
                        } else {
                            str = "";
                        }
                        str +=  "\"" + docType.getSystemId() + "\"";
                        doWrite(writer, str, node, line, column, offset);
                        column += str.length();
                        offset += str.length();
                    }
                    
                    String internalSubset = docType.getInternalSubset();
                    if (internalSubset != null && !internalSubset.equals("")) {
                        str = " [ "+internalSubset+" ]";
                        doWrite(writer, str, node, line, column, offset);
                        column += str.length();
                        offset += str.length();
                    }
                    
                    str = ">";
                    doWrite(writer, str, node, line, column, offset);
                    column += str.length();
                    offset += str.length();
                    
                    //need to add a newline so that the next item is on a new line
                    //since text nodes are not included outside of the root element
                    if (!formatting) {
                        column = 0;
                        str = m_newLine + currentIndent;
                        doWrite(writer, str, node, line, column, offset);
                        column += currentIndent.length();
                        offset += str.length();
                    }
                    
                    break;//}}}
            }
        }
    }//}}}
    
    //{{{ doWrite()
    /**
     * Performs an actual write and implements error handling.
     */
    private void doWrite(Writer writer, String str, Node wnode, int line, int column, int offset) throws DOMSerializerException {
        try {
            writer.write(str, 0, str.length());
        } catch (IOException ioe) {
            
            DefaultDOMLocator loc = new DefaultDOMLocator(wnode, line, column, offset, null);
            
            throwError(loc, "io-error", DOMError.SEVERITY_FATAL_ERROR, ioe);
        }
    }//}}}
    
    //{{{ throwError()
    /**
     * Throws an error, notifying the ErrorHandler object if necessary.
     * @return the value returned by the error handler or false if the severity was SEVERITY_FATAL_ERROR
     */
    private void throwError(DOMLocator loc, String type, short severity, Exception e) throws DOMSerializerException {
        Object rawHandler = config.getParameter(DOMSerializerConfiguration.ERROR_HANDLER);
        boolean handled = false;
        if (severity == DOMError.SEVERITY_WARNING) {
            handled = true;
        }
        DOMSerializerError error = new DOMSerializerError(loc, e, severity, type);
        if (rawHandler != null) {
            DOMErrorHandler handler = (DOMErrorHandler)rawHandler;
            handled = handler.handleError(error);
        }
        
        if ((severity == DOMError.SEVERITY_ERROR && !handled) || severity == DOMError.SEVERITY_FATAL_ERROR) {
            throw new DOMSerializerException(error);
        }
    }//}}}
    
    //{{{ normalizeCharacters()
    
    private String normalizeCharacters(String text) {
        StringBuffer newText = new StringBuffer();
        //This is a dumb quick fix and should be changed.
        for (int i=0; i<text.length();i++) {
            //this must be first or it picks up the other
            //entities.
            String str = text.substring(i, i+1);
            if (str.equals("&")) {
                str = "&amp;";
            }
            if (str.equals(">")) {
                str = "&gt;";
            }
            if (str.equals("<")) {
                str = "&lt;";
            }
            if (str.equals("\'")) {
                str = "&apos;";
            }
            if (str.equals("\"")) {
                str = "&quot;";
            }
            newText.append(str);
        }
        return newText.toString();
    }//}}}
    
    private DOMSerializerConfiguration config;
    private LSSerializerFilter m_filter;
    private String m_newLine;
    
    //}}}
}
