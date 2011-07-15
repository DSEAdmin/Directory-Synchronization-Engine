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
package server.id.ldap;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextOperations;

import server.id.AVP;
import server.id.Attribute;
import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.Util;


public class LdapEntry implements Entry {
  private static class AttributeComparator implements Comparator<String> {
    public int compare(String o1, String o2) {
      return o1.compareToIgnoreCase(o2);
    }    
  }
  
  private static final AttributeComparator acomp;
  
  static {
    acomp = new AttributeComparator();
  }
  
  private final Log log = LogFactory.getLog(getClass());
  private TreeMap<String, AVP> entry;
  
  public LdapEntry() {
    entry = new TreeMap<String, AVP>(acomp);    
  }

  public LdapEntry(DirContextOperations ctx) {
    this.entry = new TreeMap<String, AVP>(acomp);    
    
    LdapAVP avp = new LdapAVP("dn", ctx.getNameInNamespace());
    addAv(avp);

    Attributes attrs = ctx.getAttributes();
    NamingEnumeration<?> ne = attrs.getAll();
    try {
      while (ne.hasMore()) {
        javax.naming.directory.Attribute attr = (javax.naming.directory.Attribute) ne.next();
        addAv(attr);
      }
    } catch (NamingException ex) {
      log.warn("Naming exception", ex);
    }
  }
  
  public LdapEntry(com.unboundid.ldap.sdk.Entry entry) {
    this.entry = new TreeMap<String, AVP>(acomp);    

    LdapAVP avp = new LdapAVP("dn", entry.getDN());
    addAv(avp);
    
    Collection<com.unboundid.ldap.sdk.Attribute> attributes = entry.getAttributes();
    
    for ( com.unboundid.ldap.sdk.Attribute attr : attributes) {
      addAv(attr);
    }
  }
  
  public Iterable<AVP> getAll() {
    return entry.values();
  }

  public void addAv(AVP avp) {
    entry.put(avp.getAttribute().getName(), avp);
  }
  
  public void addAv(javax.naming.directory.Attribute attr) throws NamingException {
    log.trace("Adding remote attribute " + attr.getID() + " to entry");
    AVP avp = new LdapAVP(attr.getID());
    NamingEnumeration<?> values = attr.getAll();    
    while (values.hasMore()) {
      Object val = values.next();
      avp.addValue(val);
      if (log.isTraceEnabled()) {
        if (val instanceof String) {
          log.trace("Attribute : " + attr.getID() + " type : " + val.getClass().getName() + " Value : " + 
              val);
        } else if (val instanceof byte[]) {
          log.trace("Attribute : " + attr.getID() + " type : " + val.getClass().getName() + " Binary Value : " + 
              Util.byteArrayToHexString((byte[])val));          
        } else {
          log.trace("Attribute : " + attr.getID() + " type : " + val.getClass().getName() + " Value : " + val);
        }
      }
    }
    addAv(avp);
  }

  public AVP getAvp(String attribute) {
    return entry.get(attribute);
  }

  public AVP getAvp(Attribute attribute) {
    return entry.get(attribute.getName());
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("\n");
    sb.append(entry.get(AttributeVirtualization.DN).getValue());
    sb.append("\n");
    for(Iterator<AVP> it = entry.values().iterator(); it.hasNext();) {
      AVP avp = it.next();
      sb.append(avp.toString());
    }
    return sb.toString();
  }

  public void addAv(com.unboundid.ldap.sdk.Attribute attr) {
    log.trace("Adding remote attribute " + attr.getName() + " to entry.");
    AVP avp = new LdapAVP(attr.getName());
    String[] values = attr.getValues();
    for (String val : values) {
      avp.addValue(val);
      log.trace("Attribute " + attr.getName() + " value " + val);
    }
    addAv(avp);    
  }
}
