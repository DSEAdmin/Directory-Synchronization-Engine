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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RunHistory {
  public static class RunInfo {
    private Date startTime;
    private Date endTime;
    private String runType;
    private boolean success;
    private Throwable exception;
    private String errorDescription;

    public RunInfo() {
      super();
    }

    public Date getStartTime() {
      return startTime;
    }

    public void setStartTime(Date startTime) {
      this.startTime = startTime;
    }

    public Date getEndTime() {
      return endTime;
    }

    public void setEndTime(Date endTime) {
      this.endTime = endTime;
    }

    public String getRunType() {
      return runType;
    }

    public void setRunType(String runType) {
      this.runType = runType;
    }

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public Throwable getException() {
      return exception;
    }

    public void setException(Throwable exception) {
      this.exception = exception;
    }

    public String getErrorDescription() {
      return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
      this.errorDescription = errorDescription;
    }
  }
  
  private static final int HIST_SIZE = 20;
  private List<RunInfo> history;
  private int errorCount;
  
  public RunHistory() {
    super();
    history = Collections.synchronizedList(new LinkedList<RunInfo>());
    errorCount = 0;
  }
  
  synchronized public void add(RunInfo ri) {
    if (ri.isSuccess() == true) {
      errorCount = 0;
    } else {
      errorCount++;
    }
    if (history.size() == HIST_SIZE) {
      history.remove(0); //remove the oldest element
    }
    history.add(ri); //adds to the end of the list
  }
  
  synchronized public int getErrorCount() {
    return errorCount;
  }
}
