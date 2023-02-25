/*
ErrorListDialog.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2001 Slava Pestov
Portions Copyright (C) 2005 Ian Lewis (IanLewis@member.fsf.org)

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

package net.sourceforge.jsxe.gui;

//{{{ Imports
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;
import net.sourceforge.jsxe.util.Log;
//}}}

/**
 * A dialog that displays multiple errors based on a Vector containing string
 * objects.
 * @author Slava Pestov
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: ErrorListDialog.java,v 1.3 2005/04/18 20:03:17 ian_lewis Exp $
 */
public class ErrorListDialog extends EnhancedDialog {
    
        //{{{ ErrorListDialog constructor
        public ErrorListDialog(Frame frame, String title, String caption, Vector messages, boolean pluginError) {
            super(frame,title,!pluginError);

            JPanel content = new JPanel(new BorderLayout(12,12));
            content.setBorder(new EmptyBorder(12,12,12,12));
            setContentPane(content);

            Box iconBox = new Box(BoxLayout.Y_AXIS);
            iconBox.add(new JLabel(UIManager.getIcon("OptionPane.errorIcon")));
            iconBox.add(Box.createGlue());
            content.add(BorderLayout.WEST,iconBox);

            JPanel centerPanel = new JPanel(new BorderLayout());

            JLabel label = new JLabel(caption);
            label.setBorder(new EmptyBorder(0,0,6,0));
            centerPanel.add(BorderLayout.NORTH,label);

            JList errors = new JList(messages);
           // errors.setCellRenderer(new ErrorListCellRenderer());
            errors.setVisibleRowCount(Math.min(Math.max(messages.size(),4),10));

            // need this bullshit scroll bar policy for the preferred size
            // hack to work
            JScrollPane scrollPane = new JScrollPane(errors,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            Dimension size = scrollPane.getPreferredSize();
            size.width = Math.max(size.width,400);
            scrollPane.setPreferredSize(size);

            centerPanel.add(BorderLayout.CENTER,scrollPane);

            content.add(BorderLayout.CENTER,centerPanel);

            Box buttons = new Box(BoxLayout.X_AXIS);
            buttons.add(Box.createGlue());

            ok = new JButton("OK");
            ok.addActionListener(new ActionHandler());

            buttons.add(ok);

            buttons.add(Box.createGlue());
            content.add(BorderLayout.SOUTH,buttons);

            getRootPane().setDefaultButton(ok);

            pack();
            setLocationRelativeTo(frame);
            setVisible(true);
        } //}}}

        //{{{ ok() method
        public void ok() {
            dispose();
        } //}}}

        //{{{ cancel() method
        public void cancel(){
            dispose();
        } //}}}

        //{{{ Private members
        private JButton ok, pluginMgr;
        //}}}

        //{{{ ActionHandler class
        class ActionHandler implements ActionListener {
            //{{{ actionPerformed() method
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource() == ok) {
                    dispose();
                }
            } //}}}
        } //}}}
}
