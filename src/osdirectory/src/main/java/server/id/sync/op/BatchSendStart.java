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
import server.id.sync.messages.v1.ChangeRequest;
import server.id.sync.server.SynchronizationService;


public class BatchSendStart extends ObligationProcessorBase {
  private Log log = LogFactory.getLog(getClass());

  public void processObligation(EventProcessingObligation obligation, ChangeEvent event, Callback cb) throws Exception {
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
    
    cb.setNumEntriesToSvc(0); //reset to 0
    ChangeRequest request = syncService.createChangeRequest(cb.getConnector(), cb.isDelta());
    StringBuffer sb = new StringBuffer();
    sb.append("<arg name=\"changeReqVersion\" type=\"long\">").append(request.getVersion()).append("</arg>");
    sb.append("<arg name=\"changeReqConnectorUuid\" type=\"string\">").append(UUID.valueOf(request.getConnectorUuid())).append("</arg>");
    sb.append("<arg name=\"changeReqConnectorMajorVersion\" type=\"long\">").append(request.getConnectorMajorVersion()).append("</arg>");
    sb.append("<arg name=\"changeReqConnectorMinorVersion\" type=\"long\">").append(request.getConnectorMinorVersion()).append("</arg>");
    sb.append("<arg name=\"changeReqChangeNumber\" type=\"long\">").append(request.getChangeNumber()).append("</arg>");
    sb.append("<arg name=\"changeReqChangeFragmentNumber\" type=\"long\">").append(request.getChangeFragmentNumber()).append("</arg>");
    sb.append("<arg name=\"synchronizationServiceIsDelta\" type=\"boolean\">").append(cb.isDelta()).append("</arg>");
    audit(obligation, event, cb, sb.toString());
  }

}
