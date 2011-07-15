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

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.safehaus.uuid.UUIDGenerator;

import server.id.Util;


@Entity
@Table(name = "agent_dit_virtualization_entry")
public class DitVirtualizationEntry extends EntityBase implements Comparable<DitVirtualizationEntry>, DitVirtualizationEntry {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Column(name = "ordinal_id", unique = false, nullable = false)
  private Integer ordinalId;

  @Column(name = "local_container", unique = false, nullable = false)
  private String localContainer;
  
  @Column(name = "local_container_base64_uuid", unique = false, nullable = false)
  private String localContainerBase64Uuid;

  @Column(name = "remote_container", unique = false, nullable = false)
  private String remoteContainer;
  
  @Column(name = "remote_container_base64_uuid", unique = false, nullable = false)
  private String remoteContainerBase64Uuid;

  @Column(name = "base64_uuid", unique = true, nullable = false)
  private String base64Uuid;
  
  @Transient
  private Group lc;
  
  @Transient
  private Group rc;
  
  @ManyToOne(optional = false)
  @JoinColumn(name = "agent_connector_id", nullable = false)
  private Connector connector;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "dvEntry")
  private Set<Constraint> constraints;
  
  public DitVirtualizationEntry() {
  }

  public DitVirtualizationEntry(Connector c, DitVirtualizationEntry dvEntry) {
    super();
    this.connector = c;
    this.ordinalId = dvEntry.getOrdinalId();
    this.localContainer = dvEntry.getLocalContainer().getIdentifier();
    this.localContainerBase64Uuid = Util.base64Encode(dvEntry.getLocalContainer().getUuid());
    this.remoteContainer = dvEntry.getRemoteContainer().getIdentifier();
    this.remoteContainerBase64Uuid = Util.base64Encode(dvEntry.getRemoteContainer().getUuid());
    this.base64Uuid = Util.base64Encode(dvEntry.getUuid());
    this.lc = new Group(localContainer, Util.base64Decode(localContainerBase64Uuid), Group.GroupType.INTERNAL_CONTAINER);
    this.rc = new Group(remoteContainer, Util.base64Decode(remoteContainerBase64Uuid), Group.GroupType.REMOTE_CONTAINER);
    if (dvEntry instanceof DitVirtualizationEntry) {
      DitVirtualizationEntry dv = (DitVirtualizationEntry)dvEntry;
      this.constraints = dv.getConstraints();
    }
  }
  
  public DitVirtualizationEntry(Connector c, Integer ordinalId, String localContainer, byte[] localContainerUuid, 
      String remoteContainer, byte[] remoteContainerUuid, Set<Constraint> constraints) {
    super();
    this.connector = c;
    this.ordinalId = ordinalId;
    this.localContainer = localContainer;
    this.localContainerBase64Uuid = Util.base64Encode(localContainerUuid);
    this.remoteContainer = remoteContainer;
    this.remoteContainerBase64Uuid = Util.base64Encode(remoteContainerUuid);
    this.base64Uuid = Util.base64Encode(UUIDGenerator.getInstance().generateRandomBasedUUID().asByteArray());
    this.lc = new Group(localContainer, localContainerUuid, Group.GroupType.INTERNAL_CONTAINER);
    this.rc = new Group(remoteContainer, remoteContainerUuid, Group.GroupType.REMOTE_CONTAINER);
    this.constraints = constraints;
  }

  public Integer getOrdinalId() {
    return ordinalId;
  }

  public void setOrdinalId(Integer ordinalId) {
    this.ordinalId = ordinalId;
  }

  public Group getLocalContainer() {
    if (lc == null) {
      this.lc = new Group(localContainer, Util.base64Decode(localContainerBase64Uuid), Group.GroupType.INTERNAL_CONTAINER);      
    }
    return lc;
  }

  public void setLocalContainer(String localOrgGroup) {
    this.localContainer = localOrgGroup;
  }

  public void setLocalContainerUuid(byte[] uuid) {
    this.localContainerBase64Uuid = Util.base64Encode(uuid);
  }
  
  public Group getRemoteContainer() {
    if (rc == null) {
      this.rc = new Group(remoteContainer, Util.base64Decode(remoteContainerBase64Uuid), Group.GroupType.REMOTE_CONTAINER);      
    }
    return rc;
  }

  public void setRemoteContainer(String remoteOrgGroup) {
    this.remoteContainer = remoteOrgGroup;
  }

  public void setRemoteContainerUuid(byte[] uuid) {
    this.remoteContainerBase64Uuid = Util.base64Encode(uuid);
  }
  
  public int compareTo(DitVirtualizationEntry o) {
    if (this.ordinalId < o.ordinalId)
      return -1;
    else if (this.ordinalId > o.ordinalId)
      return 1;
    else
      return 0;
  }

  public void setConnector(Connector connector) {
    this.connector = connector;
  }

  public Connector getConnector() {
    return connector;
  }

  public byte[] getUuid() {
    return Util.base64Decode(base64Uuid);
  }

  public void setUuid(byte[] uuid) {
    this.base64Uuid = Util.base64Encode(uuid);
  }

  public Set<Constraint> getConstraints() {
    return constraints;
  }

  public void setConstraints(Set<Constraint> constraints) {
    this.constraints = constraints;
  }
  
  @SuppressWarnings("unused")
  private String getLocalContainerBase64Uuid() {
    return localContainerBase64Uuid;
  }

  @SuppressWarnings("unused")
  private void setLocalContainerBase64Uuid(String localContainerBase64Uuid) {
    this.localContainerBase64Uuid = localContainerBase64Uuid;
  }

  @SuppressWarnings("unused")
  private String getRemoteContainerBase64Uuid() {
    return remoteContainerBase64Uuid;
  }

  @SuppressWarnings("unused")
  private void setRemoteContainerBase64Uuid(String remoteContainerBase64Uuid) {
    this.remoteContainerBase64Uuid = remoteContainerBase64Uuid;
  }

  @SuppressWarnings("unused")
  private String getBase64Uuid() {
    return base64Uuid;
  }

  @SuppressWarnings("unused")
  private void setBase64Uuid(String base64Uuid) {
    this.base64Uuid = base64Uuid;
  }

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).
      append("ordinalId", ordinalId).
      append("localContainer", localContainer).
      append("remoteContainer", remoteContainer).
      append("constraints", constraints).toString();
  }
}
