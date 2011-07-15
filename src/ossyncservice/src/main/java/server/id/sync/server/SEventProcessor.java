/*************************BEGINE LICENSE BLOCK**********************************
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
 *  The Original Code is Directory Synchronization Engine(DSE).
 * 
 *  The Initial Developer of the Original Code is IronKey, Inc..
 *  Portions created by the Initial Developer are Copyright (C) 2011
 *  the Initial Developer. All Rights Reserved.
 * 
 *  Contributor(s): Shirish Rai
 * 
 **************************END LICENSE BLOCK***********************************/
package server.id.sync.server;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import OperationResult;
import OperationType;
import server.id.IdObject;
import server.id.dao.ChangePoller;
import server.id.sync.ChangeEvent;
import server.id.sync.EventGenerator;
import server.id.sync.EventProcessingObligation;
import server.id.sync.EventProcessor;
import server.id.sync.ObligationProcessor;
import server.id.sync.ObligationProcessor.Callback;
import server.id.sync.op.ObligationProcessingException;

public class SEventProcessor extends EventProcessor {
  private final Log log = LogFactory.getLog(getClass());
  private ChangePoller idDao;
  private EventGenerator eventGenerator;

  public void setEventGenerator(EventGenerator eventGenerator) {
    this.eventGenerator = eventGenerator;
  }

  public void setIdDao(ChangePoller idDao) {
    this.idDao = idDao;
  }

  private boolean initialize(Callback cb) throws Exception {
	cb.setNumEntriesFromIds(0);
	cb.setNumEntriesToSvc(0);
	return true;
  }
  
  private boolean finalize(Callback cb) throws Exception {
	idDao.saveOperationResult(OperationType.SYNCHRONIZATION_OPERATION, OperationResult.SUCCESS, 
		"", cb.getNumEntriesFromIds(), cb.getNumEntriesToSvc());
	return true;
  }

  @Transactional(readOnly = false)
  private boolean processObligationsForEvent(IdObject ido, Callback cb) 
  	throws Exception {
	log.debug("Starting obligation processing transaction");
    ChangeEvent event = eventGenerator.generateEvent(idDao, ido);
    if (event == null) {
      log.warn("Could not generate event for " + ido.getDn());
      return false;
    }
    List<EventProcessingObligation> eventObligations = getObligations(event);
	for (EventProcessingObligation task : eventObligations) {
      task.setChangeEvent(event);
      ObligationProcessor op = obligationMap.getObligationProcessor(task);
      if (op == null) {
        log.warn("Null OP for " + task);
        throw new ObligationProcessingException("Null OP for " + task);
      } else {
        op.processObligation(task, task.getChangeEvent(), cb);
      }
    }
	log.debug("Done obligation processing transaction");
    return true;
  }
  
  private int processObligation(List<IdObject> entries, Callback cb) throws Exception {
	int numEntriesProcessed = 0;
    for (IdObject ido : entries) {
      if (ido == null)
        continue;
      if (processObligationsForEvent(ido, cb) == true)
        numEntriesProcessed++;
    }
    return numEntriesProcessed;
  }
  
  @Override
  public boolean processAllEvents(Callback cb) throws Exception {
    cb.setConnector(idDao.getConnector());
    cb.setIdDao(idDao);

	try {
	  long entriesReceived = 0;
	  long entriesProcessed = 0;
      initialize(cb);

      List<IdObject> entries = idDao.getNextGroupChangeSet();
      entriesReceived += entries.size();
      entriesProcessed += processObligation(entries, cb);

      entries = idDao.getNextUserChangeSet();
      entriesReceived += entries.size();
      entriesProcessed += processObligation(entries, cb);
      
      entries = idDao.getNextContainerChangeSet();
      entriesReceived += entries.size();
      entriesProcessed += processObligation(entries, cb);
      cb.setNumEntriesFromIds(entriesReceived);
      cb.setNumEntriesToSvc(entriesProcessed);
      
      finalize(cb);
	} catch (Exception ex) {
      log.warn("Exception", ex);
      idDao.rollbackState();
      idDao.saveOperationResult(OperationType.SYNCHRONIZATION_OPERATION, OperationResult.FAILURE, 
          ex.getMessage(), cb.getNumEntriesFromIds(), cb.getNumEntriesToSvc());
      throw ex;	  
	}
	
	return true;
  }

}
