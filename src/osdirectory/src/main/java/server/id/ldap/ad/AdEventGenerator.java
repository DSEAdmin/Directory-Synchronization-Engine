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
package server.id.ldap.ad;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.AVP;
import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.Group;
import server.id.IdObject;
import server.id.IdObjectFactory;
import server.id.IdObjectType;
import server.id.Util;
import server.id.dao.ChangePoller;
import server.id.dao.LocalIdentityStore;
import server.id.ldap.LdapUtils;
import server.id.sync.ChangeEvent;
import server.id.sync.EventGenerator;

import com.unboundid.ldap.sdk.LDAPException;

public class AdEventGenerator extends EventGenerator {

  private final Log log = LogFactory.getLog(getClass());
  protected LocalIdentityStore localStore;
  protected IdObjectFactory idObjectFactory;
  
  public ChangeEvent generateEvent(ChangePoller idDao, IdObject remoteObj) {
    Entry e = localStore.findLocalObject(null, idDao.getConnector(), remoteObj);
    IdObject localObj = null;
    if (e != null) {
      localObj = idObjectFactory.create(idDao.getConnector(), e);
    }
    ChangeEvent changeEvent = null;
    if (remoteObj.getObjectType() == IdObjectType.PERSON) {
      changeEvent = generateUserEvent(idDao, localObj, remoteObj);
    } else if (remoteObj.getObjectType() == IdObjectType.GROUP) {
      Group group = (Group) remoteObj;
      changeEvent = generateGroupEvent(idDao, localObj, group);      
    } else if (remoteObj.getObjectType() == IdObjectType.CONTAINER) {
      changeEvent = generateContainerEvent(idDao, localObj, remoteObj);      
    } else {
      log.error("Unknown remote object type. Cannot generate change events");
    }
    changeEvent.setIdVirtualization(idVirtualization);
    return changeEvent;
  }

  private ChangeEvent generateObjectEvent(ChangePoller poller, IdObject localObj, IdObject remoteObj, 
      DitVirtualizationEntry dvEntry, RoleVirtualizationEntry rvEntry) {
    //Check if its under deleted objects container
    if (isTombstoneEntry(remoteObj)) {
      DitVirtualizationEntry prevDvEntry = null;
      boolean effectsInScopeObjects = false;
      if (localObj != null) {
        AVP dvAvp = localObj.getAVP(AttributeVirtualization.DV_ENTRY);
        if (dvAvp == null) {
          prevDvEntry = null;
        } else {
          byte[] dvUuid = (byte[])dvAvp.getValue();
          prevDvEntry = idVirtualization.getDvEntry(poller.getConnector().getDvEntries(), dvUuid);
        }
        effectsInScopeObjects = idVirtualization.effectsInScopeObjects(poller.getConnector().getDvEntries(), localObj);
      }
      return deleteChangeEvent(poller.getConnector(), localObj, remoteObj, dvEntry, prevDvEntry, rvEntry, effectsInScopeObjects);
    } else if (localObj == null) {//Else check if there is a local object
      return addChangeEvent(poller.getConnector(), localObj, remoteObj, dvEntry, rvEntry);
    } else {
      if (isRename(localObj, remoteObj)) { //rename and modify
        DitVirtualizationEntry prevDvEntry = null;
        AVP dvAvp = localObj.getAVP(AttributeVirtualization.DV_ENTRY);
        if (dvAvp == null) {
          prevDvEntry = null;
        } else {
          byte[] dvUuid = (byte[])dvAvp.getValue();
          prevDvEntry = idVirtualization.getDvEntry(poller.getConnector().getDvEntries(), dvUuid);
        }
        boolean effectsInScopeObjects = idVirtualization.effectsInScopeObjects(poller.getConnector().getDvEntries(), remoteObj);
        boolean prevEffectsInScopeObjects = idVirtualization.effectsInScopeObjects(poller.getConnector().getDvEntries(), localObj);
        return renameModifyChangeEvent(poller.getConnector(), localObj, remoteObj, dvEntry, prevDvEntry, rvEntry, 
            effectsInScopeObjects, prevEffectsInScopeObjects);
      } else { //Modify
        return modifyChangeEvent(poller.getConnector(), localObj, remoteObj, dvEntry, rvEntry);
      }
    }
  }  
  
  private ChangeEvent generateContainerEvent(ChangePoller poller, IdObject localObj, IdObject remoteObj) {
    DitVirtualizationEntry dvEntry = idVirtualization.isObjectMapped(poller.getConnector().getDvEntries(), remoteObj);
    return generateObjectEvent(poller, localObj, remoteObj, dvEntry, null);
  }

  private ChangeEvent generateGroupEvent(ChangePoller poller, IdObject localObj, Group remoteObj) {
    RoleVirtualizationEntry rvEntry = idVirtualization.isGroupMapped(poller
        .getConnector().getRvEntries(), remoteObj.getUUID());
    return generateObjectEvent(poller, localObj, remoteObj, null, rvEntry);
  }

  private ChangeEvent generateUserEvent(ChangePoller poller, IdObject localObj, IdObject remoteObj) {
    DitVirtualizationEntry dvEntry = idVirtualization.isObjectMapped(poller.getConnector().getDvEntries(), remoteObj);
    return generateObjectEvent(poller, localObj, remoteObj, dvEntry, null);
  }

