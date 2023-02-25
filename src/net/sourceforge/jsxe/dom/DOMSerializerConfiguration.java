/*
DOMSerializerConfiguration.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

This attempts to conform to the DOM3 implementation in Xerces. It conforms
to DOM3 as of Xerces 2.3.0. I'm not one to stay on the bleeding edge but
there is as close to a standard interface for load & save as you can get and I
didn't want to work around the fact that current serializers aren't very good.

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

//{{{ DOM classes
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.DOMStringList;
//}}}

//{{{ Java classes
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
//}}}

import net.sourceforge.jsxe.util.Log;

//}}}

/**
 * <p>DOMSerializerConfiguration is the default implementation of the DOMConfiguration
 * interface to be used with the DOMSerializer class.</p>
 * 
 * <p>Currently, this class only supports the required options with few exceptions.
 *    The <code>"format-pretty-print"</code> option is supported.
 *    A <code>"soft-tabs"</code> option is supported which specifies whether
 *    to emulate tabs with spaces.
 *    An <code>"indent"</code> option is supported to specify the indent/tab
 *    size when the <code>"soft-tabs"</code> feature is true. This has no effect
 *    if <code>"soft-tabs"</code> is false.</p>
 *
 * @author <a href="mailto:IanLewis at member dot fsf dot org">Ian Lewis</a>
 * @version $Id: DOMSerializerConfiguration.java,v 1.17 2006/04/08 16:30:49 ian_lewis Exp $
 * @see DOMSerializer
 */
public class DOMSerializerConfiguration implements DOMConfiguration {
    
    //{{{ DOMConfiguration defined parameters
    public static final String CANONICAL_FORM         = "canonical-form";
    public static final String CDATA_SECTIONS         = "cdata-sections";
    public static final String CHAR_NORMALIZATION     = "check-character-normalization";
    public static final String COMMENTS               = "comments";
    public static final String DATATYPE_NORMALIZATION = "datatype-normalization";
    public static final String ENTITIES               = "entities";
    public static final String ERROR_HANDLER          = "error-handler";
    public static final String INFOSET                = "infoset";
    public static final String NAMESPACES             = "namespaces";
    public static final String NAMESPACE_DECLARATIONS = "namespace-declarations";
    public static final String NORMALIZE_CHARS        = "normalize-characters";
    public static final String SPLIT_CDATA            = "split-cdata-sections";
    public static final String VALIDATE_XML           = "validate";
    public static final String VALIDATE_IF_SCHEMA     = "validate-if-schema";
    public static final String WELL_FORMED            = "well-formed";
    public static final String WS_IN_ELEMENT_CONTENT  = "element-content-whitespace";
    //}}}
    
    //{{{ LSSerializer defined parameters
    public static final String DISCARD_DEFAULT_CONTENT    = "discard-default-content";
    public static final String FORMAT_XML                 = "format-pretty-print";
    public static final String IGNORE_UNKNOWN_CHAR_DENORM = "ignore-unknown-character-denormalizations";
    public static final String XML_DECLARATION            = "xml-declaration";
    //}}}
    
    //{{{ Additional parameters supported by DOMSerializerConfiguration
    /**
     * Use spaces instead of tabs if this is true.
     */
    public static final String SOFT_TABS    = "soft-tabs";
    /**
     * The number of spaces to use as an tab when SOFT_TABS is true.
     */
    public static final String INDENT       = "indent";
    /**
     * The encoding to use in the XML declaration.
     */
    public static final String XML_ENCODING = "encoding";
    //}}}
    
    //{{{ DOMSerializerConfiguration constructor
    
    public DOMSerializerConfiguration() {
        
        //set the default boolean parameters for a DOMConfiguration
        setFeature(CANONICAL_FORM,              false);
        setFeature(CDATA_SECTIONS,              true);
        setFeature(CHAR_NORMALIZATION,          false);
        setFeature(COMMENTS,                    true);
        setFeature(DATATYPE_NORMALIZATION,      false);
        setFeature(ENTITIES,                    true);
        //infoset is not present because it is determined
        //by checking the values of other features.
        setFeature(NAMESPACES,                  true);
        setFeature(NAMESPACE_DECLARATIONS,      true);
        setFeature(NORMALIZE_CHARS,             true);
        setFeature(SPLIT_CDATA,                 true);
        setFeature(VALIDATE_XML,                false);
        setFeature(VALIDATE_IF_SCHEMA,          false);
        setFeature(WELL_FORMED,                 true);
        setFeature(WS_IN_ELEMENT_CONTENT,       true);
        
        //LSSeraializer features
        setFeature(DISCARD_DEFAULT_CONTENT,     true);
        setFeature(FORMAT_XML,                  false);
        setFeature(IGNORE_UNKNOWN_CHAR_DENORM,  true);
        setFeature(XML_DECLARATION,             true);
        
        //DOMSerializer parameters
        setFeature(SOFT_TABS,                   false);
        setParameter(INDENT,                    new Integer(4));
        setParameter(XML_ENCODING,              null);
    }//}}}
    
    //{{{ DOMSerializerConfiguration constructor
    
