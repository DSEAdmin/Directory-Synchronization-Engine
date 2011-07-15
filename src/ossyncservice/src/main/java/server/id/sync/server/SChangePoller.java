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

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.mail.MethodNotSupportedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.AVP;
import server.id.Attribute;
import server.id.AttributeVirtualization;
import server.id.Container;
import server.id.Group;
import server.id.IdObject;
import server.id.IdObjectImpl;
import server.id.IdObjectType;
import server.id.IdAVP;
import server.id.IdAttribute;
import server.id.IdEntry;
import server.id.Util;
import server.id.dao.ChangePoller;
import server.id.dao.InvalidConfigurationException;
import server.id.dao.State;
import server.id.ldap.ObjectTypeSpec;
import server.id.ldap.ad.ADPerson;
import server.id.sync.messages.v1.ChangeDef;
import server.id.sync.messages.v1.ChangeRequest;

public class SChangePoller implements ChangePoller {
  private final Log log = LogFactory.getLog(getClass());
  private EntitiesDao serviceEntitiesDao;
  private Account acct;
  private Account account;
  private ChangeRequest request;
  private Connector connector;
  private ObjectTypeSpec ocSpec = null;
    
  public ChangeRequest getRequest() {
    return request;
  }

  public void setRequest(ChangeRequest request) {
    this.request = request;
  }

  public Account getAccount() {
	if (acct == null)
	  getAccountFromDB();
	return acct;
  }
  
  public void setAccount(Account account) {
	this.account = account;
  }
  
  public void setServiceEntitiesDao(EntitiesDao serviceEntitiesDao) {
    this.serviceEntitiesDao = serviceEntitiesDao;
  }

  private void getAccountFromDB() {
	acct = serviceEntitiesDao.getAccountByName(account.getName());
  }
  
  public Connector getConnector() {
	if (connector == null) {
	  if (acct == null)
		getAccountFromDB();
	  if (acct == null)
		return null;
	  connector = serviceEntitiesDao.getConnectorByUuid(acct, request
		  .getConnectorUuid());
	}
	return connector;
  }

  public Connector refreshConnector() {
	if (connector == null) {
	  return null;
	}
	return serviceEntitiesDao.refreshConnector((Connector) connector);
  }
  
  public void setConnector(Connector connector) {
	this.connector = connector;
  }
  
  public void setOcSpec(ObjectTypeSpec ocSpec) {
	this.ocSpec = ocSpec;
  }
  
  public List<IdObject> getNextChangeSet() throws Exception {
	getConnector();
	if (request == null)
	  throw new IllegalArgumentException("Null ChangeRequest");
	if (validateRequest() == false) {
	  log.warn("Validation of ChangeRequest failed");
	  throw new IllegalArgumentException("Validation of request failed");
	}
	
	List<IdObject> objects = new LinkedList<IdObject>();
	List<ChangeDef> changes = request.getChangeDefs();
	
	if (changes == null || changes.size() == 0) {
	  log.debug("No changes received");
	} else {
	  for (ChangeDef change : changes) {
		IdObject obj = getRemoteObject(change);
		objects.add(obj);
	  }
	}
	
	return objects;
  }

  public IdObject getCompleteObject(String objectId) throws Exception {
	throw new MethodNotSupportedException("SChangePoller does not support getCompleteObject");
  }
  
  public void resetStateToInitial() {
	//Do nothing
  }

  public void rollbackState() {
	//Do nothing
  }

  public void saveStateToDB() {
	// TODO Auto-generated method stub
	//Save the changeNumber
  }

  private IdObject getRemoteObject(ChangeDef change) {
	IdEntry entry = new IdEntry();
	Attribute changeTypeAttr = new IdAttribute(AttributeVirtualization.CHANGE_TYPE);
	AVP changeType = new IdAVP(changeTypeAttr);
	changeType.addValue(change.getChangeType().name());
	entry.addAv(changeType);
	entry.addAv(change.getAttributes());
	
	AVP dnavp = entry.getAvp(AttributeVirtualization.DN);
	String dn = (String)dnavp.getValue(); 
	if (dnavp == null) {
	  log.warn("Attribute " + AttributeVirtualization.DN + " not found in entry");
	  return null;
	}
	
	log.debug("Processing entry with DN " + dn);
	
	AVP avp = entry.getAvp(AttributeVirtualization.OBJECTTYPE);
	if (avp == null) {
	  log.warn("Attribute " + AttributeVirtualization.OBJECTTYPE + " not found in entry");
	  return null;
	}

	IdObjectImpl idObject = null;
	@SuppressWarnings("unchecked")
	Iterable<String> oc = (Iterable<String>)avp.getValues();
	if (ocSpec.isPerson(oc)) {
	  idObject = new ADPerson(entry);
	} else if (ocSpec.isContainer(oc)) {
	  idObject = new Container(entry);
	} else if (ocSpec.isGroup(oc)) {
	  idObject = new Group(entry);
	} else {
	  log.warn("Unsupported object type");
	  return null;
	}
	
	return idObject;
  }

