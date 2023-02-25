/*
ActionSet.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

Copyright (C) 2001 Slava Pestov
Portions Copyright (C) 2004 Ian Lewis

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
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
Optionally, you may find a copy of the GNU General Public License
from http://www.fsf.org/copyleft/gpl.txt
*/

package net.sourceforge.jsxe;

import java.util.*;
import javax.swing.Action;

/**
 * A set of actions.
 *
 * @author Slava Pestov
 * @author Ian Lewis (<a href="mailto:IanLewis@member.fsf.org">IanLewis@member.fsf.org</a>)
 * @version $Id: ActionSet.java,v 1.4 2005/04/21 17:57:28 ian_lewis Exp $
 * @since jsXe 0.4 beta
 */
public class ActionSet {
   
   //{{{ ActionSet constructor
   
   /**
    * Creates a new action set.
    */
   public ActionSet() {
      this(null);
   }//}}}

   //{{{ ActionSet constructor
   
   /**
    * Creates a new action set.
    * @param label The label for the action set
    */
   public ActionSet(String label) {
      this.label = label;
      actions = new Hashtable();
   }//}}}

   //{{{ getLabel()
   
   /**
    * Return the action source label.
    */
   public String getLabel() {
      return label;
   }//}}}

   //{{{ setLabel()
   
   /**
    * Sets the action source label.
    * @param label The label
    */
   public void setLabel(String label) {
      this.label = label;
   }//}}}

   //{{{ addAction()
   
   /**
    * Adds an action to the action set.
    * @param name the internal name for the action
    * @param action The action
    */
   public void addAction(String name, Action action) {
      actions.put(name,action);
   }//}}}

   //{{{ removeAction()
   
   /**
    * Removes an action from the action set.
    * @param name The internal action name
    */
   public void removeAction(String name) {
      actions.remove(name);
   }//}}}

   //{{{ removeAllActions()
   
   /**
    * Removes all actions from the action set.
    */
   public void removeAllActions() {
      actions.clear();
   }//}}}

   //{{{ getAction()
   
   /**
    * Returns an action with the specified name.
    * @param name The action name
    */
   public Action getAction(String name) {
      return (Action)actions.get(name);
   }//}}}

   //{{{ getActionCount()
   
   /**
    * Returns the number of actions in the set.
    */
   public int getActionCount() {
      return actions.size();
   }//}}}

   //{{{ getActions()
   
   /**
    * Returns an array of all actions in this action set.
    */
   public Action[] getActions() {
      Action[] retVal = new Action[actions.size()];
      Enumeration enum = actions.elements();
      int i = 0;
      while(enum.hasMoreElements()) {
         retVal[i++] = (Action)enum.nextElement();
      }
      return retVal;
   }//}}}

   //{{{ contains()
   
   /**
    * Returns if this action set contains the specified action.
    * @param action The action
    */
   public boolean contains(Action action) {
      return actions.contains(action);
   }//}}}

   //{{{ toString()
   /**
    * Returns getLabel()
    * @see ActionSet#getLabel()
    */
   public String toString() {
      return getLabel();
   }//}}}

   // package-private members
  // void getActions(Vector vec) {
  //    Enumeration enum = actions.elements();
  //    while(enum.hasMoreElements()) {
  //       vec.addElement(enum.nextElement());
  //    }
  // }

   //{{{ Private Members
   private String label;
   private Hashtable actions;
   //}}}
}
