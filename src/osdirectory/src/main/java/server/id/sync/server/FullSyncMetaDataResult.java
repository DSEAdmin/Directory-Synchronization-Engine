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

import server.id.sync.messages.v1.FullSyncMetaDataResponse;

public class FullSyncMetaDataResult {
  private long version;
  private Result result;
  private String errorString;
  
  public FullSyncMetaDataResult(FullSyncMetaDataResponse response) {
   version = response.getVersion();
   result = Result.valueOf(response.getResult().name());
   errorString = response.getErrorString();
  }

  public FullSyncMetaDataResult(Result result, String error) {
    this.version = 1;
    this.result = result;
    this.errorString = error;
  }
  
  public FullSyncMetaDataResponse getFullSyncMetaDataResponse() {
    FullSyncMetaDataResponse response = new FullSyncMetaDataResponse();
    response.setVersion(version);
    response.setResult(server.id.sync.messages.v1.Result.valueOf(result.name()));
    response.setErrorString(errorString);
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

  public void setVersion(long version) {
    this.version = version;
  }

  public void setResult(Result result) {
    this.result = result;
  }

  public void setErrorString(String errorString) {
    this.errorString = errorString;
  }



}
