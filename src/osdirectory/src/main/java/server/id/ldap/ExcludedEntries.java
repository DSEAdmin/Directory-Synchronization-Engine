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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ExcludedEntries {
  private Log log = LogFactory.getLog(getClass());
  private List<LdapName> bases;
  private HashSet<LdapName> entries;
  
  public ExcludedEntries(Connector connector, DirectorySpec ds) {
    bases = new ArrayList<LdapName>();
    try {
      LdapName base = new LdapName(connector.getBase());
      for (LdapName name : ds.getSystemEntriesSpec().getSubTrees()) {
        LdapName entry = (LdapName)base.clone();
        entry.addAll(name);
        bases.add(entry);
        log.debug("Added system base " + entry.toString() + " to ObjectMapper for AD DAO");
      }
      
      entries = new HashSet<LdapName>();
      for (LdapName name : ds.getSystemEntriesSpec().getEntries()) {
        LdapName entry = (LdapName)base.clone();
        entry.addAll(name);
        entries.add(entry);
        log.debug("Adding system entry " + entry.toString() + " to ObjectMapper for AD DAO");
      }
    } catch (InvalidNameException e) {
      log.error("Could not parse base of connector " + connector.getName(), e);
      bases = null;
      entries = null;
    }
  }
  
  public boolean isSystemBase(Name name) {
    for (LdapName e : bases) {
      log.trace("Comparing " + name.toString() + " with " + e.toString());
      if (name.startsWith(e)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean isSystemEntry(Name name) {
    return entries.contains(name);
  }
}
