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
package server.id.sync;

import server.id.IdObject;
import server.id.IdVirtualization;


public class ChangeEvent {
  public enum ChangeType {
    UNKNOWN,
    DELETE, 
    ADD, 
    MODIFY, 
    RENAME_MODIFY
  }
  
  protected Account account;
  protected Connector connector;
  protected IdVirtualization idVirtualization;
  protected ChangeType type; 
  protected IdObject remoteObj; //The change received from the AIS
  protected IdObject localObj; //The local copy if any
  protected DitVirtualizationEntry dvEntry; //If in scope will have a non-null value
  protected DitVirtualizationEntry prevDvEntry; //localObj's dvEntry. Needed for delete and rename
  protected RoleVirtualizationEntry rvEntry; //If this is a mapped group
  protected RoleVirtualizationEntry prevRvEntry; //What was this previously mapped to. Used in case of rename
  protected boolean effectsInScopeObjects; //Used for containers in delete and rename
  protected boolean prevEffectsInScopeObjects; //Used for containers in rename

  public ChangeEvent() {
    type = ChangeType.UNKNOWN;
  }
  
  public ChangeEvent(Connector connector, ChangeType type, IdObject remoteObj, IdObject localObj,
      DitVirtualizationEntry dvEntry, DitVirtualizationEntry prevDvEntry, RoleVirtualizationEntry rvEntry, 
      RoleVirtualizationEntry prevRvEntry, 
      boolean effectsInScopeObjects,
      boolean prevEffectsInScopeObjects) {
    this.connector = connector;
    this.type = type;
    this.remoteObj = remoteObj;
    this.localObj = localObj;
    this.dvEntry = dvEntry;
    this.prevDvEntry = prevDvEntry;
    this.rvEntry = rvEntry;
    this.prevRvEntry = prevRvEntry;
    this.effectsInScopeObjects = effectsInScopeObjects;
    this.prevEffectsInScopeObjects = prevEffectsInScopeObjects;
  }

  public ChangeType getType() {
    return type;
  }

  public IdObject getRemoteObj() {
    return remoteObj;
  }

  public IdObject getLocalObj() {
    return localObj;
  }

  public DitVirtualizationEntry getPrevDvEntry() {
    return prevDvEntry;
  }

  public boolean isEffectsInScopeObjects() {
    return effectsInScopeObjects;
  }

  public boolean isPrevEffectsInScopeObjects() {
    return prevEffectsInScopeObjects;
  }
  
  public DitVirtualizationEntry getDvEntry() {
    return dvEntry;
  }
  
  public ChangeType getChangeType() {
    return type;
  }
  
  public Connector getConnector() {
    return connector;
  }
  
  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public void setConnector(Connector connector) {
    this.connector = connector;
  }

  public void setType(ChangeType type) {
    this.type = type;
  }

  public void setRemoteObj(IdObject remoteObj) {
    this.remoteObj = remoteObj;
  }

  public void setLocalObj(IdObject localObj) {
    this.localObj = localObj;
  }

  public void setDvEntry(DitVirtualizationEntry dvEntry) {
    this.dvEntry = dvEntry;
  }

  public void setPrevDvEntry(DitVirtualizationEntry prevDvEntry) {
    this.prevDvEntry = prevDvEntry;
  }

  public void setEffectsInScopeObjects(boolean effectsInScopeObjects) {
    this.effectsInScopeObjects = effectsInScopeObjects;
  }

  public void setPrevEffectsInScopeObjects(boolean prevEffectsInScopeObjects) {
    this.prevEffectsInScopeObjects = prevEffectsInScopeObjects;
  }
  
  public RoleVirtualizationEntry getRvEntry() {
    return rvEntry;
  }

  public void setRvEntry(RoleVirtualizationEntry rvEntry) {
    this.rvEntry = rvEntry;
  }

  public RoleVirtualizationEntry getPrevRvEntry() {
    return prevRvEntry;
  }

  public void setPrevRvEntry(RoleVirtualizationEntry prevRvEntry) {
    this.prevRvEntry = prevRvEntry;
  }

  public IdVirtualization getIdVirtualization() {
    return idVirtualization;
  }

  public void setIdVirtualization(IdVirtualization idVirtualization) {
    this.idVirtualization = idVirtualization;
  }
}
