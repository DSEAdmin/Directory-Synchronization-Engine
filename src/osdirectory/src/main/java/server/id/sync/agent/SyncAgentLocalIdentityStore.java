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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.IdObject;
import server.id.IdObjectType;
import server.id.Util;
import server.id.dao.LocalIdentityStore;
import server.id.ldap.LdapUtils;
import server.id.sync.ChangeEvent.ChangeType;
import server.id.sync.agent.domain.DirectoryObject;
import server.id.sync.agent.domain.Member;

import com.unboundid.ldap.sdk.LDAPException;

public class SyncAgentLocalIdentityStore implements LocalIdentityStore {

  private final Log log = LogFactory.getLog(getClass());
  private HibernateTemplate hibernateTemplate;
  public void setHibernateTemplate(HibernateTemplate template) {
    this.hibernateTemplate = template;
  }

  public Connector refreshConnector(Connector connector) throws DataAccessException {
    log.trace("refreshConnector");
    try {
//      hibernateTemplate.refresh(connector);
      hibernateTemplate.load(connector, connector.getId());
      return connector;
    } catch (DataAccessException ex) {
      log.warn("DataAccessException: " , ex);
      throw ex;
    }
  }

  public List<? extends Connector> getConnectors(Account account)  throws DataAccessException {
    log.trace("getConnectors");
    try {
      @SuppressWarnings("unchecked")
      List<server.id.sync.agent.domain.Connector> connectors = 
        hibernateTemplate.findByNamedQuery("server.id.sync.agent.domain.Connector.All");
      log.debug("returning " + connectors.size() + " connectors");
      return connectors;
    } catch (DataAccessException ex) {
      log.warn("DataAccessException:", ex);
      throw ex;
    }
  }

  public Connector getConnectorByName(Account account, String name) throws DataAccessException {
    log.trace("getConnectorsByName");
    try {
      @SuppressWarnings("unchecked")
      List<server.id.sync.agent.domain.Connector> connectors = 
        hibernateTemplate.findByNamedQueryAndNamedParam("server.id.sync.agent.domain.Connector.byName",
            "name", name);
      if (connectors.size() == 1)
        return connectors.get(0);
      else
        return null;
    } catch (DataAccessException ex) {
      log.warn("DataAccessException:", ex);
      throw ex;
    }    
  }
  
  public Connector saveConnector(Account account, Connector connector) throws DataAccessException {
    try {
      log.trace("saveConnector");
      server.id.sync.agent.domain.Connector c = 
        new server.id.sync.agent.domain.Connector(connector);
      Long id = (Long) hibernateTemplate.save(c);
      log.debug("Saved connector " + c.getName());
      return (Connector)hibernateTemplate.get(server.id.sync.agent.domain.Connector.class, id);
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;
    }
  }

  public void deleteConnector(Account account, Connector connector) throws DataAccessException {
    try {
      log.trace("deleteConnector");
      server.id.sync.agent.domain.Connector c = 
        (server.id.sync.agent.domain.Connector)
        hibernateTemplate
      .get(server.id.sync.agent.domain.Connector.class, connector.getId());       
      if (c == null) {
        log.warn("Cannot delete connector that does not exist : " + connector.getName());
      }
      hibernateTemplate.delete(c);
      log.debug("Deleted connector " + c.getName());
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;
    }
  }

  public Connector updateConnector(Account account, Connector connector) throws DataAccessException {
    try {
      log.trace("updateConnector");
      server.id.sync.agent.domain.Connector c = 
        (server.id.sync.agent.domain.Connector)
        hibernateTemplate
      .get(server.id.sync.agent.domain.Connector.class, connector.getId());       
      if (c == null) {
        log.warn("Cannot update connector that does not exist : " + connector.getName());
      }

      hibernateTemplate.deleteAll(c.getDvEntries());
      hibernateTemplate.deleteAll(c.getRvEntries());
      c.setRvEntries(null); //needed so that deleted entries are not resaved on flush
      c.setDvEntries(null);
      hibernateTemplate.flush(); //needed before evict
      hibernateTemplate.evict(c); //needed for update. 
      server.id.sync.agent.domain.Connector newConnector = 
        new server.id.sync.agent.domain.Connector(connector);
      newConnector.setId(c.getId());
      newConnector.setMajorVersion(c.getMajorVersion());
      newConnector.setMinorVersion(0);
      hibernateTemplate.update(newConnector);
      resetStateToInitial(account, newConnector);
      log.debug("Updated connector");
      return newConnector;
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;      
    }
  }

