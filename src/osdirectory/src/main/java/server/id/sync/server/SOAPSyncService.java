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
package server.id.sync.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.sync.messages.v1.ChangeResponse;
import server.id.sync.messages.v1.ConnectorConfiguration;
import server.id.sync.messages.v1.ConnectorConfigurationRequest;
import server.id.sync.messages.v1.ConnectorConfigurationResponse;
import server.id.sync.messages.v1.ConnectorInformation;
import server.id.sync.messages.v1.ConnectorStatusRequest;
import server.id.sync.messages.v1.ConnectorStatusResponse;
import server.id.sync.messages.v1.Container;
import server.id.sync.messages.v1.ContainerRequest;
import server.id.sync.messages.v1.ContainerResponse;
import server.id.sync.messages.v1.FullSyncMetaDataRequest;
import server.id.sync.messages.v1.FullSyncMetaDataResponse;
import server.id.sync.messages.v1.RoleRequest;
import server.id.sync.messages.v1.RoleResponse;
import server.id.sync.operations.v1_0.SynchronizationService;



public class SOAPSyncService extends AbstractSyncServiceImpl {
  private final Log log = LogFactory.getLog(getClass());
  private SynchronizationService syncClient;
  
  public SOAPSyncService() {    
  }
  
  public void setSyncClient(SynchronizationService syncClient) {
    this.syncClient = syncClient;
  }

  public ChangeResult sendChangeRequest(boolean more) throws Exception {
    if (changeRequest == null)
      throw new IllegalArgumentException("No changeRequest available to send");
    changeRequest.setMoreFragments(more);
    
    try {
      ChangeResponse response = syncClient.requestChange(changeRequest);
      ChangeResult result = new ChangeResult(response);
      fragmentNumber = 0;
      changeRequest = null;
      return result;
    } catch (RuntimeException ex) {
      log.error("Exception: ", ex);
      ChangeResult result = new ChangeResult(changeRequest, Result.OTHER_FAILURE, ex.getMessage());
      return result;      
    } catch (Exception e) {
      log.error("Exception: ", e);
      ChangeResult result = new ChangeResult(changeRequest, Result.OTHER_FAILURE, e.getMessage());
      return result;
    }
  }

  public ConnectorStatusResult pushStatus(Connector connector) {
    try {
      ConnectorStatusRequest request = new ConnectorStatusRequest();
      request.setVersion(1);
      if (connector != null) {
        ConnectorInformation ci = getConnectorInformation(connector);
        request.setConnectorInformation(ci);
      }
      ConnectorStatusResponse response = syncClient.pushStatus(request);
      return new ConnectorStatusResult(response);
    } catch (RuntimeException rex) {
      log.error("Exception: ", rex);
      return new ConnectorStatusResult(Result.OTHER_FAILURE, rex.getMessage());      
    } catch (Exception ex) {
      log.error("Exception: ", ex);
      return new ConnectorStatusResult(Result.OTHER_FAILURE, ex.getMessage());      
    }
  }

  public ConnectorConfigurationResult pushConnectorConfiguration(Connector connector) throws Exception {
    if (connector == null) {
      throw new IllegalArgumentException("The list of connectors is null");      
    }
    try {
      ConnectorConfigurationRequest request = new ConnectorConfigurationRequest();
      request.setVersion(1);
      ConnectorConfiguration cc = getConnectorConfiguration(connector);
      request.setConnectorConfiguration(cc);
      ConnectorConfigurationResponse response = syncClient.pushConfiguration(request);
      return new ConnectorConfigurationResult(response);
    } catch (RuntimeException rex) {
      log.error("Exception: ", rex);
      return new ConnectorConfigurationResult(Result.OTHER_FAILURE, rex.getMessage());      
    } catch (Exception ex) {
      log.error("Exception: ", ex);
      return new ConnectorConfigurationResult(Result.OTHER_FAILURE, ex.getMessage());      
    }
  }

  public List<? extends Group> getLocalContainers() {
    try {
      List<server.id.sync.agent.domain.Group> groups = new ArrayList<server.id.sync.agent.domain.Group>();
      ContainerRequest request = new ContainerRequest();
      request.setVersion(1);
      ContainerResponse response = syncClient.getLocalContainers(request);
      if (response != null) {
        List<Container> containers = response.getContainer();
        if (containers != null) {
          for (Container c : containers) {
            server.id.sync.agent.domain.Group g = 
              new server.id.sync.agent.domain.Group(
                  c.getName(), c.getUuid(), GroupType.INTERNAL_CONTAINER);
            groups.add(g);
          }
        }
      }
      return groups;
    } catch (Exception ex) {
      log.error("Exception: ", ex);
      return null;
    }
  }

  public List<? extends Role> getLocalRoles() {
    try {
      List<server.id.sync.agent.domain.Role> roles = new ArrayList<server.id.sync.agent.domain.Role>();
      RoleRequest request = new RoleRequest();
      request.setVersion(1);
      RoleResponse response = syncClient.getLocalRoles(request);
      if (response != null) {
        List<server.id.sync.messages.v1.Role> localRoles = response.getRole();
        if (roles != null) {
          for (server.id.sync.messages.v1.Role lr : localRoles) {
            server.id.sync.agent.domain.Role r = new server.id.sync.agent.domain.Role(lr.getName(), lr.getUuid());
            roles.add(r);
          }
        }
      }
      return roles;
    } catch (Exception ex) {
      log.error("Exception: ", ex);
      return null;
    }
  }

  public FullSyncMetaDataResult pushFullSyncMetaData(Connector connector, List<byte[]> uuids) throws Exception {
    if (connector == null) {
      throw new IllegalArgumentException("The list of connectors is null");      
    }
    FullSyncMetaDataRequest request = getFullSyncMetaDataRequest(connector, uuids);
    try {
      FullSyncMetaDataResponse response = syncClient.pushFullSyncMetaData(request);
      if (response != null) {
        FullSyncMetaDataResult result = new FullSyncMetaDataResult(response);
        return result;
      } else {
        log.error("pushFullSyncMetaData: null response");
        return null;
      }
    } catch (Exception ex) {
      log.error("Exception: ", ex);
      return null;
    }
  }

}
