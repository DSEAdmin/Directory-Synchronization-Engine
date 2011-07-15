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
package server.id.sync.server.op;


import java.util.Date;
import java.util.Set;


import server.id.Audit;
import server.id.sync.ChangeEvent;
import server.id.sync.EventProcessingObligation;
import server.id.sync.op.ObligationProcessorBase;

public abstract class ServiceObligationProcessorBase extends
	ObligationProcessorBase {
  protected DBDao idDao;
  
  public void setIdDao(DBDao idDao) {
    this.idDao = idDao;
  }

  public void setServiceEntitiesDao(EntitiesDao serviceEntitiesDao) {
    this.serviceEntitiesDao = serviceEntitiesDao;
  }

  protected void audit(User user,
	  LogDefType logDefType,
	  EventProcessingObligation obligation, 
	  ChangeEvent event, 
	  Callback cb, 
	  String message) {
	int currTime = (int)((new Date()).getTime() / 1000);
	String auditMessage = generateAuditMessage(obligation, event, cb, message);
	LogDef ld = serviceEntitiesDao.getLogDef(logDefType);
	Set<UserInvite> uiSet = user.getUserInvites();
	UserInvite userInvite = null;
	if (uiSet != null && uiSet.size() == 1) {
	  userInvite = uiSet.iterator().next();
	}
	UserLog ul = new UserLog(device, userInvite, user, ld, currTime, currTime, "", auditMessage, "0.0.0.0", null, null, null, null, null);
	serviceEntitiesDao.addUserLog(ul);
	Audit.log(auditMessage);
  }

  protected void auditFailure(User user,
	  LogDefType logDefType,
	  EventProcessingObligation obligation, 
	  ChangeEvent event, 
	  Callback cb, 
	  String failureMessage,
	  String message) {
	int currTime = (int)((new Date()).getTime() / 1000);
	String auditMessage = generateFailureAuditMessage(obligation, event, cb, failureMessage, message);
	LogDef ld = serviceEntitiesDao.getLogDef(logDefType);
	Set<UserInvite> uiSet = user.getUserInvites();
	UserInvite userInvite = null;
	if (uiSet != null && uiSet.size() == 1) {
	  userInvite = uiSet.iterator().next();
	}
	UserLog ul = new UserLog(device, userInvite, user, ld, currTime, currTime, "", auditMessage, "0.0.0.0", null, null, null, null, null);
	serviceEntitiesDao.addUserLog(ul);
	Audit.log(auditMessage);
  }
}
