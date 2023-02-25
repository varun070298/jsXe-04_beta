/*
JARClassLoader.java
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
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
Optionally, you may find a copy of the GNU General Public License
from http://www.fsf.org/copyleft/gpl.txt
*/

package net.sourceforge.jsxe;

//{{{ Imports

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.lang.reflect.Modifier;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.util.ArrayListEnumeration;
import net.sourceforge.jsxe.util.Log;
import net.sourceforge.jsxe.util.MiscUtilities;

//}}}

/**
 * A class loader implementation that loads classes from JAR files. Also manages
 * getting files from plugin JARs.
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: JARClassLoader.java,v 1.18 2006/02/04 00:34:38 ian_lewis Exp $
 * @since jsXe 0.4 beta
 */
public class JARClassLoader extends ClassLoader {
    
    //{{{ Public static members
    public static final String PLUGIN_NAME    = "jsxe-plugin-name";
    public static final String PLUGIN_CLASS   = "jsxe-plugin-class";
    public static final String PLUGIN_VERSION = "jsxe-plugin-version";
    public static final String PLUGIN_URL     = "jsxe-plugin-url";
    public static final String PLUGIN_HUMAN_READABLE_NAME = "jsxe-plugin-human-readable-name";
    public static final String PLUGIN_DESCRIPTION = "jsxe-plugin-description";
    //}}}
    
    //{{{ ClassLoader methods
    
    //{{{ findClass()
    
    protected Class findClass(String name) throws ClassNotFoundException {
        
        String classFileName = name.replace('.','/').concat(".class");
        Iterator jarItr = m_jarFiles.values().iterator();
        
        while (jarItr.hasNext()) {
            try {
                
                JarFile zipFile = (JarFile)jarItr.next();
                ZipEntry entry = zipFile.getEntry(classFileName);
                
                if (entry != null) {
                    
                    InputStream in = zipFile.getInputStream(entry);
                    
                    boolean fail = false;
                    int len = (int)entry.getSize();
                    byte[] data = new byte[len];
                    int success = 0;
                    int offset = 0;
                    
                    while(success < len && !fail) {
                        len -= success;
                        offset += success;
                        
                        success = in.read(data,offset,len);
                        if(success == -1) {
                            fail = true;
                        }
                    }
                    Class c = defineClass(name,data,0,data.length);
                    return c;
                }
                
            } catch(IOException io) {
                //failed, try the next jar
            }
        }
        throw new ClassNotFoundException(name);
        
    }//}}}

    //{{{ findResources()
    
    protected Enumeration findResources(String name) throws IOException {
        Iterator fileItr = m_files.values().iterator();
        Iterator jarItr  = m_jarFiles.values().iterator();
        ArrayList urls = new ArrayList();
        
        while (fileItr.hasNext()) {
            File file = (File)fileItr.next();
            JarFile jarfile = (JarFile)jarItr.next();
            JarEntry entry = jarfile.getJarEntry(name);
            if (entry != null) {
                urls.add(new URL("jar:"+file.toURL().toString()+"!/"+name));
            }
        }
        
        return new ArrayListEnumeration(urls);
    }//}}}
    
    //{{{ findResource
    
    protected URL findResource(String name) {
        Iterator filesItr = m_files.values().iterator();
        Iterator jarItr = m_jarFiles.values().iterator();
        
        while (jarItr.hasNext()) {
            try {
                File file = (File)filesItr.next();
                JarFile jarfile = (JarFile)jarItr.next();
                JarEntry entry = jarfile.getJarEntry(name);
                if (entry != null) {
                    return new URL("jar:"+file.toURL().toString()+"!/"+name);
                }
            } catch (IOException ioe) {
                jsXe.exiterror(this, ioe, 1);
            }
        }
        
        return null;
    }//}}}

    //}}}
    
    //{{{ addJarFile()
    /**
     * Adds a jar file to the search path for the class loader and
     * loads the jar as a plugin.
     * @param path the path to the jar file
     */
    public void addJarFile(String path) throws FileNotFoundException, IOException {
        addJarFile(new File(path));
    }//}}}
    
