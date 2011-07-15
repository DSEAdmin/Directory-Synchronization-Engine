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
package server.id.ldap.ldif;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.AVP;
import server.id.AttributeTransform;
import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.IdVirtualization;
import server.id.Util;
import server.id.dao.ChangePoller;
import server.id.dao.LocalIdentityStore;
import server.id.ldap.LdapUtils;

import com.unboundid.ldap.sdk.LDAPException;

public class MemberOfAttributeTransform implements AttributeTransform {

  private final Log log = LogFactory.getLog(getClass());
  private String dnAttrName;
  private IdVirtualization idVirtualization;
  private LocalIdentityStore idStore;
  
  public void setIdStore(LocalIdentityStore idStore) {
    this.idStore = idStore;
  }

  public void setDnAttrName(String dnAttrName) {
    this.dnAttrName = dnAttrName;
  }
  
  public void setIdVirtualization(IdVirtualization idVirtualization) {
    this.idVirtualization = idVirtualization;
  }

  public List<String> getRemoteAttributes() {
    List<String> ret = new ArrayList<String>();
    return ret;
  }

  public List<String> getBinaryRemoteAttributes() {
    List<String> ret = new ArrayList<String>();
    return ret;
  }

  public Iterable<? extends Object> virtualize(ChangePoller changePoller, Entry remoteEntry) {
    AVP avp = remoteEntry.getAvp(dnAttrName);
    assert(avp != null);
    
    String dn = (String)avp.getValue();
    String ndn = null;
    try {
      ndn = LdapUtils.normalizeDn(dn);
    } catch (LDAPException ex) {
      log.warn("LDAPException:", ex);
      return null;
    }
    
    return virtualize(changePoller, ndn);
  }

  private Iterable<? extends Object> virtualize(ChangePoller changePoller, String ndn) {
    List<byte[]> ret = new ArrayList<byte[]>();
    List<? extends Entry> groups = idStore.getMemberOf(changePoller.getAccount(), 
        changePoller.getConnector(), ndn);
    for(Entry e : groups) {
      AVP avp = e.getAvp(AttributeVirtualization.UUID);
      assert(avp != null);
      byte[] groupUuid = (byte[])avp.getValue();
      RoleVirtualizationEntry rvEntry = idVirtualization.isGroupMapped(changePoller.getConnector().getRvEntries(),
          groupUuid);
      if (rvEntry != null) {
        log.debug("virtualize: adding memberOf " + rvEntry.getLocalRole().getIdentifier() + " [ " + Util.uuidToString(rvEntry.getRemoteRole().getUuid()) + " ]");
        ret.add(rvEntry.getUuid());
      }
    }
    
    if (ret.size() > 0)
      return ret;
    else
      return null;
  }
}
