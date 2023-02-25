/*
AddDocTypeAction.java
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

package treeview.action;

//{{{ imports

import treeview.*;

//{{{ AWT classes
import java.awt.event.ActionEvent;
import java.awt.*;
//}}}

//{{{ Swing classes
import javax.swing.*;
//}}}

//{{{ DOM classes
import org.w3c.dom.DOMException;
//}}}

//{{{ jsXe classes
import net.sourceforge.jsxe.jsXe;
import net.sourceforge.jsxe.DocumentBuffer;
import net.sourceforge.jsxe.gui.DocumentView;
import net.sourceforge.jsxe.gui.EnhancedDialog;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.gui.TabbedView;
import net.sourceforge.jsxe.util.Log;
//}}}

//{{{ Java classes
import java.io.IOException;
//}}}

//}}}

/**
 * An action that adds a document type to the XML Document.
 *
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: AddDocTypeAction.java,v 1.3 2006/04/08 20:37:41 ian_lewis Exp $
 */
public class AddDocTypeAction extends AbstractAction {
    
    //{{{ AddDocTypeAction constructor
    /**
     * Creates a action that adds a new document type node.
     */
    public AddDocTypeAction() {}//}}}
    
    //{{{ actionPerformed()
    
    public void actionPerformed(ActionEvent e) {
        TabbedView tabbedview = jsXe.getActiveView();
        DocumentView view = tabbedview.getDocumentView();
        if (view instanceof DefaultView) {
            m_defView = (DefaultView)view;
            m_tree = m_defView.getDefaultViewTree();
            
            /*
            Show a dialog to prompt for name and PUBLIC and SYSTEM identifiers.
            */
            EditDocTypeDialog dialog = new EditDocTypeDialog(tabbedview, null, null, null);
        }
    }//}}}
    
    //{{{ Private members
    
    DefaultView m_defView;
    DefaultViewTree m_tree;
    
    //{{{ EditDocTypeDialog
    
    private class EditDocTypeDialog extends EnhancedDialog {
        
        //{{{ EditDocTypeDialog constructor
        