    //{{{ addJarFile()
    /**
     * Adds a jar file to the search path for the class loader and loads
     * the jar as a plugin
     * @param file the file to add
     */
    public void addJarFile(File file) throws FileNotFoundException, IOException {
        if (file.exists()) {
            
            JarFile jarFile = new JarFile(file);
            
            setProperties(jarFile); //if there is an IOException then this jar is messed up.
            
            definePackages(jarFile);
            m_files.put(file.getName(), file);
            m_jarFiles.put(file.getName(), jarFile);
            
           // Enumeration entries = jarFile.entries();
           // while (entries.hasMoreElements()) {
           //     ZipEntry entry = (ZipEntry)entries.nextElement();
           //     String name = entry.getName();
           //     if(name.endsWith(".class")) {
           //         
           //         
           //         if (name.endsWith("Plugin.class"))
           //             pluginClasses.add(name);
           //     }
           // }
            
        } else {
            throw new FileNotFoundException("The jar file was not found");
        }
    }//}}}
    
    //{{{ addDirectory
    /**
     * Adds all jar files in a directory to the search path for the class
     * loader.
     * @param path the path for the directory containing jar files
     * @return an ArrayList of pathnames of jar files that could not be loaded.
     */
    public ArrayList addDirectory(String path) {
        ArrayList errors = new ArrayList();
        
        File directory = new File(path);
        File[] files = directory.listFiles(new FileFilter() {//{{{
            public boolean accept(File pathname) {
                return (pathname.getName().endsWith(".jar"));
            }
        });//}}}
        if (files != null) {
            for (int i=0; i<files.length; i++) {
                try {
                    addJarFile(files[i]);
                } catch (IOException e) {
                    errors.add(e);
                }
            }
        }
        
        return errors;
    }//}}}
    
    //{{{ getAllPluginNames()
    /**
     * Gets a list of all the names of the loaded plugins.
     * @return an ArrayList of strings containing the names of the plugins
     * @since jsXe 0.3pre15
     */
    public ArrayList getAllPluginNames() {
        ArrayList names = new ArrayList();
        names.addAll(getViewPluginNames());
        names.addAll(getActionPluginNames());
        return names;
    }//}}}
    
    //{{{ getAllPlugins()
    /**
     * Gets all plugins.
     * @return an ArrayList of plugins
     * @since jsXe 0.3pre15
     */
    public ArrayList getAllPlugins() {
        ArrayList plugins = new ArrayList();
        plugins.addAll(getViewPlugins());
        plugins.addAll(getActionPlugins());
        return plugins;
    }//}}}
    
    //{{{ getViewPluginNames()
    /**
     * Gets the names of all loaded view plugins
     * @return an ArrayList of ViewPlugins
     */
    public ArrayList getViewPluginNames() {
        return new ArrayList(m_viewPlugins.keySet());
    }//}}}
    
    //{{{ getViewPlugins()
    /**
     * Gets all view plugins. You should run startPlugins() before calling this function.
     * @return an ArrayList of ViewPlugin objects
     */
    public ArrayList getViewPlugins() {
        Iterator pluginItr = getViewPluginNames().iterator();
        ArrayList plugins = new ArrayList();
        while (pluginItr.hasNext()) {
            String pluginName = pluginItr.next().toString();
            ViewPlugin plugin = (ViewPlugin)m_viewPlugins.get(pluginName);
            plugins.add(plugin);
        }
        return plugins;
    }//}}}
    
    //{{{ getViewPlugin()
    /**
     * Gets the view plugin with the given name
     * @return the view plugin
     */
    public ViewPlugin getViewPlugin(String name) {
        return (ViewPlugin)m_viewPlugins.get(name);
    }//}}}
    
    //{{{ getActionPluginNames()
    /**
     * Returns an ArrayList object containing the names of the all installed 
     * action plugins that are not view plugins.
     * @return an ArrayList of ActionPlugins
     */
    public ArrayList getActionPluginNames() {
        return new ArrayList(m_actionPlugins.keySet());
    }//}}}
    
