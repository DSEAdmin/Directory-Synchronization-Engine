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
package server.id.sync.agent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.mail.MethodNotSupportedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import server.id.IdObject;
import server.id.dao.ChangePoller;
import server.id.dao.InvalidConfigurationException;
import server.id.dao.LocalIdentityStore;
import server.id.dao.State;
import server.id.sync.ChangeEvent;
import server.id.sync.EventGenerator;
import server.id.sync.EventProcessingObligation;
import server.id.sync.EventProcessor;
import server.id.sync.ObligationProcessor;
import server.id.sync.ObligationProcessor.Callback;
import server.id.sync.op.ObligationProcessingException;
import server.id.sync.server.ConnectorConfigurationResult;
import server.id.sync.server.ConnectorStatusResult;
import server.id.sync.server.FullSyncMetaDataResult;
import server.id.sync.server.Result;


public class AgentEventProcessor extends EventProcessor {
  private Log log = LogFactory.getLog(getClass());
  protected LocalIdentityStore localStore;
  protected ChangePoller idDao;
  protected EventGenerator eventGenerator;

  public void setLocalStore(LocalIdentityStore localStore) {
    this.localStore = localStore;
  }
  
  public void setIdDao(ChangePoller idDao) {
    this.idDao = idDao;
  }
  
  public void setEventGenerator(EventGenerator eventGenerator) {
    this.eventGenerator = eventGenerator;
  }
  
  private EventProcessingObligation getBatchStart() {
    return new EventProcessingObligation(EventProcessingObligation.ObligationType.BATCH_SEND_START, 
        "Initialize the batch send operation", 0);
  }
  private EventProcessingObligation getBatchEnd() {
    return new EventProcessingObligation(EventProcessingObligation.ObligationType.BATCH_SEND_END, 
        "Finalize the batch send operation", 0);
  }
  
  //Use knowledge of task dependency graph to generate a list or ordered task to execute
  private List<EventProcessingObligation> getTaskList(List<IdObject> entries, Callback cb) {
    long entriesReceived = 0;
    long entriesProcessed = 0;
    
    List<EventProcessingObligation> remote = new LinkedList<EventProcessingObligation>();
    List<EventProcessingObligation> local = new LinkedList<EventProcessingObligation>();

    if (cb.isEnablePaging()) {
      EventProcessingObligation start = getBatchStart();
      remote.add(start);
    }
    
    for (IdObject ido : entries) {
      entriesReceived++;
      if (ido == null)
        continue;
      ChangeEvent event = eventGenerator.generateEvent(idDao, ido);
      if (event == null) {
        log.warn("Could not generate event for " + ido.getDn());
        continue;
      }
      List<EventProcessingObligation> eventObligations = getObligations(event);
      for (EventProcessingObligation o : eventObligations) {
        o.setChangeEvent(event);
        if (o.getObligationGroup() == EventProcessingObligation.ObligationGroup.DB) {
          local.add(o);
        } else {
          remote.add(o);
        }
      }
      entriesProcessed++;
    }
    
    if (cb.isEnablePaging()) {
      EventProcessingObligation end = getBatchEnd();
      remote.add(end);
    }
    
    remote.addAll(local);
    log.debug("Entries Reveived = " + entriesReceived + " Entries Processed = " + entriesProcessed);
    return remote;
  }

  private void sendConnectorConfiguration(Callback cb) throws Exception {
    Connector c = cb.getConnector();
    
    ConnectorConfigurationResult configResult = cb.getSynchronizationService().pushConnectorConfiguration(c);
    if (configResult.getResult() != Result.SUCCESS) {
      log.warn("Could not update connector " + c.getName() + " configuration. Failure is " + 
          configResult.getResult().name() + " errorString = " + configResult.getErrorString());
      throw new ObligationProcessingException("Could not update connector " + c.getName() + " configuration. Failure is " + 
          configResult.getResult().name() + " errorString = " + configResult.getErrorString());
    } else {
      log.debug("Connector configuration updated to version " + c.getMajorVersion() + "." + c.getMinorVersion());
      cb.setServiceConnectorMajorVersion(c.getMajorVersion());
      cb.setServiceConnectorMinorVersion(c.getMinorVersion());      
    }
    
    //TODO Audit logging    
  }
  