  private boolean validateRequest() {
	if (connector == null || request == null) {
	  log.warn("The connector or request is null");
	  return false;
	}
	
	if (Arrays.equals(request.getConnectorUuid(), connector.getUuid()) == false) {
	  log.warn("Connector uuid does not match with connector uuid in request. " +
		  connector.getName() + " " + Util.byteArrayToHexString(request.getConnectorUuid()));
	  return false;
	}
	
	if (request.getConnectorMajorVersion() != connector.getMajorVersion()) {
	  log.warn("Connector major version does not match with connector major version in request. " +
		  connector.getMajorVersion() + " " + request.getConnectorMajorVersion());
	  return false;
	}

	if (request.getConnectorMinorVersion() < connector.getMinorVersion()) {
	  log.warn("Connector minor version greater than connector minor version in request. " +
		  connector.getMinorVersion() + " " + request.getConnectorMinorVersion());
	  return false;	  
	}
	
	if (request.isDelta() == true) {
	  if (request.getChangeNumber() < connector.getChangeNumber()) {
		log.warn("Change request with change number " + request.getChangeNumber() + " has already been" +
				" applied. Connector is at change number " + connector.getChangeNumber());
		return false;
	  }	  
	}
	
	return true;
  }

  public State getContainerState() {
	return State.POLL;
  }

  public State getGroupState() {
	return State.POLL;
  }

  public List<IdObject> getNextContainerChangeSet() throws Exception {
	List<IdObject> list = getNextChangeSet();
	Iterator<IdObject> it = list.iterator();
	while (it.hasNext()) {
	  IdObject o = it.next();
	  if (o.getObjectType() != IdObjectType.CONTAINER) {
		log.debug("Ignore object " + o.getDn() + " of type " + o.getObjectType().name());
		it.remove();
	  }
	}
	return list;
  }

  public List<IdObject> getNextGroupChangeSet() throws Exception {
	List<IdObject> list = getNextChangeSet();
	Iterator<IdObject> it = list.iterator();
	while (it.hasNext()) {
	  IdObject o = it.next();
	  if (o.getObjectType() != IdObjectType.GROUP) {
		log.debug("Ignore object " + o.getDn() + " of type " + o.getObjectType().name());
		it.remove();
	  }
	}
	return list;
  }

  public List<IdObject> getNextUserChangeSet() throws Exception {
	List<IdObject> list = getNextChangeSet();
	Iterator<IdObject> it = list.iterator();
	while (it.hasNext()) {
	  IdObject o = it.next();
	  if (o.getObjectType() != IdObjectType.PERSON) {
		log.debug("Ignore object " + o.getDn() + " of type " + o.getObjectType().name());
		it.remove();
	  }
	}
	return list;
  }

  public State getUserState() {
	return State.POLL;
  }

  public boolean validateConfiguration()
	  throws MethodNotSupportedException, InvalidConfigurationException {
	throw new MethodNotSupportedException("validateConfiguration not supported by SChangePoller");
  }

  public Iterable<? extends Object> getGroupMembership(String objectId) throws Exception {
	throw new MethodNotSupportedException("SChangePoller does not support getGroupMembership");
  }

  public void saveOperationResult(OperationType opType,
	  OperationResult opResult, String resultString, long numChangesFromIds,
	  long numChangesToSvc) throws Exception {
	OperationResult or = new OperationResult();
    int currTime = (int)((new Date()).getTime() / 1000);
	or.setCreateDate(currTime);
	or.setLastDate(currTime);
	or.setOpType(opType);
	or.setOpResult(opResult);
	or.setResultString(resultString);
	or.setNumChangesFromAgent(numChangesFromIds);
	or.setConnector((Connector) connector);
	serviceEntitiesDao.addOperationResult(or);
  }

}
