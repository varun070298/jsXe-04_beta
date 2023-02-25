/*
ProgressSplashScreenWindow.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 1998, 1999, 2001 Trish Hartnett
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
package net.sourceforge.jsxe.gui;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import net.sourceforge.jsxe.util.Log;
import net.sourceforge.jsxe.jsXe;

/**
 * A splashscreen that popups when the jsxe application starts
 * @author Trish Hartnett (<a href="mailto:trishah136@users.sourceforge.net">trishah136@users.sourceforge.net</a>)
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: ProgressSplashScreenWindow.java,v 1.6 2006/04/18 19:35:05 ian_lewis Exp $
 */
public class ProgressSplashScreenWindow extends JWindow {   
    private JProgressBar progressBar;
    //private JTextArea versionOutput;
    private JScrollPane outputScroll;
    private JLabel imageLabel;

     //{{{ ProgressSplashScreenWindow constructor
    public ProgressSplashScreenWindow() {
        initComponents();
        setSize(200, 185);  
        updateSplashScreenDialog(0); // set status to 1 initially
    }
    //  }}}

     //{{{ initComponents()
    /**
     * Initializes the components for the splash screen.
     *
     */
    public void initComponents() {
        
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.TOP);
        imageLabel.setIcon(new javax.swing.ImageIcon(
                ProgressSplashScreenWindow.class
                        .getResource("/net/sourceforge/jsxe/icons/jsxeBig.jpg")));

        //JPanel middlePanel = new JPanel();
        //m_versionOutput = new JTextArea(1,20);
        //m_versionOutput.setText(jsXe.getAppTitle() + " " + jsXe.getVersion());
        //m_versionOutput.setEditable(false);
        
        int barLength = 100; 
        progressBar = new JProgressBar(0, barLength);
        progressBar.setSize(100, 20);
        progressBar.setValue(0);
        //progressBar.setString("0%");
        progressBar.setString(jsXe.getVersion());
        progressBar.setStringPainted(true);
        //middlePanel.setLayout(new BorderLayout());
        //middlePanel.add(m_versionOutput, BorderLayout.NORTH);
        //middlePanel.add(progressBar, BorderLayout.CENTER);

        /*
        versionOutput = new JTextArea(1, 20);
        versionOutput.setMargin(new Insets(5, 5, 5, 5));
        versionOutput.setEditable(false);
        versionOutput.setFont(new java.awt.Font("Monospaced", 0, 12));
        
        String version = "Version " + jsXe.getVersion();
        StringBuffer buf = new StringBuffer();
        int spaces = (int)((25 - version.length())/2);
        for (int i=0;i<spaces;i++) {
            buf.append(" ");
        }
        version = buf.toString()+version;
        versionOutput.setText(version);
        */
        
        JPanel contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(imageLabel, BorderLayout.NORTH);
        //contentPane.add(versionOutput, BorderLayout.CENTER);
        contentPane.add(progressBar, BorderLayout.SOUTH);
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
    }
    //  }}}

     //{{{ updateSplashScreenDialog()
    /**
     * Updates the progress bar displayed by the splashscreen
     *
     * @param progress The new progress setting for the progress bar. if progress is 4, splashscreen disposes itself.
     */
    public void updateSplashScreenDialog(int progress) {
        updateProgessBar(progress);
        //updateTextArea(progress);
    //  try {
    //      Thread.sleep(1000);
    //  } catch (InterruptedException e) {
    //      e.printStackTrace();
    //  }
    }
    //  }}}

     //{{{ updateProgessBar()
    /**
     * Updates the progress bar displayed by the splashscreen
     *
     * @param percentage Updates percentage completed of progressbar
     */
    public void updateProgessBar(int percentage) {      
        progressBar.setValue(percentage);
        //progressBar.setString(percentage + "%");
    } //}}}

}
