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
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.Constants;
import server.id.dao.LocalIdentityStore;
import server.id.sync.EventProcessor;
import server.id.sync.ObligationProcessor;
import server.id.sync.server.SynchronizationService;
import server.id.test.InitializeDb.DomainException;


public class ActiveDirectoryTest extends AbstractTest {

  private class SOAPCallback extends ObligationProcessor.Callback {

    public SOAPCallback() {
      SynchronizationService service = (SynchronizationService)applicationContext.getBean(Constants.getSoapSyncServiceClientBeanName());
      setSynchronizationService(service);
      setEnablePaging(true);
    }
  }

  
  public static final String EVENT_PROCESSOR = "testADEventProcessor";
  protected final Log log = LogFactory.getLog(getClass());
  protected LDAPContactDAO ldapContact;
  
  protected ServiceDAO serviceDB;
  protected AdUser newAdUser;
  @Override
  protected void onSetUp() throws Exception {
    super.onSetUp();
    ldapContact = (LDAPContactDAO)applicationContext.getBean("ldapContact");
    serviceDB = new ServiceDAO();
  }


  //@SuppressWarnings("unused")
  public void SyncData() {
    log.trace("testGetObligations");
    System.setProperty("server.id.localIdStoreBean", "testLocalIdStore");
    System.setProperty("java.security.auth.login.config", (getClass().getResource("jaas.conf")).toExternalForm());
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

      SOAPCallback scb = new SOAPCallback();
      EventProcessor ep = daoFactory.getEventProcessor(applicationContext, null, conn, null);
      ep.processAllEvents(scb);
    } catch (RuntimeException ex) {
      log.error("Uncaught runtime exception:", ex);
      throw ex;
    } catch (Exception ex) {
      log.error("Uncaught checked exception:", ex);
    } finally {
    }

  }
  protected User getUser(){
	//get the account object.
	  Account account = null;
	  User user = null;
	  account = (Account) serviceDB.getAccountByName("testAccount");
	  
	  //Get UserTable Info.
	  //List<User> users = serviceDB.getUserByUsername(account,newAdUser.getUserName());
	  List<User> users = serviceDB.getUserByUsernameWithComponents(account,newAdUser.getUserName());
	  if(users != null && users.size() != 0){
		  if(users.size() == 1 ){
			  user = users.get(0);
			  return user;
		  }
	  }
	  return user;
  }
}

