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

import server.id.AVP;
import server.id.sync.ChangeEvent;
import server.id.sync.ChangeEvent.ChangeType;
import server.id.sync.messages.v1.ChangeRequest;


public interface SynchronizationService {
  public ChangeRequest createChangeRequest(Connector connector, boolean delta);
  public boolean addChangeToChangeRequest(ChangeEvent change);
  public boolean addChangeToChangeRequest(ChangeType changeType, Iterable<? extends AVP> avps);
  public ChangeResult sendChangeRequest(boolean more) throws Exception;
  
  public ConnectorStatusResult pushStatus(Connector connector) throws Exception;
  public ConnectorConfigurationResult pushConnectorConfiguration(Connector connector) throws Exception;
  public List<? extends Role> getLocalRoles() throws Exception;
  public List<? extends Group> getLocalContainers() throws Exception;
  
  public FullSyncMetaDataResult pushFullSyncMetaData(Connector connector, List<byte[]> uuids) throws Exception;
  
  String getServiceType();
}
