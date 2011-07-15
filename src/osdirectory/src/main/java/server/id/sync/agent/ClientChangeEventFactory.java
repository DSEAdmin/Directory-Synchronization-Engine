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
package server.id.sync.agent;

import server.id.AVP;
import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.sync.ChangeEvent;
import server.id.sync.messages.v1.Attribute;
import server.id.sync.messages.v1.ChangeDef;
import server.id.sync.messages.v1.ChangeRequest;
import server.id.sync.messages.v1.ChangeType;


public class ClientChangeEventFactory {
  
  private void setAVP(ChangeDef cd, Iterable<? extends AVP> avps) {
    for(AVP avp : avps) {
      Attribute attr = new Attribute();
      attr.setName(avp.getAttribute().getName());
      for(Object val : avp.getValues()) {
        if (val instanceof byte[]) {
          attr.getBinaryValues().add((byte[])val);
        } else {
          attr.getStringValues().add(val.toString());
        }
      }
      cd.getAttributes().add(attr);
    }    
  }
  
  private void setAVP(ChangeDef cd, String name, byte[] value) {
    Attribute attr = new Attribute();
    attr.setName(name);
    attr.getBinaryValues().add(value);
    cd.getAttributes().add(attr);
  }
  
  @SuppressWarnings("unused")
  private void setAVP(ChangeDef cd, String name, String value) {
    Attribute attr = new Attribute();
    attr.setName(name);
    attr.getStringValues().add(value);
    cd.getAttributes().add(attr);    
  }
  
  private void setAVP(ChangeDef cd, Entry entry) {
    setAVP(cd, entry.getAll());
  }
    
  public ChangeRequest createChangeRequestV1(Connector connector, long version, long fragmentNumber, boolean delta) {
    ChangeRequest changeRequest = new ChangeRequest();
    changeRequest.setVersion(version);
    changeRequest.setConnectorUuid(connector.getUuid());
    changeRequest.setConnectorMajorVersion(connector.getMajorVersion());
    changeRequest.setConnectorMinorVersion(connector.getMinorVersion());
    changeRequest.setChangeNumber(connector.getChangeNumber());
    changeRequest.setChangeFragmentNumber(fragmentNumber);
    changeRequest.setMoreFragments(false);
    changeRequest.setDelta(delta);
    return changeRequest;
  }
  
  public ChangeDef getChangeDefV1FromChangeEvent(ChangeEvent change) {
    ChangeDef cd = new ChangeDef();
    cd.setChangeType(ChangeType.valueOf(change.getChangeType().name()));
    setAVP(cd, change.getRemoteObj().getEntry());
    setAVP(cd, AttributeVirtualization.DV_ENTRY, change.getDvEntry().getUuid());
    return cd;
  }

  public ChangeDef getChangeDefV1FromChangeEvent(server.id.sync.ChangeEvent.ChangeType changeType,
      Iterable<? extends AVP> avps) {
    ChangeDef cd = new ChangeDef();
    cd.setChangeType(ChangeType.valueOf(changeType.name()));
    setAVP(cd, avps);
    return cd;
  }
}