  public Connector updateConnectorRvEntry(Account account, Connector connector, IdObject localObj,
      IdObject remoteObj, ChangeType changeType) {
    boolean modified = false;
    String newdn = remoteObj.getDn();
    Set<? extends RoleVirtualizationEntry> rvEntries = connector.getRvEntries();
    Iterator<? extends RoleVirtualizationEntry> it = rvEntries.iterator();
    while (it.hasNext()) {
      server.id.sync.agent.domain.RoleVirtualizationEntry rvEntry = 
        (server.id.sync.agent.domain.RoleVirtualizationEntry) it.next();
      if (Arrays.equals(remoteObj.getUUID(), rvEntry.getRemoteRole().getUuid())) {
        if (changeType == ChangeType.RENAME_MODIFY) {
          rvEntry.setRemoteRole(newdn);
          hibernateTemplate.update(rvEntry);
          modified = true;
        } else if (changeType == ChangeType.DELETE) {
          it.remove();
          hibernateTemplate.delete(rvEntry);
          modified = true;
        }
      }   
    }
    
    if (modified == true) {
      server.id.sync.agent.domain.Connector c = 
        (server.id.sync.agent.domain.Connector) connector;
      c.setMinorVersion(c.getMinorVersion() + 1);
      hibernateTemplate.update(c);
    }
    
    return connector;
  }

  public Connector updateConnectorDvEntry(Account account, Connector connector, IdObject localObj, IdObject remoteObj,
      ChangeType changeType) {
    boolean modified = false;
    String newdn = remoteObj.getDn();
    Set<? extends DitVirtualizationEntry> dvEntries = connector.getDvEntries();
    Iterator<? extends DitVirtualizationEntry> it = dvEntries.iterator();
    while (it.hasNext()) {
      server.id.sync.agent.domain.DitVirtualizationEntry dvEntry = 
        (server.id.sync.agent.domain.DitVirtualizationEntry) it.next();
      if (Arrays.equals(remoteObj.getUUID(), dvEntry.getRemoteContainer().getUuid())) {
        if (changeType == ChangeType.RENAME_MODIFY) {
          dvEntry.setRemoteContainer(newdn);
          hibernateTemplate.update(dvEntry);
          modified = true;
        } else if (changeType == ChangeType.DELETE) {
          it.remove();
          hibernateTemplate.delete(dvEntry);
          modified = true;
        }
      }   
    }
    
    if (modified == true) {
      server.id.sync.agent.domain.Connector c = 
        (server.id.sync.agent.domain.Connector) connector;
      c.setMinorVersion(c.getMinorVersion() + 1);
      hibernateTemplate.update(c);
    }
    
    return connector;    
  }

  public void resetStateToInitial(Account account, Connector connector) throws DataAccessException {
    try {
      log.trace("resetStateToInitial");
      log.debug("looking up connector by name");
      server.id.sync.agent.domain.Connector c = (server.id.sync.agent.domain.Connector) hibernateTemplate
          .get(server.id.sync.agent.domain.Connector.class, connector.getId());

      if (c == null) {
        log.warn("resetStateToInitial: could not find connector " + connector.getName());
      } else {
        log.debug("Found connector " + c.getName());
        c.setUserCookie(null);
        c.setGroupCookie(null);
        c.setContainerCookie(null);
        c.setSyncStatus(SyncStatus.FULL_SYNC_IN_PROGRESS);
//        c.setChangeNumber(0); //Don't reset the change number
        log.debug("Set cookie to null");
        log.debug("deleting all directory objects related to this connector");
        //I could also use bulkUpdate call
        @SuppressWarnings("unchecked")
        List list = hibernateTemplate.findByNamedQueryAndNamedParam(
            "server.id.sync.agent.domain.DirectoryObject.all", new String[] { "cid" }, new Object[] {
                connector.getId() });
        hibernateTemplate.deleteAll(list);
        log.debug("Deleted all directory objects related to this connector");
        hibernateTemplate.update(c);
        log.debug("deleted all directory objects");
      }
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;
    }
  }

