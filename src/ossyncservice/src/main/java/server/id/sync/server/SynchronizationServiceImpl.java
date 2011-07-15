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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;



import server.id.Util;
import server.id..IdentityFactory;
import server.id.sync.EventProcessor;
import server.id.sync.ObligationProcessor;
import server.id.sync.messages.v1.ChangeRequest;
import server.id.sync.messages.v1.ChangeResponse;
import server.id.sync.messages.v1.ConnectorConfigurationRequest;
import server.id.sync.messages.v1.ConnectorConfigurationResponse;
import server.id.sync.messages.v1.ConnectorStatusRequest;
import server.id.sync.messages.v1.ConnectorStatusResponse;
import server.id.sync.messages.v1.Container;
import server.id.sync.messages.v1.ContainerRequest;
import server.id.sync.messages.v1.ContainerResponse;
import server.id.sync.messages.v1.FullSyncMetaDataRequest;
import server.id.sync.messages.v1.FullSyncMetaDataResponse;
import server.id.sync.messages.v1.RoleRequest;
import server.id.sync.messages.v1.RoleResponse;


@javax.jws.WebService(name = "SynchronizationService", serviceName = "SynchronizationService",
    portName = "SynchronizationServiceV1.0",
    targetNamespace = "http://operations.sync.id.server/v1.0", 
    wsdlLocation = "file:src/main/wsdl/SynchronizationServiceV1.wsdl" ,
    endpointInterface = "server.id.sync.operations.v1_0.SynchronizationService")
public class SynchronizationServiceImpl implements  server.id.sync.operations.v1_0.SynchronizationService, ApplicationContextAware {
  private final Log log = LogFactory.getLog(getClass());
  private ApplicationContext applicationContext;
  private Entities serviceEntities;
  private IdentityFactory Factory;
  private  ;
  
  private static class OPCallback extends ObligationProcessor.Callback {
	public OPCallback() {
	}
  }

  public void setApplicationContext(ApplicationContext applicationContext)
  	throws BeansException {
	this.applicationContext = applicationContext;
  }
  
  public void setServiceEntities(Entities serviceEntities) {
	this.serviceEntities = serviceEntities;
  }

  public void setFactory(IdentityFactory Factory) {
	this.Factory = Factory;
  }
  
  public void setDao(Dao dao) {
    this.dao = dao;
  }

  /* (non-Javadoc)
   * @see server.id.sync.operations.v1.SynchronizationService#requestChange(server.id.sync.messages.v1.ChangeRequest  changeRequest )*
   */
  public server.id.sync.messages.v1.ChangeResponse requestChange(server.id.sync.messages.v1.ChangeRequest changeRequest) { 
      log.trace("Executing operation requestChange");
      ChangeResult result = null;
      try {
    	if (changeRequest == null) {
    	  server.id.sync.messages.v1.ChangeResponse response = new ChangeResponse();
    	  response.setResult(server.id.sync.messages.v1.Result.PROTOCOL_ERROR);
    	  log.warn("requestChange: changeRequest was null");
    	  response.setDescription("changeRequest was null");
    	  return response;
    	} else {
    	  result = new ChangeResult(changeRequest, Result.SUCCESS, "");
    	}
    	
    	OPCallback cb = new OPCallback();
    	Map<String, Object> options = new HashMap<String, Object>();
    	options.put(IdentityFactory.changeRequest, changeRequest);
    	String accountName = "testAccount";//TODO account name from cert
    	Account account = serviceEntities.getAccountByName(accountName); 
    	if (account == null) {
    	  log.warn("requestChange: could not find account with name " + accountName);
    	  result.setResult(Result.NO_SUCH_ACCOUNT);
    	  result.setDescription("No such account : " + accountName);
    	  return result.getChangeResponse();
    	}
    	Connector c = serviceEntities.getConnectorByUuid(account, changeRequest.getConnectorUuid());
    	if (c == null) {
    	  log.warn("requestChange: could not find connector with uuid " + 
    		  Util.byteArrayToHexString(changeRequest.getConnectorUuid()));
    	  result.setResult(Result.NO_SUCH_CONNECTOR);
    	  result.setDescription("No such connector : " + Util.byteArrayToHexString(changeRequest.getConnectorUuid()));
    	  return result.getChangeResponse();    	  
    	}
    	
    	if (validateRequest(c, changeRequest, result) == false) {
    	  log.warn("requestChange: validation of request failed");
    	  return result.getChangeResponse();
    	}
    	
    	EventProcessor ep = Factory.getEventProcessor(applicationContext, account, c, options);
    	if (ep.processAllEvents(cb) == true) {
    	  log.debug("requestChange: successfully processed change");
    	  return result.getChangeResponse();
    	} else {
    	  log.warn("requestChange: failed to process change");
    	  result.setResult(Result.OTHER_FAILURE);
    	  result.setDescription("Internal Error");
    	  return result.getChangeResponse();    	  
    	}
      } catch (Exception ex) {
    	log.warn("Exception in requestChange:", ex);
    	result.setResult(Result.OTHER_FAILURE);
    	result.setDescription("Internal Error: " + ex.getMessage());
    	return result.getChangeResponse();    	  
      }
  }

