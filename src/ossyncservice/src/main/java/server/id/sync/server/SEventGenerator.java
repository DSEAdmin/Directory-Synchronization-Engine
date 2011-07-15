/*************************BEGINE LICENSE BLOCK**********************************
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
 *  The Original Code is Directory Synchronization Engine(DSE).
 * 
 *  The Initial Developer of the Original Code is IronKey, Inc..
 *  Portions created by the Initial Developer are Copyright (C) 2011
 *  the Initial Developer. All Rights Reserved.
 * 
 *  Contributor(s): Shirish Rai
 * 
 **************************END LICENSE BLOCK***********************************/
package server.id.sync.server;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import server.id.AttributeVirtualization;
import server.id.Container;
import server.id.Group;
import server.id.IdObject;
import server.id.IdObjectType;
import server.id..ChangePoller;
import server.id.sync.ChangeEvent;
import server.id.sync.EventGenerator;
import server.id.sync.ChangeEvent.ChangeType;

public class SEventGenerator extends EventGenerator {
  private final Log log = LogFactory.getLog(getClass());

  private Dao dao;
  
  public void setdao(Dao dao) {
	this.dao = dao;
  }
  
  /**
   * 
   * @param id
   * @param remoteObj
   * @return
   * @throws IllegalArgumentException
   * 
   *           Find the user first by 1. uuid, 2. email 3. username
   */
  private IdObject getLocalObject(ChangePoller id, IdObject remoteObj)
	  throws IllegalArgumentException {
	Account acct = id.getAccount();
	Account account;
	if (acct instanceof Account) {
	  account = (Account) acct;
	} else {
	  log.warn("Account instance is not an entity type");
	  throw new IllegalArgumentException(
		  "Account instance must be an entity type");
	}

	Connector conn = id.getConnector();
	Connector connector;
	if (conn instanceof Connector) {
	  connector = (Connector) conn;
	} else {
	  log.warn("Connector instance is not an entity type");
	  throw new IllegalArgumentException(
		  "Connector instance must be an entity type");
	}
	
	if (remoteObj.getObjectType() == IdObjectType.PERSON) {

	  User user = dao.findUser(account, connector, remoteObj);
	  if (user != null) {
		return dao.getUser(user);
	  } else {
		return null;
	  }
	} else if (remoteObj.getObjectType() == IdObjectType.GROUP) {
	  RemoteGroup rg = dao.findRemoteGroup(connector, remoteObj);
	  if (rg != null) {
		return dao.getRemoteGroup(rg);
	  } else {
		return null;
	  }
	} else if (remoteObj.getObjectType() == IdObjectType.CONTAINER) {
	  return null;
	} else {
	  return null;
	}
  }

  @Override
  public ChangeEvent generateEvent(ChangePoller id, IdObject remoteObj) {
	IdObject localObj = getLocalObject(id, remoteObj);
	ChangeEvent changeEvent = null;
	if (remoteObj.getObjectType() == IdObjectType.PERSON) {
	  changeEvent = generateUserEvent(id, localObj, remoteObj);
	} else if (remoteObj.getObjectType() == IdObjectType.GROUP) {
	  Group group = (Group) remoteObj;
	  changeEvent = generateGroupEvent(id, localObj, group);
	} else if (remoteObj.getObjectType() == IdObjectType.CONTAINER) {
	  Container container = (Container) remoteObj;
	  changeEvent =  generateContainerEvent(id, localObj, container);
	} else {
	  log.error("Unknown remote object type. Cannot generate change events");
	}
	changeEvent.setAccount(id.getAccount());
	changeEvent.setIdVirtualization(idVirtualization);
	return changeEvent;
  }

  private ChangeEvent generateContainerEvent(ChangePoller poller,
	  IdObject localObj, Container remoteObj) {
	DitVirtualizationEntry dvEntry = idVirtualization.getDvEntry(poller
		.getConnector().getDvEntries(), (byte[])remoteObj.getAttributeValue(AttributeVirtualization.DV_ENTRY));
	return generateObjectEvent(poller, localObj, remoteObj, dvEntry, null);
  }

