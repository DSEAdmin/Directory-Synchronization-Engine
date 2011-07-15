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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@MappedSuperclass
@EntityListeners(EntityBase.class)
public abstract class EntityBase implements java.io.Serializable {
  @Transient
  private final Log log = LogFactory.getLog(getClass());
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  protected Long id;

  @Column(name = "create_date")
  protected long createDate;

  @Column(name = "last_date")
  protected long lastDate;

  @PrePersist
  public void preCreate() {
    log.debug("preCreate");
    createDate = System.currentTimeMillis();
    lastDate = System.currentTimeMillis();
  }

  @PreUpdate
  public void preUpdate() {
    log.debug("preUpdate");
    lastDate = System.currentTimeMillis();
  }

  public EntityBase() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getCreateDate() {
    return new Date(createDate);
  }

  public void setCreateDate(Date createDate) {
    this.createDate = createDate.getTime();
  }

  public Date getLastDate() {
    return new Date(lastDate);
  }

  public void setLastDate(Date lastDate) {
    this.lastDate = lastDate.getTime();
  }  
}