  protected void synchronizeConnectorConfiguration(Callback cb) throws Exception {
    boolean needsSynchronization = false;
    Connector c = cb.getConnector();
    ConnectorStatusResult result = null;
    if (c.getMode() == OperationMode.AGENT_SYNCHRONIZATION) {
      result = cb.getSynchronizationService().pushStatus(c);
      if (result.getResult() == Result.NO_SUCH_CONNECTOR) {
        needsSynchronization = true;
      } else if (result.getResult() != Result.SUCCESS) {
        log.warn("Could not get status of connector : " + c.getName() + 
            ". Error : " + result.getResult().name() + " : " + result.getErrorString());
        throw new ObligationProcessingException("Could not get status of connector : " + c.getName() + 
            ". Error : " + result.getResult().name() + " : " + result.getErrorString());
      } else {
        if (Arrays.equals(result.getConnectorUuid(), c.getUuid()) == false) {
          throw new ObligationProcessingException("Connector uuid sent in status does not match with one returned");
        }
        if (c.getMajorVersion() != result.getMajorVersionNumber() || 
            c.getMinorVersion() != result.getMinorVersionNumber()) {
          needsSynchronization = true;
        } else {
          log.debug("Connector configuration does not need synchronization");
        }
        
        cb.setServiceConnectorMajorVersion(result.getMajorVersionNumber());
        cb.setServiceConnectorMinorVersion(result.getMinorVersionNumber());
      }
    }
    
    if (needsSynchronization == true) {
      log.info("Connector " + c.getName() + " needs configuration synchronization. Remote : " + 
          result.getMajorVersionNumber() + "." + result.getMinorVersionNumber() + " Local : " +
          c.getMajorVersion() + "." + c.getMinorVersion());
      sendConnectorConfiguration(cb);
    }
  }
  
  protected boolean initialize(Callback cb) throws Exception {
    synchronizeConnectorConfiguration(cb);
    if (cb.isEnablePaging() == false) {
      log.debug("initialize: Paging is disabled");
      EventProcessingObligation start = getBatchStart();
      ObligationProcessor op = obligationMap.getObligationProcessor(start);
      op.processObligation(start, start.getChangeEvent(), cb);
    }
    if (cb.getIdDao().getContainerState() == State.INITIAL && cb.getIdDao().getGroupState() == State.INITIAL
        && cb.getIdDao().getUserState() == State.INITIAL) {
      try {
        if (cb.getIdDao().validateConfiguration() == false) {
          log.warn("Failed to validate configuration: ");
          throw new ObligationProcessingException("Failed to validate Configuration");
        }
      } catch (InvalidConfigurationException ex) {
        log.warn("The configuration is outdated. Fix the errors first: " + ex.getLocalizedMessage(), ex);
        throw ex;
      } catch (MethodNotSupportedException ex) {
        log.info("Validation of configuration not supported. Assuming the configuration is valid");
      }
    }
    SyncStatus syncStatus = cb.getConnector().getSyncStatus();
    if (syncStatus == SyncStatus.FULL_SYNC_DONE || syncStatus == SyncStatus.DELTA_SYNC_DONE) {
      localStore.setSyncStatus(null, cb.getConnector(), SyncStatus.DELTA_SYNC_IN_PROGRESS);
    }
    return true;
  }
  
  protected boolean finalize(Callback cb) throws Exception {
    if (cb.isEnablePaging() == false) {
      log.debug("finalize: Paging is disabled");
      EventProcessingObligation end = getBatchEnd();
      ObligationProcessor op = obligationMap.getObligationProcessor(end);
      op.processObligation(end, end.getChangeEvent(), cb);      
    }
    SyncStatus syncStatus = cb.getConnector().getSyncStatus();
    if (cb.getIdDao().getUserState() == State.POLL &&
        cb.getIdDao().getGroupState() == State.POLL &&
        cb.getIdDao().getContainerState() == State.POLL) {
      if (syncStatus == SyncStatus.DELTA_SYNC_IN_PROGRESS) {
        localStore.setSyncStatus(null, cb.getConnector(), SyncStatus.DELTA_SYNC_DONE);
      } else if (syncStatus == SyncStatus.FULL_SYNC_IN_PROGRESS) {
        sendFullSyncMetaData(cb);
      }
    }
    return true;
  }

