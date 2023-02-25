/*
CatalogManager.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

 * Copyright (C) 2001, 2003 Slava Pestov
 * Portions copyright (C) 2002 Chris Stevenson
 * Portions copyright (C) 2005 Ian Lewis (IanLewis@member.fsf.org)

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
import java.awt.Component;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import org.apache.xml.resolver.Catalog;
import net.sourceforge.jsxe.gui.TabbedView;
import net.sourceforge.jsxe.gui.Messages;
import net.sourceforge.jsxe.util.MiscUtilities;
import net.sourceforge.jsxe.util.Log;
import org.xml.sax.*;
//}}}

/**
 * The CatalogManager implements cataloging external entities to a local cache
 * so that it can be later retrieved without having to retrieve it from the
 * external source on every parse.
 * 
 * @author Slava Pestov
 * @author Chris Stevenson
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: CatalogManager.java,v 1.5 2006/01/19 06:55:22 ian_lewis Exp $
 */
public class CatalogManager {

    //{{{ resolve() method
    public static InputSource resolve(String current, String publicId, String systemId) throws Exception {
        load();

        if (publicId != null && publicId.length() == 0)
            publicId = null;

        if (systemId != null && systemId.length() == 0)
            systemId = null;

        String newSystemId = null;

        /* we need this hack to support relative path names inside
         * cached files. we want them to be resolved relative to
         * the original system ID of the cached resource, not the
         * cache file name on disk. */
        String parent;
        if (current != null) {
            Entry entry = (Entry)reverseResourceCache.get(current);
            if (entry != null) {
                parent = entry.uri;
            } else {
                parent = MiscUtilities.getParentOfPath(current);
            }
        } else {
            parent = null;
        }
        
        if (publicId == null && systemId != null && parent != null) {
            if (systemId.startsWith(parent)) {
                // first, try resolving a relative name,
                // to handle built-in DTDs
                newSystemId = systemId.substring(parent.length());
                if (newSystemId.startsWith("/"))
                    newSystemId = newSystemId.substring(1);
                newSystemId = resolveSystem(newSystemId);
            }
        }
        
        // next, try resolving full path name
        if (newSystemId == null) {
            if (publicId == null)
                newSystemId = resolveSystem(systemId);
            else
                newSystemId = resolvePublic(systemId,publicId);
        }

        // well, the catalog can't help us, so just assume the
        // system id points to a file
        if (newSystemId == null) {
            if (systemId == null) {
                return null;
            } else {
                if (MiscUtilities.isURL(systemId)) {
                    newSystemId = systemId;
                }
            }
        }

        if (newSystemId == null)
            return null;
        
        Log.log(Log.MESSAGE, CatalogManager.class, "Attempting to load SystemId: "+newSystemId);
        
        if (newSystemId.startsWith("file:")) {
            //try to find the external entity in the same directory as the file
            //systemId will default to the current directory instead of the file's directory
            String testId = MiscUtilities.getParentOfPath(current)+MiscUtilities.getFileName(newSystemId);
            if ((new File(new URI(testId))).exists()) {
                newSystemId = testId;
            }
            
            String filename = MiscUtilities.uriToFile(newSystemId);
            
            DocumentBuffer buf = jsXe.getOpenBuffer(new File(filename));
            if (buf != null) {
                Log.log(Log.MESSAGE, CatalogManager.class, "Found open buffer for " + newSystemId);
                InputSource source = new InputSource(systemId);
                
                //TODO: loads a document as a string. May be resource intensive.
                source.setCharacterStream(new StringReader(buf.getText(0, buf.getLength())));
                return source;
            } else {
                try {
                    InputSource source = new InputSource(systemId);
                    source.setByteStream(new URL(newSystemId).openStream());
                    Log.log(Log.MESSAGE, CatalogManager.class, "Using local file: "+newSystemId);
                    return source;
                } catch (IOException ioe) {
                    /*
                    The file in the cache may have been cleared manually.
                    Remove the file from the cache properties
                    */
                    Log.log(Log.WARNING, CatalogManager.class, "Cannot find cached file "+newSystemId);
                    Entry entry = (Entry)reverseResourceCache.get(newSystemId);
                    if (entry != null) {
                        resourceCache.remove(entry);
                        resourceCache.remove(new Entry(Entry.PUBLIC, publicId, entry.uri));
                        reverseResourceCache.remove(newSystemId);
                        
                        //try again...
                        return resolve(current, publicId, systemId);
                    }
                    throw ioe;
                }
            }
        } else {
            if (cache != 0) {
                TabbedView view = jsXe.getActiveView();
                if (cache == 2 || showDownloadResourceDialog(view, newSystemId)) {
                    InputSource source = new InputSource(systemId);
                    File file = copyToLocalFile(newSystemId);
                    addUserResource(publicId,systemId,file.toURL().toString());
                    source.setByteStream(new FileInputStream(file));
                    return source;
                }//Try to open this this normally.
                return null;
            } else {
                return null;
            }
        }
    } //}}}

