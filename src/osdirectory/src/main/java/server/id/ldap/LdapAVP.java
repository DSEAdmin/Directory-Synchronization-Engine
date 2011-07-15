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

import java.util.ArrayList;
import java.util.Collection;


import java.util.List;

import server.id.AVP;
import server.id.Attribute;
import server.id.Util;

public class LdapAVP implements AVP {
  LdapAttribute attribute;
  List<Object> values;
  
  public LdapAVP(String name) {
    attribute = new LdapAttribute(name);
    this.values = new ArrayList<Object>();
  }
  
  public LdapAVP(String name, List<Object> values) {
    attribute = new LdapAttribute(name);
    this.values = new ArrayList<Object>(values);
  }
  
  public LdapAVP(String name, Object value) {
    attribute = new LdapAttribute(name);
    this.values = new ArrayList<Object>();
    this.values.add(value);
  }
  
  public Attribute getAttribute() {
    return attribute;
  }

  public Object getValue() {
    if (values.size() > 0)
      return values.get(0);
    return null;
  }

  public Collection<Object> getValues() {
    return values;
  }

  public void addValue(Object value) {
    values.add(value);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(attribute.getName());
    sb.append("\n");
    for (Object v : values) {
      sb.append("\t");
      if (v instanceof byte[])
        sb.append(Util.byteArrayToHexString((byte[])v));
      else 
        sb.append(v.toString());
      sb.append("\n");
    }
    return sb.toString();
  }
}
