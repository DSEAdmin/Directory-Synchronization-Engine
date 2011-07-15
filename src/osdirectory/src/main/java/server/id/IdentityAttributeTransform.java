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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.dao.ChangePoller;


public class IdentityAttributeTransform implements AttributeTransform {
  private final Log log = LogFactory.getLog(getClass());
  private RemoteAttribute remoteAttribute;
  
  public IdentityAttributeTransform() {
  }
  
  public void setRemoteAttribute(RemoteAttribute remoteAttribute) {
    this.remoteAttribute = remoteAttribute;
  }
  
  public List<String> getRemoteAttributes() {
    List<String> ret = new ArrayList<String>();
    ret.add(remoteAttribute.getName());
    return ret;
  }

  public List<String> getBinaryRemoteAttributes() {
    List<String> ret = new ArrayList<String>();
    if (remoteAttribute.isBinary()) {
      ret.add(remoteAttribute.getName());
    }
    return ret;
  }

  public Iterable<? extends Object> virtualize(ChangePoller changePoller, Entry remoteEntry) {
    AVP avp = remoteEntry.getAvp(remoteAttribute.getName());
    if (avp == null) {
      return null;
    }
    Iterable<? extends Object> values = avp.getValues();
    
    if (log.isTraceEnabled()) {
      log.trace("virtualize: For attribute " + remoteAttribute.getName());
      for(Object o : values) {
        if (o instanceof byte[]) {
          log.trace("\treturning binary value: " + Util.byteArrayToHexString((byte[])o));            
        } else {
          log.trace("\treturning value: " + o);
        }
      }
    }
    
    return values;
  }
}