    /*{{{ reload() method
    public static void reload(Entry e)
    {
        if(e.type == Entry.PUBLIC)
        {
            throw new RuntimeException(
                "Cannot reload a DTD from a PUBLIC id, only a SYSTEM id." );
        }

        try
        {

            if(isLocal(e))
            {

                File oldDtdFile = new File((String)resourceCache.get(e));

                File newFile = copyToLocalFile(new URL(e.id));

                oldDtdFile.delete();
                newFile.renameTo(oldDtdFile);

                JOptionPane.showMessageDialog(
                    null,
                    "Reloaded DTD to " + oldDtdFile );
            }
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog( null, ex.getMessage() );
        }
    } *///}}}

    //{{{ isLocal() method
    public static boolean isLocal(Entry e) {
        if (e == null || jsXe.getSettingsDirectory() == null)
            return false;

        try {
            URL url = new File(jsXe.getSettingsDirectory()).toURL();
            String fileUrl = (String)resourceCache.get(e);
            return fileUrl.startsWith(url.toString());
        } catch (MalformedURLException ex) {
            return false;
        }
    } //}}}

    //{{{ propertiesChanged() method
    public static void propertiesChanged() {
        
        if (jsXe.getSettingsDirectory() == null) {
            cache = 0;
        } else {
            resourceDir = jsXe.getSettingsDirectory()+System.getProperty("file.separator")+"dtds";
            cache = jsXe.getIntegerProperty("xml.cache", 1);
        }

        if (cache == 0)
            clearCache();

        loadedCatalogs = false;
    } //}}}

    //{{{ save() method
    public static void save() {
        if (loadedCache) {
            int systemCount = 0;
            int publicCount = 0;

            Iterator keys = resourceCache.keySet().iterator();
            while(keys.hasNext()) {
                Entry entry = (Entry)keys.next();
                Object uri = resourceCache.get(entry);
                if (uri == IGNORE)
                    continue;

                if (entry.type == Entry.PUBLIC) {
                    jsXe.setProperty("xml.cache.public-id." + publicCount,entry.id);
                    jsXe.setProperty("xml.cache.public-id." + publicCount
                        + ".uri",uri.toString());
                    publicCount++;
                } else {
                    jsXe.setProperty("xml.cache.system-id." + systemCount,entry.id);
                    jsXe.setProperty("xml.cache.system-id." + systemCount + ".uri",uri.toString());
                    systemCount++;
                }
            }

            jsXe.setProperty("xml.cache.public-id." + publicCount, null);
            jsXe.setProperty("xml.cache.public-id." + publicCount + ".uri", null);
            jsXe.setProperty("xml.cache.system-id." + systemCount, null);
            jsXe.setProperty("xml.cache.system-id." + systemCount + ".uri", null);
        }
    } //}}}

