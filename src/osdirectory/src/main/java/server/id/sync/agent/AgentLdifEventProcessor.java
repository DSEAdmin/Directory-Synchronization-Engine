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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.IdObject;
import server.id.dao.State;
import server.id.sync.ObligationProcessor.Callback;

public class AgentLdifEventProcessor extends AgentEventProcessor {
  private Log log = LogFactory.getLog(getClass());

  
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
        
        entries = idDao.getNextContainerChangeSet();
        
        if (entries == null || entries.size() == 0) {
          entries = idDao.getNextGroupChangeSet();
          if (entries == null || entries.size() == 0) {
            entries = idDao.getNextUserChangeSet();
            more = idDao.getContainerState() == State.MORE_AVAILABLE;
          }
        }
        
        cb.setMore(more);
        
        if (entries != null) {
          processEntries(entries, cb);
        }

        cb.setConnector(idDao.refreshConnector());        
      } while (more == true);

      cb.getIdDao().saveStateToDB();

      finalize(cb);
    } catch (Exception ex) {
      log.warn("Exception", ex);
      idDao.rollbackState();
      throw ex;
    }

    return true;
  }
}
