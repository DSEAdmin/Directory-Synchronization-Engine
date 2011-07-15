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
package server.id.sync.server;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;

import server.id.Constants;
import server.id.dao.ChangePoller;
import server.id.dao.IdentityDAOFactory;
import server.id.ldap.ObjectTypeSpec;
import server.id.sync.ChangeEventPDPFactory;
import server.id.sync.EventGenerator;
import server.id.sync.EventProcessor;
import server.id.sync.messages.v1.ChangeRequest;
import server.id.sync.op.ObligationMap;

public class ServerIdentityDAOFactory extends Object implements
	IdentityDAOFactory {
  private final Log log = LogFactory.getLog(getClass());
  private IdentityDAOFactory.DeploymentType deploymentType;
    
  public void setDeploymentType(IdentityDAOFactory.DeploymentType deploymentType) {
    this.deploymentType = deploymentType;
  }

  private ObjectTypeSpec getObjectTypeSpec(BeanFactory bf, Connector c) {
	if (c.getType() == IdentityStoreType.ACTIVE_DIRECTORY 
		|| c.getType() == IdentityStoreType.ACTIVE_DIRECTORY_LDIF) {
	  	return (ObjectTypeSpec)bf.getBean(Constants.getAdOcTypeSpecBeanName());
	}
	return null;
  }
  
  private ChangePoller getDao(BeanFactory bf, Account account, Connector c, Map<String, Object> options) {
    log.trace("getDao");
    if (options == null) {
      throw new IllegalArgumentException("Option " + IdentityDAOFactory.changeRequest + 
          " required for  connector");
    }
    ChangeRequest changeRequest = (ChangeRequest)options.get(IdentityDAOFactory.changeRequest);
    if (changeRequest == null) {
      throw new IllegalArgumentException("Option " + IdentityDAOFactory.changeRequest + 
      " required for  connector");      
    }

    EntitiesDao serviceEntitiesDao = (EntitiesDao)bf.getBean(Constants.getServerDBDaoBeanName());
    ObjectTypeSpec ocSpec = getObjectTypeSpec(bf, c);
    SChangePoller changePoller = new SChangePoller();
    changePoller.setServiceEntitiesDao(serviceEntitiesDao);
    changePoller.setAccount(account);
    changePoller.setRequest(changeRequest);
    changePoller.setConnector(c);
    changePoller.setOcSpec(ocSpec);
    return changePoller;
  }
  
  public ChangePoller getChangePoller(BeanFactory bf, Account account, Connector c,
	  Map<String, Object> options) throws Exception {
	return getDao(bf, account, c, options);
  }

  public EventProcessor getEventProcessor(BeanFactory bf, Account account, Connector c,
	  Map<String, Object> options) throws Exception {
	if (deploymentType == DeploymentType.SERVICE) {
	  ChangePoller idDao = getChangePoller(bf, account, c, options);
	  ObligationMap om = (ObligationMap) bf.getBean(Constants
		  .getServiceOPMapBeanName());
	  EventGenerator eventGenerator = getEventGenerator(bf, c);
	  ChangeEventPDPFactory pdpFactory = (ChangeEventPDPFactory) bf
		  .getBean(Constants.getAdServicePDPFactoryBeanName());
	  SEventProcessor eventProcessor = new SEventProcessor();

	  eventProcessor.setPdpFactory(pdpFactory);
	  eventProcessor.setObligationMap(om);
	  eventProcessor.setIdDao(idDao);
	  eventProcessor.setEventGenerator(eventGenerator);
	  eventProcessor.init();

	  return eventProcessor;
	} else
	  return null;
  }

  private EventGenerator getEventGenerator(BeanFactory bf, Connector c) {
	return (EventGenerator)bf.getBean(Constants.getServviceEventGeneratorBeanName());
  }

}
