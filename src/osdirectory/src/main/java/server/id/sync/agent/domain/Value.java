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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@Table(name = "agent_value")
public class Value extends EntityBase {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Column(name = "value", unique = false, nullable = true)
  private byte[] value;

  @ManyToOne(optional = false)
  @JoinColumn(name = "agent_attribute_id", nullable = false)
  private Attribute attribute;
  
  public Value() {
  }
  
  public Value(Attribute attribute, byte[] value) {
    super();
    this.setAttribute(attribute);
    this.value = value;
  }
  
  public Value(Attribute attribute, Object val) {
    this.setAttribute(attribute);
    if (val instanceof byte[]) {
      this.value = (byte[])val;
    } else if (val instanceof String) {
      this.value = ((String)val).getBytes();
    }
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }

  public void setAttribute(Attribute attribute) {
    this.attribute = attribute;
  }

  public Attribute getAttribute() {
    return attribute;
  }
  
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).
      append("value", value).toString();
  }
}
