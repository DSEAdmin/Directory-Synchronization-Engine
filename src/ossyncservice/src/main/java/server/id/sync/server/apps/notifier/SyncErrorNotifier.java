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
package server.id.sync.server.apps.notifier;


import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.Trigger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import EntityDao;
import Connector;
import OperationResult;

public class SyncErrorNotifier extends Observable {
  private ClassPathXmlApplicationContext applicationContext;
  private final Log log = LogFactory.getLog(getClass());
  private EntityDao entities;
  
  public EntityDao getEntities() {
    return entities;
  }

  public void setEntities(EntityDao entities) {
    this.entities = entities;
  }

  private String[] getConfigLocations() {
	String propFile = ClassLoader.getSystemResource(
	"server/id/sync/test/syncservice.properties").getPath();
	System.setProperty("syncservice.properties", propFile);

	return new String[] { "server/id/sync/server/apps/notifier/NotifierBeans.xml" };
  }

  public SyncErrorNotifier() {
    log.info("Creating application context");
    String[] config = getConfigLocations();
    applicationContext = new ClassPathXmlApplicationContext(config);
    entities = (EntityDao)applicationContext.getBean("serviceEntitiesDAO");
    log.info("Done creating application context : " + applicationContext);
  }
  
  public SyncNotification getSyncNotification(Connector connector) {
	SyncNotification sn = new SyncNotification();
	sn.setConnector(connector);
	try {
	  Date startTime = new Date(connector.getCreateDate());
	  Trigger trigger = new CronTrigger(connector.getName() + "CronTrigger",
		  "scheduleCheckGroup", "scheduleCheckJob", null, startTime, null, 
		  /*connector.getSyncSchedule()*/"0 0 12 * * ?", TimeZone.getTimeZone(connector.getTimeZone()));
	  Date prevFireTime = trigger.getPreviousFireTime();
	  sn.setLastScheduledSyncTime(prevFireTime);
	  long prevUTCFireTime = prevFireTime.getTime();
	  List<OperationResult> lastSyncResults = entities
		  .getOperationResultsAfter(connector, prevUTCFireTime);
	  sn.setSyncedOnSchedule(false);
	  if (lastSyncResults != null) {
		for (OperationResult or : lastSyncResults) {
		  if (or.getOpResult() == OperationResult.SUCCESS) {
			sn.setSyncedOnSchedule(true);
			sn.setLastSynced(new Date(or.getCreateDate()));
		  }
		}
	  }
	} catch (ParseException e) {
	  e.printStackTrace();
	}

	return sn;
  }
  
  public void checkConnectors() {
	List<Connector> connectors = entities.getAllConnectors();
	for (Connector c : connectors) {
	  SyncNotification sn = getSyncNotification(c);
	  if (sn.isSyncedOnSchedule() == false) {
		setChanged();
		notifyObservers(sn);
	  }
	}
  }
  
  public static void main(String[] args) throws Exception {
	SyncErrorNotifier sen = new SyncErrorNotifier();
	sen.checkConnectors();
  }
}
