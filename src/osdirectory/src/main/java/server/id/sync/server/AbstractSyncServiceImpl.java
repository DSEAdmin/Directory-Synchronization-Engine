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

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import server.id.AVP;
import server.id.sync.ChangeEvent;
import server.id.sync.ChangeEvent.ChangeType;
import server.id.sync.agent.ClientChangeEventFactory;
import server.id.sync.messages.v1.ChangeDef;
import server.id.sync.messages.v1.ChangeRequest;
import server.id.sync.messages.v1.ConnectorConfiguration;
import server.id.sync.messages.v1.ConnectorInformation;
import server.id.sync.messages.v1.ConnectorStatus;
import server.id.sync.messages.v1.FullSyncMetaDataRequest;
import server.id.sync.messages.v1.IdentityStoreType;
import server.id.sync.messages.v1.OperationMode;
import server.id.sync.messages.v1.UserList;


public abstract class AbstractSyncServiceImpl implements SynchronizationService {
  public String serviceType = getClass().getSimpleName();
  protected final static QName _ConnectorConfigurationResponse_QNAMEv1 = new QName("http://messages.sync.id.server/v1.0", "connectorConfigurationResponse");
  protected final static QName _ChangeResponse_QNAMEv1 = new QName("http://messages.sync.id.server/v1.0", "changeResponse");
  protected final static QName _ConnectorConfigurationRequest_QNAMEv1 = new QName("http://messages.sync.id.server/v1.0", "connectorConfigurationRequest");
  protected final static QName _ChangeRequest_QNAMEv1 = new QName("http://messages.sync.id.server/v1.0", "changeRequest");
  protected final static QName _ConnectorStatusRequest_QNAMEv1 = new QName("http://messages.sync.id.server/v1.0", "connectorStatusRequest");
  protected final static QName _ConnectorStatusResponse_QNAMEv1 = new QName("http://messages.sync.id.server/v1.0", "connectorStatusResponse");
  protected final static QName _FullSyncMetaDataRequest_QNAMEv1 = new QName("http://messages.sync.id.server/v1.0", "fullSyncMetaDataRequest");
  protected final static QName _FullSyncMetaDataResponse_QNAMEv1 = new QName("http://messages.sync.id.server/v1.0", "fullSyncMetaDataResponse");

  protected long fragmentNumber;
  protected ChangeRequest changeRequest;
  protected ClientChangeEventFactory changeFactory;
  
  protected AbstractSyncServiceImpl() {    
  }
  
  protected AbstractSyncServiceImpl(ClientChangeEventFactory changeFactory) {
    this.changeFactory = changeFactory;
  }
  
  protected long getVersion() { 
    return 1;
  }
  
  public void setChangeFactory(ClientChangeEventFactory changeFactory) {
    this.changeFactory = changeFactory;
  }

  public String getServiceType() {
    return serviceType;
  }
  
  synchronized public boolean addChangeToChangeRequest(ChangeEvent change) {
    ChangeDef cd = changeFactory.getChangeDefV1FromChangeEvent(change);
    changeRequest.getChangeDefs().add(cd);
    return true;
  }
  
  synchronized public boolean addChangeToChangeRequest(ChangeType changeType, Iterable<? extends AVP> avps) {
    ChangeDef cd = changeFactory.getChangeDefV1FromChangeEvent(changeType, avps);
    changeRequest.getChangeDefs().add(cd);
    return true;    
  }

  synchronized public ChangeRequest createChangeRequest(Connector connector, boolean delta) {
    fragmentNumber += 1;
    changeRequest = changeFactory.createChangeRequestV1(connector, getVersion(), fragmentNumber, delta);
    return changeRequest;
  }
  
  synchronized ConnectorInformation getConnectorInformation(Connector connector) {
    if (connector == null) {
      return null;
    } 

    ConnectorInformation ci = new ConnectorInformation();
    ci.setConnectorMajorVersion(connector.getMajorVersion());
    ci.setConnectorMinorVersion(connector.getMinorVersion());
    ci.setConnectorUuid(connector.getUuid());
    ConnectorStatus status = new ConnectorStatus();
    status.setLastSuccessfulUpdate(0); //TODO
    status.setLastConnToResource(0); //TODO
    ci.setStatus(status);
    return ci;
  }
  
  synchronized ConnectorConfiguration getConnectorConfiguration(Connector connector) {
    if (connector == null)
      return null;
    
    ConnectorConfiguration cc = new ConnectorConfiguration();
    cc.setMajorVersion(connector.getMajorVersion());
    cc.setMinorVersion(connector.getMinorVersion());
    cc.setMode(OperationMode.valueOf(connector.getMode().name()));
    cc.setName(connector.getName());
    cc.setAdmin(connector.getAdminName());
    cc.setType(IdentityStoreType.valueOf(connector.getType().name()));
    cc.setConnectorUuid(connector.getUuid());
    cc.setAutoCreateContainers(connector.getAutoCreateContainers());
    cc.setSyncSchedule(connector.getSyncSchedule());
    cc.setTimeZoneID(TimeZone.getDefault().getID());
    
    SortedSet<? extends DitVirtualizationEntry> dvEntries = connector.getDvEntries();
    for(DitVirtualizationEntry dvEntry : dvEntries) {
      server.id.sync.messages.v1.DitVirtualizationEntry entry = 
        new server.id.sync.messages.v1.DitVirtualizationEntry();
      entry.setUuid(dvEntry.getUuid());
      entry.setLocalContainer(dvEntry.getLocalContainer().getIdentifier());
      entry.setLocalContainerUuid(dvEntry.getLocalContainer().getUuid());
      entry.setRemoteContainer(dvEntry.getRemoteContainer().getIdentifier());
      entry.setRemoteContainerUuid(dvEntry.getRemoteContainer().getUuid());
      entry.setOrdinalId(dvEntry.getOrdinalId());
      cc.getDvEntries().add(entry);
    }
    
    Set<? extends RoleVirtualizationEntry> rvEntries = connector.getRvEntries();
    for (RoleVirtualizationEntry rvEntry : rvEntries) {
      server.id.sync.messages.v1.RoleVirtualizationEntry entry = 
        new server.id.sync.messages.v1.RoleVirtualizationEntry();
      entry.setUuid(rvEntry.getUuid());
      entry.setLocalRole(rvEntry.getLocalRole().getIdentifier());
      entry.setLocalRoleUuid(rvEntry.getLocalRole().getUuid());
      entry.setRemoteRole(rvEntry.getRemoteRole().getIdentifier());
      entry.setRemoteRoleUuid(rvEntry.getRemoteRole().getUuid());
      cc.getRvEntries().add(entry);
    }
    
    return cc;
  }  
  
  protected FullSyncMetaDataRequest getFullSyncMetaDataRequest(Connector connector, List<byte[]> uuids) {
    FullSyncMetaDataRequest request = new FullSyncMetaDataRequest();
    request.setVersion(1);
    request.setConnectorUuid(connector.getUuid());
    request.setConnectorMajorVersion(connector.getMajorVersion());
    request.setConnectorMinorVersion(connector.getMinorVersion());
    UserList ul = new UserList();
    for (byte[] uuid : uuids) {
      ul.getUuid().add(uuid);
    }
    request.setUserList(ul);
    return request;
  }
}
