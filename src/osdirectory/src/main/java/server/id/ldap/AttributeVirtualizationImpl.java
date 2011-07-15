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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import server.id.AVP;
import server.id.Attribute;
import server.id.AttributeTransform;
import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.IdAVP;
import server.id.IdAttribute;
import server.id.IdEntry;
import server.id.dao.ChangePoller;


public class AttributeVirtualizationImpl implements AttributeVirtualization, InitializingBean {
  private static class AttributeComparator implements Comparator<String> {
    public int compare(String o1, String o2) {
      return o1.compareToIgnoreCase(o2);
    }    
  }
  
  private static final AttributeComparator acomp;
  
  static {
    acomp = new AttributeComparator();
  }

  private Map<String, AttributeTransform> transformations;
  private Set<String> remoteAttrs;
  private Set<String> binaryRemoteAttrs;
  private final Log log = LogFactory.getLog(getClass());
  
  public AttributeVirtualizationImpl() {
    transformations = new TreeMap<String, AttributeTransform>(acomp);
  }
  
  public void setTransformations(Map<String, AttributeTransform> transformations) {
    this.transformations.putAll(transformations);
  }
  
  public void afterPropertiesSet() throws Exception {
    remoteAttrs = new HashSet<String>();
    binaryRemoteAttrs = new HashSet<String>();
    for (AttributeTransform txfm : transformations.values()) {
      for(String attr : txfm.getRemoteAttributes()) {
        remoteAttrs.add(attr);
      }
      for(String attr : txfm.getBinaryRemoteAttributes()) {
        binaryRemoteAttrs.add(attr);
      }
    }
  }

  public Set<String> getAllRemoteAttributes() {
    return remoteAttrs;
  }

  public Set<String> getAllBinaryRemoteAttributes() {
    return binaryRemoteAttrs;
  }

  public Set<String> getAllVirtualAttributes() {
    return transformations.keySet();
  }

  public AttributeTransform getAttributeTransform(String virtAttr) {
    return transformations.get(virtAttr);
  }

  public Entry virtualize(ChangePoller changePoller, Entry remoteEntry) throws Exception {
    log.trace("virtualize");
    IdEntry entry = new IdEntry();
    Set<Map.Entry<String, AttributeTransform>> entrySet = transformations.entrySet();
    for(Map.Entry<String, AttributeTransform> txfm: entrySet) {
      log.debug("Mapping virtual attribute " + txfm.getKey());
      Iterable<? extends Object> values = txfm.getValue().virtualize(changePoller, remoteEntry);
      if (values != null) {
        Attribute attr = new IdAttribute(txfm.getKey());
        AVP avp = new IdAVP(attr);
        for(Object o : values) {
          log.trace("Adding value " + o + " to virtual attribute " + txfm.getKey());
          avp.addValue(o);
        }
        entry.addAv(avp);
      } else {
        log.debug("Virtual attribute " + txfm.getKey() + " maps to NO value");
      }
    }
    return entry;
  }
}
