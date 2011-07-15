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
package server.id.sync.agent.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import server.id.Util;


import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Table(name = "agent_connector")
@NamedQueries({
      @NamedQuery(name="server.id.sync.agent.domain.Connector.byName", 
          query="from Connector as c where upper(c.name) = upper(:name)"),
      @NamedQuery(name="server.id.sync.agent.domain.Connector.All", 
          query="select distinct c from Connector as c"),
      @NamedQuery(name="server.id.sync.agent.domain.Connector.byUuid",
          query="select distinct c from Connector as c where c.base64Uuid = (:connectorUuid)")
})
public class Connector extends EntityBase implements Connector {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Column(name = "major_version", unique = false, nullable = false)
  private long majorVersion;
  
  @Column(name = "minor_version", unique = false, nullable = false)
  private long minorVersion;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "type", unique = false, nullable = false)
  private IdentityStoreType type;

  @Index(name = "connectorNameIndex")
  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Index(name = "connectorUuidIndex")
  @Column(name = "base64_uuid", nullable = false, unique = true)
  private String base64Uuid;
  
  @Column(name = "realm", unique = false, nullable = true)
  private String realm;
  
  @Column(name = "primary_host", unique = false, nullable = true)
  private String primaryHost;

  @Column(name = "primary_port", unique = false, nullable = true)
  private int primaryPort;
  
  @Column(name = "backup_host", unique = false, nullable = true)
  private String backupHost;

  @Column(name = "backup_port", unique = false, nullable = true)
  private int backupPort;
  
  @Column(name = "gc", unique = false, nullable = true)
  private boolean gc;

  @Column(name = "admin_name", unique = false, nullable = true)
  private String adminName;

  @Column(name = "password", unique = false, nullable = true)
  private String password;

  @Column(name = "_admin_name", unique = false, nullable = true)
  private String AdminName;
  
  @Column(name = "protocol", unique = false, nullable = true)
  private String protocol;

  @Enumerated(EnumType.STRING)
  @Column(name = "mode", unique = false, nullable = true)
  private OperationMode mode;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "connector")
  private Set<RoleVirtualizationEntry> rvEntries;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "connector")
  @Sort(type = SortType.NATURAL)
  private SortedSet<DitVirtualizationEntry> dvEntries;

  @Column(name = "base", unique = false, nullable = true)
  private String base;
  
  @Column(name = "sync_schedule", unique = false, nullable = true)
  private String syncSchedule;
  
  @Column(name = "user_cookie", unique = false, nullable = true)
  private byte[] userCookie;
  
  @Column(name = "group_cookie", unique = false, nullable = true)
  private byte[] groupCookie;
  
  @Column(name = "container_cookie", unique = false, nullable = true)
  private byte[] containerCookie;
  
  @Column(name = "change_number", unique = false, nullable = true)
  private long changeNumber;
  
  @Column(name = "page_size", unique = false, nullable = true)
  private long pageSize;

  @Column(name = "retry_interval", unique = false, nullable = true)
  private long retryInterval;
  
  @Column(name = "retry_count", unique = false, nullable = true)
  private long retryCount;
  
  @Column(name = "auto_create_containers", unique = false, nullable = false)
  private boolean autoCreateContainers;
  
  @SuppressWarnings("unused")
  @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "connector")
  private Set<DirectoryObject> dirObjects;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "sync_status", unique = false, nullable = false)
  SyncStatus syncStatus;
  
  @SuppressWarnings("unused")
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "connector")
  private Set<OperationResult> operationResult;
  
  public Connector() {
  }

  public Connector(Connector c) {
    super();
    this.majorVersion = c.getMajorVersion();
    this.minorVersion = c.getMinorVersion();
    this.type = c.getType();
    this.name = c.getName();
    this.base64Uuid = Util.base64Encode(c.getUuid());
    this.realm = c.getRealm();
    this.primaryHost = c.getPrimaryHost();
    this.primaryPort = c.getPrimaryPort();
    this.backupHost = c.getBackupHost();
    this.backupPort = c.getBackupPort();
    this.gc = c.isGc();
    this.adminName = c.getAdminName();
    this.password = c.getPassword();
    this.AdminName = c.getAdminName();
    this.protocol = c.getProtocol();
    this.mode = c.getMode();
    this.rvEntries = new HashSet<RoleVirtualizationEntry>();
    for (RoleVirtualizationEntry r : c.getRvEntries()) {
      RoleVirtualizationEntry rvEntry = new RoleVirtualizationEntry(this, r);
      rvEntries.add(rvEntry);
    }
    this.dvEntries = new TreeSet<DitVirtualizationEntry>();
    for (DitVirtualizationEntry d : c.getDvEntries()) {
      DitVirtualizationEntry dvEntry = new DitVirtualizationEntry(this, d);
      dvEntries.add(dvEntry);
    }
    this.base = c.getBase();
    this.syncSchedule = c.getSyncSchedule();
    this.userCookie = c.getUserCookie();
    this.groupCookie = c.getGroupCookie();
    this.containerCookie = c.getContainerCookie();
    this.changeNumber = c.getChangeNumber();
    this.pageSize = c.getPageSize();
    this.retryInterval = c.getRetryInterval();
    this.retryCount = c.getRetryCount();
    this.syncStatus = c.getSyncStatus();
    this.autoCreateContainers = c.getAutoCreateContainers();
  }
  
  public Connector(long majorVersion, long minorVersion, IdentityStoreType type, String name, byte[] connectorUuid, String realm,
      String primaryHost, int primaryPort, String backupHost, int backupPort, boolean gc, String adminName,
      String password, String AdminName, String protocol, OperationMode mode, Set<RoleVirtualizationEntry> roleVirtualizationEntries,
      SortedSet<DitVirtualizationEntry> ditVirtualizationEntries, String base, String syncSchedule, byte[] userCookie,
      byte[] groupCookie, byte[] containerCookie, long changeNumber, long pageSize, long retryInterval, 
      long retryCount, boolean autoCreateContainers) {
    super();
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.type = type;
    this.name = name;
    this.base64Uuid = Util.base64Encode(connectorUuid);
    this.realm = realm;
    this.primaryHost = primaryHost;
    this.primaryPort = primaryPort;
    this.backupHost = backupHost;
    this.backupPort = backupPort;
    this.gc = gc;
    this.adminName = adminName;
    this.password = password;
    this.AdminName = AdminName;
    this.protocol = protocol;
    this.mode = mode;
    this.rvEntries = roleVirtualizationEntries;
    this.dvEntries = ditVirtualizationEntries;
    this.base = base;
    this.syncSchedule = syncSchedule;
    this.userCookie = userCookie;
    this.groupCookie = groupCookie;
    this.containerCookie = containerCookie;
    this.changeNumber = changeNumber;
    this.pageSize = pageSize;
    this.retryInterval = retryInterval;
    this.retryCount = retryCount;
    this.autoCreateContainers = autoCreateContainers;
    this.syncStatus = SyncStatus.FULL_SYNC_IN_PROGRESS;
  }

  public long getMajorVersion() {
    return majorVersion;
  }

  public void setMajorVersion(long majorVersion) {
    this.majorVersion = majorVersion;
  }

  public long getMinorVersion() {
    return minorVersion;
  }

  public void setMinorVersion(long minorVersion) {
    this.minorVersion = minorVersion;
  }

  public IdentityStoreType getType() {
    return type;
  }

  public void setType(IdentityStoreType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @SuppressWarnings("unused")
  private String getBase64Uuid() {
    return base64Uuid;
  }
  
  @SuppressWarnings("unused")
  private void setBase64Uuid(String base64Uuid) {
    this.base64Uuid = base64Uuid;
  }
  
  public byte[] getUuid() {
    return Util.base64Decode(base64Uuid);
  }

  public void setUuid(byte[] uuid) {
    this.base64Uuid = Util.base64Encode(uuid);
  }

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public String getPrimaryHost() {
    return primaryHost;
  }

  public void setPrimaryHost(String primaryHost) {
    this.primaryHost = primaryHost;
  }

  public int getPrimaryPort() {
    return primaryPort;
  }

  public void setPrimaryPort(int primaryPort) {
    this.primaryPort = primaryPort;
  }

  public String getBackupHost() {
    return backupHost;
  }

  public void setBackupHost(String backupHost) {
    this.backupHost = backupHost;
  }

  public int getBackupPort() {
    return backupPort;
  }

  public void setBackupPort(int backupPort) {
    this.backupPort = backupPort;
  }

  public boolean isGc() {
    return gc;
  }

  public void setGc(boolean gc) {
    this.gc = gc;
  }

  public String getAdminName() {
    return adminName;
  }

  public void setAdminName(String adminName) {
    this.adminName = adminName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public OperationMode getMode() {
    return mode;
  }

  public void setMode(OperationMode mode) {
    this.mode = mode;
  }

  public Set<? extends RoleVirtualizationEntry> getRvEntries() {
    return rvEntries;
  }

  public void setRvEntries(Set<RoleVirtualizationEntry> roleVirtualizationEntries) {
    this.rvEntries = roleVirtualizationEntries;
  }

  public SortedSet<? extends DitVirtualizationEntry> getDvEntries() {
    return dvEntries;
  }

  public void setDvEntries(SortedSet<DitVirtualizationEntry> ditVirtualizationEntries) {
    this.dvEntries = ditVirtualizationEntries;
  }

  public String getBase() {
    return base;
  }

  public void setBase(String base) {
    this.base = base;
  }

  public String getSyncSchedule() {
    return syncSchedule;
  }

  public void setSyncSchedule(String syncSchedule) {
    this.syncSchedule = syncSchedule;
  }

  public byte[] getUserCookie() {
    return userCookie;
  }

  public void setUserCookie(byte[] userCookie) {
    this.userCookie = userCookie;
  }

  public byte[] getGroupCookie() {
    return groupCookie;
  }

  public void setGroupCookie(byte[] groupCookie) {
    this.groupCookie = groupCookie;
  }

  public byte[] getContainerCookie() {
    return containerCookie;
  }

  public void setContainerCookie(byte[] containerCookie) {
    this.containerCookie = containerCookie;
  }

  public long getChangeNumber() {
    return changeNumber;
  }

  public void setChangeNumber(long changeNumber) {
    this.changeNumber = changeNumber;
  }

  public long getPageSize() {
    return pageSize;
  }
  
  public void setPageSize(long pageSize) {
    this.pageSize = pageSize;
  }

  public String getAdminName() {
    return AdminName;
  }
  
  public void setAdminName(String AdminName) {
    this.AdminName = AdminName;
  }

  public long getRetryInterval() {
    return retryInterval;
  }

  public void setRetryInterval(long retryInterval) {
    this.retryInterval = retryInterval;
  }

  public long getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(long retryCount) {
    this.retryCount = retryCount;
  }

  public boolean getAutoCreateContainers() {
    return this.autoCreateContainers;
  }
  
  public void setAutoCreateContainers(boolean autoCreateContainers) {
    this.autoCreateContainers = autoCreateContainers;
  }
  
  public SyncStatus getSyncStatus() {
    return syncStatus;
  }

  public void setSyncStatus(SyncStatus syncStatus) {
    this.syncStatus = syncStatus;
  }
  
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).
      append("majorVersion", majorVersion).
      append("minorVersion", minorVersion).
      append("type", type).
      append("name", name).
      append("connectorUuid", base64Uuid).
      append("realm", realm).
      append("primaryHost", primaryHost).
      append("primaryPort", primaryPort).
      append("backupHosst", backupHost).
      append("backupPort", backupPort).
      append("gc", gc).
      append("adminName", adminName).
      append("password", password).
      append("AdminName", AdminName).
      append("protocol", protocol).
      append("mode", mode).
      append("base", base).
      append("syncSchedule", syncSchedule).
      append("userCookie", userCookie).
      append("groupCookie", groupCookie).
      append("containerCookie", containerCookie).
      append("rvEntries", rvEntries).
      append("dvEntries", dvEntries).
      append("changeNumber", changeNumber).
      append("retryInterval", retryInterval).
      append("retryCount", retryCount).
      append("autoCreateContainers", autoCreateContainers).
      append("syncStatus", syncStatus.name()).
      append("pageSize", pageSize).toString();
  }
}
