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

import server.id.AVP;
import server.id.AttributeVirtualization;
import server.id.IdAVP;
import server.id.IdAttribute;
import server.id.IdEntry;
import server.id.IdObject;
import server.id.IdObjectImpl;
import server.id.IdVirtualization;
import server.id.sync.ChangeEvent;
import server.id.sync.EventProcessingObligation;
import server.id.sync.server.SynchronizationService;


public class ForwardAsDeleteEvent extends ObligationProcessorBase {
  private Log log = LogFactory.getLog(getClass());
  private IdVirtualization idVirtualization;
  
  public void setIdVirtualization(IdVirtualization idVirtualization) {
    this.idVirtualization = idVirtualization;
  }
  
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
    
    if (event.getLocalObj() == null) {
      log.warn("LocalObj is null. Local object should never be null for delete to be forwarded");
      throw new IllegalArgumentException("LocalObj is null. Local object should never be null for delete to be forwarded");
    }

    AVP dvAvp = event.getLocalObj().getAVP(AttributeVirtualization.DV_ENTRY);
    if (dvAvp == null) {
      log.warn("Only previously in scope object can be forwarded as delete");
      throw new IllegalArgumentException("Only previously in scope object can be forwarded as delete");      
    }
    byte[] dvUuid = (byte[])dvAvp.getValue();
    DitVirtualizationEntry prevDvEntry = idVirtualization.getDvEntry(event.getConnector().getDvEntries(), dvUuid);
    
    IdEntry delEntry = new IdEntry();
    AVP avp = null;
    avp = event.getRemoteObj().getAVP(AttributeVirtualization.DN);
    delEntry.addAv(avp);
    avp = event.getRemoteObj().getAVP(AttributeVirtualization.UUID);
    delEntry.addAv(avp);
    avp = event.getRemoteObj().getAVP(AttributeVirtualization.OBJECTTYPE);
    delEntry.addAv(avp);
    avp = event.getRemoteObj().getAVP(AttributeVirtualization.PARENT_UUID);
    if (avp != null)
      delEntry.addAv(avp);
    avp = new IdAVP(new IdAttribute(AttributeVirtualization.DELETED));
    avp.addValue("true");
    delEntry.addAv(avp);
    IdObject delRemoteObject = new IdObjectImpl(delEntry);
    
    ChangeEvent delChangeEvent = new ChangeEvent(event.getConnector(), ChangeEvent.ChangeType.DELETE, 
        delRemoteObject, event.getLocalObj(), prevDvEntry, prevDvEntry, null, null, event.isEffectsInScopeObjects(), false);

    if (syncService.addChangeToChangeRequest(delChangeEvent) == false) {
      log.warn("Failed to add change to ChangeRequest");
      throw new ObligationProcessingException("Failed to add change request to ChangeRequest");
    } else {
      cb.incrNumEntriesToSvc();
    }
    
    audit(obligation, delChangeEvent, cb, null);
  }

}
