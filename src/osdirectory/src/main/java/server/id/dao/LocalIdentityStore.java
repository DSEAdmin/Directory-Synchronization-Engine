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

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import server.id.Entry;
import server.id.IdObject;
import server.id.sync.ChangeEvent.ChangeType;
import server.id.sync.agent.domain.Member;


@Transactional
public interface LocalIdentityStore {
  Object getDelegate();
  
  @Transactional(readOnly = true)
  public Connector refreshConnector(Connector connector) throws DataAccessException;
  
  @Transactional(readOnly = true)
  public List<? extends Connector> getConnectors(Account account) throws DataAccessException;
  
  public Connector getConnectorByName(Account account, String name) throws DataAccessException;
  
  public Connector saveConnector(Account account, Connector connector) throws DataAccessException;

  public Connector updateConnector(Account account, Connector connector) throws DataAccessException;
  
  public void deleteConnector(Account account, Connector connector) throws DataAccessException;
  
  public void resetStateToInitial(Account account, Connector connector) throws DataAccessException; 

  public Connector setSyncState(Account account, Connector connector, byte[] userCookie, byte[] groupCookie, 
      byte[] containerCookie) throws DataAccessException;
  
  public Connector setSyncStatus(Account account, Connector connector, SyncStatus syncStatus) throws DataAccessException;
  
  public Entry saveIdObject(Account account, Connector connector, DitVirtualizationEntry dvEntry, IdObject object) throws DataAccessException;

  public Entry updateIdObject(Account account, Connector connector, DitVirtualizationEntry dvEntry, IdObject object) throws DataAccessException;
  
  public Entry updateIdObjectDvEntry(Account account, Connector connector, byte[] uuid, DitVirtualizationEntry dvEntry) throws DataAccessException;
  
  public void deleteIdObject(Account account, Connector connector, IdObject object) throws DataAccessException;
  
  public void deleteChildren(Account account, Connector connector, IdObject object) throws DataAccessException;
  
  @Transactional(readOnly = true)
  public Entry findLocalObject(Account account, Connector connector, IdObject remoteObj) throws DataAccessException;
  
  @Transactional(readOnly = true)
  public Entry findLocalObject(Account account, Connector connector, byte[] uuid) throws DataAccessException;

  @Transactional(readOnly = true)
  public Entry findLocalObject(Account account, Connector connector, String normalizedDn) throws DataAccessException;
  
  @Transactional(readOnly = true)
  public List<? extends Entry> getChildren(Account account, Connector connector, IdObject object) throws DataAccessException;

  @Transactional(readOnly = true)
  public List<? extends Entry> getMemberOf(Account account, Connector connector, String normalizedDn) throws DataAccessException;
  
  @Transactional(readOnly = true)
  public List<Member> getMembers(Account account, Connector connector, IdObject group) throws DataAccessException;
  
  public Connector updateConnectorRvEntry(Account account, Connector connector, IdObject localObj, IdObject remoteObj, ChangeType changeType);

  public Connector updateConnectorDvEntry(Account account, Connector connector, IdObject localObj, IdObject remoteObj, ChangeType changeType);
  
  public List<byte[]> getSynchronizedUsersUuid(Account account, Connector connector) throws DataAccessException;
  
  public void setOperationResult(OperationType opType, OperationResult opResult, String resultString, 
      long numChangesFromIds, long numChangesToSvc, Connector connector) throws DataAccessException;
}