    //{{{ clearCache() method
    public static void clearCache() {
        load();

        Iterator files = resourceCache.values().iterator();
        while (files.hasNext()) {
            Object obj = files.next();
            if (obj instanceof String) {
                String file = (String)MiscUtilities.uriToFile((String)obj);
                Log.log(Log.NOTICE,CatalogManager.class,"Deleting " + file);
                new File(file).delete();
            }
        }
        resourceCache.clear();
    } //}}}

    //{{{ reloadCatalogs() method
    public static void reloadCatalogs() {
        loadedCatalogs = false;
    } //}}}

    //{{{ Private members

    //{{{ Static variables
    private static boolean loadedCache;
    private static boolean loadedCatalogs;
    
    /**
     * 0 = Always Download
     * 1 = Ask before cache
     * 2 = Always Cache
     */
    private static int cache;
    private static Catalog catalog;
    private static Set catalogFiles;
    private static HashMap resourceCache;
    private static HashMap reverseResourceCache;
    private static String resourceDir;

    // placeholder for DTDs we never want to download
    private static Object IGNORE = new Object();
    //}}}

    //{{{ addUserResource() method
    /**
     * Don't want this public because then invoking {@link clearCache()}
     * will remove this file, not what you would expect!
     */
    private static void addUserResource(String publicId, String systemId, String url)
    {
        if (publicId != null) {
            Entry pe = new Entry( Entry.PUBLIC, publicId, url );
            resourceCache.put( pe, url );
        }

        Entry se = new Entry( Entry.SYSTEM, systemId, url );
        resourceCache.put( se, url );
        reverseResourceCache.put(url,se);
    } //}}}