    public DOMSerializerConfiguration(DOMConfiguration config) throws DOMException {
        this();
        Iterator iterator = m_supportedParameters.iterator();
        while (iterator.hasNext()) {
            String param = iterator.next().toString();
            setParameter(param, config.getParameter(param));
        }
    }//}}}
    
    //{{{ Implemented DOMConfiguration methods
    
    //{{{ canSetParameter()
    
    public boolean canSetParameter(String name, Object value) {
        
        if (value == null) {
            return (m_supportedParameters.indexOf(name) != -1);
        }
        
        if (value instanceof Boolean) {
            boolean booleanValue = ((Boolean)value).booleanValue();
            
            //couldn't think of a slicker way to do this
            //that was worth the time to implement
            //and extra processing.
            if (name.equals(CANONICAL_FORM)) {
                return !booleanValue;
            }
            if (name.equals(CDATA_SECTIONS)) {
                return true;
            }
            if (name.equals(CHAR_NORMALIZATION)) {
                return !booleanValue;
            }
            if (name.equals(COMMENTS)) {
                return true;
            }
            if (name.equals(DATATYPE_NORMALIZATION)) {
                return true;
            }
            if (name.equals(ENTITIES)) {
                return true;
            }
            if (name.equals(WELL_FORMED)) {
                return true;
            }
            if (name.equals(INFOSET)) {
                return true;
            }
            if (name.equals(NAMESPACES)) {
                return true;
            }
            if (name.equals(NAMESPACE_DECLARATIONS)) {
                return true;
            }
            if (name.equals(NORMALIZE_CHARS)) {
                return true;
            }
            if (name.equals(SPLIT_CDATA)) {
                return true;
            }
            if (name.equals(VALIDATE_XML)) {
                return !booleanValue;
            }
            if (name.equals(VALIDATE_IF_SCHEMA)) {
                return !booleanValue;
            }
            if (name.equals(WS_IN_ELEMENT_CONTENT)) {
                return true;
            }
            
            if (name.equals(DISCARD_DEFAULT_CONTENT)) {
                return true;
            }
            if (name.equals(FORMAT_XML)) {
                return true;
            }
            if (name.equals(IGNORE_UNKNOWN_CHAR_DENORM)) {
                return booleanValue;
            }
            if (name.equals(XML_DECLARATION)) {
                return true;
            }
            if (name.equals(SOFT_TABS)) {
                return true;
            }
            
            return false;
        } else {
            if (name.equals(ERROR_HANDLER)) {
                if (value instanceof DOMErrorHandler) {
                    return true;
                }
            }
            if (name.equals(INDENT)) {
                if (value instanceof Integer) {
                    return true;
                }
            }
            if (name.equals(XML_ENCODING)) {
                if (value instanceof String) {
                    return true;
                }
            }
        }
        return false;
    }//}}}
    
    //{{{ getParameter()
    
    public Object getParameter(String name) throws DOMException {
        
        if (m_supportedParameters.indexOf(name) != -1) {
            
            if (name.equals("infoset")) {
                boolean namespaceDeclarations = getFeature(NAMESPACE_DECLARATIONS);
                boolean validateIfSchema      = getFeature(VALIDATE_IF_SCHEMA);
                boolean entities              = getFeature(ENTITIES);
                boolean datatypeNormalization = getFeature(DATATYPE_NORMALIZATION);
                boolean cdataSections         = getFeature(CDATA_SECTIONS);
                
                boolean whitespace = getFeature(WS_IN_ELEMENT_CONTENT);
                boolean comments   = getFeature(COMMENTS);
                boolean namespaces = getFeature(NAMESPACES);
                
                return (Boolean.valueOf(!namespaceDeclarations &&
                        !validateIfSchema &&
                        !entities &&
                        !datatypeNormalization &&
                        !cdataSections &&
                        whitespace &&
                        comments &&
                        namespaces));
            } else {
                return m_parameters.get(name);
            }
            
        } else {
            
            throw new DOMException(DOMException.NOT_FOUND_ERR ,"NOT_FOUND_ERR: Parameter "+name+" not recognized");
            
        }
    }//}}}
    
    //{{{ getParameterNames()
    
    public DOMStringList getParameterNames() {
        return new DOMStringListImpl(m_supportedParameters);
    }//}}}
    
    //{{{ setParameter()
    
