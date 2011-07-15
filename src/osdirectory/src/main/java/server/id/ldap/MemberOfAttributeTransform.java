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
import org.springframework.ldap.NameNotFoundException;

import server.id.AVP;
import server.id.AttributeTransform;
import server.id.Entry;
import server.id.IdVirtualization;
import server.id.Util;
import server.id.dao.ChangePoller;


public class MemberOfAttributeTransform implements AttributeTransform {
  private final Log log = LogFactory.getLog(getClass());
  private String dnAttrName;
  private IdVirtualization idVirtualization;
  
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

  public Iterable<? extends Object> virtualize(ChangePoller changePoller, Entry remoteEntry) throws Exception {
    List<byte[]> ret = new ArrayList<byte[]>();
    AVP avp = remoteEntry.getAvp(dnAttrName);
    assert (avp != null);
    String dn = (String) avp.getValue();

    log.debug("virtualize: get memberOf attribute from the directory");
    Iterable<? extends Object> memberOfs = null;
    try {
      memberOfs = changePoller.getGroupMembership(dn);
    } catch (NameNotFoundException e) {
      log.info("The name " + dn + " was not found. It may have been deleted.");
    }

    if (memberOfs != null) {
      for (Object memberOf : memberOfs) {
        RoleVirtualizationEntry rvEntry = idVirtualization.isGroupMapped(changePoller.getConnector().getRvEntries(),
            (String) memberOf);
        if (rvEntry != null) {
          log.debug("virtualize: adding memberOf " + rvEntry.getLocalRole().getIdentifier() + " [ "
              + Util.uuidToString(rvEntry.getRemoteRole().getUuid()) + " ]");
          ret.add(rvEntry.getUuid());
        }
      }
    }
    if (ret.size() > 0)
      return ret;
    else
      return null;    
  }    
}
