/*
SourceViewSearchDialog.java
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

package sourceview;

//{{{ imports
/*
All classes are listed explicitly so
it is easy to see which package it
belongs to.
*/

//{{{ jsXe classes
import net.sourceforge.jsxe.jsXe;
import net.sourceforge.jsxe.DocumentBuffer;
import net.sourceforge.jsxe.dom.XMLDocument;
import net.sourceforge.jsxe.gui.EnhancedDialog;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.util.Log;
//}}}

//{{{ jEdit syntax classes
import org.syntax.jedit.JEditTextArea;
//}}}

//{{{ Swing components
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.AbstractAction;
import javax.swing.border.Border;
import javax.swing.text.Segment;
//}}}

//{{{ AWT components
import java.awt.*;
import java.awt.event.*;
//}}}

//{{{ Java base classes
import java.io.IOException;
//}}}

//}}}

public class SourceViewSearchDialog extends EnhancedDialog {
    
    //{{{ Private static members
    private static int m_dialogHeight = 200;
    private static int m_dialogWidth = 350;
    private static SourceViewSearchDialog m_dialog = null;
    private static final String IGNORE_CASE = "source.ignore.case";
    //}}}
    
    //{{{ Public static members
    
    //{{{ getSearchDialog()
    
    public static SourceViewSearchDialog getSearchDialog() {
        return m_dialog;
    }//}}}
    
    //{{{ showSearchDialog()
    
    public static void showSearchDialog(SourceView view) {
        if (m_dialog == null) {
            //Assumed that a view is added to a frame.
            Container parent = view.getParent();
            while (!(parent instanceof Frame)) {
                parent = parent.getParent();
            }
            Frame frame = (Frame)parent;
            //display find dialog
            m_dialog = new SourceViewSearchDialog(frame, view);
        }
        m_dialog.setVisible(true);
    }//}}}
    
    //}}}
    
    //{{{ SourceViewSearchDialog constructor
    
    public SourceViewSearchDialog(Frame parentFrame, SourceView view) {
        super(parentFrame, "Search and Replace", false);
        
        m_view = view;
        
        JPanel frame = new JPanel();
        getContentPane().add(frame,BorderLayout.CENTER);
        
        JButton findButton = new JButton("Find");
        JButton replaceButton = new JButton("Replace&Find");
       // JButton replaceAllButton = new JButton("Replace All");
        JButton cancelButton = new JButton(Messages.getMessage("common.cancel"));
        
        
        findButton.addActionListener(new FindAction());
        replaceButton.addActionListener(new ReplaceAction());
       // replaceAllButton.addActionListener(new ReplaceAllAction());
        cancelButton.addActionListener(new CancelAction());
        
        Border border = BorderFactory.createEmptyBorder(10,10,10,10);
        frame.setBorder(border);
        
        setSize(m_dialogWidth, m_dialogHeight);
        setLocationRelativeTo(parentFrame);
        
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        frame.setLayout(layout);
        
        m_findComboBox = new JComboBox();
        m_findComboBox.setName("FindComboBox");
        m_findComboBox.setEditable(true);
        
        m_replaceComboBox = new JComboBox();
        m_replaceComboBox.setName("ReplaceComboBox");
        m_replaceComboBox.setEditable(true);
        
        DocumentBuffer buffer = m_view.getDocumentBuffer();
        boolean ignoreCase = Boolean.valueOf(jsXe.getProperty(IGNORE_CASE)).booleanValue();
        m_ignoreCaseCheckBox = new JCheckBox("Ignore Case", ignoreCase);
        
        constraints.gridy      = 0;
        constraints.gridx      = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.weightx    = 1.0f;
        constraints.fill       = GridBagConstraints.BOTH;
        constraints.insets     = new Insets(1,0,1,0);
        
        JLabel searchLabel = new JLabel("Search for:");
        layout.setConstraints(searchLabel, constraints);
        frame.add(searchLabel);
        
        constraints.gridy      = 1;
        constraints.gridx      = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.weightx    = 1.0f;
        constraints.fill       = GridBagConstraints.BOTH;
        constraints.insets     = new Insets(1,0,1,0);
        
        layout.setConstraints(m_findComboBox, constraints);
        frame.add(m_findComboBox);
        
        constraints.gridy      = 2;
        constraints.gridx      = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.weightx    = 1.0f;
        constraints.fill       = GridBagConstraints.BOTH;
        constraints.insets     = new Insets(1,0,1,0);
        
        JLabel replaceLabel = new JLabel("Replace With:");
        layout.setConstraints(replaceLabel, constraints);
        frame.add(replaceLabel);
        
        constraints.gridy      = 3;
        constraints.gridx      = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.weightx    = 1.0f;
        constraints.fill       = GridBagConstraints.BOTH;
        constraints.insets     = new Insets(1,0,1,0);
        
        layout.setConstraints(m_replaceComboBox, constraints);
        frame.add(m_replaceComboBox);
        
        constraints.gridy      = 4;
        constraints.gridx      = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.weightx    = 1.0f;
        constraints.fill       = GridBagConstraints.BOTH;
        constraints.insets     = new Insets(1,0,1,0);
        
        layout.setConstraints(m_ignoreCaseCheckBox, constraints);
        frame.add(m_ignoreCaseCheckBox);
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(findButton);
        buttonsPanel.add(replaceButton);
        buttonsPanel.add(cancelButton);
        getContentPane().add(frame, BorderLayout.NORTH);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(findButton);
        
    }//}}}
    