    //{{{ getActionPlugins()
    /**
     * Gets all action plugins that are not view plugins. You should run startPlugins()
     * before calling this function.
     * @return an ArrayList of ActionPlugin objects
     */
    public ArrayList getActionPlugins() {
        Iterator pluginItr = getActionPluginNames().iterator();
        ArrayList plugins = new ArrayList();
        while (pluginItr.hasNext()) {
            String pluginName = pluginItr.next().toString();
            ActionPlugin plugin = (ActionPlugin)m_actionPlugins.get(pluginName);
            plugins.add(plugin);
        }
        return plugins;
    }//}}}
    
    //{{{ getActionPlugin()
    /**
     * Gets an action plugin by name. Not for use in retrieving ViewPlugins.
     * @param name the name of the ActionPlugin you want to retrieve.
     * @return the ActionPlugin or null if a plugin with the name given is not loaded.
     */
    public ActionPlugin getActionPlugin(String name) {
        return (ActionPlugin)m_actionPlugins.get(name);
    }//}}}
    
    //{{{ getPlugin()
    /**
     * Gets the plugin with the given name. This may return
     * either view plugins or ActionPlugins
     * @return the plugin with the given name
     * @since jsXe 0.3pre15
     */
    public ActionPlugin getPlugin(String name) {
        ActionPlugin plugin = getViewPlugin(name);
        if (plugin == null) {
            plugin = getActionPlugin(name);
        }
        return plugin;
    }//}}}
    
    //{{{ startPlugins()
    /**
     * Starts all the plugins from their respective jar files.
     * @return an ArrayList of errors (either Strings or Exceptions).
     */
    public ArrayList startPlugins() {
        Iterator jarItr = m_jarFiles.keySet().iterator();
        ArrayList errors = new ArrayList();
        
        while (jarItr.hasNext()) {
            JarFile jarFile = (JarFile)m_jarFiles.get(jarItr.next().toString());
            
            try {
                startPlugin(jarFile);
            } catch (IOException e) {
                errors.add(e);
            } catch (PluginDependencyException e2) {
                errors.add(e2);
            } catch (PluginLoadException e3) {
                errors.add(e3);
            }
        }
        
        return errors;
        
    }//}}}
    
    //{{{ getPluginProperty()
    /**
     * Gets a property for the plugin with the given name.
     * @return the value of the property
     * @since jsXe 0.3pre15
     */
    public String getPluginProperty(String name, String key) {
        return m_pluginProperties.getProperty(name+"."+key);
    }//}}}
    
    //{{{ getPluginProperty()
    /**
     * Gets a property for the given plugin.
     * @return the value of the property.
     * @since jsXe 0.3pre15
     */
    public String getPluginProperty(ActionPlugin plugin, String key) {
        return m_pluginProperties.getProperty(plugin.getClass().getName()+"."+key);
    }//}}}
    
    //{{{ Private Members
    
    //{{{ checkDependencies()
    
