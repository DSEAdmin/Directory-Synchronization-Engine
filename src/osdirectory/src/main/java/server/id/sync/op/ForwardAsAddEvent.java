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

import server.id.IdObject;
import server.id.dao.ChangePoller;
import server.id.sync.ChangeEvent;
import server.id.sync.EventProcessingObligation;
import server.id.sync.server.SynchronizationService;



public class ForwardAsAddEvent extends ObligationProcessorBase {
  private Log log = LogFactory.getLog(getClass());
  
  public void processObligation(EventProcessingObligation obligation, ChangeEvent event, Callback cb) 
    throws IllegalArgumentException, ObligationProcessingException {
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
    
    ChangePoller cp = cb.getIdDao();
    if (cp == null) {
      log.warn("ForwardAsAddEvent: requires idDao");
      throw new IllegalArgumentException("ForwardAsAddEvent: No idDao available");
    }
    
    IdObject fullObj = null;
    try {
      fullObj = cp.getCompleteObject(event.getRemoteObj().getDn());
    } catch (Exception e) {
      log.warn("Failed to get full object for " + event.getRemoteObj().getDn()
          + " This could be because the object was deleted while we were processing it. " +
          		"If that is the case, the problem will be resolved in the next sync run."
          , e);      
      throw new ObligationProcessingException("Failed to get full object for " + event.getRemoteObj().getDn() + " : " + e.getMessage());
    }
    
    event.getRemoteObj().addAndOverrideAttributes(fullObj);
    
    if (syncService.addChangeToChangeRequest(event) == false) {
      log.warn("Failed to add change to ChangeRequest");
      throw new ObligationProcessingException("Failed to add change request to ChangeRequest");
    } else {
      cb.incrNumEntriesToSvc();
    }
    
    audit(obligation, event, cb, null);
  }

}
