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
package server.id.sync.server;

import server.id.sync.messages.v1.ChangeRequest;
import server.id.sync.messages.v1.ChangeResponse;

public class ChangeResult {
  private long version;
  private byte[] connectorUuid;
  private long connectorMajorVersion;
  private long connectorMinorVersion;
  private long changeNumber;
  private long changeFragmentNumber;
  private Result result;
  private String description;
  
  public ChangeResult(ChangeResponse response) {
   version = response.getVersion();
   connectorUuid = response.getConnectorUuid();
   connectorMajorVersion = response.getConnectorMajorVersion();
   connectorMinorVersion = response.getConnectorMinorVersion();
   changeNumber = response.getChangeNumber();
   changeFragmentNumber = response.getChangeFragmentNumber();
   result = Result.valueOf(response.getResult().name());
   description = response.getDescription();
  }
  
  public ChangeResult(ChangeRequest request, Result result, String description) {
   version = request.getVersion();
   connectorUuid = request.getConnectorUuid();
   connectorMajorVersion = request.getConnectorMajorVersion();
   connectorMinorVersion = request.getConnectorMinorVersion();
   changeNumber = request.getChangeNumber();
   changeFragmentNumber = request.getChangeFragmentNumber();
   this.result = result;
   this.description = description;
  }

  public ChangeResponse getChangeResponse() {
    ChangeResponse cr = new ChangeResponse();
    
    cr.setVersion(version);
    cr.setConnectorUuid(connectorUuid);
    cr.setConnectorMajorVersion(connectorMajorVersion);
    cr.setConnectorMinorVersion(connectorMinorVersion);
    cr.setChangeNumber(changeNumber);
    cr.setChangeFragmentNumber(changeFragmentNumber);
    cr.setResult(server.id.sync.messages.v1.Result.valueOf(result.name()));
    cr.setDescription(description);
    
    return cr;
  }
  
  public byte[] getConnectorUuid() {
    return connectorUuid;
  }

  public long getConnectorMajorVersion() {
    return connectorMajorVersion;
  }

  public long getConnectorMinorVersion() {
    return connectorMinorVersion;
  }

  public long getChangeNumber() {
    return changeNumber;
  }

  public long getChangeFragmentNumber() {
    return changeFragmentNumber;
  }

  public Result getResult() {
    return result;
  }

  public String getDescription() {
    return description;
  }
  
  public long getVersion() {
    return version;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setResult(Result result) {
    this.result = result;
  }
}