  @Transactional(readOnly = false)
  private void sendFullSyncMetaData(Callback cb) throws Exception {
    List<byte[]> users = localStore.getSynchronizedUsersUuid(null, cb.getConnector());
    try {
      FullSyncMetaDataResult result = cb.getSynchronizationService().pushFullSyncMetaData(cb.getConnector(), users);
      if (result.getResult() != Result.SUCCESS) {
        log.warn("Could not push full synchronization meta data. Will try again during next synchronization cycle");
        throw new ObligationProcessingException("Error with pushFullSyncMetaData:" + result.getErrorString());
      }
    } catch (Exception e) {
      log.warn("Exception", e);
      throw e;
    }
    localStore.setSyncStatus(null, cb.getConnector(), SyncStatus.FULL_SYNC_DONE);    
  }

  @Transactional(readOnly = false)
  private boolean processDBObligations(EventProcessingObligation task, 
      Iterator<EventProcessingObligation> it, Callback cb) throws Exception {
    log.debug("Starting opligation processing in DB group in a transaction");
    while (task != null) {
      ObligationProcessor op = obligationMap.getObligationProcessor(task);
      if (op == null) {
        log.warn("Null OP for " + task);
        throw new ObligationProcessingException("Null OP for " + task);
      } else {
        op.processObligation(task, task.getChangeEvent(), cb);
      }
      
      if (it.hasNext() == true) {
        task = it.next();
      } else {
        task = null;
      }
    }           
    log.debug("Done opligation processing in DB group in a transaction");    
    return true;
  }

  protected boolean processEntries(List<IdObject> entries, Callback cb) throws Exception {
    List<EventProcessingObligation> taskList = getTaskList(entries, cb);
    Iterator<EventProcessingObligation> it = taskList.iterator();
    EventProcessingObligation task = null;
    while (it.hasNext() == true) {
      task = it.next();
      if (task.getObligationGroup() == EventProcessingObligation.ObligationGroup.DB) {
        break;
      }
      ObligationProcessor op = obligationMap.getObligationProcessor(task);
      if (op == null) {
        log.warn("Null OP for " + task);
        throw new ObligationProcessingException("Null OP for " + task);
      } else {
        op.processObligation(task, task.getChangeEvent(), cb);
        task = null;
      }
    }
 
    processDBObligations(task, it, cb);

    return true;
  }

  /**
   * We get containers first because they may modify the DitVirtualization tables. Then we need the 
   * groups because we need to know about the members before we get the users. The groups may also 
   * modify the Rv table. While we do all this we will need to keep the Dv table synchronized between
   * the agent and the service/server. Rv is not that necessary as we get away by using the uuid. 
   */  
  @Override
  public boolean processAllEvents(Callback cb) throws Exception {
    cb.setDelta(idDao.getUserState() != State.INITIAL); //If the user is in initial, so should be others
    cb.setConnector(idDao.refreshConnector());
    cb.setIdDao(idDao);
    
    try {
      initialize(cb);
      int i = 0;
      boolean more = true;
      
      do {
        i += 1;
        log.debug("Iteration " + i + " of processAllEvents");

        //Dv may have changed. Synchronize before moving ahead
        if (cb.getServiceConnectorMajorVersion() != cb.getConnector().getMajorVersion() || 
            cb.getServiceConnectorMinorVersion() != cb.getConnector().getMinorVersion()) {
          synchronizeConnectorConfiguration(cb);
        }
        
        List<IdObject> entries = null;
        
        entries = idDao.getNextChangeSet();
        
        more = idDao.getUserState() == State.MORE_AVAILABLE;
        
        cb.setMore(more);
        
        if (entries != null) {
          cb.setNumEntriesFromIds(entries.size());
          processEntries(entries, cb);
        } else {
          cb.setNumEntriesFromIds(0);
        }

        cb.getIdDao().saveStateToDB();

        cb.setConnector(idDao.refreshConnector());        
        idDao.saveOperationResult(OperationType.SYNCHRONIZATION_OPERATION, OperationResult.SUCCESS, 
            "", cb.getNumEntriesFromIds(), cb.getNumEntriesToSvc());
      } while (more == true);

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
