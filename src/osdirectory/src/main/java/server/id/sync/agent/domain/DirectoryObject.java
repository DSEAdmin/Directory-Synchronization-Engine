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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import javax.persistence.CascadeType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Index;

import server.id.AVP;
import server.id.AttributeTypeDef;
import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.IdObject;
import server.id.IdObjectType;
import server.id.Util;


@Entity
@Table(name = "agent_directory_object")
@NamedQueries({
  @NamedQuery(name="server.id.sync.agent.domain.DirectoryObject.byUuid", 
      query="select d " +
      		"from DirectoryObject as d " +
      		"where d.connector.id = (:cid) and d.base64Uuid = (:uuid)"),
  @NamedQuery(name="server.id.sync.agent.domain.DirectoryObject.all",
      query="select d " +
      "from DirectoryObject as d " +
      "where d.connector.id = (:cid)"),
  @NamedQuery(name="server.id.sync.agent.domain.DirectoryObject.Children",
      query="select d " +
      "from DirectoryObject as d " +
      "where d.connector.id = (:cid) and d.normalizedDn like (:base)"),
  @NamedQuery(name="server.id.sync.agent.domain.DirectoryObject.byNdn",
      query="select d " +
      "from DirectoryObject as d " +
      "where d.connector.id = (:cid) and d.normalizedDn = (:ndn)"),
  @NamedQuery(name="server.id.sync.agent.domain.DirectoryObject.syncedAll",
      query="select d " +
      "from DirectoryObject as d " +
      "where d.connector.id = (:cid) and d.type = 'PERSON' and d.dvEntry is not null ")
})
public class DirectoryObject extends EntityBase implements Entry {
  private static class AttributeComparator implements Comparator<Attribute> {
    public int compare(Attribute o1, Attribute o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }    
  }
  private static final AttributeComparator acomp;
  
  static {
    acomp = new AttributeComparator();
  }
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Enumerated(EnumType.STRING)
  @Index(name = "typeIndex")
  @Column(name = "type", unique = false, nullable = false)
  IdObjectType type;
  
  @Column(name = "dn", unique = false, nullable = true)
  private String dn;
  
  @Index(name = "normalizedDnIndex")
  @Column(name = "normalized_dn", unique = false, nullable = true)
  private String normalizedDn;
  
  @Index(name = "directoryObjectUuidIndex")
  @Column(name = "base64_uuid", unique = true, nullable = false)
  private String base64Uuid;
  
