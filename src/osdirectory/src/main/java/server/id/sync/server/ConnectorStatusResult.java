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

import server.id.sync.messages.v1.ConnectorStatusResponse;

public class ConnectorStatusResult {
  private long version;
  private byte[] connectorUuid;
  private long majorVersionNumber;
  private long minorVersionNumber;
  private Result result;
  private String errorString;
  
  public ConnectorStatusResult(ConnectorStatusResponse response) {
   version = response.getVersion();
   connectorUuid = response.getConnectorUuid();
   if (response.getConnectorMajorVersion()  != null)
     majorVersionNumber = response.getConnectorMajorVersion();
   if (response.getConnectorMinorVersion() != null)
     minorVersionNumber = response.getConnectorMinorVersion();
   result = Result.valueOf(response.getResult().name());
   errorString = response.getErrorString();
  }

  public ConnectorStatusResult(Result result, String error) {
    this.version = 1;
    this.result = result;
    this.errorString = error;
  }
  
  public ConnectorStatusResponse getConnectorStatusResponse() {
    ConnectorStatusResponse response = new ConnectorStatusResponse();
    response.setConnectorMajorVersion(majorVersionNumber);
    response.setConnectorMinorVersion(minorVersionNumber);
    response.setConnectorUuid(connectorUuid);
    response.setErrorString(errorString);
    response.setResult(server.id.sync.messages.v1.Result.valueOf(result.name()));
    response.setVersion(version);
    return response;
  }
  
  public long getVersion() {
    return version;
  }

  public Result getResult() {
    return result;
  }

  public String getErrorString() {
    return errorString;
  }

  public byte[] getConnectorUuid() {
    return connectorUuid;
  }

  public long getMajorVersionNumber() {
    return majorVersionNumber;
  }

  public long getMinorVersionNumber() {
    return minorVersionNumber;
  }

  public void setResult(Result result) {
    this.result = result;
  }

  public void setErrorString(String errorString) {
    this.errorString = errorString;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public void setConnectorUuid(byte[] connectorUuid) {
    this.connectorUuid = connectorUuid;
  }

  public void setMajorVersionNumber(long majorVersionNumber) {
    this.majorVersionNumber = majorVersionNumber;
  }

  public void setMinorVersionNumber(long minorVersionNumber) {
    this.minorVersionNumber = minorVersionNumber;
  }

}
