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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.IdObjectType;
import server.id.sync.ChangeEvent;
import server.id.sync.EventProcessingObligation;

public class DeleteLocalEntry extends ServiceObligationProcessorBase {

  private Log log = LogFactory.getLog(getClass());

  public void processObligation(EventProcessingObligation obligation,
	  ChangeEvent event, Callback cb) throws Exception {
	log.debug("processObligation: " + obligation);
	assert (event.getLocalObj() != null);
	if (event.getRemoteObj().getObjectType() == IdObjectType.PERSON) {
	  User user = idDao.deleteUser(event);	  
	  audit(user, null, LogDefType.sync_agent_deleted_user, obligation, event, cb, null);
	} else {
	  log.warn("Unimplemented");
	}
  }

}
