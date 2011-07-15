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
package server.id.sync;

import java.util.HashMap;
import java.util.Map;

import server.id.dao.ChangePoller;
import server.id.sync.server.SynchronizationService;


public interface ObligationProcessor {
  public static abstract class Callback {
    private SynchronizationService synchronizationService;
    private Connector connector;
    private boolean more;
    private boolean delta;
    private boolean enablePaging;
    private ChangePoller idDao;
    private Account account;
    private long serviceConnectorMajorVersion;
    private long serviceConnectorMinorVersion;
    private Map<byte[], DitVirtualizationEntry> updateDvMap;
    private long numEntriesFromIds;
    private long numEntriesToSvc;
    
    public SynchronizationService getSynchronizationService() {
      return synchronizationService;
    }
    public void setSynchronizationService(SynchronizationService synchronizationService) {
      this.synchronizationService = synchronizationService;
    }
    public Connector getConnector() {
      return connector;
    }
    public void setConnector(Connector connector) {
      this.connector = connector;
    }
    public boolean isMore() {
      return more;
    }
    public void setMore(boolean more) {
      this.more = more;
    }
    public boolean isDelta() {
      return delta;
    }
    public void setDelta(boolean delta) {
      this.delta = delta;
    }    
    public boolean isEnablePaging() {
      return enablePaging;
    }
    public void setEnablePaging(boolean enablePaging) {
      this.enablePaging = enablePaging;
    }
    public ChangePoller getIdDao() {
      return idDao;
    }
    public void setIdDao(ChangePoller idDao) {
      this.idDao = idDao;
    }
    public Account getAccount() {
      return account;
    }
    public void setAccount(Account account) {
      this.account = account;
    }
    public long getServiceConnectorMajorVersion() {
      return serviceConnectorMajorVersion;
    }
    public void setServiceConnectorMajorVersion(long serviceConnectorMajorVersion) {
      this.serviceConnectorMajorVersion = serviceConnectorMajorVersion;
    }
    public long getServiceConnectorMinorVersion() {
      return serviceConnectorMinorVersion;
    }
    public void setServiceConnectorMinorVersion(long serviceConnectorMinorVersion) {
      this.serviceConnectorMinorVersion = serviceConnectorMinorVersion;
    }
    public void addUpdateDv(byte[] uuid, DitVirtualizationEntry dvEntry) {
      if (updateDvMap == null) {
        updateDvMap = new HashMap<byte[], DitVirtualizationEntry>();
      }
      updateDvMap.put(uuid, dvEntry);
    }
    public Map<byte[], DitVirtualizationEntry> getUpdateDvMap() {
      return updateDvMap;
    }
    public long getNumEntriesFromIds() {
      return numEntriesFromIds;
    }
    public void setNumEntriesFromIds(long numEntriesFromIds) {
      this.numEntriesFromIds = numEntriesFromIds;
    }
    public long getNumEntriesToSvc() {
      return numEntriesToSvc;
    }
    public void setNumEntriesToSvc(long numEntriesToSvc) {
      this.numEntriesToSvc = numEntriesToSvc;
    }
    synchronized public void incrNumEntriesToSvc() {
      this.numEntriesToSvc += 1;
    }
  }
  
  public void processObligation(EventProcessingObligation obligation, ChangeEvent event, Callback cb) throws Exception;
}
