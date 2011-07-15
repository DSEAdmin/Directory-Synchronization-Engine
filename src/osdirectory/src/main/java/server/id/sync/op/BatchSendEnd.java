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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.safehaus.uuid.UUID;

import server.id.sync.ChangeEvent;
import server.id.sync.EventProcessingObligation;
import server.id.sync.server.ChangeResult;
import server.id.sync.server.Result;
import server.id.sync.server.SynchronizationService;


public class BatchSendEnd extends ObligationProcessorBase {
  private Log log = LogFactory.getLog(getClass());

  public void processObligation(EventProcessingObligation obligation, ChangeEvent event, Callback cb) 
  throws Exception {
    log.debug("processObligation: " + obligation);
    if (cb == null) {
      log.warn("Need callback in ForwardEvent to get SynchronizationService");
      throw new IllegalArgumentException("Need callback in ForwardEvent to get SynchronizationServer");
    }
    
    SynchronizationService syncService = cb.getSynchronizationService();
    if (syncService == null) {
      log.warn("ForwardEvent: No SynchronizationService available");
      throw new IllegalArgumentException("ForwardEvent: No SynchronizationService available");
    }

    ChangeResult result = syncService.sendChangeRequest(cb.isMore());
    if (result != null && result.getResult() == Result.SUCCESS) {
    
      StringBuffer sb = new StringBuffer();
      sb.append("<arg name=\"changeRespVersion\" type=\"long\">").append(result.getVersion()).append("</arg>");
      sb.append("<arg name=\"changeRespConnectorUuid\" type=\"string\">").append(UUID.valueOf(result.getConnectorUuid())).append("</arg>");
      sb.append("<arg name=\"changeRespConnectorMajorVersion\" type=\"long\">").append(result.getConnectorMajorVersion()).append("</arg>");
      sb.append("<arg name=\"changeRespConnectorMinorVersion\" type=\"long\">").append(result.getConnectorMinorVersion()).append("</arg>");
      sb.append("<arg name=\"changeRespChangeNumber\" type=\"long\">").append(result.getChangeNumber()).append("</arg>");
      sb.append("<arg name=\"changeRespChangeFragmentNumber\" type=\"long\">").append(result.getChangeFragmentNumber()).append("</arg>");
      sb.append("<arg name=\"synchronizationServiceMoreEvents\" type=\"boolean\">" + cb.isMore() + "</arg>");
      audit(obligation, event, cb, sb.toString());
    } else {
      auditFailure(obligation, event, cb, 
          result != null ? result.getDescription() : "No result received", null);
      log.error("Failed to send changes: " + 
          result != null ? result.getDescription() : "No result received");
      throw new ObligationProcessingException("Failed to send changes");
    }
  }

}
