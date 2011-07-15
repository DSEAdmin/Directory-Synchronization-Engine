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

import javax.mail.MethodNotSupportedException;

import server.id.IdObject;


public interface ChangePoller {
  public List<IdObject> getNextChangeSet() throws Exception;
  public List<IdObject> getNextUserChangeSet() throws Exception;
  public List<IdObject> getNextGroupChangeSet() throws Exception;
  public List<IdObject> getNextContainerChangeSet() throws Exception;
  public IdObject getCompleteObject(String objectId) throws Exception;
  public Iterable<? extends Object> getGroupMembership(String objectId) throws Exception;
  public void saveStateToDB(); //TODO what exceptions to throw
  public void saveOperationResult(OperationType opType, OperationResult opResult, String resultString, 
      long numChangesFromIds, long numChangesToSvc) throws Exception;
  public State getUserState();
  public State getGroupState();
  public State getContainerState();
  public void resetStateToInitial();
  public void rollbackState();
  public Connector getConnector();
  public Connector refreshConnector();
  public Account getAccount();

  /**
   * Make sure all the elements in the dv and rv entries still exist in the directory and make sure
   * that the DN in the configuration is correct. This method could update teh DN in the configuration.
   * @return true if the method completed successfully. If the configuration was invalid, an 
   * exception would be thrown
   * @throws MethodNotSupportedException
   * @throws InvalidConfigurationException
   */
  public boolean validateConfiguration() throws MethodNotSupportedException,
  InvalidConfigurationException, IllegalArgumentException;
}
