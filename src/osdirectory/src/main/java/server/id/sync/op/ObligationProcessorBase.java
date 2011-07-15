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


import server.id.Audit;
import server.id.IdObject;
import server.id.Util;
import server.id.sync.ChangeEvent;
import server.id.sync.EventProcessingObligation;
import server.id.sync.ObligationProcessor;
import server.id.sync.server.SynchronizationService;


public abstract class ObligationProcessorBase implements ObligationProcessor {

  protected String generateAuditMessage(EventProcessingObligation obligation, ChangeEvent event, Callback cb, String message) {
    String obligationGroup = obligation.getObligationGroup().name();
    String obligationType = obligation.getType().name();
    long obligationOrdinalId = obligation.getOrdinalId();
    String obligationDescription = obligation.getDescription();

    String connectorName = null;
    long connectorMajorVersion = -1;
    long connectorMinorVersion = -1;

    String changeType = null;

    String rObjectUuid = null;
    String rObjectParentUuid = null;
    String rObjectDn = null;

    String lObjectUuid = "null";
    String lObjectParentUuid = null;
    String lObjectDn = null;

    String currLocalContainer = null;
    String currRemoteContainer = null;

    String prevLocalContainer = null;
    String prevRemoteContainer = null;

    boolean effectsInScopeObjects = false;
    boolean prevEffectsInScopeObjects = false;

    IdObject localObject = null;
    DitVirtualizationEntry currDvEntry = null;
    DitVirtualizationEntry prevDvEntry = null;

    if (event != null) {
      connectorName = event.getConnector().getName();
      connectorMajorVersion = event.getConnector().getMajorVersion();
      connectorMinorVersion = event.getConnector().getMinorVersion();

      changeType = event.getChangeType().name();

      rObjectUuid = Util.uuidToString(event.getRemoteObj().getUUID());
      if (event.getRemoteObj().getParentUUID() != null) {
        rObjectParentUuid = Util.uuidToString(event.getRemoteObj().getParentUUID());
      }
      rObjectDn = event.getRemoteObj().getNormalizedDn();

      localObject = event.getLocalObj();
      if (localObject != null) {
    	if (localObject.getUUID() != null) { //null is possible if the user was previously locally created
    		lObjectUuid = Util.uuidToString(localObject.getUUID());
    	}
        if (localObject.getParentUUID() != null)
          lObjectParentUuid = Util.uuidToString(localObject.getParentUUID());
        lObjectDn = localObject.getNormalizedDn();
      }

      currDvEntry = event.getDvEntry();
      if (currDvEntry != null) {
        currLocalContainer = currDvEntry.getLocalContainer().getIdentifier();
        currRemoteContainer = currDvEntry.getRemoteContainer().getIdentifier();
      }

      prevDvEntry = event.getPrevDvEntry();
      if (prevDvEntry != null) {
        prevLocalContainer = prevDvEntry.getLocalContainer().getIdentifier();
        prevRemoteContainer = prevDvEntry.getRemoteContainer().getIdentifier();
      }

      effectsInScopeObjects = event.isEffectsInScopeObjects();
      prevEffectsInScopeObjects = event.isPrevEffectsInScopeObjects();
    }

    String serviceType = null;
    SynchronizationService syncService = cb.getSynchronizationService();
    if (syncService != null) {
      serviceType = syncService.getServiceType();
    }

    StringBuffer sb = new StringBuffer();
    sb.append("<msg><os>");
    sb.append(System.getProperty("os.arch")).append("</os>");
    sb.append("<gid>").append("1").append("</gid>");
    sb.append("<id>").append("1").append("</id>");
    sb.append("<create_time>").append(System.currentTimeMillis()).append("</create_time>");
    sb.append("<args>");

    sb.append("<arg name=\"taskGroup\" type=\"string\">").append(obligationGroup).append("</arg>");
    sb.append("<arg name=\"taskType\" type=\"string\">").append(obligationType).append("</arg>");
    sb.append("<arg name=\"taskOrdinalId\" type=\"long\">").append(obligationOrdinalId).append("</arg>");
    sb.append("<arg name=\"taskDescription\" type=\"string\">").append(obligationDescription).append("</arg>");

    if (event != null) {
      sb.append("<arg name=\"connectorName\" type=\"string\">").append(connectorName).append("</arg>");
      sb.append("<arg name=\"connectorMajorVersion\" type=\"long\">").append(connectorMajorVersion).append("</arg>");
      sb.append("<arg name=\"connectorMinorVersion\" type=\"long\">").append(connectorMinorVersion).append("</arg>");

      sb.append("<arg name=\"changeType\" type=\"string\">").append(changeType).append("</arg>");

      sb.append("<arg name=\"remoteObjectUuid\" type=\"string\">").append(rObjectUuid).append("</arg>");
      if (rObjectParentUuid != null)
        sb.append("<arg name=\"remoteObjectParentUuid\" type=\"string\">").append(rObjectParentUuid).append("</arg>");
      sb.append("<arg name=\"remoteObjectDn\" type=\"string\">").append(rObjectDn).append("</arg>");

      if (localObject != null) {
        sb.append("<arg name=\"localObjectUuid\" type=\"string\">").append(lObjectUuid).append("</arg>");
        if (lObjectParentUuid != null)
          sb.append("<arg name=\"localObjectParentUuid\" type=\"string\">").append(lObjectParentUuid).append("</arg>");
        sb.append("<arg name=\"localObjectDn\" type=\"string\">").append(lObjectDn).append("</arg>");
      }

      if (currDvEntry != null) {
        sb.append("<arg name=\"currentLocalContainer\" type=\"string\">").append(currLocalContainer).append("</arg>");
        sb.append("<arg name=\"currentRemoteContainer\" type=\"string\">").append(currRemoteContainer).append("</arg>");
      }

      if (prevDvEntry != null) {
        sb.append("<arg name=\"previousLocalContainer\" type=\"string\">").append(prevLocalContainer).append("</arg>");
        sb.append("<arg name=\"previousRemoteContainer\" type=\"string\">").append(prevRemoteContainer)
            .append("</arg>");
      }

      sb.append("<arg name=\"effectsInSopeObjects\" type=\"boolean\">").append(effectsInScopeObjects).append("</arg>");
      sb.append("<arg name=\"previouslyEffectedInSopeObjects\" type=\"boolean\">").append(prevEffectsInScopeObjects)
          .append("</arg>");
    }
    if (syncService != null) {
      sb.append("<arg name=\"synchronizationServiceType\" type=\"string\">").append(serviceType).append("</arg>");
    }

    if (message != null) {
      sb.append("<arg name=\"message\">").append(message).append("</arg>");
    }

    sb.append("</args>");
    sb.append("</msg>");

    return sb.toString();
  }

  protected String generateFailureAuditMessage(EventProcessingObligation obligation, 
      ChangeEvent event, Callback cb,
      String failureMsg,
      String message) {
    String msg = generateAuditMessage(obligation, event, cb, null);
    StringBuffer sb = new StringBuffer();
    sb.append("<arg name=\"failureMessage\">").append(failureMsg).append("</arg>");
    sb.append("<arg name=\"message\">").append(message).append("</arg>");
    msg = msg + sb.toString();
    return msg;
  }  
  
  protected void audit(EventProcessingObligation obligation, ChangeEvent event, Callback cb, String message) {
    Audit.log(generateAuditMessage(obligation, event, cb, message));
  }

  protected void auditFailure(EventProcessingObligation obligation, ChangeEvent event, Callback cb, String failureMsg, String message) {
    Audit.log(generateFailureAuditMessage(obligation, event, cb, failureMsg, message));
  }
}
