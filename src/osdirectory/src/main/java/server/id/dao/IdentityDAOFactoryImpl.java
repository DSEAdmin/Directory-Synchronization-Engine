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
package server.id.dao;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;

import server.id.AttributeVirtualization;
import server.id.Constants;
import server.id.IdObjectFactory;
import server.id.dao.LocalIdentityStore;
import server.id.ldap.DirectorySpec;
import server.id.ldap.LdapContextSource;
import server.id.ldap.LdapTemplate;
import server.id.ldap.ad.ADChangePoller;
import server.id.ldap.ldif.LdifChangePoller;
import server.id.sync.ChangeEventPDPFactory;
import server.id.sync.EventGenerator;
import server.id.sync.EventProcessor;
import server.id.sync.agent.AgentEventProcessor;
import server.id.sync.op.ObligationMap;


public class IdentityDAOFactoryImpl implements IdentityDAOFactory {

  private final Log log = LogFactory.getLog(getClass());
  private IdentityDAOFactory.DeploymentType deploymentType;
  
  public IdentityDAOFactoryImpl() {
  }
  
  public void setDeploymentType(IdentityDAOFactory.DeploymentType deploymentType) {
    this.deploymentType = deploymentType;
  }
  
  private ChangePoller getAdDAO(BeanFactory bf, Connector c) throws Exception {
    log.trace("getAdDAO");
    AttributeVirtualization av = 
      (AttributeVirtualization) bf.getBean(Constants.getAdVaBeanName());
    DirectorySpec ds = 
      (DirectorySpec) bf.getBean(Constants.getAdDirSpecBeanName());
    LocalIdentityStore lIdStore = 
      (LocalIdentityStore)bf.getBean(Constants.getLocalIdStoreBeanName());
    IdObjectFactory idObjFactory = 
      (IdObjectFactory)bf.getBean(Constants.getAdObjectFactoryBeanName());
    LdapContextSource ctx = new LdapContextSource(c, av);
    LdapTemplate template = new LdapTemplate(ctx);
    ADChangePoller adDao = new ADChangePoller(template);
    adDao.setDirSpec(ds);
    adDao.setAv(av);
    adDao.setConnector(c);
    adDao.setLIdStore(lIdStore);
    adDao.setIdObjectFactory(idObjFactory);
    adDao.init();
    return adDao;
  }

  private EventGenerator getEventGenerator(BeanFactory bf, Connector c) throws Exception {
    if (c.getType() == IdentityStoreType.ACTIVE_DIRECTORY || c.getType() == IdentityStoreType.ACTIVE_DIRECTORY_LDIF) {
      return (EventGenerator)bf.getBean(Constants.getAdEventGeneratorBean());
    }
    return null;    
  }
  
  public EventProcessor getEventProcessor(BeanFactory bf, Account account, Connector c, Map<String, Object> options) throws Exception {
    log.trace("getEventProcessor");
    LocalIdentityStore lIdStore = 
      (LocalIdentityStore)bf.getBean(Constants.getLocalIdStoreBeanName());
    ChangePoller idDao = getChangePoller(bf, account, c, options);
    ObligationMap om = (ObligationMap)bf.getBean(Constants.getAgentOPMapBeanName());
    EventGenerator eventGenerator = getEventGenerator(bf, c);
    ChangeEventPDPFactory pdpFactory = 
      (ChangeEventPDPFactory)bf.getBean(Constants.getAdAgentPDPFactoryBeanName());
    if (deploymentType == IdentityDAOFactory.DeploymentType.AGENT) {
      AgentEventProcessor eventProcessor = new AgentEventProcessor();
      eventProcessor.setLocalStore(lIdStore);
      eventProcessor.setPdpFactory(pdpFactory);
      eventProcessor.setIdDao(idDao);
      eventProcessor.setObligationMap(om);
      eventProcessor.setEventGenerator(eventGenerator);
      eventProcessor.init();
      return eventProcessor;
    } else
      return null;
  }

  public ChangePoller getChangePoller(BeanFactory bf, Account account, Connector c, Map<String, Object> options) throws Exception {
    log.trace("getIdentityDAO");
    if (c.getType() == IdentityStoreType.ACTIVE_DIRECTORY) {
      return getAdDAO(bf, c);
    } else if (c.getType() == IdentityStoreType.ACTIVE_DIRECTORY_LDIF) {
      return getAdLdifDAO(bf, c, options);
    }
    return null;
  }

  private ChangePoller getAdLdifDAO(BeanFactory bf, Connector c, Map<String, Object> options) throws Exception {
    log.trace("getAdLdifDAO");
    if (options == null) {
      throw new IllegalArgumentException("Option " + IdentityDAOFactory.ldifFileName + 
          " required for LDIF connector");
    }
    String ldifFileName = (String)options.get(IdentityDAOFactory.ldifFileName);
    if (ldifFileName == null) {
      throw new IllegalArgumentException("Option " + IdentityDAOFactory.ldifFileName + 
      " required for LDIF connector");      
    }
    AttributeVirtualization av = 
      (AttributeVirtualization) bf.getBean(Constants.getAdVaBeanName());
    DirectorySpec ds = 
      (DirectorySpec) bf.getBean(Constants.getAdDirSpecBeanName());
    LocalIdentityStore lIdStore = 
      (LocalIdentityStore)bf.getBean(Constants.getLocalIdStoreBeanName());
    IdObjectFactory idObjFactory = 
      (IdObjectFactory)bf.getBean(Constants.getAdObjectFactoryBeanName());
    LdifChangePoller ldifDao = new LdifChangePoller();
    ldifDao.setDirSpec(ds);
    ldifDao.setAv(av);
    ldifDao.setConnector(c);
    ldifDao.setLIdStore(lIdStore);
    ldifDao.setIdObjectFactory(idObjFactory);
    ldifDao.setLdifFile(ldifFileName);
    ldifDao.init();
    return ldifDao;
  }

}
