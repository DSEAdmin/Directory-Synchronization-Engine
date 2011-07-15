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
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

public class SystemEntriesSpec {
  private List<String> subTrees;
  private List<String> entries;
  private List<LdapName> subTreeEntries;
  private List<LdapName> ldapEntries;
  
  public SystemEntriesSpec() {
    subTreeEntries = new ArrayList<LdapName>();
    ldapEntries = new ArrayList<LdapName>();
  }
  
  public void setSubTrees(List<String> dns) {
    this.subTrees = dns;
  }
  public void setEntries(List<String> dns) {
    this.entries = dns;
  }
  
  public void init() throws InvalidNameException {
    for (String dn : subTrees) {
      LdapName name = new LdapName(dn);
      subTreeEntries.add(name);
    }
    for (String dn : entries) {
      LdapName name = new LdapName(dn);
      ldapEntries.add(name);
    }
  }
  
  public List<LdapName> getSubTrees() {
    return subTreeEntries;
  }
  
  public List<LdapName> getEntries() {
    return ldapEntries;
  }
}
