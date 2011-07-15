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
package server.id.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import server.id.krb.RealmInfo;


public class TestConnector implements Connector, RealmInfo {
  UUID connectorUuid;
  SortedSet<DitVirtualizationEntry> dvEntries;
  Set<RoleVirtualizationEntry> rvEntries;
  
  public TestConnector() {
  }
  
  public TestConnector(int a) {
  }  
  
  public String getAdminName() {
    return "Administrator@SHIRISH.COM";
  }

  public String getBackupHost() {
    return null;
  }

  public int getBackupPort() {
    return 389;
  }

  public SortedSet<? extends DitVirtualizationEntry> getDvEntries() {
    return dvEntries;
  }

  public OperationMode getMode() {
    return OperationMode.AGENT_SYNCHRONIZATION;
  }

  public String getName() {
    return "testConnector";
  }

  public String getPassword() {
    return "Secret00";
  }

  public String getPrimaryHost() {
    return "192.168.30.10";
  }

  public int getPrimaryPort() {
    return 389;
  }

  public String getProtocol() {
    return "ldap";
  }

  public String getRealm() {
    return "SHIRISH.COM";
  }

  public Set<? extends RoleVirtualizationEntry> getRvEntries() {
    return rvEntries;
  }

  public IdentityStoreType getType() {
    return IdentityStoreType.ACTIVE_DIRECTORY;
  }

  public boolean isGc() {
    return false;
  }

  public List<String> getKdc() {
    List<String> ret = new ArrayList<String>();
    ret.add("192.168.30.10");
    return ret;
  }

  public String getKrbRealm() {
    return getRealm();
  }

  public String getBase() {
    return "dc=shirish,dc=com";
  }

  public long getMajorVersion() {
    return 1;
  }

  public long getMinorVersion() {
    return 0;
  }

  public String getSyncSchedule() {
    return "";
  }

  public Long getId() {
    return new Long(-1);
  }

  public long getChangeNumber() {
    return 0;
  }

  public void setChangeNumber(long changeNumber) {
  }

  public long getPageSize() {
    return Integer.MAX_VALUE;
  }

  public String getAdminName() {
    return "Admin";
  }

  public byte[] getContainerCookie() {
    return null;
  }

  public byte[] getGroupCookie() {
    return null;
  }

  public byte[] getUserCookie() {
    return null;
  }

  public void setContainerCookie(byte[] cookie) {
  }

  public void setGroupCookie(byte[] cookie) {
  }

  public void setUserCookie(byte[] cookie) {
  }

  public byte[] getConnectorUuid() {
    if (connectorUuid == null) {
      connectorUuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
    }
    return connectorUuid.asByteArray();
  }

  public byte[] getUuid() {
    return null;
  }

  public long getRetryCount() {
    return 0;
  }

  public long getRetryInterval() {
    return 0;
  }

  public SyncStatus getSyncStatus() {
    return null;
  }

  public boolean getAutoCreateContainers() {
    return false;
  }

}
