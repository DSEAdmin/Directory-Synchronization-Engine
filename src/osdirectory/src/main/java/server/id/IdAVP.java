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

import java.util.ArrayList;
import java.util.List;

import server.id.AVP;
import server.id.Attribute;


public class IdAVP implements AVP {
  private Attribute attribute;
  private List<Object> values;
  
  public IdAVP(Attribute attribute) {
	this.attribute = attribute;
	values = new ArrayList<Object>();
  }
  
  public void addValue(Object value) {
	values.add(value);
  }

  public Attribute getAttribute() {
	return attribute;
  }

  public Object getValue() {
    if (values.size() > 0)
      return values.get(0);
    return null;
  }

  public Iterable<? extends Object> getValues() {
	return (Iterable<Object>)values;
  }

}