  private ChangeEvent generateGroupEvent(ChangePoller poller,
	  IdObject localObj, Group remoteObj) {
	RoleVirtualizationEntry rvEntry = idVirtualization.isGroupMapped(poller
		.getConnector().getRvEntries(), remoteObj.getUUID());
	return generateGroupEvent(poller, localObj, remoteObj, rvEntry);
  }

  private ChangeEvent generateUserEvent(ChangePoller poller, IdObject localObj,
	  IdObject remoteObj) {
	DitVirtualizationEntry dvEntry = idVirtualization.getDvEntry(poller
		.getConnector().getDvEntries(), (byte[])remoteObj.getAttributeValue(AttributeVirtualization.DV_ENTRY));
	return generateObjectEvent(poller, localObj, remoteObj, dvEntry, null);
  }

  private ChangeEvent generateGroupEvent(ChangePoller poller,
	  IdObject localObj, IdObject remoteObj,
	  RoleVirtualizationEntry rvEntry) {
	String ct = (String) remoteObj
		.getAttributeValue(AttributeVirtualization.CHANGE_TYPE);
	if (ct == null) {
	  log.warn("No changeType attribute found in remoteObject "
		  + remoteObj.getDn());
	  return null;
	}
	ChangeType changeType = ChangeType.valueOf(ct);
	if (changeType == null || changeType == ChangeType.UNKNOWN) {
	  log.warn("Unknown change type " + ct + " for " + remoteObj.getDn());
	  return null;
	}
	
	if (log.isDebugEnabled() == true) {
	  log.debug("For object " + remoteObj.getDn() + " Event : " + changeType);
	  if (log.isTraceEnabled() == true) {
		log.trace("Remote Object details: " + remoteObj);
		log.trace("Local Object details: " + localObj != null ? localObj
			: "null");
	  }
	}
	
	return new ChangeEvent(poller.getConnector(), changeType, remoteObj,
		localObj, null, null, rvEntry, null, false, false);
  } 
  
  private ChangeEvent generateObjectEvent(ChangePoller poller,
	  IdObject localObj, IdObject remoteObj, DitVirtualizationEntry dvEntry,
	  RoleVirtualizationEntry rvEntry) {
	String ct = (String) remoteObj
		.getAttributeValue(AttributeVirtualization.CHANGE_TYPE);
	if (ct == null) {
	  log.warn("No changeType attribute found in remoteObject "
		  + remoteObj.getDn());
	  return null;
	}
	ChangeType changeType = ChangeType.valueOf(ct);
	if (changeType == null || changeType == ChangeType.UNKNOWN) {
	  log.warn("Unknown change type " + ct + " for " + remoteObj.getDn());
	  return null;
	}

	// Check if its under deleted objects container
	if (changeType == ChangeType.DELETE) {
	  if (localObj == null) {
		log.info("Delete event received for " + remoteObj.getDn()
			+ " for which there is no local object.");
		return null;
	  }
	  return deleteChangeEvent(poller.getConnector(), localObj, remoteObj,
		  dvEntry, null, null, false);
	}

	if (changeType == ChangeType.ADD) {
	  if (localObj == null)
		return addChangeEvent(poller.getConnector(), localObj, remoteObj,
			dvEntry);
	  else {
		return renameModifyChangeEvent(poller.getConnector(), localObj,
			remoteObj, dvEntry, null, null, null, false,
			false);
	  }
	}

	if (changeType == ChangeType.MODIFY) {
	  if (localObj == null) {
		log.info("Modify event received for " + remoteObj.getDn()
			+ " for which there is no local object.");
		return null;
	  }
	  return modifyChangeEvent(poller.getConnector(), localObj, remoteObj,
		  dvEntry, null);
	}

	if (changeType == ChangeType.RENAME_MODIFY) {
	  if (localObj == null) {
		log.info("Modify event received for " + remoteObj.getDn()
			+ " for which there is no local object.");
		return null;
	  }
	  return renameModifyChangeEvent(poller.getConnector(), localObj,
		  remoteObj, dvEntry, null, null, null, false,
		  false);
	}

	return null;
  }

