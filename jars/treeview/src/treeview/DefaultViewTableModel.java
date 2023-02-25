/*
DefaultViewTableModel.java
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

package treeview;

//{{{ imports
/*
All classes are listed explicitly so
it is easy to see which package it
belongs to.
*/

//{{{ jsXe classes
import net.sourceforge.jsxe.dom.AdapterNode;
import net.sourceforge.jsxe.dom.completion.ElementDecl;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.util.Log;
//}}}

//{{{ Swing components
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
//}}}

//{{{ AWT components
import java.awt.Component;
//}}}

//{{{ DOM classes
import org.w3c.dom.DOMException;
//import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//}}}

//{{{ Java base classes
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ListIterator;
//}}}

//}}}

/**
 * The TableModel used with the table used to display attributes 
 * in the lower left of the DefaultView
 * @author <a href="mailto:IanLewis at member dot fsf dot org">Ian Lewis</a>
 * @version $Id: DefaultViewTableModel.java,v 1.8 2005/05/30 21:27:32 ian_lewis Exp $
 * @see DefaultView
 */
public class DefaultViewTableModel implements TableModel {
    
    //{{{ DefaultViewTableModel constructor
    
    protected DefaultViewTableModel(Component parent, AdapterNode adapterNode) {
        m_currentNode = adapterNode;
        m_view = parent;
        updateTable(m_currentNode);
    }//}}}

    //{{{ TableModel methods
    
    //{{{ addTableModelListener()
    
    public void addTableModelListener(TableModelListener l) {
        if (l != null && !m_tableListenerList.contains(l) ) {
            m_tableListenerList.add(l);
        }
    }//}}}
    
    //{{{ getColumnClass()
    
    public Class getColumnClass(int columnIndex) {
        //the attributes table should contain strings only
        return (new String()).getClass();
    }//}}}
    
    //{{{ getColumnCount()
    
    public int getColumnCount() {
        //the attributes table will always contain 2 columns
        //an attribute and value
        return 2;
    }//}}}
    
    //{{{ getColumnName()
    
    public String getColumnName(int columnIndex) {
        if (columnIndex==0) {
            return Messages.getMessage("xml.attribute");
        } else {
            return Messages.getMessage("xml.attribute.value.short");
        }
    }//}}}
    
    //{{{ getRowCount()
    
    public int getRowCount() {
        return m_data[0].size();
    }//}}}