    //{{{ dispose()
    
    public void dispose() {
        m_dialog = null;
        super.dispose();
    }//}}}
    
    //{{{ ok()
    
    public void ok() {
        find(false);
    }//}}}
    
    //{{{ cancel()
    
    public void cancel() {
        jsXe.setProperty(IGNORE_CASE, String.valueOf(m_ignoreCaseCheckBox.isSelected()));
        dispose();
    }//}}}
    
    //{{{ Private Members
    
    //{{{ FindAction class
    
    public class FindAction implements ActionListener {
        
        //{{{ actionPerformed()
        
        public void actionPerformed(ActionEvent e) {
            find(false);
        }//}}}
        
    }//}}}
    
    //{{{ CancelAction class
    
    private class CancelAction implements ActionListener {
        
        //{{{ actionPerformed()
        
        public void actionPerformed(ActionEvent e) {
            cancel();
        }//}}}
        
    }//}}}
    
    //{{{ ReplaceAction class
    
    private class ReplaceAction implements ActionListener {
        
        //{{{ actionPerformed()
        
        public void actionPerformed(ActionEvent e) {
            find(true);
        }//}}}
        
    }//}}}
    
    //{{{ find()
    
    private void find(boolean doReplace) {
        find(doReplace, m_view.getTextArea().getCaretPosition());
    }
    
    //{{{ find()
    
    private void find(boolean doReplace, int startIndex) {
        try {
            Object searchItem = m_findComboBox.getSelectedItem();
            Object replaceItem = m_replaceComboBox.getSelectedItem();
            boolean ignoreCase = m_ignoreCaseCheckBox.isSelected();
            
            String search = "";
            if (searchItem != null) {
                search = searchItem.toString();
            }
            
            String replace = "";
            if (replaceItem != null) {
                replace = replaceItem.toString();
            }
            
            RESearchMatcher matcher = new RESearchMatcher(search, replace, ignoreCase);
            
            JEditTextArea textArea = m_view.getTextArea();
            
            //replace previous text
            if (doReplace) {
                String selText = textArea.getSelectedText();
                if (selText != null && !selText.equals("")) { 
                    String replaceString = matcher.substitute(selText);
                    textArea.setSelectedText(replaceString);
                }
            }
            
            DocumentBuffer buffer = m_view.getDocumentBuffer();
            Segment seg = buffer.getSegment(0, buffer.getLength());
            int caretPosition = startIndex;
            CharIndexedSegment charSeg = new CharIndexedSegment(seg, caretPosition);
            
            int[] match = matcher.nextMatch(charSeg, false, true, true, false);
            
            buffer.setProperty(SourceView.LAST_FIND_STRING, search);
            
            if (match != null) {
                Log.log(Log.DEBUG, this, match[0] + " "+ match[1]);
                int start = match[0]+caretPosition;
                int end = match[1]+caretPosition;
               // textArea.requestFocus();
                textArea.select(start, end);
            } else {
                int again = JOptionPane.showConfirmDialog(m_view, "No more matches were found. Continue search from the beginning?", "No More Matches Found", JOptionPane.YES_NO_OPTION);
                if (again == 0) {
                    find(doReplace, 0);
                }
            }
            
            requestFocus();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(m_view, ex, "Search Error", JOptionPane.WARNING_MESSAGE);
        }
    }//}}}
    
    private SourceView m_view;
    private JComboBox m_findComboBox;
    private JComboBox m_replaceComboBox;
    private JCheckBox m_ignoreCaseCheckBox;
    
    //}}}
}