  private ChangeEvent modifyChangeEvent(Connector connector, IdObject localObj, IdObject remoteObj, DitVirtualizationEntry dvEntry, 
      RoleVirtualizationEntry rvEntry) {
    if (log.isDebugEnabled() == true) {
      log.debug("For object " + remoteObj.getDn() + " Event : MODIFY, ELD = " + (localObj != null) + " inScope = " + (dvEntry != null) +
          " isMapped = " + (rvEntry != null));
      if (log.isTraceEnabled() == true) {
        log.trace("Remote Object details: " + remoteObj);
        log.trace("Local Object details: " + localObj != null ? localObj : "null");
      }
    }
    return new ChangeEvent(connector, ChangeEvent.ChangeType.MODIFY, remoteObj, localObj, dvEntry, null, rvEntry, null, false, false);
  }

  private ChangeEvent renameModifyChangeEvent(Connector connector, IdObject localObj, IdObject remoteObj, DitVirtualizationEntry dvEntry, DitVirtualizationEntry prevDvEntry, 
      RoleVirtualizationEntry rvEntry, boolean effectsInScopeObjects, boolean prevEffectsInScopeObjects) {
    if (log.isDebugEnabled() == true) {
      log.debug("For object " + remoteObj.getDn() + " Event : RENAME_MODIFY, ELD = " + (localObj != null) + " inScope = " + (dvEntry != null) +
          " isMapped = " + (rvEntry != null));
      if (log.isTraceEnabled() == true) {
        log.trace("Remote Object details: " + remoteObj);
        log.trace("Local Object details: " + localObj != null ? localObj : "null");
      }
    }
    return new ChangeEvent(connector, ChangeEvent.ChangeType.RENAME_MODIFY, remoteObj, localObj, dvEntry, prevDvEntry, rvEntry, null, effectsInScopeObjects, prevEffectsInScopeObjects);
  }

  private ChangeEvent deleteChangeEvent(Connector connector, IdObject localObj, IdObject remoteObj, DitVirtualizationEntry dvEntry, DitVirtualizationEntry prevDvEntry, 
      RoleVirtualizationEntry rvEntry, 
      boolean effectsInScopeObjects) {
    if (log.isDebugEnabled() == true) {
      log.debug("For object " + remoteObj.getDn() + " Event : DELETE, ELD = " + (localObj != null) + " inScope = " + (dvEntry != null));
      if (log.isTraceEnabled() == true) {
        log.trace("Remote Object details: " + remoteObj);
        log.trace("Local Object details: " + localObj != null ? localObj : "null");
      }
    }
    return new ChangeEvent(connector, ChangeEvent.ChangeType.DELETE, remoteObj, localObj, dvEntry, prevDvEntry, rvEntry, null, effectsInScopeObjects, false);
  }
  
  private boolean isRename(IdObject localObj, IdObject remoteObj) {
    assert(localObj != null);
    byte[] lParentUUID = localObj.getParentUUID();
    byte[] rParentUUID = remoteObj.getParentUUID();
    if (lParentUUID != null && rParentUUID != null) {
      if (log.isTraceEnabled() == true) {
        log.trace("isRename: Comparing " + 
            Util.byteArrayToHexString(lParentUUID) 
            + " with " + Util.byteArrayToHexString(rParentUUID));
      }
      if (Arrays.equals(lParentUUID, rParentUUID) == false) {
        log.trace("isRename: Object " + remoteObj.getDn() + " got a rename event. old parent UUID and new parent UUID differ");
        return true;
      }
    }
    try {
      if (LdapUtils.dnCompare(localObj.getDn(), remoteObj.getDn()) != 0) {
        log.trace("isRename: object renamed : " + localObj.getDn() + " : " + remoteObj.getDn());
        return true;
      }
    } catch (LDAPException ex) {
      log.warn("LDAPException:", ex);
      return false;
    }
    return false;
  }

  private boolean isTombstoneEntry(IdObject remoteObj) {
    String deleted = (String)remoteObj.getAttributeValue(AttributeVirtualization.DELETED);
    if (deleted != null && deleted.equalsIgnoreCase("TRUE")) {
      return true;
    }
    return false;
  }

  private ChangeEvent addChangeEvent(Connector connector, IdObject localObj, IdObject remoteObj, 
      DitVirtualizationEntry dvEntry, RoleVirtualizationEntry rvEntry) {
    if (log.isDebugEnabled() == true) {
      log.debug("For object " + remoteObj.getDn() + " Event : ADD, ELD = " + (localObj != null) + " inScope = " + (dvEntry != null) +
          " isMapped = " + (rvEntry != null));
      if (log.isTraceEnabled() == true) {
        log.trace("Remote Object details: " + remoteObj);
        log.trace("Local Object details: " + localObj != null ? localObj : "null");
      }
    }
    return new ChangeEvent(connector, ChangeEvent.ChangeType.ADD, remoteObj, localObj, dvEntry, null, rvEntry, null, false, false);
  }

  public void setIdObjectFactory(IdObjectFactory idObjectFactory) {
    this.idObjectFactory = idObjectFactory;
  }

  public void setLocalStore(LocalIdentityStore localStore) {
    this.localStore = localStore;
  }

}