    //{{{ getValueAt()
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            String name = m_data[0].get(rowIndex).toString();
            ElementDecl eleDecl = m_currentNode.getElementDecl();
            ElementDecl.AttributeDecl attrDecl = ((eleDecl != null) ? eleDecl.getAttribute(name) : null);
            if (attrDecl != null && attrDecl.values != null) {
                return new EditTagDialog.Attribute.Value(m_data[columnIndex].get(rowIndex).toString(), attrDecl.values);
            }
        }
        return m_data[columnIndex].get(rowIndex);
    }//}}}
    
    //{{{ isCellEditable()
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        //Do not allow editing of attribute values that have no
        //attribute defined yet.
        if (columnIndex==1 && (((String)getValueAt(rowIndex,0)).equals(""))) {
            return m_addingAttribute;
        }
        return true;
    }//}}}
    
    //{{{ removeTableModelListener()
    
    public void removeTableModelListener(TableModelListener listener) {
        if (listener!=null) {
            m_tableListenerList.remove(listener);
        }
    }//}}}
    
    //{{{ setValueAt()
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        //pad with empty values if necessary (this shouldn't really happen)
        while (rowIndex+1 > getRowCount()) {
            m_data[columnIndex].add("");
        }
        try {
            //If setting a value on the last row
            if (rowIndex+1 == getRowCount()) {
                //we must be editing an attribute name.
                if (columnIndex == 0 && !aValue.equals("")) {
                    //DefaultViewTable ensures that we will set the value of this
                    //attribute directly after setting the attribute name
                    m_addingAttribute = true;
                    m_data[0].set(rowIndex, aValue.toString());
                    //now DefaultViewTable will force edit of the attribute value
                } else {
                    if (m_addingAttribute) {
                        // this must be before setAttribute in case exceptions are thrown.
                        m_addingAttribute = false;
                        
                        m_currentNode.setAttribute(m_data[0].get(rowIndex).toString(), aValue.toString());
                        updateTable(m_currentNode);
                        fireTableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE));
                    }
                }
            
            //Otherwise we are editing an existing attribute.
            } else {
                //We don't want to allow the user to set an attribute
                //name to an empty string.
                if (columnIndex==1 || !aValue.equals("")) {
                    //We need to check if there really is a change.
                    //If we don't the UI croaks NullPointerExceptions
                    //when trying to update the UI for the table.
                    if (!aValue.equals(getValueAt(rowIndex, columnIndex))) {
                        if (columnIndex == 0) {
                            //we are renaming the attribute, remove the old one first
                            String oldAttribute = (String)getValueAt(rowIndex, 0);
                            String oldValue;
                            Object rawOldValue = getValueAt(rowIndex, 1);
                            if (rawOldValue instanceof EditTagDialog.Attribute.Value) {
                                oldValue = ((EditTagDialog.Attribute.Value)rawOldValue).value;
                            } else {
                                oldValue = rawOldValue.toString();
                            }
                            m_currentNode.removeAttribute(oldAttribute);
                            try {
                                m_currentNode.setAttribute(aValue.toString(), oldValue);
                            } catch (DOMException e) {
                                //if we fail setting the new name then we need to put the old one back.
                                //this shouldn't throw exceptions
                                m_currentNode.setAttribute(oldAttribute, oldValue);
                                throw e;
                            }
                        } else {
                            m_currentNode.setAttribute((String)getValueAt(rowIndex, 0), aValue.toString());
                        }
                       // data[columnIndex].setElementAt(aValue,rowIndex);
                        updateTable(m_currentNode);
                        fireTableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE));
                    }
                }
            }
        } catch (DOMException dome) {
            updateTable(m_currentNode);
            JOptionPane.showMessageDialog(m_view, dome, "XML Error", JOptionPane.WARNING_MESSAGE);
        }
    }//}}}
    
    //}}}
    
    //{{{ removeRow()
    /**
     * Removes a row from the table and removes the attribute
     * from the node that this table is showing the attributes of.
     * @param row the index of the row in the table to remove
     */
    public void removeRow(int row) {
        try {
            m_currentNode.removeAttributeAt(row);
            updateTable(m_currentNode);
            fireTableChanged(new TableModelEvent(this, row));
        } catch (DOMException e) {
            JOptionPane.showMessageDialog(m_view, e, "XML Error", JOptionPane.WARNING_MESSAGE);
        }
    }//}}}
    
    //{{{ setAdapterNode()
    /**
     * Sets the AdapterNode that this table model is showing
     * the attributes of.
     * @param adapterNode the node to represent
     */
    public void setAdapterNode(AdapterNode adapterNode) {
        m_currentNode = adapterNode;
        updateTable(m_currentNode);
        fireTableChanged(new TableModelEvent(this));
    }//}}}
    
    //{{{ updateTable()
    
    protected void updateTable() {
        updateTable(m_currentNode);
    }//}}}
    
    //{{{ Private members
    
    //{{{ fireTableChanged()
    
    private void fireTableChanged(TableModelEvent e) {
        ListIterator listeners = m_tableListenerList.listIterator();
        while (listeners.hasNext()) {
            TableModelListener listener = (TableModelListener)listeners.next();
            listener.tableChanged(e);
        }
    }//}}}
    
    //{{{ updateTable()
    
    private void updateTable(AdapterNode selectedNode) {
        m_currentNode = selectedNode;
        m_data[0].removeAll(m_data[0]);
        m_data[1].removeAll(m_data[1]);
        if (selectedNode!=null) {
            NamedNodeMap attrs = selectedNode.getAttributes();
            if (selectedNode.getNodeType() == Node.ELEMENT_NODE) {
                if (attrs!=null) {
                    for(int i = 0; i < attrs.getLength(); i++) {
                        m_data[0].add(attrs.item(i).getNodeName());
                        m_data[1].add(attrs.item(i).getNodeValue());
                    }
                    //One extra table entry for adding an attribute
                    m_data[0].add("");
                    m_data[1].add("");
                }
            }
        }
    }//}}}
    
    private Component m_view;
    
    private boolean m_addingAttribute = false;
    
    private AdapterNode m_currentNode;
    private ArrayList m_tableListenerList = new ArrayList();
    private ArrayList[] m_data={
        new ArrayList(),
        new ArrayList()
    };
    //}}}

}