  public Connector setSyncState(Account account, Connector connector, 
      byte[] userCookie, byte[] groupCookie, byte[] containerCookie) throws DataAccessException {
    try {
      log.trace("setCookie");
      server.id.sync.agent.domain.Connector c = 
        (server.id.sync.agent.domain.Connector)
        hibernateTemplate
      .get(server.id.sync.agent.domain.Connector.class, connector.getId());       
      if (c == null) {
        log.warn("Cannot setCookie for connector that does not exist : " + connector.getName());
      }
      c.setUserCookie(userCookie);
      c.setGroupCookie(groupCookie);
      c.setContainerCookie(containerCookie);
      c.setChangeNumber(c.getChangeNumber() + 1);
      hibernateTemplate.update(c);
      log.debug("setCookie for connector " + c.getName());
      return c;
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;      
    }    
  }

  public void setOperationResult(OperationType opType, OperationResult opResult, String resultString, 
      long numChangesFromIds, long numChangesToSvc, Connector connector) throws DataAccessException {
    try {
      log.trace("DataAccessException");
      server.id.sync.agent.domain.Connector c = (server.id.sync.agent.domain.Connector) connector;
      server.id.sync.agent.domain.OperationResult or = 
        new server.id.sync.agent.domain.OperationResult(opType, opResult, resultString, numChangesFromIds, numChangesToSvc, c);
      hibernateTemplate.save(or);
      log.debug("Saved operation result");
    } catch (DataAccessException e) {
      log.warn("DataAccessExceptin", e);
      throw e;
    }
  }
  
  public Connector setSyncStatus(Account account, Connector connector, 
      SyncStatus syncStatus) throws DataAccessException {
    try {
      log.trace("setSyncStatus");
      server.id.sync.agent.domain.Connector c = 
        (server.id.sync.agent.domain.Connector)
        hibernateTemplate
      .get(server.id.sync.agent.domain.Connector.class, connector.getId());       
      if (c == null) {
        log.warn("Cannot set sync status for connector that does not exist : " + connector.getName());
      }
      c.setSyncStatus(syncStatus);
      hibernateTemplate.update(c);
      log.debug("set SyncStatus for connector " + c.getName());
      return c;
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;      
    }        
  }

  public Entry findLocalObject(Account account, Connector connector, IdObject remoteObj) throws DataAccessException {
    log.trace("findLocalObject");
    log.debug("Trying to find local object for " + remoteObj.getDn());
    return findLocalObject(account, connector, remoteObj.getUUID());
  }

  public Entry findLocalObject(Account account, Connector connector, byte[] uuid) throws DataAccessException {
    try {
      log.trace("findLocalObject");
      log.debug("Trying to find local object for " + Util.uuidToString(uuid));
      String base64Uuid = Util.base64Encode(uuid);
      @SuppressWarnings("unchecked")
      List list = hibernateTemplate.findByNamedQueryAndNamedParam(
          "server.id.sync.agent.domain.DirectoryObject.byUuid", new String[] { "uuid", "cid" }, new Object[] {
              base64Uuid, connector.getId() });
      if (list == null || list.size() == 0) {
        log.debug("No users found");
        return null;
      } else {
        if (list.size() > 1) {
          log.warn("More than one entry found with the uuid for object " + Util.uuidToString(uuid));
          return null;
        }
        log.debug("Found local object for " + Util.uuidToString(uuid));
        return (Entry) list.get(0);
      }
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;
    }
  }

