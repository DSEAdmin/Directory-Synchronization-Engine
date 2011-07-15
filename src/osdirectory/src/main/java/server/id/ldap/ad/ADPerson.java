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
package server.id.ldap.ad;

import java.util.Collection;

import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.IdObjectType;
import server.id.Person;


public class ADPerson extends Person {
  
  public ADPerson(Entry entry, IdObjectType type) {
    super(entry, type);
  }

  public ADPerson(Entry entry) {
    super(entry, IdObjectType.PERSON);
  }

  @SuppressWarnings("unchecked")
  public Collection<String> getMemberOf() {
    return (Collection<String>)getAttributeValues(AttributeVirtualization.MEMBER_OF);    
  }

  @SuppressWarnings("unchecked")
  public Collection<byte[]> getRvEntryUuid() {
    return (Collection<byte[]>)getAttributeValues(AttributeVirtualization.RV_ENTRY);    
  }
  
  public String getAccountControl() {
    return (String)getAttributeValue(AttributeVirtualization.ACCT_CONTROL);
  }
  
  public String getName() {
    return (String)getAttributeValue(AttributeVirtualization.NAME); 
  }
  
  public String getDn() {
    return (String)getAttributeValue(AttributeVirtualization.DN);     
  }  
}
