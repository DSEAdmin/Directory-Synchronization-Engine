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
package server.id.ldap.ldif;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.mail.MethodNotSupportedException;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.AttributeVirtualization;
import server.id.IdObject;
import server.id.IdObjectFactory;
import server.id.IdObjectType;
import server.id.dao.ChangePoller;
import server.id.dao.InvalidConfigurationException;
import server.id.dao.LocalIdentityStore;
import server.id.dao.State;
import server.id.ldap.DirectorySpec;
import server.id.ldap.ExcludedEntries;
import server.id.ldap.LdapEntry;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFEntrySource;
import com.unboundid.ldif.LDIFReader;

public class LdifChangePoller implements ChangePoller {
  private final Log log = LogFactory.getLog(getClass());
  private State state;
  private DirectorySpec ds;
  private AttributeVirtualization av;
  private Connector connector;  
  private LocalIdentityStore lIdStore;
  private IdObjectFactory idObjectFactory;
  private String ldifFile;
  private ExcludedEntries ee;
  
  public void setAv(AttributeVirtualization av) {
    this.av = av;
  }
  public void setConnector(Connector connector) {
    this.connector = connector;
  }
  public void setLIdStore(LocalIdentityStore lIdStore) {
    this.lIdStore = lIdStore;
  }
  public void setIdObjectFactory(IdObjectFactory idObjectFactory) {
    this.idObjectFactory = idObjectFactory;
  }
  public void setDirSpec(DirectorySpec ds) {
    this.ds = ds;
  }
  public void setLdifFile(String ldifFile) {
    this.ldifFile = ldifFile;
  }
  public void init() {
    state = State.POLL;
    this.ee = new ExcludedEntries(connector, ds);
  }

  
  public Connector getConnector() {
    return connector;
  }

  public Connector refreshConnector() {
    return lIdStore.refreshConnector(connector);
  }

  public List<IdObject> getNextChangeSet() throws Exception {
    List<IdObject> objects = new LinkedList<IdObject>();
    LDIFEntrySource entrySource = new LDIFEntrySource(
        new LDIFReader(ldifFile));

    for (Entry entry = entrySource.nextEntry(); entry != null; entry = entrySource.nextEntry()) {
      log.debug("getNextChange: mapping : " + entry.getDN());
      LdapName entryName;
      try {
        entryName = new LdapName(entry.getDN());
      } catch (InvalidNameException e1) {
        log.error("Returned entry has invalid name" + e1);
        continue;
      }
      if (ee.isSystemBase(entryName)) {
        log.debug("Skipping system base : " + entry.getDN());
        continue;
      }
      if (ee.isSystemEntry(entryName)) {
        log.debug("Skipping system entry : " + entry.getDN());
        continue;
      }
      
      LdapEntry le = new LdapEntry(entry);

      server.id.Entry e = av.virtualize(LdifChangePoller.this, le);

      IdObject idObj = idObjectFactory.create(connector, e);

      if (idObj == null) {
        log.debug("doMapFromContext: null IdObject");        
        continue;
      } else {
        log.debug("Created object DN: " + idObj.getDn() != null ? idObj.getDn() : "null");
      }
      
      if (log.isTraceEnabled()) {
        log.trace(e);
      }

      objects.add(idObj);
    }
    return objects;
  }

  private List<IdObject> filter(List<IdObject> objs, IdObjectType type)  {
    if (objs != null) {
      Iterator<IdObject> it = objs.iterator();
      while (it.hasNext()) {
        IdObject obj = it.next();
        if (obj.getObjectType() != type) {
          it.remove();
        }
      }
    }
    return objs;
  }
  
  public List<IdObject> getNextContainerChangeSet() throws Exception {
    return filter(getNextChangeSet(), IdObjectType.CONTAINER);
  }
 
  public List<IdObject> getNextGroupChangeSet() throws Exception {
    return filter(getNextChangeSet(), IdObjectType.GROUP);
  }
  
  public List<IdObject> getNextUserChangeSet() throws Exception {
    return filter(getNextChangeSet(), IdObjectType.PERSON);
  }
  
  public IdObject getCompleteObject(String objectId) throws Exception {
    throw new MethodNotSupportedException("LdifChangePoller does not support getGroupMembership");
  }
  
  public void resetStateToInitial() {
  }

  public void rollbackState() {
  }

  public void saveStateToDB() {
  }

  synchronized public void saveOperationResult(OperationType opType, OperationResult opResult, String resultString, 
      long numChangesFromIds, long numChangesToSvc) throws Exception {
    lIdStore.setOperationResult(opType, opResult, resultString, numChangesFromIds, numChangesToSvc, connector);
  }

  public Account getAccount() {
    return null;
  }

  public State getContainerState() {
    return state;
  }

  public State getGroupState() {
    return state;
  }

  public State getUserState() {
    return state;
  }

  public boolean validateConfiguration() throws MethodNotSupportedException,
      InvalidConfigurationException {
    throw new MethodNotSupportedException("validateConfiguration not supported by LdifChangePoller");  
  }

  public Iterable<? extends Object> getGroupMembership(String objectId) throws Exception {
    throw new MethodNotSupportedException("LdifChangePoller does not support getGroupMembership");
  }
}
