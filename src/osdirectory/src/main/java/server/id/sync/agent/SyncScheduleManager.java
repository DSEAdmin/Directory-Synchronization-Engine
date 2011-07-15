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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdScheduler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import server.id.dao.IdentityDAOFactory;
import server.id.dao.LocalIdentityStore;
import server.id.sync.server.SynchronizationService;


public class SyncScheduleManager implements ApplicationContextAware {
  public static final String connectorJobGroup = "ConnectorJobGroup";
  public static final String connectorCronTriggerGroup = "ConnectorCronTriggerGroup";
  public static final String CONNECTOR = "server.id.sync.agent.Connector";
  public static final String RUN_HISTORY = "server.id.sync.agent.RunHistory";
  public static final String RUNTYPE = "server.id.sync.agent.RunType";
  public static final String SYNCSERVICE = "server.id.sync.agent.SynchronizationService";
  public static final String DAO_FACTORY = "server.id.sync.agent.DaoFactory";
  public static final String APPLICATION_CONTEXT = "server.id.sync.agent.ApplicationContext";
  public static final String SCHEDULER = "server.id.sync.agent.SyncScheduleManager";
  public static final String RUNTYPE_AGENT = "Agent";
  public static final String RUNTYPE_MANUAL = "Manual";
  
  private final Log log = LogFactory.getLog(getClass());
  private StdScheduler scheduler;
  private LocalIdentityStore idStore;
  private SynchronizationService soapSynchronizationService;
  private IdentityDAOFactory daoFactory; 
  private ApplicationContext applicationContext;
  
  public void setScheduler(StdScheduler springSchedular) {
    this.scheduler = springSchedular;
  }

  public void setIdStore(LocalIdentityStore localIdStore) {
    this.idStore = localIdStore;
  }
  
  public void setSoapSynchronizationService(SynchronizationService soapSynchronizationService) {
    this.soapSynchronizationService = soapSynchronizationService;
  }

  public void setDaoFactory(IdentityDAOFactory daoFactory) {
    this.daoFactory = daoFactory;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public SyncScheduleManager() {
  }

  private void putJobParameters(JobDetail job, server.id.sync.agent.domain.Connector c) {
    job.getJobDataMap().put(CONNECTOR, c);
    job.getJobDataMap().put(RUN_HISTORY, new RunHistory());
    job.getJobDataMap().put(SCHEDULER, this);    
    job.getJobDataMap().put(DAO_FACTORY, daoFactory);
    job.getJobDataMap().put(APPLICATION_CONTEXT, applicationContext);        
  }
  
  private void putTriggerParameters(Trigger trigger) {
    trigger.getJobDataMap().put(RUNTYPE, RUNTYPE_AGENT);
    trigger.getJobDataMap().put(SYNCSERVICE, soapSynchronizationService);
  }
  
  private void putParameters(JobDetail job, Trigger trigger, server.id.sync.agent.domain.Connector c) {
    putJobParameters(job, c);
    putTriggerParameters(trigger);
  }
  
  public void rescheduleFailedJob(String jobName, String jobGroup,
      Connector connector,
      RunHistory history) {    
    if (connector.getRetryCount() == 0 || history.getErrorCount() >= connector.getRetryCount()) {
      log.info("rescheduleFailedJob: " + jobName + " / " + jobGroup + " exceeded, equals or disabled retry count. Not rescheduled");
      return;
    }
    try {
      JobDetail job = scheduler.getJobDetail(jobName, jobGroup);
      Trigger[] triggers = scheduler.getTriggersOfJob(jobName, jobGroup);
      Date next = null;
      for (Trigger t : triggers) {
        Date td = t.getFireTimeAfter(new Date());
        if (next == null || next.after(td)) {
          next = td;
        }
      }
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.SECOND, (int)connector.getRetryInterval());
      Date fireTime = calendar.getTime();
      if ( next == null || next.after(fireTime)) {
        SimpleTrigger fTrigger = new SimpleTrigger(connector.getName() + "FTrigger",
            "FailedTriggerGroup", fireTime);
        Date date = scheduler.scheduleJob(job, fTrigger);
        log.info("Successfully created a schedule. First fire time is : " + date);
      } else {
        log.info("Next scheduled fire time of job " + jobName + " / " + jobGroup + "before calculated retry time. " +
        		"Not scheduling retry");
      }
    } catch (SchedulerException ex) {
      log.warn("Failed to reschedule job " + jobName + " / " + jobGroup, ex);
    }
  }
  
  public void init() throws Exception {
    log.trace("init: Initializing SyncScheduleManager");
    List<? extends Connector> connectors = idStore.getConnectors(null);
    for (Connector c : connectors) {
      server.id.sync.agent.domain.Connector connector = 
        (server.id.sync.agent.domain.Connector) c;
      if (connector.getMode() == OperationMode.AGENT_SYNCHRONIZATION) {
        try {
          log.info("Creating scheduled job for connector " + connector.getName());
          JobDetail job = new JobDetail(connector.getName() + "Job", connectorJobGroup, ConnectorSyncJob.class);
          Trigger trigger = new CronTrigger(connector.getName() + "CronTrigger", connectorCronTriggerGroup, 
              connector.getSyncSchedule());
          putParameters(job, trigger, connector);
          Date date = scheduler.scheduleJob(job, trigger);
          log.info("Successfully created a schedule. First fire time is : " + date);
        } catch (SchedulerException ex) {
          log.warn("Failed to schedule sync jobs for connector " + connector.getName(), ex);
        } catch (Exception ex) {
          log.warn("Exception : Failed to schedule sync jobs for connector " + connector.getName(), ex);
        }
      }
    }
  }
}