  @Index(name = "directoryObjectParentUuidIndex")
  @Column(name = "base64_parent_uuid", unique = false, nullable = true)
  private String base64ParentUuid;  
  
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "dirObject")
  private Set<Attribute> attributes;
  
  @ManyToOne(optional = false)
  @JoinColumn(name = "agent_connector_id", nullable = false)
  private Connector connector;
  
  @ManyToOne(optional = true, fetch = FetchType.LAZY )
  @JoinColumn(name = "dit_virtualization_entry_id", nullable = true)
  private DitVirtualizationEntry dvEntry;
  
  public DirectoryObject() {
    attributes = new TreeSet<Attribute>(acomp);
  }
  
  public DirectoryObject(Connector c, IdObject obj, DitVirtualizationEntry dvEntry) {
    super();
    this.connector = c;
    this.dvEntry = dvEntry;
    this.type = obj.getObjectType();
    this.dn = obj.getDn();
    this.normalizedDn = obj.getNormalizedDn();
    this.base64Uuid = Util.base64Encode(obj.getUUID());
    this.base64ParentUuid = Util.base64Encode(obj.getParentUUID());
    this.attributes = new TreeSet<Attribute>(acomp);
    AVP oc = obj.getAVP(AttributeVirtualization.OBJECTTYPE);
    if (oc != null) {
      Attribute ocattr = new Attribute(this, AttributeTypeDef.Type.STRING, oc);
      attributes.add(ocattr);
    }
  }
  
  public DirectoryObject(Connector c, IdObjectType type, String dn, 
      String normalizedDn, byte[] uuid, byte[] parentUuid,
      DitVirtualizationEntry dvEntry,
      Set<Attribute> attributes) {
    super();
    this.connector = c;
    this.dvEntry = dvEntry;
    this.type = type;
    this.dn = dn;
    this.normalizedDn = normalizedDn;
    this.base64Uuid = Util.base64Encode(uuid);
    this.base64ParentUuid = Util.base64Encode(parentUuid);
    this.attributes = attributes;
  }

  public static DirectoryObject getDirectoryObject(Connector connector, DitVirtualizationEntry dvEntry, 
      IdObject object) {
    DirectoryObject dirObject = new DirectoryObject();
    dirObject.connector = connector;
    dirObject.dvEntry = dvEntry;
    dirObject.type = object.getObjectType();
    dirObject.dn = object.getDn();
    dirObject.normalizedDn = object.getNormalizedDn();
    dirObject.base64Uuid = Util.base64Encode(object.getUUID());
    if (object.getParentUUID() != null) {
      dirObject.base64ParentUuid = Util.base64Encode(object.getParentUUID());
    } else {
      dirObject.base64ParentUuid = null;
    }
    AVP oc = object.getAVP(AttributeVirtualization.OBJECTTYPE);
    if (oc != null) {
      Attribute ocattr = new Attribute(dirObject, AttributeTypeDef.Type.STRING, oc);
      dirObject.getAttributes().add(ocattr);
    }
    return dirObject;
  }
  
  public IdObjectType getType() {
    return type;
  }

  public void setType(IdObjectType type) {
    this.type = type;
  }

  public String getDn() {
    return dn;
  }

  public void setDn(String dn) {
    this.dn = dn;
  }

  public String getNormalizedDn() {
    return normalizedDn;
  }

  public void setNormalizedDn(String normalizedDn) {
    this.normalizedDn = normalizedDn;
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

  @SuppressWarnings("unused")
  private String getBase64ParentUuid() {
    return base64ParentUuid;
  }
  
  @SuppressWarnings("unused")
  private void setBase64ParentUuid(String base64ParentUuid) {
    this.base64ParentUuid = base64ParentUuid;
  }
  
  public byte[] getParentUuid() {
    return Util.base64Decode(base64ParentUuid);
  }

  public void setParentUuid(byte[] parentUuid) {
    this.base64ParentUuid = Util.base64Encode(parentUuid);
  }

  public Set<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(Set<Attribute> attributes) {
    this.attributes = attributes;
  }

  public void addAv(AVP avp) {
    String attrName = avp.getAttribute().getName();
    if (attrName.equalsIgnoreCase(AttributeVirtualization.DN)) {
      dn = (String) avp.getValue();
    } else if (attrName.equalsIgnoreCase(AttributeVirtualization.NDN)) {
      normalizedDn = (String) avp.getValue();
    } else if (attrName.equalsIgnoreCase(AttributeVirtualization.PARENT_UUID)) {
      base64ParentUuid = Util.base64Encode((byte[])avp.getValue());
    } else if (attrName.equalsIgnoreCase(AttributeVirtualization.UUID)) {
      base64Uuid = Util.base64Encode((byte[]) avp.getValue());
    } else {
      AttributeTypeDef.Type type = AttributeTypeDef.Type.STRING;
      if (avp.getAttribute().getAttributeTypeDef() != null) {
        type = avp.getAttribute().getAttributeTypeDef().getType();
      }
      Attribute attr = new Attribute(this, type, avp);
      if (attributes.contains(attr)) {
        attributes.remove(attr);
      }
      attributes.add(attr);
    }
  }

  private Set<Attribute> addFixedAttributes() {
    Set<Attribute> fixedAttrs = new HashSet<Attribute>();
    if (dn != null)
      fixedAttrs.add(new Attribute(this, AttributeVirtualization.DN, dn));
    if (normalizedDn != null)
      fixedAttrs.add(new Attribute(this, AttributeVirtualization.NDN, normalizedDn));
    if (base64ParentUuid != null)
      fixedAttrs.add(new Attribute(this, AttributeVirtualization.PARENT_UUID, Util.base64Decode(base64ParentUuid)));
    if (base64Uuid != null)
      fixedAttrs.add(new Attribute(this, AttributeVirtualization.UUID, Util.base64Decode(base64Uuid)));
    if (dvEntry != null)
      fixedAttrs.add(new Attribute(this, AttributeVirtualization.DV_ENTRY, dvEntry.getUuid()));
    return fixedAttrs;
  }

  
  public Iterable<? extends AVP> getAll() {
    Set<Attribute> ret = new HashSet<Attribute>(attributes);
    ret.addAll(addFixedAttributes());
    return ret;
  }

  public AVP getAvp(String attribute) {
    if (attribute.equalsIgnoreCase(AttributeVirtualization.DN)) {
      if (dn != null)
        return new Attribute(this, AttributeVirtualization.DN, dn);
      else
        return null;
    } else if (attribute.equalsIgnoreCase(AttributeVirtualization.NDN)) {
      if (normalizedDn != null)
        return new Attribute(this, AttributeVirtualization.NDN, normalizedDn);
      else
        return null;
    } else if (attribute.equalsIgnoreCase(AttributeVirtualization.PARENT_UUID)) {
      if (base64ParentUuid != null)
        return new Attribute(this, AttributeVirtualization.PARENT_UUID, Util.base64Decode(base64ParentUuid));
      else
        return null;
    } else if (attribute.equalsIgnoreCase(AttributeVirtualization.UUID)) {
      if (base64Uuid != null)
        return new Attribute(this, AttributeVirtualization.UUID, Util.base64Decode(base64Uuid));
      else
        return null;
    } if (attribute.equalsIgnoreCase(AttributeVirtualization.DV_ENTRY)) {
      if (dvEntry != null) 
        return new Attribute(this, AttributeVirtualization.DV_ENTRY, dvEntry.getUuid());
      else
        return null;
    } else {
      for (Attribute a : attributes) {
        if (a.getName().equalsIgnoreCase(attribute)) {
          return a;
        }
      }
    }
    return null;
  }

  public AVP getAvp(server.id.Attribute attribute) {
    return getAvp(attribute.getName());
  }

  public void setConnector(Connector connector) {
    this.connector = connector;
  }

  public Connector getConnector() {
    return connector;
  }
  
  public void setDvEntry(DitVirtualizationEntry dvEntry) {
    this.dvEntry = dvEntry;
  }

  public DitVirtualizationEntry getDvEntry() {
    return dvEntry;
  }

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).
      append("type", type).
      append("dn", dn).
      append("normalizedDn", normalizedDn).
      append("uuid", base64Uuid).
      append("parentUuid", base64ParentUuid).
      append("dvEntryUuid", dvEntry.getUuid()).
      append("attributes", attributes).toString();
  }
}
