/*
TabbedView.java
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

package net.sourceforge.jsxe.gui;

//{{{ imports
/*
All classes are listed explicitly so
it is easy to see which package it
belongs to.
*/

//{{{ jsXe classes
import net.sourceforge.jsxe.*;
import net.sourceforge.jsxe.action.*;
import net.sourceforge.jsxe.util.Log;
//}}}

//{{{ Swing components
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
//}}}

//{{{ AWT components
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
//}}}

//{{{ Java base classes
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ArrayList;
//}}}

//}}}

/**
 * The view container that holds the JTabbedPane that holds
 * all open DocumentViews.
 *
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: TabbedView.java,v 1.81 2006/04/07 15:44:06 ian_lewis Exp $
 * @see DocumentView
 */
public class TabbedView extends JFrame {
    
    //{{{ TabbedView constructor
    /**
     * Constructs a new TabbedView with the default view
     * @param buffer the buffer to use initially
     */
    public TabbedView(DocumentBuffer buffer) throws IOException {
        
        init();
        
        addDocumentBuffer(buffer);
    }//}}}
    
    //{{{ TabbedView constructor
    /**
     * Constructs a new TabbedView using the document view with the name specified.
     * @param buffer the buffer to use initially
     * @param documentViewName the document view name
     */
    public TabbedView(DocumentBuffer buffer, String documentViewName) throws IOException, UnrecognizedPluginException {
        
        init();
        
        addDocumentBuffer(buffer, documentViewName);
    }//}}}
    
    //{{{ getDocumentBuffer()
    
    public DocumentBuffer getDocumentBuffer() {
        return getDocumentView().getDocumentBuffer();
    }//}}}
    
    //{{{ getDocumentView()
    /**
     * Gets the current DocumentView that is being displayed
     * by the JTabbedPane
     * @return the current DocumentView
     */
    public DocumentView getDocumentView() {
        int index = tabbedPane.getSelectedIndex();
        if (index >= 0) {
            return (DocumentView)m_documentViews.get(index);
        } else {
            return null;
        }
    }//}}}
    
    //{{{ getDocumentView()
    /**
     * Gets the DocumentView for a currently opened DocumentBuffer.
     * DocumentBuffers should always be open as they cannot be created outside
     * of opening a document via jsXe.openXMLDocument()
     * @return the DocumentView object
     */
    public DocumentView getDocumentView(DocumentBuffer buffer) {
        if (buffer != null) {
            int docViewCount = m_documentViews.size();
            for (int i=0; i<docViewCount; i++) {
                DocumentView docView = (DocumentView)m_documentViews.get(i);
                if (docView.getDocumentBuffer() == buffer) {
                    return docView;
                }
            }
        }
        return null;
    }//}}}
    
    //{{{ addDocumentBuffer()
    /**
     * Adds a buffer to the main view. This is essentially opening
     * the document in jsXe.
     * @param buffer The DocumentBuffer to add to the view
     * @throws IOException if the buffer could not be opened
     * @throws UnrecognizedPluginException if the plugin requested doesn't exist or is not loaded
     */
    public void addDocumentBuffer(DocumentBuffer buffer, String documentViewName) throws IOException, UnrecognizedPluginException {
        
        ViewPlugin plugin = jsXe.getPluginLoader().getViewPlugin(documentViewName);
        
        if (plugin != null) {
            DocumentView newDocView = plugin.newDocumentView(buffer);
        
            buffer.addDocumentBufferListener(m_docBufListener);
            
            m_documentViews.add(newDocView);
            Component comp = newDocView.getDocumentViewComponent();
            
            tabbedPane.addTab(buffer.getName(), getTabIcon(buffer), comp);
            tabbedPane.setSelectedComponent(comp);
            
            updateTitle();
        } else {
            throw new UnrecognizedPluginException(documentViewName);
        }
    }//}}}
    