        public EditDocTypeDialog(TabbedView parent, String name, String publicId, String systemId) {
            super(parent, Messages.getMessage("TreeView.EditDocType.Title"), true);
            
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            
            getContentPane().setLayout(layout);
            
            int gridY = 0;
            
            JLabel nameLabel = new JLabel(Messages.getMessage("xml.doctype.name"));
            m_nameField = new JTextField(name);
            
            JLabel publicIDLabel = new JLabel(Messages.getMessage("xml.doctype.public"));
            m_publicIDField = new JTextField(publicId);
            
            JLabel systemIDLabel = new JLabel(Messages.getMessage("xml.doctype.system"));
            m_systemIDField = new JTextField(systemId);
            
            constraints.gridy      = gridY;
            constraints.gridx      = 0;
            constraints.gridheight = 1;
            constraints.gridwidth  = 1;
            constraints.weightx    = 1.0f;
            constraints.fill       = GridBagConstraints.BOTH;
            constraints.insets     = new Insets(1,0,1,0);
            
            layout.setConstraints(nameLabel, constraints);
            getContentPane().add(nameLabel);
            
            constraints.gridy      = gridY++;
            constraints.gridx      = 1;
            constraints.gridheight = 1;
            constraints.gridwidth  = 1;
            constraints.weightx    = 1.0f;
            constraints.fill       = GridBagConstraints.BOTH;
            constraints.insets     = new Insets(1,0,1,0);
            
            layout.setConstraints(m_nameField, constraints);
            getContentPane().add(m_nameField);
            
            constraints.gridy      = gridY;
            constraints.gridx      = 0;
            constraints.gridheight = 1;
            constraints.gridwidth  = 1;
            constraints.weightx    = 1.0f;
            constraints.fill       = GridBagConstraints.BOTH;
            constraints.insets     = new Insets(1,0,1,0);
            
            layout.setConstraints(publicIDLabel, constraints);
            getContentPane().add(publicIDLabel);
            
            constraints.gridy      = gridY++;
            constraints.gridx      = 1;
            constraints.gridheight = 1;
            constraints.gridwidth  = 1;
            constraints.weightx    = 1.0f;
            constraints.fill       = GridBagConstraints.BOTH;
            constraints.insets     = new Insets(1,0,1,0);
            
            layout.setConstraints(m_publicIDField, constraints);
            getContentPane().add(m_publicIDField);
            
            constraints.gridy      = gridY;
            constraints.gridx      = 0;
            constraints.gridheight = 1;
            constraints.gridwidth  = 1;
            constraints.weightx    = 1.0f;
            constraints.fill       = GridBagConstraints.BOTH;
            constraints.insets     = new Insets(1,0,1,0);
            
            layout.setConstraints(systemIDLabel, constraints);
            getContentPane().add(systemIDLabel);
            
            constraints.gridy      = gridY++;
            constraints.gridx      = 1;
            constraints.gridheight = 1;
            constraints.gridwidth  = 1;
            constraints.weightx    = 1.0f;
            constraints.fill       = GridBagConstraints.BOTH;
            constraints.insets     = new Insets(1,0,1,0);
            
            layout.setConstraints(m_systemIDField, constraints);
            getContentPane().add(m_systemIDField);
            
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
            
            buttonPanel.add(Box.createGlue());
            JButton close = new JButton(Messages.getMessage("common.ok"));
            close.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    ok();
                }
            });
            JButton cancel = new JButton(Messages.getMessage("common.cancel"));
            cancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancel();
                }
            });
            
            getRootPane().setDefaultButton(close);
            buttonPanel.add(close);
            buttonPanel.add(Box.createGlue());
            buttonPanel.add(cancel);
            buttonPanel.add(Box.createGlue());
            
            constraints.gridy      = gridY++;
            constraints.gridx      = 0;
            constraints.gridheight = 1;
            constraints.gridwidth  = 2;
            constraints.weightx    = 1.0f;
            constraints.fill       = GridBagConstraints.BOTH;
            constraints.insets     = new Insets(1,0,1,0);
            
            layout.setConstraints(buttonPanel, constraints);
            getContentPane().add(buttonPanel);
            
            loadGeometry(this, m_geometryName);
            
            updateSize();
            
            show();
            
        }//}}}
        
        //{{{ getName()
        public String getName() {
            return m_nameField.getText();
        }//}}}
        
        //{{{ getPublicId()
        public String getPublicId() {
            return m_publicIDField.getText();
        }//}}}
        
        //{{{ getSystemId()
        public String getSystemId() {
            return m_systemIDField.getText();
        }//}}}
        
        //{{{ ok()
        public void ok() {
            String name = getName();
            String systemId = getSystemId();
            String publicId = getPublicId();
            
            if (name != null && !name.equals("") && systemId != null && !systemId.equals("")) {
            
                DocumentBuffer document = m_defView.getDocumentBuffer();
                /*
                DocumentType nodes are not modifiable so we need to insert the
                XML text. The DocumentBuffer will re-parse as necessary.
                */
                
                /*
                We need to find the end of the XML declaration. So we know where to
                insert the DocType
                */
                try {
                    boolean found = false;
                    for (int i=1; !found; i++) {
                        String text = document.getText(0, Math.min(100, document.getLength())*i);
                        int end = text.indexOf("?>");
                        if (end != -1) {
                            found = true;
                            end += 2; //We want the index after the 2 ?> characters
                            
                            StringBuffer docType = new StringBuffer("\n<!DOCTYPE "+name);
                            if (publicId != null && !publicId.equals("")) {
                                docType.append(" PUBLIC \""+publicId+"\"");
                            }
                            if ((publicId == null || publicId.equals("")) && systemId != null && !systemId.equals("")) {
                                docType.append(" SYSTEM");
                            }
                            if (systemId != null && !systemId.equals("")) {
                                docType.append(" \""+systemId+"\"");
                            }
                            docType.append(">\n");
                            document.insertText(end,docType.toString());
                        }
                    }
                    m_tree.updateUI();
                } catch (IOException ioe) {
                    jsXe.exiterror(this, ioe, 1);
                }
            } else {
                JOptionPane.showMessageDialog(this, "A Document Type Definition must have a name and SYSTEM Identifier", "Error", JOptionPane.WARNING_MESSAGE);
            }
            
            cancel();
        }//}}}
        
        //{{{ cancel()
        public void cancel() {
            saveGeometry(this, m_geometryName);
            dispose();
        }//}}}
        
        //{{{ Private Members
        
        //updateSize()
        private void updateSize() {
            Dimension currentSize = getSize();
            Dimension requestedSize = getPreferredSize();
            Dimension newSize = new Dimension(
                Math.max(currentSize.width,requestedSize.width),
                Math.max(currentSize.height,requestedSize.height)
            );
            if(newSize.width < 300)
                newSize.width = 300;
            if(newSize.height < 150)
                newSize.height = 150;
            setSize(newSize);
            validate();
        } //}}}
        
        private JTextField m_nameField;
        private JTextField m_publicIDField;
        private JTextField m_systemIDField;
        private static final String m_geometryName = "treeview.editdoctypenode";
        //}}}
        
    }//}}}
    
    //}}}
}

