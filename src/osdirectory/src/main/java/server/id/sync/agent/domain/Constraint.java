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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import server.id.AttributeTypeDef;
import server.id.AttributeTypeDef.Type;


@Entity
@Table(name = "agent_constraint")
public class Constraint extends EntityBase implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Column(name = "attribute_name", unique = false, nullable = false)
  private String attributeName;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "attribute_type", unique = false, nullable = false)
  private AttributeTypeDef.Type attributeType;
  
  @Column(name = "attribute_value", unique = false, nullable = false)
  private byte[] attributeValue;

  @ManyToOne(optional = false)
  @JoinColumn(name = "agent_dit_virtualization_entry_id", nullable = false)
  private DitVirtualizationEntry dvEntry;

  public Constraint() {
  }
  
  public Constraint(String attributeName, Type attributeType, byte[] attributeValue, DitVirtualizationEntry dvEntry) {
    super();
    this.attributeName = attributeName;
    this.attributeType = attributeType;
    this.attributeValue = attributeValue;
    this.dvEntry = dvEntry;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public AttributeTypeDef.Type getAttributeType() {
    return attributeType;
  }

  public void setAttributeType(AttributeTypeDef.Type attributeType) {
    this.attributeType = attributeType;
  }

  public byte[] getAttributeValue() {
    return attributeValue;
  }

  public void setAttributeValue(byte[] attributeValue) {
    this.attributeValue = attributeValue;
  }

  public DitVirtualizationEntry getDvEntry() {
    return dvEntry;
  }

  public void setDvEntry(DitVirtualizationEntry dvEntry) {
    this.dvEntry = dvEntry;
  }
  
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).
      append("attributeName", attributeName).
      append("attributeType", attributeType).
      append("attributeValue", new String(attributeValue)).toString();
  }
}