  public Entry findLocalObject(Account account, Connector connector, String ndn) throws DataAccessException {
    try {
      log.trace("findLocalObject");
      log.debug("Trying to find local object for " + ndn);
      @SuppressWarnings("unchecked")
      List list = hibernateTemplate.findByNamedQueryAndNamedParam(
          "server.id.sync.agent.domain.DirectoryObject.byNdn", 
          new String[] { "ndn", "cid" }, new Object[] {
              ndn, connector.getId() });
      if (list == null || list.size() == 0) {
        log.debug("No users found");
        return null;
      } else {
        if (list.size() > 1) {
          log.warn("More than one entry found with the DN for object " + ndn);
          return null;
        }
        log.debug("Found local object for " + ndn);
        return (Entry) list.get(0);
      }
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;
    }
  }

  public List<byte[]> getSynchronizedUsersUuid(Account account, Connector connector) throws DataAccessException {
    try {
      log.trace("getSynchronizedUsersUuid");
      List<byte[]> users = new LinkedList<byte[]>();
      @SuppressWarnings("unchecked")
      List list = hibernateTemplate.findByNamedQueryAndNamedParam
        ("server.id.sync.agent.domain.DirectoryObject.syncedAll", 
            "cid", connector.getId());
      for (Object o : list) {
        DirectoryObject dirObj = (DirectoryObject)o;
        users.add(dirObj.getUuid());
      }
      return users;
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;
    }
  }

  public List<? extends Entry> getChildren(Account account, Connector connector, IdObject object) throws DataAccessException {
    try {
      log.trace("getChildren");
      log.debug("Trying to find children for DN : " + object.getNormalizedDn());
      @SuppressWarnings("unchecked")
      List<Entry> list = hibernateTemplate.findByNamedQueryAndNamedParam(
          "server.id.sync.agent.domain.DirectoryObject.Children", new String[] {"cid", "base"}, new Object[] {
              connector.getId(), "%_" + object.getNormalizedDn()});
      log.debug("Returning " + list.size() + " object as childre for DN : " + object.getNormalizedDn());
      return list;
    } catch (DataAccessException e) {
      log.warn("DataAccessException", e);
      throw e;
    }
  }

  public void deleteIdObject(Account account, Connector connector, IdObject object) throws DataAccessException {
    log.trace("deleteIdObject");
    DirectoryObject obj = (DirectoryObject)findLocalObject(account, connector, object);
    if (obj == null) {
      log.warn("Did not delete IdObject as it could not be found" + object.getDn());      
    } else {
      deleteDirObject(account, connector, obj);
    }
  }

  private void deleteDirObject(Account account, Connector connector, DirectoryObject obj) {
    try {
      if (obj != null) {
        hibernateTemplate.delete(obj);
        if (obj.getType() == IdObjectType.GROUP) {
          removeMembers(account, connector, obj);          
        }
        log.debug("Deleted IdObject " + obj.getDn());
      } else {
        log.warn("Did not delete IdObject as it could not be found");
      }
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;
    }    
  }