    //{{{ addDocumentBuffer()
    /**
     * Adds a buffer to the main view. This is essentially opening the document
     * in jsXe. The TabbedView attempts to open the buffer
     * in each registered DocumentView until it is successful.
     * @param buffer The DocumentBuffer to add to the view
     * @throws IOException if the buffer could not be opened.
     */
    public void addDocumentBuffer(DocumentBuffer buffer) throws IOException {
        if (buffer != null) {
            
            Iterator types = jsXe.getPluginLoader().getViewPluginNames().iterator();
            
            StringBuffer buf = new StringBuffer();
            
            while (types.hasNext()) {
                String viewName = types.next().toString();
                try {
                    addDocumentBuffer(buffer, viewName);
                    return;
                } catch (IOException ioe) {
                    buf.append(buffer.getName() + ": "+ioe.getMessage() + "\n");
                }
            }
            
            String msg = "Could not open buffer in any installed document views";
            String error = buf.toString();
            if (!error.equals("")) {
                msg=msg+"\n\n"+error;
            }
            throw new IOException(msg);
            
        }
    }//}}}
    
    //{{{ setDocumentBuffer()
    /**
     * Sets the current buffer and makes sure it is displayed. If
     * the document is not already open then this method does nothing.
     * @param buffer The buffer to set
     * @return true if the document was set and is visable
     */
    public boolean setDocumentBuffer(DocumentBuffer buffer) throws IOException {
        boolean success = true;
        if (buffer != null) {
            DocumentBuffer[] buffers = jsXe.getDocumentBuffers();
            for (int i=0; i < buffers.length; i++) {
                if (buffers[i] == buffer) {
                    tabbedPane.setSelectedIndex(i);
                    success = true;
                }
            }
            updateTitle();
           // updateMenuBar();
        }
        return success;
    }//}}}
    
