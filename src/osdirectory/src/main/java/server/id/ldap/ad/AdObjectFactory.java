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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.AVP;
import server.id.AttributeVirtualization;
import server.id.Container;
import server.id.Entry;
import server.id.Group;
import server.id.IdObject;
import server.id.IdObjectFactory;
import server.id.Util;
import server.id.dao.LocalIdentityStore;
import server.id.ldap.ObjectTypeSpec;


public class AdObjectFactory implements IdObjectFactory {
  private final Log log = LogFactory.getLog(getClass());
  private ObjectTypeSpec ocSpec;
  private LocalIdentityStore idStore;
  
  public AdObjectFactory() {}
  
  public void setOcSpec(ObjectTypeSpec ocs) {
    ocSpec = ocs;
  }
  public void setIdStore(LocalIdentityStore idStore) {
    this.idStore = idStore;
  }
  
  @SuppressWarnings("unchecked")
  public IdObject create(Connector connector, Entry entry) {
    AVP avp = entry.getAvp(AttributeVirtualization.OBJECTTYPE);
    Iterable<String> objectClasses = null;
    if (avp == null) {
      String dn = "null";
      AVP dnavp = entry.getAvp(AttributeVirtualization.DN);
      if (dnavp != null) {
        dn = (String) dnavp.getValue();
      }
      log.debug("ADBUG: Attribute " + AttributeVirtualization.OBJECTTYPE + 
          " not found in entry with DN: " + dn);
      
      objectClasses = getOcFromLocalEntry(connector, entry);
      
      if (objectClasses == null)
        return null;
    } else {
      Iterable<?> values = avp.getValues();

      objectClasses = (Iterable<String>) (values);
    }

    if (log.isTraceEnabled()) {
      for (String s : objectClasses) {
        log.trace("Objectclass: " + s);
      }
    }
    
    if (ocSpec.isPerson(objectClasses)) {
      log.trace("Returning ADPerson");
      return new ADPerson(entry);
    } else if (ocSpec.isGroup(objectClasses)) {
      log.trace("Returning Group");
      return new Group(entry);
    } else if (ocSpec.isContainer(objectClasses)) {
      log.trace("Returning Container");
      return new Container(entry);
    } else
      log.trace("Returning null");
      return null;
  }

  @SuppressWarnings("unchecked")
  private Iterable<String> getOcFromLocalEntry(Connector connector, Entry entry) {
    AVP avp = entry.getAvp(AttributeVirtualization.UUID);
    if (avp == null) {
      return null;
    }
    
    byte[] uuid = (byte[])avp.getValue();
    
    if (uuid == null) {
      log.warn("Attribute UUID not found");
      return null;
    }
    
    Entry localObj = idStore.findLocalObject(null, connector, uuid);
    
    if (localObj == null) {
      log.debug("No local object found for uuid " + Util.uuidToString(uuid));
      return null;
    }

    AVP oc = localObj.getAvp(AttributeVirtualization.OBJECTTYPE);

    Iterable<?> values = oc.getValues();

    Iterable<String> objectClasses = (Iterable<String>) (values);
    
    entry.addAv(oc); //put this in the remote entry
    
    return objectClasses;
  }

}
