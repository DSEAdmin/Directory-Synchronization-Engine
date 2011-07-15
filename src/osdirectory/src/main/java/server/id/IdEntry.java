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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.AVP;
import server.id.Attribute;
import server.id.AttributeVirtualization;
import server.id.Entry;


public class IdEntry implements Entry {
  private final Log log = LogFactory.getLog(getClass());
  private Map<String, AVP> entry;
  private static class AttributeComparator implements Comparator<String> {
    public int compare(String o1, String o2) {
      return o1.compareToIgnoreCase(o2);
    }    
  }
  
  private static final AttributeComparator acomp;
  
  static {
    acomp = new AttributeComparator();
  }
    
  public IdEntry() {
	entry = new TreeMap<String, AVP>(acomp);
  }
  
  public void addAv(AVP avp) {
    entry.put(avp.getAttribute().getName(), avp);
  }

  public Iterable<? extends AVP> getAll() {
    return entry.values();
  }

  public AVP getAvp(String attribute) {
    return entry.get(attribute);
  }

  public AVP getAvp(Attribute attribute) {
    return entry.get(attribute.getName());
  }

  public void addAv(List<server.id.sync.messages.v1.Attribute> attributes) {
    for (server.id.sync.messages.v1.Attribute attr : attributes) {
      log.debug("Adding attribute " + attr.getName());
      IdAttribute attr = new IdAttribute(attr.getName());
      IdAVP avp = new IdAVP(attr);
      if (attr.getBinaryValues() != null && attr.getBinaryValues().size() > 0) {
        for (byte[] value : attr.getBinaryValues()) {
          avp.addValue(value);
          log.trace("Added binary " + Util.byteArrayToHexString(value) + " to attribute " + attr.getName());
        }
      } else if (attr.getStringValues() != null && attr.getStringValues().size() > 0) {
        for (String value : attr.getStringValues()) {
          avp.addValue(value);
          log.trace("Added " + value + " to attribute " + attr.getName());
        }
      } else {
        log.debug("Attribute has no values");
      }
      entry.put(attr.getName(), avp);
    }
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
  
}
