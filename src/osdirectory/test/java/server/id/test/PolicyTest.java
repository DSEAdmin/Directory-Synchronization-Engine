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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.Constants;
import server.id.dao.LocalIdentityStore;
import server.id.krb.KrbConfigManager;
import server.id.sync.ChangeEventPDPFactory;
import server.id.sync.EventProcessor;
import server.id.sync.ObligationProcessor;
import server.id.sync.PolicyBuilder;
import server.id.sync.agent.ClientChangeEventFactory;
import server.id.sync.server.FileSyncService;
import server.id.sync.server.SynchronizationService;
import server.id.test.InitializeDb.DomainException;


import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.Indenter;

public class PolicyTest extends AbstractTest {
  private class OPCallback extends ObligationProcessor.Callback {

    public OPCallback() {
      ClientChangeEventFactory factory = 
        (ClientChangeEventFactory)applicationContext.getBean(Constants.getClientChangeEventFactoryBeanName());
      setSynchronizationService(new FileSyncService(factory,"ChangeReq.xml"));
      setEnablePaging(false);
    }
  }

  private class SOAPCallback extends ObligationProcessor.Callback {

    public SOAPCallback() {
      SynchronizationService service = (SynchronizationService)applicationContext.getBean(Constants.getSoapSyncServiceClientBeanName());
      setSynchronizationService(service);
      setEnablePaging(true);
    }
  }

  
  public static final String EVENT_PROCESSOR = "testADEventProcessor";
  private final Log log = LogFactory.getLog(getClass());
  private ChangeEventPDPFactory pdpFactory;
  KrbConfigManager krbMgr;

  @Override
  protected void onSetUp() throws Exception {
    super.onSetUp();
    pdpFactory = (ChangeEventPDPFactory) applicationContext.getBean(Constants.getAdAgentPDPFactoryBeanName());
  }

  
  public void testBuildPolicy() {
    log.trace("");
    PolicyBuilder pb = new PolicyBuilder(pdpFactory.getPolicySetName(), pdpFactory.getPolicies());
    AbstractPolicy policy = pb.buildXACMLPolicy();
    policy.encode(System.out, new Indenter());
  }
  
  @SuppressWarnings("unused")
  public void testGetObligations() {
    log.trace("testGetObligations");
    System.setProperty("server.id.localIdStoreBean", "testLocalIdStore");
    System.setProperty("java.security.krb5.conf", "krb5.conf");
    System.setProperty("java.security.auth.login.config", (getClass().getResource("jaas.conf")).toExternalForm());
    log.info("java.security.krb5.conf set to " + System.getProperty("java.security.krb5.conf"));
    log.info("java.security.auth.login.config set to" + System.getProperty("java.security.auth.login.config"));
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

      OPCallback cb = new OPCallback();
      SOAPCallback scb = new SOAPCallback();
      EventProcessor ep = daoFactory.getEventProcessor(applicationContext, null, conn, null);
      boolean done = false;
      do {
        ep.processAllEvents(scb);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
          String input;
          while (true) {
            System.out.println("Press x to exit, return to continue");
            input = in.readLine();
            if (input.startsWith("x") || input.startsWith("X")) {
              done = true;
              break;
            } else {
              break;
            }
          }
        } catch (IOException e) {
          System.err.println();
        }
      } while (done == false);
    } catch (RuntimeException ex) {
      log.error("Uncaught runtime exception:", ex);
      throw ex;
    } catch (Exception ex) {
      log.error("Uncaught checked exception:", ex);
    } finally {
    }

  }
  
}
