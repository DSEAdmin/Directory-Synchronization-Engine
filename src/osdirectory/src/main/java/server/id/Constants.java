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
package server.id;

public class Constants {
  private static final String ADVA_BEAN = "attrVirtualizationAD";
  private static final String SUNVA_BEAN = "attrVirtualizationSUN";
  private static final String IDDAO_FACTORY_BEAN = "identityDAOFactory";
  private static final String KRBMGR_BEAN = "kerberosConfigManager";
  private static final String AD_FILTER_SPEC = "adFilterSpec";
  private static final String LOCAL_ID_STORE = "localIdStore";
  private static final String AD_OC_TYPE_SPEC = "adObjectTypeSpec";
  private static final String AD_OBJECT_FACTORY = "adObjectFactory";
  private static final String AD_AGENT_PDP_FACTORY = "adAgentPDPFactory";
  private static final String LDAP_VIRTUALIZATION = "ldapVirtualization";
  private static final String AD_SYS_ENTRIES_SPEC = "adSystemEntriesSpec";
  private static final String AD_DIR_SPEC = "adDirectorySpec";
  private static final String AGENT_OP_MAP = "agentObligationMap";
  private static final String AD_EVENT_GENERATOR = "adEventGenerator";
  private static final String CLIENT_CHANGE_EVENT_FACTORY = "clientChangeEventFactory";
  private static final String SERVER_CHANGE_EVENT_FACTORY_AD = "serverChangeEventFactoryForAD";
  private static final String SERVER_DB_DAO = "serviceEntitiesDAO";
  private static final String SERVICE_IDDAO_FACTORY_BEAN = "serviceIdentityDAOFactory";
  private static final String AD_SERVICE_PDP_FACTORY_BEAN = "adServicePDPFactory";
  private static final String SERVICE_EVENT_GENERATOR = "serviceEventGenerator";
  private static final String SERVICE_OP_MAP = "serviceObligationMap";
  private static final String SOAP_SYNC_SERVICE_CLIENT = "soapSyncServiceClient";
  
  static public String getAdVaBeanName() {
    return System.getProperty("server.id.adVaBean", ADVA_BEAN);
  }
  
  static public String getSunVaBeanName() {
    return System.getProperty("server.id.sunVaBean", SUNVA_BEAN);
  }
  
  static public String getIdDaoFactoryBeanName() {
    return System.getProperty("server.id.idDAOFactoryBean", IDDAO_FACTORY_BEAN);
  }
  
  static public String getKrbManagerBeanName() {
    return System.getProperty("server.id.krbManagerBean", KRBMGR_BEAN);
  }
  
  static public String getAdFilterSpecBeanName() {  
    return System.getProperty("server.id.adFilterSpecBean", AD_FILTER_SPEC);
  }
    
  static public String getLocalIdStoreBeanName() {
    return System.getProperty("server.id.localIdStoreBean", LOCAL_ID_STORE);
  }
  
  static public String getAdOcTypeSpecBeanName() {
    return System.getProperty("server.id.adOcTypeSpecBean", AD_OC_TYPE_SPEC);    
  }
  
  static public String getAdObjectFactoryBeanName() {
    return System.getProperty("server.id.adObjectFactoryBean", AD_OBJECT_FACTORY);        
  }
  
  static public String getAdAgentPDPFactoryBeanName() {
    return System.getProperty("server.id.adAgentPDPFactoryBean", AD_AGENT_PDP_FACTORY);            
  }
  
  static public String getLdapVirtualizationBeanName() {
    return System.getProperty("server.id.ldapVirtualizationBean", LDAP_VIRTUALIZATION);
  }
  
  static public String getAdSysEntriesSpecBeanName() {
    return System.getProperty("server.id.adSysEntriesBean", AD_SYS_ENTRIES_SPEC);    
  }
  
  static public String getAdDirSpecBeanName() {
    return System.getProperty("server.id.adDirSpecBean", AD_DIR_SPEC); 
  }
  
  static public String getAgentOPMapBeanName() {
    return System.getProperty("server.id.agentOPMap", AGENT_OP_MAP);
  }
  
  static public String getAdEventGeneratorBean() {
    return System.getProperty("server.id.adEventGenerator", AD_EVENT_GENERATOR);
  }
  
  static public String getClientChangeEventFactoryBeanName() {
    return System.getProperty("server.id.clientChangeEventFactory", CLIENT_CHANGE_EVENT_FACTORY);
  }
  
  static public String getServerChangeEventFactoryForADBeanName() {
    return System.getProperty("server.id.serverChangeEventFactoryForAD", SERVER_CHANGE_EVENT_FACTORY_AD);    
  }
  
  static public String getServerDBDaoBeanName() {
    return System.getProperty("server.id.serverDBDao", SERVER_DB_DAO);        
  }
  
  static public String getServiceIdDaoFactoryBeanName() {
    return System.getProperty("server.id.serviceIdDAOFactoryBean", SERVICE_IDDAO_FACTORY_BEAN);            
  }

  public static String getAdServicePDPFactoryBeanName() {
    return System.getProperty("server.id.adServicePDPFactoryBean", AD_SERVICE_PDP_FACTORY_BEAN);            
  }
  
  public static String getServviceEventGeneratorBeanName() {
    return System.getProperty("server.id.serviceEventGeneratorBean", SERVICE_EVENT_GENERATOR);                
  }
  
  public static String getServiceOPMapBeanName() {
    return System.getProperty("server.id.serviceOPMapBean", SERVICE_OP_MAP);                    
  }
  
  public static String getSoapSyncServiceClientBeanName() {
    return System.getProperty("server.id.soapSyncServiceClientBean", SOAP_SYNC_SERVICE_CLIENT);                        
  }
}