    public void setParameter(String name, Object value) throws DOMException {
        
        if (value instanceof String &&
            (value.toString().equalsIgnoreCase("true") ||
             value.toString().equalsIgnoreCase("false")))
        {
            Log.log(Log.WARNING,this, "Possibly setting XML serializer config boolean feature "+name+" with string value");
        }
        
        if (m_supportedParameters.indexOf(name) != -1) {
            if ( value != null ) {
                if (canSetParameter(name, value)) {
                    /*
                    if the parameter is infoset
                    then force the other parameters to
                    values that the infoset option
                    requires.
                    */
                    if (name.equals(INFOSET)) {
                        setFeature(NAMESPACE_DECLARATIONS,false);
                        setFeature(VALIDATE_IF_SCHEMA,    false);
                        setFeature(ENTITIES,              false);
                        setFeature(DATATYPE_NORMALIZATION,false);
                        setFeature(CDATA_SECTIONS,        false);
                        
                        setFeature(WS_IN_ELEMENT_CONTENT, true);
                        setFeature(COMMENTS,              true);
                        setFeature(NAMESPACES,            true);
                        return;
                    }
                    if (name.equals(FORMAT_XML) && ((Boolean)value).booleanValue()) {
                        /*
                        The element-content-whitespace parameter is ignored
                        when serializing since the parameter only makes sense
                        when the document is validated by DTD or Schema that
                        specifies that an element MUST have only child elements
                        (element-content).
                        
                        Also if the DOM validates this info when being edited
                        then the serializer could never write out whitespace
                        in element content without invalidating the document.
                        See
                        http://xml.apache.org/xerces2-j/javadocs/dom3-api/index.html
                        Section 2.10
                        */
                       // setFeature(WS_IN_ELEMENT_CONTENT, false);
                        setFeature(CANONICAL_FORM, false);
                    }
                   // if (name.equals(WS_IN_ELEMENT_CONTENT) && ((Boolean)value).booleanValue()) {
                   //     setFeature(FORMAT_XML, false);
                   // }
                    
                    m_parameters.put(name, value);
                    
                } else {
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Parameter "+name+" and value "+value.toString()+" not supported.");
                }
            } else {
                m_parameters.remove(name);
            }
        } else {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "Parameter "+name+" is not recognized.");
        }
    }//}}}
    
    //}}}
    
    //{{{ getFeature()
    
    /**
     * <p>A convenience method to retrieve the value that a boolean
     * parameter (feature) is set to.</p>
     * @param name The name of the feature to get the value of
     * @return The current setting for the given feature
     */
    public boolean getFeature(String name) throws DOMException {
        Object parameter = getParameter(name);
        
        if (name.equals("error-handler") || name.equals("indent") || !(parameter instanceof Boolean)) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "NOT_FOUND_ERR: "+name+" is not a feature.");
        }
        return ((Boolean)parameter).booleanValue();
        
    }//}}}
    
    //{{{ setFeature()
    
    /**
     * <p>A convenience method to set the value of a boolean parameter (feature)</p>
     * @param name The feature to set the value of
     * @param value The boolean value to set to the feature
     */
    public void setFeature(String name, boolean value) throws DOMException {
        setParameter(name, Boolean.valueOf(value));
    }//}}}
    
    //{{{ Private static members
    
    private static ArrayList m_supportedParameters = null;
    
    static {
        //create a vector of the supported parameters
        m_supportedParameters = new ArrayList(22);
        
        //DOMConfiguration defined parameters
        m_supportedParameters.add(CANONICAL_FORM);
        m_supportedParameters.add(CDATA_SECTIONS);
        m_supportedParameters.add(CHAR_NORMALIZATION);
        m_supportedParameters.add(COMMENTS);
        m_supportedParameters.add(DATATYPE_NORMALIZATION);
        m_supportedParameters.add(ENTITIES);
        m_supportedParameters.add(ERROR_HANDLER);
        m_supportedParameters.add(INFOSET);
        m_supportedParameters.add(NAMESPACES);
        m_supportedParameters.add(NAMESPACE_DECLARATIONS);
        m_supportedParameters.add(NORMALIZE_CHARS);
        m_supportedParameters.add(SPLIT_CDATA);
        m_supportedParameters.add(VALIDATE_XML);
        m_supportedParameters.add(VALIDATE_IF_SCHEMA);
        m_supportedParameters.add(WELL_FORMED);
        m_supportedParameters.add(WS_IN_ELEMENT_CONTENT);
        
        //LSSerializer defined parameters
        m_supportedParameters.add(DISCARD_DEFAULT_CONTENT);
        m_supportedParameters.add(FORMAT_XML);
        m_supportedParameters.add(IGNORE_UNKNOWN_CHAR_DENORM);
        m_supportedParameters.add(XML_DECLARATION);
        
        //Additional parameters supported by DOMSerializerConfiguration
        m_supportedParameters.add(SOFT_TABS);
        m_supportedParameters.add(INDENT);
        m_supportedParameters.add(XML_ENCODING);
    }//}}}
    
    //{{{ Private members
    
    //{{{ DOMStringListImpl class
    
    private static class DOMStringListImpl implements DOMStringList {
        
        //{{{ DOMStringListImpl constructor
        
        public DOMStringListImpl(ArrayList list) {
            m_list = list;
        }//}}}
        
        //{{{ contains()
        
        public boolean contains(String str) {
            for (int i=0; i<m_list.size(); i++) {
                if (m_list.get(i).toString().equals(str)) {
                    return true;
                }
            }
            return false;
        }//}}}
        
        //{{{ getLength()
        
        public int getLength() {
            return m_list.size();
        }//}}}
        
        //{{{ item()
        
        public String item(int index) {
            return m_list.get(index).toString();
        }//}}}
        
        //{{{ Private members
        private ArrayList m_list;
        //}}}
        
    }//}}}
    
    private Hashtable m_parameters = new Hashtable(16);
    
    //}}}
}