  private ChangeEvent deleteChangeEvent(
	  Connector connector, IdObject localObj,
	  IdObject remoteObj, DitVirtualizationEntry dvEntry,
	  DitVirtualizationEntry prevDvEntry, RoleVirtualizationEntry rvEntry, 
	  boolean effectsInScopeObjects) {
	if (log.isDebugEnabled() == true) {
	  log.debug("For object " + remoteObj.getDn() + " Event : DELETE, ELD = "
		  + (localObj != null) + " inScope = " + (dvEntry != null));
	  if (log.isTraceEnabled() == true) {
		log.trace("Remote Object details: " + remoteObj);
		log.trace("Local Object details: " + localObj != null ? localObj
			: "null");
	  }
	}
	return new ChangeEvent(connector, ChangeEvent.ChangeType.DELETE, remoteObj,
		localObj, dvEntry, prevDvEntry, rvEntry, null, effectsInScopeObjects, false);
  }

  private ChangeEvent addChangeEvent(Connector connector,
	  IdObject localObj, IdObject remoteObj, DitVirtualizationEntry dvEntry) {
	if (log.isDebugEnabled() == true) {
	  log.debug("For object " + remoteObj.getDn() + " Event : ADD, ELD = "
		  + (localObj != null) + " inScope = " + (dvEntry != null));
	  if (log.isTraceEnabled() == true) {
		log.trace("Remote Object details: " + remoteObj);
		log.trace("Local Object details: " + localObj != null ? localObj
			: "null");
	  }
	}
	return new ChangeEvent(connector, ChangeEvent.ChangeType.ADD, remoteObj,
		localObj, dvEntry, null, null, null, false, false);
  }

  private ChangeEvent modifyChangeEvent(
	  Connector connector, IdObject localObj,
	  IdObject remoteObj, DitVirtualizationEntry dvEntry, RoleVirtualizationEntry rvEntry) {
	if (log.isDebugEnabled() == true) {
	  log.debug("For object " + remoteObj.getDn() + " Event : MODIFY, ELD = "
		  + (localObj != null) + " inScope = " + (dvEntry != null));
	  if (log.isTraceEnabled() == true) {
		log.trace("Remote Object details: " + remoteObj);
		log.trace("Local Object details: " + localObj != null ? localObj
			: "null");
	  }
	}
	return new ChangeEvent(connector, ChangeEvent.ChangeType.MODIFY, remoteObj,
		localObj, dvEntry, null, rvEntry, null, false, false);
  }

  private ChangeEvent renameModifyChangeEvent(
	  Connector connector, IdObject localObj,
	  IdObject remoteObj, DitVirtualizationEntry dvEntry,
	  DitVirtualizationEntry prevDvEntry, RoleVirtualizationEntry rvEntry, 
	  RoleVirtualizationEntry prevRvEntry, 
	  boolean effectsInScopeObjects,
	  boolean prevEffectsInScopeObjects) {
	if (log.isDebugEnabled() == true) {
	  log.debug("For object " + remoteObj.getDn()
		  + " Event : RENAME_MODIFY, ELD = " + (localObj != null)
		  + " inScope = " + (dvEntry != null));
	  if (log.isTraceEnabled() == true) {
		log.trace("Remote Object details: " + remoteObj);
		log.trace("Local Object details: " + localObj != null ? localObj
			: "null");
	  }
	}
	return new ChangeEvent(connector, ChangeEvent.ChangeType.RENAME_MODIFY,
		remoteObj, localObj, dvEntry, prevDvEntry, rvEntry, prevRvEntry, effectsInScopeObjects,
		prevEffectsInScopeObjects);
  }

}
