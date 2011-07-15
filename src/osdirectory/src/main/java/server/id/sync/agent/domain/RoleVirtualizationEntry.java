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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.safehaus.uuid.UUIDGenerator;

import server.id.Util;


@Entity
@Table(name = "agent_role_virtualization_entry")
public class RoleVirtualizationEntry extends EntityBase implements RoleVirtualizationEntry {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Column(name = "local_role", unique = false, nullable = false)
  private String localRole;

  @Column(name = "local_role_base64_uuid", unique = false, nullable = false)
  private String localRoleBase64Uuid;
  
  @Column(name = "remote_role", unique = false, nullable = false)
  private String remoteRole;

  @Column(name = "remote_role_base64_uuid", unique = false, nullable = false)
  private String remoteRoleBase64Uuid;
  
  @Column(name = "base64_uuid", unique = true, nullable = false)
  private String base64Uuid;
  
  @ManyToOne(optional = false)
  @JoinColumn(name = "agent_connector_id", nullable = false)
  private Connector connector;

  @Transient
  Role lr;
  
  @Transient
  Group rr;
  
  public RoleVirtualizationEntry() {
  }

  public RoleVirtualizationEntry(Connector c, RoleVirtualizationEntry rvEntry) {
    super();
    this.connector = c;
    this.localRole = rvEntry.getLocalRole().getIdentifier();
    this.localRoleBase64Uuid = Util.base64Encode(rvEntry.getLocalRole().getUuid());
    this.remoteRole = rvEntry.getRemoteRole().getIdentifier();
    this.remoteRoleBase64Uuid = Util.base64Encode(rvEntry.getRemoteRole().getUuid());
    this.base64Uuid = Util.base64Encode(rvEntry.getUuid());
    this.lr = new Role(localRole, Util.base64Decode(localRoleBase64Uuid));
    this.rr = new Group(remoteRole, Util.base64Decode(remoteRoleBase64Uuid), Group.GroupType.REMOTE_ROLE);
  }
  
  public RoleVirtualizationEntry(Connector c, String role, byte[] localRoleUuid, String remoteGroup, byte[] remoteRoleUuid) {
    super();
    this.connector = c;
    this.localRole = role;
    this.localRoleBase64Uuid = Util.base64Encode(localRoleUuid);
    this.remoteRole = remoteGroup;
    this.remoteRoleBase64Uuid = Util.base64Encode(remoteRoleUuid);
    this.base64Uuid = Util.base64Encode(UUIDGenerator.getInstance().generateRandomBasedUUID().asByteArray());
    this.lr = new Role(localRole, Util.base64Decode(localRoleBase64Uuid));
    this.rr = new Group(remoteRole, Util.base64Decode(this.remoteRoleBase64Uuid), Group.GroupType.REMOTE_ROLE);
  }

  public Role getLocalRole() {
    if (lr == null) {
      this.lr = new Role(localRole, Util.base64Decode(localRoleBase64Uuid));      
    }
    return lr;
  }

  public void setLocalRole(String role) {
    this.localRole = role;
  }

  public void setLocalRoleUuid(byte[] uuid) {
    this.localRoleBase64Uuid = Util.base64Encode(uuid);
  }
  
  public Group getRemoteRole() {
    if (rr == null) {
      this.rr = new Group(remoteRole, Util.base64Decode(remoteRoleBase64Uuid), Group.GroupType.REMOTE_ROLE);      
    }
    return rr;
  }

  public void setRemoteRole(String remoteGroup) {
    this.remoteRole = remoteGroup;
  }

  public void setRemoteRoleUuid(byte[] uuid) {
    this.remoteRoleBase64Uuid = Util.base64Encode(uuid);
  }
  
  public Connector getConnector() {
    return connector;
  }

  public void setConnector(Connector connector) {
    this.connector = connector;
  }

  public byte[] getUuid() {
    return Util.base64Decode(base64Uuid);
  }

  public void setUuid(byte[] uuid) {
    this.base64Uuid = Util.base64Encode(uuid);
  }

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).
      append("localRole", localRole).
      append("remoteRole", remoteRole).toString();
  }
}
