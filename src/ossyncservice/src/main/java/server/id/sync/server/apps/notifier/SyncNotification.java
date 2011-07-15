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

import java.util.Date;

import Connector;

public class SyncNotification {
  private boolean syncedOnSchedule;
  Date lastSynced;
  Date lastScheduledSyncTime;
  Connector connector;

  public SyncNotification() {
	super();
  }

  public SyncNotification(boolean syncedOnSchedule, Date lastSynced,
	  Date lastScheduledSyncTime, Connector connector) {
	super();
	this.syncedOnSchedule = syncedOnSchedule;
	this.lastSynced = lastSynced;
	this.lastScheduledSyncTime = lastScheduledSyncTime;
	this.connector = connector;
  }

  public boolean isSyncedOnSchedule() {
    return syncedOnSchedule;
  }

  public void setSyncedOnSchedule(boolean syncedOnSchedule) {
    this.syncedOnSchedule = syncedOnSchedule;
  }

  public Date getLastSynced() {
    return lastSynced;
  }

  public void setLastSynced(Date lastSynced) {
    this.lastSynced = lastSynced;
  }

  public Date getLastScheduledSyncTime() {
    return lastScheduledSyncTime;
  }

  public void setLastScheduledSyncTime(Date lastScheduledSyncTime) {
    this.lastScheduledSyncTime = lastScheduledSyncTime;
  }

  public Connector getConnector() {
    return connector;
  }

  public void setConnector(Connector connector) {
    this.connector = connector;
  }
  
  public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Connector : ");
	sb.append(connector.getName());
	sb.append("\n");
	sb.append("syncedOnSchedule : ");
	sb.append(syncedOnSchedule);
	sb.append("\n");
	sb.append("lastSynced : ");
	sb.append(lastSynced);
	sb.append("\n");
	sb.append("lastScheduledSyncTime : ");
	sb.append(lastScheduledSyncTime);
	sb.append("\n");
	return sb.toString();
  }
}
