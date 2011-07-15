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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.context.ApplicationContext;

import server.id.dao.IdentityDAOFactory;
import server.id.sync.EventProcessor;
import server.id.sync.ObligationProcessor;
import server.id.sync.agent.RunHistory.RunInfo;
import server.id.sync.agent.domain.Connector;
import server.id.sync.server.SynchronizationService;


public class ConnectorSyncJob implements StatefulJob {
  private final Log log = LogFactory.getLog(getClass());

  private class SOAPCallback extends ObligationProcessor.Callback {
    public SOAPCallback(SynchronizationService synchronizationService) {
      setSynchronizationService(synchronizationService);
      setEnablePaging(true);
    }
  }

  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = context.getJobDetail().getName();
    String jobGroup = context.getJobDetail().getGroup();
    log.info("Running synchronization for job / group " + jobName + " / " + jobGroup);

    RunInfo ri = new RunHistory.RunInfo();
    ri.setStartTime(new Date());
    
    JobDataMap dataMap = context.getMergedJobDataMap();
    Connector connector = (Connector)dataMap.get(SyncScheduleManager.CONNECTOR);
    RunHistory runHistory = (RunHistory)dataMap.get(SyncScheduleManager.RUN_HISTORY);
    String runType = dataMap.getString(SyncScheduleManager.RUNTYPE);
    SynchronizationService synchronizationService = (SynchronizationService)
      dataMap.get(SyncScheduleManager.SYNCSERVICE);
    IdentityDAOFactory daoFactory = (IdentityDAOFactory)dataMap.get(SyncScheduleManager.DAO_FACTORY);
    ApplicationContext applicationContext = (ApplicationContext)
      dataMap.get(SyncScheduleManager.APPLICATION_CONTEXT);
    SyncScheduleManager syncScheduleManager = (SyncScheduleManager)dataMap.get(SyncScheduleManager.SCHEDULER);
    
    ri.setRunType(runType);
    
    SOAPCallback cb = new SOAPCallback(synchronizationService);
    try {
      EventProcessor ep = daoFactory.getEventProcessor(applicationContext, null, connector, null);
      boolean result = ep.processAllEvents(cb);
      ri.setSuccess(result);
      runHistory.add(ri);
      if (result == false) {
        log.warn("execute: processAllEvents returned false. Event Processing failed");
        syncScheduleManager.rescheduleFailedJob(jobName, jobGroup, connector, runHistory);
      } else {
        log.debug("execute: successfully processed all events");
      }
    } catch (Exception ex) {
      ri.setSuccess(false);
      ri.setException(ex);
      ri.setErrorDescription(ex.getMessage());
      log.warn("Exception : ", ex);
      runHistory.add(ri);
      syncScheduleManager.rescheduleFailedJob(jobName, jobGroup, connector, runHistory);
    } finally {
      ri.setEndTime(new Date());
      log.info("Done synchronization for job / group " + jobName + " / " + jobGroup);
    }
  }
}
