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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import server.id.AVP;
import server.id.AttributeTypeDef;
import server.id.AttributeTypeDef.Type;


@Entity
@Table(name = "agent_attribute")
public class Attribute extends EntityBase implements server.id.Attribute, server.id.AVP {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Column(name = "name", unique = false, nullable = false)
  private String name;
  
  @Column(name = "type", unique = false, nullable = false)
  @Enumerated(EnumType.STRING)
  AttributeTypeDef.Type type;
  
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "attribute")
  private Set<Value> values;
  
  @ManyToOne(optional = false)
  @JoinColumn(name = "agent_directory_object_id", nullable = false)
  private DirectoryObject dirObject;
  
  public Attribute() {
  }
  
  public Attribute(DirectoryObject dirObject, String name, String value) {
    super();
    this.dirObject = dirObject;
    this.name = name;
    this.type = Type.STRING;
    this.values = new HashSet<Value>();
    values.add(new Value(this, value));
  }
  
  public Attribute(DirectoryObject dirObject, String name, AttributeTypeDef.Type type, Set<Value> values) {
    super();
    this.dirObject = dirObject;
    this.name = name;
    this.type = type;
    this.values = values;
  }

  public Attribute(DirectoryObject dirObject, AttributeTypeDef.Type type, AVP avp) {
    this.dirObject = dirObject;
    this.name = avp.getAttribute().getName();
    this.type = type;
    this.values = new HashSet<Value>();
    for(Object o : avp.getValues()) {
      Value v = new Value(this, o);
      values.add(v);
    }
  }

  public Attribute(DirectoryObject dirObject, String attribute, byte[] value) {
    super();
    this.dirObject = dirObject;
    this.name = attribute;
    this.type = Type.BINARY;
    this.values = new HashSet<Value>();
    values.add(new Value(this, value));
  }

  public Attribute(DirectoryObject dirObject, String attribute, AttributeTypeDef.Type type) {
    super();
    this.dirObject = dirObject;
    this.name = attribute;
    this.type = type;
    this.values = new HashSet<Value>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Iterable<Object> getValues() {
    Set<Object> ret = new HashSet<Object>();
    for (Value v : values) {
      if (type == Type.BINARY) {
        ret.add(v.getValue());        
      } else {
        ret.add(new String(v.getValue()));
      }
    }
    return ret;
  }

  public void setValues(Set<Value> values) {
    this.values = values;
  }

  public AttributeTypeDef getAttributeTypeDef() {
    return null;
  }

  public void addValue(Object value) {
    Value v = new Value(this, value);
    values.add(v);
  }

  public server.id.Attribute getAttribute() {
    return this;
  }

  public Object getValue() {
    if (values != null && values.size() != 0) {
      if (type == Type.BINARY) {
        return values.iterator().next().getValue();
      } else {
        return new String(values.iterator().next().getValue());
      }
    } else 
      return null;
  }

  public void setDirObject(DirectoryObject dirObject) {
    this.dirObject = dirObject;
  }

  public DirectoryObject getDirObject() {
    return dirObject;
  }
  
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).
      append("name", name).
      append("values", values).toString();
  }
}
