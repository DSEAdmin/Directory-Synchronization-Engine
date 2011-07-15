/**********************BEGIN LICENSE BLOCK**************************************
 *   Version: MPL 1.1
 * 
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *   the License. You may obtain a copy of the License at
 *   http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 * 
 *  The Original Code is the Directory Synchronization Engine(DSE).
 * 
 *  The Initial Developer of the Original Code is IronKey, Inc.
 *  Portions created by the Initial Developer are Copyright (C) 2011
 *  the Initial Developer. All Rights Reserved.
 * 
 *  Contributor(s): Shirish Rai
 * 
 ************************END LICENSE BLOCK*************************************/
package server.id.ldap;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class ObjectTypeSpec {
  
  private static class OcComparator implements Comparator<String> {
    public int compare(String o1, String o2) {
      return o1.compareToIgnoreCase(o2);
    }    
  }
  
  static OcComparator occ;
  
  static {
    occ = new OcComparator();
  }
  
  public void setPersonOc(List<String> personOc) {
    this.personOc = new TreeSet<String>(occ);
    this.personOc.addAll(personOc);
  }
  
  public void setGroupOc(List<String> groupOc) {
    this.groupOc = new TreeSet<String>(occ);
    this.groupOc.addAll(groupOc);
  }
  
  public void setContainerOc(List<String> containerOc) {
    this.containerOc = new TreeSet<String>(occ);
    this.containerOc.addAll(containerOc);
  }
  
  private TreeSet<String> personOc;
  private TreeSet<String> groupOc;
  private TreeSet<String> containerOc;

  private boolean isMatch(TreeSet<String> allowedOcs, Iterable<String> ocs) {
    for (String oc : ocs) {
      if (allowedOcs.contains(oc))
        return true;
    }
    return false;    
  }
  
  public boolean isGroup(Iterable<String> ocs) {
    return isMatch(groupOc, ocs);
  }

  public boolean isContainer(Iterable<String> ocs) {
    return isMatch(containerOc, ocs);
  }

  public boolean isPerson(Iterable<String> ocs) {
    return isMatch(personOc, ocs);
  }
}
