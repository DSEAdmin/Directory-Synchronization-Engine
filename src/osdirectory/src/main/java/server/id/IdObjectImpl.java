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
package server.id;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;


public class IdObjectImpl implements IdObject {
  private final Log log = LogFactory.getLog(getClass());
  protected Entry entry;
  protected IdObjectType objectType;
  
  public IdObjectImpl(Entry entry) {
    this.entry = entry;
    this.objectType = IdObjectType.UNSUPPORTED;
  }
  
  public IdObjectImpl(Entry entry, IdObjectType type) {
    this.entry = entry;
    this.objectType = type;
  }
  
  /* (non-Javadoc)
   * @see server.id.IdObject#getCreateTimestamp()
   */
  public String getCreateTimestamp() {
    return (String)getAttributeValue(AttributeVirtualization.CREATETIME);
  }
  
  /* (non-Javadoc)
   * @see server.id.IdObject#getModifyTimestamp()
   */
  public String getModifyTimestamp() {
    return (String)getAttributeValue(AttributeVirtualization.MODIFYTIME);
  }
  
  /* (non-Javadoc)
   * @see server.id.IdObject#getObjectType()
   */
  public IdObjectType getObjectType() {
    return objectType;
  }
  
  /* (non-Javadoc)
   * @see server.id.IdObject#getDn()
   */
  public String getDn() {
    return (String)getAttributeValue(AttributeVirtualization.DN);
  }
  
  /* (non-Javadoc)
   * @see server.id.IdObject#getUUID()
   */
  public byte[] getUUID() {
    return (byte[])getAttributeValue(AttributeVirtualization.UUID);
  }
  
  /* (non-Javadoc)
   * @see server.id.IdObject#getParentUUID()
   */
  public byte[] getParentUUID() {
    return (byte[])getAttributeValue(AttributeVirtualization.PARENT_UUID);    
  }
  
  /* (non-Javadoc)
   * @see server.id.IdObject#getAVP(java.lang.String)
   */
  public AVP getAVP(String attribute) {    
    return entry.getAvp(attribute);
  }
  
  /* (non-Javadoc)
   * @see server.id.IdObject#getAttributeValues(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public Iterable<?> getAttributeValues(String attribute) {
    Iterable<Object> ret = null;
    AVP avp = entry.getAvp(attribute);
    if (avp != null)      
      ret = (Iterable<Object>)avp.getValues();
    return ret;
  }
  
  /* (non-Javadoc)
   * @see server.id.IdObject#getAttributeValue(java.lang.String)
   */
  public Object getAttributeValue(String attribute) {    
    Iterable<?> values = getAttributeValues(attribute);
    if (values == null || values.iterator().hasNext() == false)
      return null;
    Iterator<?> it = values.iterator();
    return it.next();
  }
  
  void setObjectType(IdObjectType type) {
    this.objectType = type;
  }
  
  public String getNormalizedDn() {
    String dn = getDn();
    if (dn != null) {
      try {
        String ndn = DN.normalize(dn);
        return ndn;
      } catch (LDAPException e) {
        log.warn("Could not parse DN : " + dn);
        return null;
      }
    }
    else
      return null;
  }
  
  public Entry getEntry() {
    return entry;
  }
  
  public String toString() {
    return entry.toString();
  }

  public void addAndOverrideAttributes(IdObject newObject) {
    Iterable<? extends AVP> attributes = newObject.getEntry().getAll();
    for(AVP avp : attributes) {
      entry.addAv(avp);
    }
  }
}
