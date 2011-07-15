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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "agent_member")
@NamedQueries( {
    @NamedQuery(name = "server.id.sync.agent.domain.Member.memberOf", 
        query = "select distinct g "
        + "from Member as m, DirectoryObject as g, Connector as c "
        + "where g.connector.id = (:cid) and m.group.id = g.id and m.normalizedDn = (:ndn)"),
    @NamedQuery(name = "server.id.sync.agent.domain.Member.member", 
        query = "select distinct m "
        + "from Member as m "
        + "where m.group = (:group)") })
public class Member extends EntityBase {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;


  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "agent_group_id", nullable = false)
  private DirectoryObject group;
  
  @Index(name = "MemberNormalizedDnIndex")
  @Column(name = "normalized_dn", unique = false, nullable = false)
  private String normalizedDn;

  public Member() {
  }
  
  public Member(DirectoryObject obj, String dn) {
    this.group = obj;
    this.normalizedDn = dn;
  }
  
  public DirectoryObject getGroup() {
    return group;
  }

  public void setGroup(DirectoryObject group) {
    this.group = group;
  }

  public String getNormalizedDn() {
    return normalizedDn;
  }

  public void setNormalizedDn(String normalizedDn) {
    this.normalizedDn = normalizedDn;
  }
}
