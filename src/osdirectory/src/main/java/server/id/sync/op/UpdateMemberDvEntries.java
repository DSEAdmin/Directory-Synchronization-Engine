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
package server.id.sync.op;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.Util;
import server.id.dao.LocalIdentityStore;
import server.id.sync.ChangeEvent;
import server.id.sync.EventProcessingObligation;


public class UpdateMemberDvEntries extends ObligationProcessorBase {
  private Log log = LogFactory.getLog(getClass());
  private LocalIdentityStore idStore;
  
  public void setIdStore(LocalIdentityStore idStore) {
    this.idStore = idStore;
  }
  
  public void processObligation(EventProcessingObligation obligation, ChangeEvent event, Callback cb) {
    log.debug("processObligation: " + obligation);

    Map<byte[], DitVirtualizationEntry> dvMap = cb.getUpdateDvMap();
    
    if (dvMap == null || dvMap.size() == 0) {
      log.debug("No member dv entries to be updated");
      return;
    }
    
    StringBuffer sb = new StringBuffer();
    
    Set<Entry<byte[], DitVirtualizationEntry>> entrySet = dvMap.entrySet();
    
    Iterator<Entry<byte[], DitVirtualizationEntry>> it = entrySet.iterator();
    
    while ( it.hasNext()) {
      Entry<byte[], DitVirtualizationEntry> entry = it.next();
      byte[] uuid = entry.getKey();
      DitVirtualizationEntry dvEntry = entry.getValue();
      idStore.updateIdObjectDvEntry(null, cb.getConnector(), uuid, dvEntry);
      String dvStr = dvEntry == null ? "null" : dvEntry.getLocalContainer().getIdentifier();
      String dvUuidStr = dvEntry == null ? "null" : Util.uuidToString(dvEntry.getUuid());
      log.debug("Updating object " + Util.uuidToString(uuid) + " to dv entry with local container " + 
          dvStr);
      sb.append("<arg name=\"updateDvEntry\" type=\"string\">").
        append(Util.uuidToString(uuid)).
        append(" \t ").
        append(dvUuidStr).
        append("</arg>");
    }
    
    audit(obligation, event, cb, new String(sb));
  }


}
