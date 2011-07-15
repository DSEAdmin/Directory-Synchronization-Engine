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
package server.id.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.Constants;
import server.id.dao.IdentityDAOFactory;
import server.id.dao.LocalIdentityStore;
import server.id.sync.EventProcessor;
import server.id.sync.ObligationProcessor;
import server.id.sync.agent.ClientChangeEventFactory;
import server.id.sync.server.FileSyncService;
import server.id.test.InitializeDb.DomainException;



public class ADLdifTest extends AbstractTest {
  private class OPCallback extends ObligationProcessor.Callback {

    public OPCallback(Connector connector) {
      ClientChangeEventFactory factory = 
        (ClientChangeEventFactory)applicationContext.getBean(Constants.getClientChangeEventFactoryBeanName());
      setSynchronizationService(new FileSyncService(factory,"ChangeReq.xml"));
      setEnablePaging(false);
    }
  }

  private final Log log = LogFactory.getLog(getClass());

  public void testEventGenerationFromLdif() {
    log.trace("testEventGenerationFromLdif");
    System.setProperty("server.id.localIdStoreBean", "testLocalIdStore");
    try {
      LocalIdentityStore idStore = (LocalIdentityStore) applicationContext.getBean("testLocalIdStore");
      if (idStore == null) {
        log.error("Could not get local identity store bean");
        throw new DomainException("Could not get local Identity store bean");
      }

      System.out.println("**** Create Connector ****");
      List<? extends Connector> connectors = idStore.getConnectors(null);
      Connector conn = null;
      if (connectors.size() == 1) {
        conn = connectors.get(0);
      } else {
        throw new Exception("Could not find connector");
      }

      OPCallback cb = new OPCallback(conn);
      Map<String, Object> options = new HashMap<String, Object>();
      options.put(IdentityDAOFactory.ldifFileName, "shirish.ldif");
      EventProcessor ep = daoFactory.getEventProcessor(applicationContext, null, conn, options);
      ep.processAllEvents(cb);
    } catch (RuntimeException ex) {
      log.error("Uncaught runtime exception:", ex);
      throw ex;
    } catch (Exception ex) {
      log.error("Uncaught checked exception:", ex);
    } finally {
    }
  }
}
