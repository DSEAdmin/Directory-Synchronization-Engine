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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.AVP;
import server.id.AttributeTransform;
import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.RemoteAttribute;
import server.id.dao.ChangePoller;
import server.id.dao.LocalIdentityStore;

import com.unboundid.ldap.sdk.LDAPException;

public class MemberAttributeTransform implements AttributeTransform {
  private final Log log = LogFactory.getLog(getClass());
  private RemoteAttribute remoteAttribute;
  private LocalIdentityStore idStore;
  private ObjectTypeSpec ocSpec;
  
  public void setIdStore(LocalIdentityStore idStore) {
    this.idStore = idStore;
  }

  public void setOcSpec(ObjectTypeSpec ocs) {
    ocSpec = ocs;
  }

  public void setRemoteAttribute(RemoteAttribute remoteAttribute) {
    this.remoteAttribute = remoteAttribute;
  }

  public List<String> getRemoteAttributes() {
    List<String> ret = new ArrayList<String>();
    ret.add(remoteAttribute.getName());
    return ret;
  }

  public List<String> getBinaryRemoteAttributes() {
    List<String> ret = new ArrayList<String>();
    if (remoteAttribute.isBinary()) {
      ret.add(remoteAttribute.getName());
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  public Iterable<? extends Object> virtualize(ChangePoller changePoller, Entry remoteEntry) {
    AVP avp = remoteEntry.getAvp(remoteAttribute.getName());
    if (avp == null)
      return null;

    List<byte[]> ret = new ArrayList<byte[]>();
  
    Iterable<? extends Object> values = avp.getValues();
    for(Object o : values) {
      String value = (String)o;
      String ndn = null;
      try {
        ndn = LdapUtils.normalizeDn(value);
      } catch (LDAPException e) {
        log.warn("LDAPException: ", e);
        continue;
      }
      
      Entry e = idStore.findLocalObject(null, changePoller.getConnector(), ndn);

      if (e == null) {
        log.debug("Ignoring member " + ndn + " as the object is not yet known");
      }

      AVP oc = e.getAvp(AttributeVirtualization.OBJECTTYPE);

      Iterable<?> ocvalues = oc.getValues();

      Iterable<String> objectClasses = (Iterable<String>) (ocvalues);

      if (ocSpec.isPerson(objectClasses) == false) {
        continue;
      }
      
      AVP uuidavp = e.getAvp(AttributeVirtualization.UUID);
      assert(uuidavp != null);
      
      ret.add((byte[])uuidavp.getValue());
    }
    
    if (ret.size() > 0)
      return ret;
    else
      return null;
  }
}