  private boolean validateRequest(Connector c, ChangeRequest changeRequest, ChangeResult result) {
	if (c.getMajorVersion() != changeRequest.getConnectorMajorVersion()) {
	  log.warn("validateRequest: incompatible connector major version numbers");
	  result.setResult(Result.PROTOCOL_ERROR);
	  result.setDescription("Incompatible connector major version numbers");
	  return false;
	}
	
	if (c.getMinorVersion() > changeRequest.getConnectorMinorVersion()) {
	  log.warn("validateRequest: incompatible connector minor version numbers");
	  result.setResult(Result.PROTOCOL_ERROR);
	  result.setDescription("Incompatible connector minor version numbers");
	  return false;	  
	}
	
	return true;
  }

  public ConnectorConfigurationResponse pushConfiguration(
	  ConnectorConfigurationRequest connectorConfigurationRequest) {
	log.trace("Executing operation pushConfiguration");
	ConnectorConfigurationResult result = null;
	try {
	  if (connectorConfigurationRequest == null) {
		ConnectorConfigurationResponse response = new ConnectorConfigurationResponse();
		response.setVersion(1);
		response.setResult(server.id.sync.messages.v1.Result.PROTOCOL_ERROR);
		response.setErrorString("Null connectorConfigurationRequest received");
		log.warn("pushConfiguration: null request received");
		return response;
	  } else {
		result = new ConnectorConfigurationResult(Result.SUCCESS, "");
	  }

	  String accountName = "testAccount";//TODO account name from cert
	  Account account = serviceEntities.getAccountByName(accountName); 
	  if (account == null) {
		log.warn("pushStatus: could not find account with name " + accountName);
		result.setResult(Result.NO_SUCH_ACCOUNT);
		result.setErrorString("No such account : " + accountName);
		return result.getConnectorConfigurationResponse();
	  }

	  result = dao.updateConnector(account, connectorConfigurationRequest);
	  
	  return result.getConnectorConfigurationResponse();
	} catch (Exception ex) {
	  log.warn("Exception in pushConnectorConfiguration:", ex);
	  result.setResult(Result.OTHER_FAILURE);
	  result.setErrorString("Internal Error: " + ex.getMessage());
	  return result.getConnectorConfigurationResponse();	  
	}
  }

  public ConnectorStatusResponse pushStatus(
	  ConnectorStatusRequest connectorStatusRequest) {
	log.trace("Executing operation pushStatus");
	ConnectorStatusResult result = null;
	try {
	  if (connectorStatusRequest == null) {
		server.id.sync.messages.v1.ConnectorStatusResponse response = new ConnectorStatusResponse();
		response.setVersion(1);
		response.setResult(server.id.sync.messages.v1.Result.PROTOCOL_ERROR);
		response.setErrorString("Null request received");
		log.warn("pushStatus: connectorStatusRequest was null");
		return response;
	  } else {
		result = new ConnectorStatusResult(Result.SUCCESS, "");
	  }

	  String accountName = "testAccount";//TODO account name from cert
	  Account account = serviceEntities.getAccountByName(accountName); 
	  if (account == null) {
		log.warn("pushStatus: could not find account with name " + accountName);
		result.setResult(Result.NO_SUCH_ACCOUNT);
		result.setErrorString("No such account : " + accountName);
		return result.getConnectorStatusResponse();
	  }
	  byte[] connectorUuid = connectorStatusRequest.getConnectorInformation().getConnectorUuid(); 
	  Connector c = serviceEntities.getConnectorByUuid(account, connectorUuid);
	  if (c == null) {
		log.warn("pushStatus: could not find connector with uuid " + 
			Util.byteArrayToHexString(connectorUuid));
		result.setResult(Result.NO_SUCH_CONNECTOR);
		result.setErrorString("No such connector : " + Util.byteArrayToHexString(connectorUuid));
		return result.getConnectorStatusResponse();    	  
	  }

	  result.setConnectorUuid(c.getUuid());
	  result.setMajorVersionNumber(c.getMajorVersion());
	  result.setMinorVersionNumber(c.getMinorVersion());
	  return result.getConnectorStatusResponse();	
	} catch (Exception ex) {
	  log.warn("Exception in pushStatus:", ex);
	  result.setResult(Result.OTHER_FAILURE);
	  result.setErrorString("Internal Error: " + ex.getMessage());
	  return result.getConnectorStatusResponse();
	}	
  }