    //{{{ copyToLocalFile() method
    private static File copyToLocalFile(String path) throws IOException {
        
        if(jsXe.getSettingsDirectory() == null)
            return null;

        String userDir = jsXe.getSettingsDirectory();

        File _resourceDir = new File(resourceDir);
        if (!_resourceDir.exists())
            _resourceDir.mkdir();

        InputStream stream;
        if (MiscUtilities.isURL(path)) {
            stream = (new URL(path)).openStream();
        } else {
            stream = new FileInputStream(path);
        }
        BufferedInputStream in = new BufferedInputStream(stream);

        File localFile = File.createTempFile("cache", ".xml", _resourceDir);
        
        Log.log(Log.MESSAGE, CatalogManager.class, "Caching file "+path+" to "+localFile.getName());
        
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localFile));

        byte[] buf = new byte[4096];
        int count = 0;
        while ((count = in.read(buf)) != -1)
            out.write(buf,0,count);
        out.close();

        return localFile;
    } //}}}

    //{{{ resolvePublic() method
    private static String resolvePublic(String systemId, String publicId) throws Exception {
        Entry e = new Entry(Entry.PUBLIC,publicId,null);
        String uri = (String)resourceCache.get(e);
        if (uri == null)
            return catalog.resolvePublic(publicId,null);
        else if (uri == IGNORE)
            return null;
        else
            return uri;
    } //}}}

    //{{{ resolveSystem() method
    private static String resolveSystem(String id) throws Exception {
        Entry e = new Entry(Entry.SYSTEM,id,null);
        String uri = (String)resourceCache.get(e);
        if (uri == null)
            return catalog.resolveSystem(id);
        else if (uri == IGNORE)
            return null;
        else
            return uri;
    } //}}}

    //{{{ showDownloadResourceDialog() method
    private static boolean showDownloadResourceDialog(Component comp, String systemId) {
        Entry e = new Entry(Entry.SYSTEM,systemId,null);
        if (resourceCache.get(e) == IGNORE)
            return false;

        int result = JOptionPane.showConfirmDialog(comp,
            Messages.getMessage("xml.download-resource.message", new String[] { systemId }),
            Messages.getMessage("xml.download-resource.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
            
        if (result == JOptionPane.YES_OPTION) {
            return true;
        } else {
            resourceCache.put(e,IGNORE);
            return false;
        }
    } //}}}

    //{{{ load() method
    private synchronized static void load() {
        if (!loadedCache) {
            Log.log(Log.MESSAGE, CatalogManager.class, "Loading cached files");
            loadedCache = true;

            resourceCache = new HashMap();
            reverseResourceCache = new HashMap();

            int i;
            String id, prop, uri;

            i = 0;
            while((id = jsXe.getProperty(prop = "xml.cache" + ".public-id." + i++, null)) != null) {
                
                try {
                    uri = jsXe.getProperty(prop + ".uri", null);
                    Log.log(Log.MESSAGE, CatalogManager.class, "Loading cached file: "+uri);
                    resourceCache.put(new Entry(Entry.PUBLIC,id,uri),uri);
                } catch(Exception ex2) {
                    Log.log(Log.ERROR,CatalogManager.class,ex2);
                }
            }

            i = 0;
            while((id = jsXe.getProperty(prop = "xml.cache" + ".system-id." + i++, null)) != null) {
                try {
                    uri = jsXe.getProperty(prop + ".uri", null);
                    Log.log(Log.MESSAGE, CatalogManager.class, "Loading cached file: "+uri);
                    Entry se = new Entry(Entry.SYSTEM,id,uri);
                    resourceCache.put(se,uri);
                    reverseResourceCache.put(uri,se);
                } catch(Exception ex2) {
                    Log.log(Log.ERROR,CatalogManager.class,ex2);
                }
            }
        }

        if (!loadedCatalogs) {
            Log.log(Log.MESSAGE, CatalogManager.class, "Loading catalogs");
            loadedCatalogs = true;
            
            catalog = new Catalog();
            catalogFiles = new HashSet();
            
            catalog.setupReaders();
            //catalog.setParserClass("org.apache.xerces.parsers.SAXParser");
            
            try {
                catalog.loadSystemCatalogs();
                
               // catalog.parseCatalog("jeditresource:XML.jar!/xml/dtds/catalog");
                
                int i = 0;
                String prop, uri;
                while((uri = jsXe.getProperty(prop = "xml.catalog." + i++, null)) != null) {
                    Log.log(Log.MESSAGE, CatalogManager.class, "Loading catalog: " + uri);
                    
                    try {
                        if (MiscUtilities.isURL(uri)) {
                            catalogFiles.add(uri);
                        } else {
                            catalogFiles.add(MiscUtilities.resolveSymlinks(uri));
                        }
                        catalog.parseCatalog(uri);
                    } catch(Exception ex2) {
                        Log.log(Log.ERROR,CatalogManager.class,ex2);
                    }
                }
                
            } catch(Exception ex1) {
                Log.log(Log.ERROR,CatalogManager.class,ex1);
            }
        }
    } //}}}

    //}}}

    //{{{ Entry class
    public static class Entry {
        public static final int SYSTEM = 0;
        public static final int PUBLIC = 1;

        public int type;
        public String id;
        public String uri;

        public Entry(int type, String id, String uri) {
            this.type = type;
            this.id = id;
            this.uri = uri;
        }

        public boolean equals(Object o) {
            if (o instanceof Entry) {
                Entry e = (Entry)o;
                return e.type == type && e.id.equals(id);
            } else
                return false;
        }

        public int hashCode() {
            return id.hashCode();
        }
    } //}}}

   // //{{{ VFSUpdateHandler class
   // /**
   //  * Reloads a catalog file when the user changes it on disk.
   //  */
   // public static class VFSUpdateHandler implements EBComponent {
   //     public void handleMessage(EBMessage msg)
   //     {
   //         if(!loadedCatalogs)
   //             return;
   //         
   //         if(msg instanceof VFSUpdate)
   //         {
   //             String path = ((VFSUpdate)msg).getPath();
   //             if(catalogFiles.contains(path))
   //                 loadedCatalogs = false;
   //         }
   //     }
   // } //}}}
}