    private void checkDependencies(JarFile file) throws IOException, PluginDependencyException {
        String name = getManifestAttribute(file, PLUGIN_NAME);
        String dep;
        int i=0;
        
        while ((dep = m_pluginProperties.getProperty(name+".dependency."+i++)) != null) {
            //parse the dependency
            int index = dep.indexOf(' ');
            if(index == -1) {
                throw new PluginDependencyException(name, name + " has an invalid dependency: " + dep);
            }
            
            String what = dep.substring(0,index);
            String arg = dep.substring(index + 1);
            if(what.equals("jdk")) {
                if (MiscUtilities.compareStrings(System.getProperty("java.version"), arg,false) < 0) {
                    throw new PluginDependencyException(name, "Java", arg, System.getProperty("java.version"));
                }
            } else {
                if (what.equals("jsxe") || what.equals("jsXe")) {
                    if(arg.length() != 11) {
                        throw new PluginDependencyException(name, "Invalid jsXe version number: " + arg);
                    }
                    
                    if (MiscUtilities.compareStrings(jsXe.getBuild(),arg,false) < 0) {
                        String needs = MiscUtilities.buildToVersion(arg);
                        throw new PluginDependencyException(name, "jsXe", needs, jsXe.getVersion());
                    }
                } else {
                    if (what.equals("plugin")) {
                        int index2 = arg.indexOf(' ');
                        if(index2 == -1) {
                            throw new PluginDependencyException(name, name + " has an invalid dependency: " + dep + " (version is missing)");
                        }
                
                        String plugin = arg.substring(0,index2);
                        String needVersion = arg.substring(index2 + 1);
                        String currVersion = getPluginProperty(plugin, PLUGIN_VERSION);
                        
                        if (currVersion == null) {
                            throw new PluginDependencyException(name, "Cannot load plugin "+name+", " + plugin + " has no version");
                        } else {
                            if (MiscUtilities.compareStrings(currVersion,needVersion,false) < 0) {
                                throw new PluginDependencyException(name, plugin, needVersion, currVersion);
                            } else {
                                if (getPlugin(plugin) instanceof ActionPlugin.Broken) {
                                    throw new PluginDependencyException(name, name + " requires plugin "+plugin+" but "+plugin+" did not load properly");
                                } else {
                                    //check dependencies of plugin we depend on.
                                    try {
                                        checkDependencies((JarFile)m_jarFiles.get(plugin));
                                    } catch (PluginDependencyException e) {
                                        throw new PluginDependencyException(name, name + " requires plugin "+plugin+" but "+plugin+" did not load properly");
                                    }
                                }
                            }
                        }
                    } else {
                        if (what.equals("class")) {
                            try {
                                loadClass(arg,false);
                            } catch(Exception e) {
                                throw new PluginDependencyException(name, "plugin "+name+" requires class "+arg);
                            }
                        } else {
                            throw new PluginDependencyException(name, name + " has unknown dependency: " + dep);
                        }
                    }
                }
            }
        }
    }//}}}
    