  @SuppressWarnings("unchecked")
  public void deleteChildren(Account account, Connector connector, IdObject object) throws DataAccessException {
    try {
      log.trace("deleteChildren");
      List<DirectoryObject> list = (List<DirectoryObject>) getChildren(account, connector, object);
      log.debug("Deleting children of " + object.getNormalizedDn());
      for ( DirectoryObject dObj : list) {
        deleteDirObject(account, connector, dObj);
      }
    } catch (DataAccessException e) {
      log.warn("DataAccessException", e);
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  public Entry saveIdObject(Account account, Connector connector, 
      DitVirtualizationEntry dvEntry, IdObject object) throws DataAccessException {
    server.id.sync.agent.domain.Connector c = null;
    try {
      c = (server.id.sync.agent.domain.Connector) hibernateTemplate.get(
          server.id.sync.agent.domain.Connector.class, connector.getId());
      if (c == null) {
        log.warn("Could not find connector " + connector.getName() + ". Could not save object " + object.getDn());
        return null;
      }
      DirectoryObject dirObject = DirectoryObject.getDirectoryObject(c, 
          (server.id.sync.agent.domain.DitVirtualizationEntry) dvEntry, object);
      Long id = (Long)hibernateTemplate.save(dirObject);
      log.debug("Saved directory object " + object.getDn());
      if (object.getObjectType() == IdObjectType.GROUP) {
        Iterable<String> members = (Iterable<String>)object.getAttributeValues(AttributeVirtualization.MEMBER);
        addMembers(account, connector, dirObject, members);
      }
      return (Entry)hibernateTemplate.get(server.id.sync.agent.domain.DirectoryObject.class, id);
    } catch (DataAccessException ex) {
      log.warn("DataAccessException: ", ex);
      throw ex;
    }
  }

  /* object may only have the changes and objectuuid. So we need to preserve whats not in object
   * and update whats in object. An attribute with no values means taht attribute has been removed
   * (non-Javadoc)
   * @see server.id.dao.LocalIdentityStore#updateIdObject(server.id.dao.Connector, server.id.IdObject)
   */
  @SuppressWarnings("unchecked")
  public Entry updateIdObject(Account account, Connector connector, DitVirtualizationEntry dvEntry, IdObject object) throws DataAccessException {
    try {
      log.trace("updateIdObject");
      DirectoryObject obj = (DirectoryObject)findLocalObject(account, connector, object);
      if (obj != null) {
        server.id.sync.agent.domain.Connector c = 
          (server.id.sync.agent.domain.Connector)
          hibernateTemplate
        .get(server.id.sync.agent.domain.Connector.class, connector.getId());       
        if (c == null) {
          log.warn("Could not find connector " + connector.getName() + ". Could not update object " + object.getDn());
          return null;
        }
        /*
        hibernateTemplate.deleteAll(obj.getAttributes());
        obj.setAttributes(null);
        DirectoryObject dirObject = DirectoryObject.getDirectoryObject(c, object);
        dirObject.setId(obj.getId());
        hibernateTemplate.flush();
        hibernateTemplate.evict(obj);
        hibernateTemplate.update(dirObject);
        log.debug("Updated idObject " + object.getDn());
        */
        obj.setDn(object.getDn());
        obj.setNormalizedDn(object.getNormalizedDn());
        obj.setDvEntry((server.id.sync.agent.domain.DitVirtualizationEntry) dvEntry);
        if (object.getParentUUID() != null) {
          obj.setParentUuid(object.getParentUUID());
        }
        if (object.getObjectType() == IdObjectType.GROUP) {
          if (object.getAVP(AttributeVirtualization.MEMBER) != null) {
            Iterable<String> members = (Iterable<String>)object.getAttributeValues(AttributeVirtualization.MEMBER);
            updateMembers(account, connector, obj, members);
          }
        }
        hibernateTemplate.update(obj);
        return obj;
      } else {
        log.warn("Did not update IdObject as it could not be found" + object.getDn());
        return null;
      }
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;      
    }    
  }
  
  public Entry updateIdObjectDvEntry(Account account, Connector connector, byte[] uuid, DitVirtualizationEntry dvEntry) throws DataAccessException {
    log.trace("updateIdObject");
    try {
      DirectoryObject obj = (DirectoryObject) findLocalObject(account, connector, uuid);
      if (obj != null) {
        obj.setDvEntry((server.id.sync.agent.domain.DitVirtualizationEntry) dvEntry);
        hibernateTemplate.update(obj);
        return obj;
      } else {
        log.warn("updateIdObjectDvEntry: Local object with uuid " + Util.uuidToString(uuid) + " not found.");
        return null;
      }
    } catch (DataAccessException e) {
      log.warn("DataAccessException:", e);
      throw e;
    }
  }

  
  public List<Member> getMembers(Account account, Connector connector, IdObject group) throws DataAccessException {
    log.trace("getMembers");
    log.debug("Trying to find memboer for DN : " + group.getDn());
    DirectoryObject gEntry = (DirectoryObject) findLocalObject(account, connector, group.getUUID());
    if (gEntry == null) {
      return null;
    }
    return getMembers(account, connector, gEntry);
  }

  private List<Member> getMembers(Account account, Connector connector, DirectoryObject group) throws DataAccessException {
    try {
      log.trace("getMembers");
      
      @SuppressWarnings("unchecked")
      List<Member> list = hibernateTemplate.findByNamedQueryAndNamedParam(
          "server.id.sync.agent.domain.Member.member", new String[] { "group" },
          new Object[] {group});
      log.debug("Returning " + list.size() + " object as member for DN : " + group.getDn());
      return list;
    } catch (DataAccessException e) {
      log.warn("DataAccessException", e);
      throw e;
    }
  }    

  public List<? extends Entry> getMemberOf(Account account, Connector connector, String normalizedDn)
      throws DataAccessException {
    try {
      log.trace("getMemberOf");
      log.debug("Trying to find memboerOf for DN : " + normalizedDn);
      @SuppressWarnings("unchecked")
      List<Entry> list = hibernateTemplate.findByNamedQueryAndNamedParam(
          "server.id.sync.agent.domain.Member.memberOf", new String[] { "cid", "ndn" }, new Object[] {
              connector.getId(), normalizedDn });
      log.debug("Returning " + list.size() + " object as memberOf for DN : " + normalizedDn);
      return list;
    } catch (DataAccessException e) {
      log.warn("DataAccessException", e);
      throw e;
    }
  }

  private void addMembers(Account account, Connector connector, DirectoryObject group, Iterable<String> members) {
    log.trace("addMembers");    
    if (members == null)
      return;
    
    for ( String member : members) {
      String ndn = null;
      try {
        ndn = LdapUtils.normalizeDn(member);
      } catch (LDAPException e) {
        log.warn("LDAPException:", e);
        continue;
      }
      log.debug("Adding member " + ndn + " to group " + group.getDn() + " in Agent DB");
      Member m = new Member(group, ndn);
      hibernateTemplate.save(m);
    }
  }

  private void removeMembers(Account account, Connector connector, DirectoryObject obj) {
    List<Member> oldMemList = getMembers(account, connector, obj);
    if (oldMemList == null)
      return;

    Set<String> oldMemSet = new HashSet<String>();
    for (Member m : oldMemList) {
      oldMemSet.add(m.getNormalizedDn());
    }
    removeMembers(account, connector, obj, oldMemSet);
  }

  private void removeMembers(Account account, Connector connector, DirectoryObject group, Iterable<String> members) {    
    if (members == null)
      return;
    
    for (String member : members) {
      hibernateTemplate.bulkUpdate("delete from Member as m where m.group = ? and m.normalizedDn = ?", 
          new Object[] {group, member});
    }
  }
  
  private void updateMembers(Account account, Connector connector, DirectoryObject group, Iterable<String> currMemList) {
    log.trace("updateMembers");

    try {
      List<Member> oldMemList = getMembers(account, connector, group);
      Set<String> oldMemSet = new HashSet<String>();
      if (oldMemList != null) {
        for (Member m : oldMemList) {
          oldMemSet.add(m.getNormalizedDn());
        }
      }

      Set<String> currMemSet = new HashSet<String>();
      if (currMemList != null) {
        for (String member : currMemList) {
          try {
            String ndn = LdapUtils.normalizeDn(member);
            currMemSet.add(ndn);
          } catch (LDAPException e) {
            log.warn("LDAPException:", e);
            continue; // this should never happen
          }
        }
      }

      Set<String> newMembers = new HashSet<String>(currMemSet);
      newMembers.removeAll(oldMemSet); // Set operation newMemSet - oldMemSet

      Set<String> removedMembers = new HashSet<String>(oldMemSet);
      removedMembers.removeAll(currMemSet); // Set operation oldMemSet -
                                            // newMemSet

      // add new members
      addMembers(account, connector, group, newMembers);

      // delete deleted members
      removeMembers(account, connector, group, removedMembers);
    } catch (DataAccessException e) {
      log.warn("DataAccessException", e);
      throw e;
    }
  }

  public Object getDelegate() {
    return hibernateTemplate;
  }
}