  public ContainerResponse getLocalContainers(ContainerRequest containerRequest) {
	log.trace("getLocalContainers");
	ContainerResponse response = new ContainerResponse();
	try {
	  String accountName = "testAccount";//TODO account name from cert
	  Account account = serviceEntities.getAccountByName(accountName); 
	  if (account == null) {
		log.warn("pushStatus: could not find account with name " + accountName);
		response.setResult(server.id.sync.messages.v1.Result.NO_SUCH_ACCOUNT);
		response.setErrorString("No such account : " + accountName);
		return response;
	  }
	  List<UserGroup> ugList = serviceEntities.getUserGroups(account);
	  if (ugList != null) {
		for (UserGroup ug : ugList) {
		  Container container = new Container();
		  container.setName(ug.getName());
		  UserGroup parent = serviceEntities.getParentGroup(ug);
		  if (parent != null) {
			container.setParent(parent.getUuid());
		  } else {
			container.setParent(null);
		  }
		  container.setUuid(ug.getUuid());
		  response.getContainer().add(container);
		}
	  }
	  response.setResult(server.id.sync.messages.v1.Result.SUCCESS);
	  return response;
	} catch (Exception ex) {
	  log.warn("getLocalContainers: ", ex);
	  response.setResult(server.id.sync.messages.v1.Result.OTHER_FAILURE);
	  response.setErrorString(ex.getMessage());
	  return response;
	}
  }

  public RoleResponse getLocalRoles(RoleRequest roleRequest) {
	log.trace("getLocalRoles");
	RoleResponse response = new RoleResponse();
	try {
	  String accountName = "testAccount";//TODO account name from cert
	  Account account = serviceEntities.getAccountByName(accountName); 
	  if (account == null) {
		log.warn("pushStatus: could not find account with name " + accountName);
		response.setResult(server.id.sync.messages.v1.Result.NO_SUCH_ACCOUNT);
		response.setErrorString("No such account : " + accountName);
		return response;
	  }
	  List<Role> roleList = serviceEntities.getRoles(account);
	  for (Role role : roleList) {
		server.id.sync.messages.v1.Role r = new server.id.sync.messages.v1.Role();
		r.setName(role.getName());
		r.setUuid(role.getUuid());
		response.getRole().add(r);
	  }
	  response.setResult(server.id.sync.messages.v1.Result.SUCCESS);
	  return response;
	} catch (Exception ex) {
	  log.warn("getLocalRoles", ex);
	  response.setResult(server.id.sync.messages.v1.Result.OTHER_FAILURE);
	  response.setErrorString(ex.getMessage());
	  return response;	  
	}
  }

  public FullSyncMetaDataResponse pushFullSyncMetaData(
	  FullSyncMetaDataRequest fullSyncMetaDataRequest) {
	FullSyncMetaDataResponse response = new FullSyncMetaDataResponse();
	try {
	  String accountName = "testAccount";//TODO account name from cert
	  Account account = serviceEntities.getAccountByName(accountName); 
	  if (account == null) {
		log.warn("pushFullSyncMetaData: could not find account with name " + accountName);
		response.setResult(server.id.sync.messages.v1.Result.NO_SUCH_ACCOUNT);
		response.setErrorString("No such account : " + accountName);
		return response;
	  }
	  return dao.applyFullSyncMetaData(account, fullSyncMetaDataRequest);
	} catch (Exception ex) {
	  log.warn("pushFullSyncMetaData", ex);
	  response.setResult(server.id.sync.messages.v1.Result.OTHER_FAILURE);
	  response.setErrorString(ex.getMessage());
	  return response;	  	  
	}
  }
}