    //{{{ definePackages() 
    /**
     * Defines all packages found in the given Java archive file. The
     * attributes contained in the specified Manifest will be used to obtain
     * package version and sealing information.
     */
    private void definePackages(JarFile zipFile) throws IOException {
        try {
            Manifest manifest = zipFile.getManifest();

            if (manifest != null) {
                Map entries = manifest.getEntries();
                Iterator i = entries.keySet().iterator();

                while(i.hasNext()) {
                    String path = (String)i.next();

                    if (!path.endsWith(".class")) {
                        String name = path.replace('/', '.');

                        if(name.endsWith("."))
                            name = name.substring(0, name.length() - 1);

                        // code url not implemented
                        definePackage(path,name,manifest,null);
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            // should never happen, not severe anyway
        }
    } //}}}

    //{{{ definePackage()
    /**
     * Defines a new package by name in this ClassLoader. The attributes
     * contained in the specified Manifest will be used to obtain package
     * version and sealing information. For sealed packages, the additional
     * URL specifies the code source URL from which the package was loaded.
     */
    private Package definePackage(String path, String name, Manifest man, URL url) throws IllegalArgumentException {
        String specTitle = null;
        String specVersion = null;
        String specVendor = null;
        String implTitle = null;
        String implVersion = null;
        String implVendor = null;
       // String sealed = null;
        URL sealBase = null;

        Attributes attr = man.getAttributes(path);

        if (attr != null) {
            specTitle = attr.getValue(Attributes.Name.SPECIFICATION_TITLE);
            specVersion = attr.getValue(Attributes.Name.SPECIFICATION_VERSION);
            specVendor = attr.getValue(Attributes.Name.SPECIFICATION_VENDOR);
            implTitle = attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            implVersion = attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            implVendor = attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
           // sealed = attr.getValue(Attributes.Name.SEALED);
        }

        attr = man.getMainAttributes();

        if (attr != null) {
            if (specTitle == null) {
                specTitle = attr.getValue(Attributes.Name.SPECIFICATION_TITLE);
            }

            if (specVersion == null) {
                specVersion = attr.getValue(Attributes.Name.SPECIFICATION_VERSION);
            }

            if (specVendor == null) {
                specVendor = attr.getValue(Attributes.Name.SPECIFICATION_VENDOR);
            }

            if (implTitle == null) {
                implTitle = attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            }

            if (implVersion == null) {
                implVersion = attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            }

            if (implVendor == null) {
                implVendor = attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
            }

           // if (sealed == null) {
           //     sealed = attr.getValue(Attributes.Name.SEALED);
           // }
        }

        return super.definePackage(name, specTitle, specVersion, specVendor,
            implTitle, implVersion, implVendor,
            sealBase);
    } //}}}
    
    //{{{ setProperties()
    
    private void setProperties(JarFile jarFile) throws IOException {
        
        String mainClass         = getManifestAttribute(jarFile, PLUGIN_CLASS);
        String pluginName        = getManifestAttribute(jarFile, PLUGIN_NAME);
        if (pluginName != null && mainClass != null) {
            String propPrefix1        = mainClass + ".";
            String propPrefix2        = pluginName + ".";
            String implVersion       = getManifestAttribute(jarFile, PLUGIN_VERSION);
            String url               = getManifestAttribute(jarFile, PLUGIN_URL);
            String humanReadableName = getManifestAttribute(jarFile, PLUGIN_HUMAN_READABLE_NAME);
            String description       = getManifestAttribute(jarFile, PLUGIN_DESCRIPTION);
            
            //prefix with both the plugin name and class
            m_pluginProperties.setProperty(propPrefix1+PLUGIN_NAME, pluginName);
            m_pluginProperties.setProperty(propPrefix2+PLUGIN_NAME, pluginName);
            //prefix the main class with the plugin name
            m_pluginProperties.setProperty(propPrefix1+PLUGIN_CLASS, mainClass);
            m_pluginProperties.setProperty(propPrefix2+PLUGIN_CLASS, mainClass);
            
            if (implVersion != null) {
                m_pluginProperties.setProperty(propPrefix1+PLUGIN_VERSION, implVersion);
                m_pluginProperties.setProperty(propPrefix2+PLUGIN_VERSION, implVersion);
            }
            if (url != null) {
                m_pluginProperties.setProperty(propPrefix1+PLUGIN_URL, url);
                m_pluginProperties.setProperty(propPrefix2+PLUGIN_URL, url);
            }
            if (humanReadableName != null) {
                m_pluginProperties.setProperty(propPrefix1+PLUGIN_HUMAN_READABLE_NAME, humanReadableName);
                m_pluginProperties.setProperty(propPrefix2+PLUGIN_HUMAN_READABLE_NAME, humanReadableName);
            }
            if (description != null) {
                m_pluginProperties.setProperty(propPrefix1+PLUGIN_DESCRIPTION, description);
                m_pluginProperties.setProperty(propPrefix2+PLUGIN_DESCRIPTION, description);
            }
            
            //Set dependency properties
            ZipEntry entry = jarFile.getEntry("dependency.props");
            //If no dependency file assume no dependencies
            if (entry != null) {
                InputStream stream = jarFile.getInputStream(entry);
                Properties dependencies = new Properties();
                dependencies.load(stream);
                
                String dep;
                int i = 0;
                while ((dep = dependencies.getProperty("dependency." + i)) != null) {
                    m_pluginProperties.setProperty(propPrefix1 + "dependency." + i, dep);
                    m_pluginProperties.setProperty(propPrefix2 + "dependency." + i++, dep);
                }
            }
        }
    }//}}}
    
    //{{{ startPlugin()
    
    private void startPlugin(JarFile jarfile) throws IOException, PluginDependencyException, PluginLoadException {
        
        Log.log(Log.NOTICE, this, "Attempting to start plugin from jar file "+jarfile.getName());
        
        String mainPluginClass = getManifestAttribute(jarfile, PLUGIN_CLASS);
        String pluginName = getManifestAttribute(jarfile, PLUGIN_NAME);
        
        if (getPlugin(pluginName) != null) {
            throw new PluginLoadException("Plugin " + pluginName + " already loaded.");
        }
        
        if (mainPluginClass != null && pluginName != null) {
            
            try {
                
                checkDependencies(jarfile);
                
                //load the plugin's localized messages
                Log.log(Log.NOTICE, this, "Loading localized messages for plugin: "+pluginName);
                Properties pluginMessages = new Properties();
                try {
                    InputStream stream = jarfile.getInputStream(jarfile.getEntry("messages/messages.en"));
                    pluginMessages.load(stream);
                    Messages.loadPluginMessages(pluginMessages);
                } catch (IOException e) {
                    Log.log(Log.WARNING, this, "Plugin "+pluginName+" does not have default messages.en");
                }
                try {
                    InputStream stream = jarfile.getInputStream(jarfile.getEntry("messages/messages."+Messages.getLanguage()));
                    pluginMessages.load(stream);
                    Messages.loadPluginMessages(pluginMessages);
                } catch (IOException e) {
                    Log.log(Log.WARNING, this, "Plugin "+pluginName+" does not have localized messages."+Messages.getLanguage());
                }
                
                Class pluginClass = loadClass(mainPluginClass);
                
                int modifiers = pluginClass.getModifiers();
                if (!Modifier.isInterface(modifiers)
                    && !Modifier.isAbstract(modifiers)
                    && ActionPlugin.class.isAssignableFrom(pluginClass)) {
                    
                    Object plugin = pluginClass.newInstance();
                    
                    if (ViewPlugin.class.isAssignableFrom(pluginClass)) {
                        //It's a view plugin
                        Log.log(Log.NOTICE, this, "Started View Plugin: "+pluginName);
                        ViewPlugin viewPlugin = (ViewPlugin)plugin;
                        m_viewPlugins.put(pluginName, viewPlugin);
                    } else {
                        //It's an Action plugin
                        Log.log(Log.NOTICE, this, "Started Action Plugin: "+pluginName);
                        ActionPlugin actionPlugin = (ActionPlugin)plugin;
                        m_actionPlugins.put(pluginName, actionPlugin);
                    }
                } else {
                    /*
                    It's not a plugin. No biggie. We need it to be loaded
                    anyway.
                    */
                    throw new PluginLoadException(jarfile, "Main class is not a plugin class");
                }
            } catch (ClassNotFoundException e) {
                throw new IOException(e.getMessage());
            } catch (InstantiationException e) {
                throw new IOException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IOException(e.getMessage());
            } catch (PluginDependencyException e) {
                m_actionPlugins.put(pluginName, new ActionPlugin.Broken());
                throw e;
            } catch (IOException e) {
                m_actionPlugins.put(pluginName, new ActionPlugin.Broken());
                throw e;
            }
            
        } else {
            throw new PluginLoadException(jarfile, "No plugin class defined.");
        }
        
    }//}}}
    
    //{{{ getManifestAttribute()
    
    private String getManifestAttribute(JarFile file, String name) throws IOException {
        return getManifestAttribute(file, new Attributes.Name(name));
    }//}}}
    
    //{{{ getManifestAttribute()
    
    private String getManifestAttribute(JarFile file, Attributes.Name name) throws IOException {
        String value = null;
        if (file != null && name != null) {
            Manifest manifest = file.getManifest();
            if (manifest != null) {
                Attributes attr = manifest.getMainAttributes();
                if (attr != null) {
                    value = attr.getValue(name);
                } 
                if (value == null) {
                    attr = manifest.getAttributes("common");
                    if (attr != null) {
                        value = attr.getValue(name);
                    }
                }
            }
        }
        return value;
    }//}}}
    
    //{{{ getNameForPlugin()
    
    private String getNameForPlugin(ActionPlugin plugin) {
        Iterator itr = getAllPluginNames().iterator();
        while (itr.hasNext()) {
            String name = itr.next().toString();
            if (getPlugin(name) == plugin) {
                return name;
            }
        }
        return null;
    }//}}}
    
    // fileName -> File
    private static HashMap m_files = new HashMap();
    // fileName -> JarFile
    private static HashMap m_jarFiles = new HashMap();
    // pluginName -> ViewPlugin
    private static HashMap m_viewPlugins = new HashMap();
    // pluginName -> ActionPlugin
    private static HashMap m_actionPlugins = new HashMap();
    
    // internal properties used for storing name, version, etc.
    private Properties m_pluginProperties = new Properties();
    
    //}}}
}