    //{{{ removeDocumentBuffer()
    /**
     * Removes a buffer from the view. If the buffer passed is not
     * already open this method does nothing.
     * @param buffer The document to remove
     * @return true if the document was removed
     */
    public boolean removeDocumentBuffer(DocumentBuffer buffer) {
        if (buffer != null) {
            int docViewCount = m_documentViews.size();
            for (int i=0; i<docViewCount; i++) {
                DocumentView docView = (DocumentView)m_documentViews.get(i);
                if (docView.getDocumentBuffer() == buffer) {
                    //only close if we _mean_ it.
                    if (docView.close()) {
                        buffer.removeDocumentBufferListener(m_docBufListener);
                        tabbedPane.remove(i);
                        m_documentViews.remove(docView);
                        //if the tab removed is not the rightmost tab
                        //stateChanged is not called for the newly selected
                        //tab for some reason so we must update manually.
                        if (i != docViewCount - 1) {
                            updateTitle();
                            updateMenuBar();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }//}}}
    
    //{{{ getBufferCount()
    /**
     * Gets the number of open buffers.
     * @return The number of documents open in this view.
     */
    public int getBufferCount() {
        return tabbedPane.getTabCount();
    }//}}}
    
    //{{{ close()
    /**
     * Closes the view.
     * @return true if you really want to close.
     * @throws IOException if all the DocumentBuffers could not be saved
     *                     due to an I/O error.
     */
    public boolean close() throws IOException {
        
        DocumentView currentDocView = null;
        
        if (!jsXe.closeAllDocumentBuffers(this)) {
            return false;
        }
        
        /* original stuff here */
//        for (int i=0; i < buffers.length; i++) { 
//          if (!jsXe.closeDocumentBuffer(this, buffers[i])) {
//                return false;
//            }
//        }
        
        //save properties
        Rectangle bounds = getBounds();
        
        jsXe.setProperty("tabbedview.width",Integer.toString((int)bounds.getWidth()));
        jsXe.setProperty("tabbedview.height",Integer.toString((int)bounds.getHeight()));
        jsXe.setProperty("tabbedview.x",Integer.toString((int)bounds.getX()));
        jsXe.setProperty("tabbedview.y",Integer.toString((int)bounds.getY()));
        
        return true;
    }//}}}

    //{{{ Private static members
    
    private static final String _WIDTH = "tabbedview.width";
    private static final String _HEIGHT = "tabbedview.height";
    private static final String _X = "tabbedview.x";
    private static final String _Y = "tabbedview.y";
    
    private static final ImageIcon m_cleanIcon = new ImageIcon(jsXe.class.getResource("/net/sourceforge/jsxe/icons/clean.png"), "clean");
    private static final ImageIcon m_dirtyIcon = new ImageIcon(jsXe.class.getResource("/net/sourceforge/jsxe/icons/dirty.png"), "dirty");
    
    //}}}
    
    //{{{ Private members
    
    //{{{ updateTitle()
    /**
     * Updates the frame title. Useful when the tab has changed to a
     * different open document..
     */
    private void updateTitle() {
        DocumentView currentDocView = getDocumentView();
        if (currentDocView != null) {
            DocumentBuffer buffer = getDocumentBuffer();
            String name = "";
            if (buffer != null) {
                name = buffer.getName();
            }
            setTitle(jsXe.getAppTitle() + " - " + name);
        } else {
            setTitle(jsXe.getAppTitle());
        }
    }//}}}
    
    //{{{ updateMenuBar()
    /**
     * Updates the menubar. Useful when the DocumentView has changed.
     */
    private void updateMenuBar() {
        
        JMenuBar menubar = new JMenuBar();
        DocumentView currentDocView = getDocumentView();
        
        if (currentDocView != null) {
            
            updateRecentFilesMenu();
            
            menubar.add(m_fileMenu);

            //Add View Specific Menus
            JMenu[] menus = currentDocView.getMenus();
            if (menus != null) {
                for (int i=0;i<menus.length;i++) {
                    menubar.add(menus[i]);
                }
            }

            menubar.add(m_viewMenu);
            
            menubar.add(m_toolsMenu);
            
            menubar.add(m_helpMenu);
            
            setJMenuBar(menubar);
            
            //Need to cause a repaint after menubar is changed.
            getRootPane().revalidate();
        }
    }//}}}
    
    //{{{ updateRecentFilesMenu()
    
    private void updateRecentFilesMenu() {
        /*
        TODO: Make this more efficient
        Currently it creates new OpenRecentFileAction objects for every recent file
        every time you close a file, open a file, or change files
        */
        m_recentFilesMenu.removeAll();
        ArrayList historyEntries = jsXe.getBufferHistory().getEntries();
        int index = 0;
        JMenu addMenu = m_recentFilesMenu;
        Iterator historyItr = historyEntries.iterator();
        while (historyItr.hasNext()) {
            BufferHistory.BufferHistoryEntry entry = (BufferHistory.BufferHistoryEntry)historyItr.next();
            addMenu.add(new JMenuItem(new OpenRecentFileAction(this, entry)));
            index++;
        }
        if (addMenu.getItemCount() == 0) {
            JMenuItem nullItem = new JMenuItem(Messages.getMessage("File.Recent.None"));
            nullItem.setEnabled(false);
            addMenu.add(nullItem);
        }
    }//}}}
    
    //{{{ init()
    
    private void init() {
        
        tabbedPane.setName("TabbedView");
        
        //{{{ load global properties
        
        int width = jsXe.getIntegerProperty(_WIDTH, -1);
        
        int height = jsXe.getIntegerProperty(_HEIGHT, -1);
        
        int x = jsXe.getIntegerProperty(_X, -1);
        
        int y = jsXe.getIntegerProperty(_Y, -1);
        
        //}}}
        
        //{{{ build and add action set
        ActionSet set = new ActionSet("Built-In Commands");
        set.addAction("open-file", new FileOpenAction(this));
        set.addAction("save-file", new FileSaveAction(this));
        set.addAction("save-as", new FileSaveAsAction(this));
        set.addAction("reload-file", new FileReloadAction(this));
        set.addAction("close-file", new FileCloseAction(this));
        set.addAction("close-all", new FileCloseAllAction(this));
        set.addAction("exit", new FileExitAction(this));
        set.addAction("general-options", new ToolsOptionsAction(this));
        set.addAction("plugin-manager", new ToolsPluginManagerAction(this));
        set.addAction("about-jsxe", new jsxeAboutDialog(this));
        set.addAction("activity-log", new ActivityLogAction(this));
        set.addAction("validation-errors", new ValidationErrorsAction(this));
        jsXe.addActionSet(set);
        //}}}
        
        createDefaultMenuItems();
        updateMenuBar();
        
        tabbedPane.addChangeListener(//{{{
            new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    //it's possible to change to another file
                    //that is using another view.
                    updateMenuBar();
                    updateTitle();
                }
           });//}}}
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        pack();
        
        //Set window options
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowHandler());
        
        setIconImage(jsXe.getIcon().getImage());
        
        setBounds(new Rectangle(x, y, width, height));
        
    }//}}}
    
    //{{{ createDefaultMenuItems()
    
    private void createDefaultMenuItems() {
        
        //{{{ Create File Menu
        m_fileMenu = new JMenu(Messages.getMessage("File.Menu"));
        m_fileMenu.setMnemonic('F');
            JMenuItem menuItem = new JMenuItem(new FileNewAction(this));
            m_fileMenu.add( menuItem );
            menuItem = new JMenuItem(jsXe.getAction("open-file"));
            m_fileMenu.add( menuItem );
            
            //Add recent files menu
            m_recentFilesMenu = new EnhancedMenu(Messages.getMessage("File.Recent"), 20);
            m_fileMenu.add(m_recentFilesMenu);
            
            m_fileMenu.addSeparator();
            menuItem = new JMenuItem(jsXe.getAction("save-file"));
            m_fileMenu.add( menuItem );
            menuItem = new JMenuItem(jsXe.getAction("save-as"));
            m_fileMenu.add( menuItem );
            m_fileMenu.addSeparator();
            menuItem = new JMenuItem(jsXe.getAction("reload-file"));
            m_fileMenu.add( menuItem );
            m_fileMenu.addSeparator();
            menuItem = new JMenuItem(jsXe.getAction("close-file"));
            m_fileMenu.add( menuItem );
            menuItem = new JMenuItem(jsXe.getAction("close-all"));
            m_fileMenu.add( menuItem );
            menuItem = new JMenuItem(jsXe.getAction("exit"));
            m_fileMenu.add( menuItem );
        //}}}
        
        //{{{ Create View Menu
        m_viewMenu = new JMenu(Messages.getMessage("View.Menu"));
        m_viewMenu.setMnemonic('V');
        ArrayList views = jsXe.getPluginLoader().getViewPlugins();
        for (int i=0; i<views.size(); i++) {
            try {
                menuItem = new JMenuItem(new SetViewAction((ViewPlugin)views.get(i)));
                m_viewMenu.add( menuItem );
            } catch (UnrecognizedPluginException e) {
                jsXe.exiterror(this, e.getMessage(), 1);
            }
        }
        //}}}
        
        //{{{ Create Tools Menu
        m_toolsMenu = new JMenu(Messages.getMessage("Tools.Menu"));
        m_toolsMenu.setMnemonic('T');
            menuItem = new JMenuItem(jsXe.getAction("general-options"));
            m_toolsMenu.add(menuItem);
            menuItem = new JMenuItem(jsXe.getAction("plugin-manager"));
            m_toolsMenu.add(menuItem);
            menuItem = new JMenuItem(jsXe.getAction("validation-errors"));
            m_toolsMenu.add(menuItem);
            
            ArrayList plugins = jsXe.getPluginLoader().getAllPlugins();
            for (int i=0; i<plugins.size(); i++) {
                ActionPlugin plugin = (ActionPlugin)plugins.get(i);
                JMenu pluginMenu = plugin.getPluginMenu();
                if (pluginMenu != null) {
                    m_toolsMenu.add(pluginMenu);
                }
            }
        //}}}
        
        //{{{ Create Help Menu
        m_helpMenu = new JMenu(Messages.getMessage("Help.Menu"));
        m_helpMenu.setMnemonic('H');
            menuItem = new JMenuItem(jsXe.getAction("activity-log"));
            m_helpMenu.add(menuItem);
            menuItem = new JMenuItem(jsXe.getAction("about-jsxe"));
            m_helpMenu.add(menuItem);
        //}}}

    }//}}}
    
    //{{{ setDocumentView()
    /**
     * Sets the DocumentView.
     */
    private void setDocumentView(DocumentView newView) {
        
        DocumentView oldView = getDocumentView();
        
        int index = tabbedPane.getSelectedIndex();
        
        if (oldView != null) {
            
            //close the previous view
            oldView.close();
            
            DocumentBuffer currentBuffer = getDocumentBuffer();
            
            //no exceptions? cool. register the new view
            m_documentViews.remove(oldView);
            m_documentViews.add(index, newView);
            tabbedPane.remove(index);
            tabbedPane.add(newView.getDocumentViewComponent(), index);
            tabbedPane.setIconAt(index, getTabIcon(currentBuffer));
            tabbedPane.setTitleAt(index, currentBuffer.getName());
            tabbedPane.setSelectedIndex(index);
            
            //not sure why stateChanged doesn't get called above but
            //update manually
            updateMenuBar();
        }
    }//}}}
    
    //{{{ getTabIcon
    
    private ImageIcon getTabIcon(DocumentBuffer buffer) {
        
        if (buffer.getStatus(DocumentBuffer.DIRTY)) {
            //Mark the tab title as dirty
            return m_dirtyIcon;
        }
        
        return m_cleanIcon;
    }//}}}
    
    //{{{ SetViewAction class
    /**
     * Temporary class to change views.
     */
    private class SetViewAction extends AbstractAction {
        
        //{{{ Instance variables
        private ViewPlugin m_view;
        //}}}
        
        //{{{ SetDefaultViewAction constructor
        
        public SetViewAction(ViewPlugin view) throws UnrecognizedPluginException {
            
            //need to get the human readable name.
            m_view = view;
            putValue(Action.NAME, jsXe.getPluginLoader().getPluginProperty(m_view, JARClassLoader.PLUGIN_HUMAN_READABLE_NAME));
        }//}}}
        
        //{{{ actionPerformed()
        
        public void actionPerformed(ActionEvent e) {
            
            DocumentBuffer buffer = getDocumentBuffer();
            try {
                DocumentView view = m_view.newDocumentView(buffer);
                setDocumentView(view);
            } catch (IOException ioe) {
                Log.log(Log.WARNING, TabbedView.this, ioe.getMessage());
                JOptionPane.showMessageDialog(TabbedView.this, ioe, "I/O Error", JOptionPane.WARNING_MESSAGE);
            }
        }//}}}
        
    }//}}}
    
    //{{{ WindowHandler class
    
    private class WindowHandler extends WindowAdapter {
        
        //{{{ windowClosing()
        
        public void windowClosing(WindowEvent e) {
            jsXe.exit(TabbedView.this);
        }//}}}
        
    }//}}}
    
    private JMenu m_fileMenu;
    private JMenu m_viewMenu;
    private JMenu m_toolsMenu;
    private JMenu m_helpMenu;
    private EnhancedMenu m_recentFilesMenu;
    private ArrayList m_documentViews = new ArrayList();
    
    private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    //The current document
    private JPanel panel;
    
    private DocumentBufferListener m_docBufListener = new DocumentBufferListener() {//{{{
    
        public void propertiesChanged(DocumentBuffer source, String propertyKey) {}
        
        public void nameChanged(DocumentBuffer source, String newName) {//{{{
            updateTitle();
            DocumentBuffer[] buffers = jsXe.getDocumentBuffers();
            for (int i=0; i < buffers.length; i++) {
                if (buffers[i] == source) {
                    tabbedPane.setTitleAt(i, source.getName());
                }
            }
        };//}}}
        
        public void bufferSaved(DocumentBuffer source) {}
        
        public void statusChanged(DocumentBuffer source, int status, boolean oldStatus) {//{{{
                if (status == DocumentBuffer.DIRTY) {
                    //It's dirtyness has changed
                    //change the tab title
                    
                    DocumentBuffer[] buffers = jsXe.getDocumentBuffers();
                    for (int i=0; i < buffers.length; i++) {
                        if (buffers[i] == source) {
                            tabbedPane.setIconAt(i, getTabIcon(source));
                            return;
                        }
                    }
                }
            }//}}}
            
    };//}}}
    //}}}
}
