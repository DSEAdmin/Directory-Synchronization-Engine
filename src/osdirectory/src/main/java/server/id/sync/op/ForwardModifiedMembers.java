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
package server.id.sync.op;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.AVP;
import server.id.AttributeTransform;
import server.id.AttributeTypeDef;
import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.IdAVP;
import server.id.IdAttribute;
import server.id.IdAttributeTypeDef;
import server.id.IdEntry;
import server.id.IdObject;
import server.id.IdObjectImpl;
import server.id.IdObjectType;
import server.id.IdVirtualization;
import server.id.Util;
import server.id.dao.LocalIdentityStore;
import server.id.ldap.LdapUtils;
import server.id.ldap.ObjectTypeSpec;
import server.id.sync.ChangeEvent;
import server.id.sync.EventProcessingObligation;
import server.id.sync.ChangeEvent.ChangeType;
import server.id.sync.agent.domain.Member;

import com.unboundid.ldap.sdk.LDAPException;

//TODO this class needs refactoring
public class ForwardModifiedMembers extends ObligationProcessorBase {
  private Log log = LogFactory.getLog(getClass());
  private LocalIdentityStore idStore;
  private AttributeTransform memberOfAt;
  private ObjectTypeSpec ocSpec;
  private IdVirtualization idVirtualization;
  
  public void setIdStore(LocalIdentityStore idStore) {
    this.idStore = idStore;
  }
  
  public void setMemberOfAt(AttributeTransform memberOfAt) {
    this.memberOfAt = memberOfAt;
  }

  public void setOcSpec(ObjectTypeSpec ocSpec) {
    this.ocSpec = ocSpec;
  }

  public void setIdVirtualization(IdVirtualization idVirtualization) {
    this.idVirtualization = idVirtualization;
  }

  @SuppressWarnings("unchecked")
  public void processObligation(EventProcessingObligation obligation, ChangeEvent event, Callback cb) throws Exception {
    log.debug("processObligation: " + obligation);
    log.debug("processObligation: " + event.getRemoteObj().getDn());
    
    byte[] rvUuid = event.getRvEntry().getUuid();
    
    Iterable<String> newMemList = (Iterable<String>) event.getRemoteObj().getAttributeValues(AttributeVirtualization.MEMBER);
    if (event.getType() == ChangeType.DELETE) {
      newMemList = new ArrayList<String>();
    }
    Set<String> newMemSet = new HashSet<String>();

    if (newMemList != null) {
      for (String member : newMemList) {
        try {
          String ndn = LdapUtils.normalizeDn(member);
          newMemSet.add(ndn);
        } catch (LDAPException e) {
          log.warn("LDAPException:", e);
          throw new ObligationProcessingException(e.getMessage());
        }
      }
    } else {
      log.debug("newMemList is null for " + event.getRemoteObj().getDn());
      if (event.getType() == ChangeType.MODIFY || event.getType() == ChangeType.RENAME_MODIFY) {
        log.debug("No change in membership...");
        return;
      }
    }
    
    List<Member> oldMemList = idStore.getMembers(null, event.getConnector(), event.getRemoteObj());
    Set<String> oldMemSet = new HashSet<String>();

    if (oldMemList != null) {
      for (Member m : oldMemList) {
        oldMemSet.add(m.getNormalizedDn());
      }
    } else {
      log.debug("oldMemList is null for " + event.getRemoteObj().getDn());
    }
    
    Set<String> newMembers = new HashSet<String>(newMemSet);
    newMembers.removeAll(oldMemSet); // Set operation newMemSet - oldMemSet
    
    Set<String> removedMembers = new HashSet<String>(oldMemSet);
    removedMembers.removeAll(newMemSet); // Set operation oldMemSet - newMemSet

    StringBuffer sb = new StringBuffer();

    //changing the DB only happens in the DB state. Here we just generate the events for server/service
    //If the user is not known, ignore them. We will find out about them in the future
    for( String newMember : newMembers) {
      log.debug("processObligation: newMember " + newMember);
      Entry dirObj = idStore.findLocalObject(event.getAccount(), event.getConnector(), newMember);
      if (dirObj == null) {
        log.debug("Know nothing about the object " + newMember);
        continue;
      }
    
      //check if it is a user
      AVP ocAvp = dirObj.getAvp(AttributeVirtualization.OBJECTTYPE);
      assert(ocAvp != null);
      Iterable<String> ocValues = (Iterable<String>) ocAvp.getValues();
      if (ocSpec.isPerson(ocValues) == false) {
        log.debug("This object is not a person. Ignoring ... : " + newMember);
        continue;
      }
      
      //check if he is in scope
      AVP prevDvUuidAvp = dirObj.getAvp(AttributeVirtualization.DV_ENTRY);
      DitVirtualizationEntry userPrevDvEntry = null;
      if (prevDvUuidAvp != null) {
        byte[] dvUuid = (byte[])prevDvUuidAvp.getValue();
        if (dvUuid == null)
          throw new ObligationProcessingException("DvEntry AVP returned with no value");
        userPrevDvEntry = idVirtualization.getDvEntry(event.getConnector().getDvEntries(), dvUuid);
        if (userPrevDvEntry == null) 
          throw new ObligationProcessingException("dvEntry uuid has no matching dvEntry : " + Util.byteArrayToHexString(dvUuid));
      } else {
        log.debug("This object was not in scope. Will check its scope now ... : " + newMember);
      }
      
      AVP dnAvp = dirObj.getAvp(AttributeVirtualization.DN);
      assert(dnAvp != null);
      IdEntry tempEntry = new IdEntry();
      tempEntry.addAv(dnAvp);
      
      List<AVP> userAvps = new ArrayList<AVP>();
      Iterable<? extends Object> memberOf = memberOfAt.virtualize(cb.getIdDao(), tempEntry);
      AVP avp = new IdAVP(new IdAttribute(AttributeVirtualization.RV_ENTRY, 
          new IdAttributeTypeDef(AttributeTypeDef.Type.BINARY)));
      if (memberOf != null) {
        Iterator<? extends Object> it = memberOf.iterator();
        while (it.hasNext()) {
          avp.addValue(it.next());
        }
      }
      log.debug("Added role " + Util.uuidToString(rvUuid));
      userAvps.add(avp);
      userAvps.add(dirObj.getAvp(AttributeVirtualization.DN));
      userAvps.add(dirObj.getAvp(AttributeVirtualization.UUID));
      userAvps.add(ocAvp);
      
      //Calculate the new dvEntry
      dirObj.addAv(avp); //rv entries
      DitVirtualizationEntry dvEntry = idVirtualization.isObjectMapped(cb.getConnector().getDvEntries(),
          new IdObjectImpl(dirObj, IdObjectType.PERSON));
      if (userPrevDvEntry != null &&  dvEntry != null) { //modify the user
        log.debug("prevDvEntry !null and dvEntry !null" + newMember);
        IdAVP dvAvp = new IdAVP(new IdAttribute(AttributeVirtualization.DV_ENTRY));
        dvAvp.addValue(dvEntry.getUuid());
        userAvps.add(dvAvp);
        cb.getSynchronizationService().addChangeToChangeRequest(ChangeEvent.ChangeType.MODIFY, userAvps);        
        cb.addUpdateDv((byte[])dirObj.getAvp(AttributeVirtualization.UUID).getValue(), dvEntry);
        cb.incrNumEntriesToSvc();
      } else if (userPrevDvEntry != null && dvEntry == null) { //delete the user
        log.debug("prevDvEntry !null and dvEntry null" + newMember);
        IdAVP dvAvp = new IdAVP(new IdAttribute(AttributeVirtualization.DV_ENTRY));
        dvAvp.addValue(userPrevDvEntry.getUuid());
        userAvps.add(dvAvp);
        IdAVP delAvp = new IdAVP(new IdAttribute(AttributeVirtualization.DELETED));
        delAvp.addValue("true");
        userAvps.add(delAvp);
        cb.getSynchronizationService().addChangeToChangeRequest(ChangeEvent.ChangeType.DELETE, userAvps);        
        cb.addUpdateDv((byte[])dirObj.getAvp(AttributeVirtualization.UUID).getValue(), null);
        cb.incrNumEntriesToSvc();
      } else if (userPrevDvEntry == null && dvEntry != null) { //add the user
        log.debug("prevDvEntry null and dvEntry !null" + newMember);
        IdObject fullObj = null;
        try {
          fullObj = cb.getIdDao().getCompleteObject(newMember);
        } catch (Exception e) {
          log.warn("Failed to get full object for " + event.getRemoteObj().getDn()
              + " This could be because the object was deleted while we were processing it. "
              , e);      
          continue;
        }
        DitVirtualizationEntry foDvEntry = idVirtualization.isObjectMapped(cb.getConnector().getDvEntries(), fullObj);
        IdAVP dvAvp = new IdAVP(new IdAttribute(AttributeVirtualization.DV_ENTRY));
        dvAvp.addValue(foDvEntry.getUuid());
        fullObj.getEntry().addAv(dvAvp);
        cb.getSynchronizationService().addChangeToChangeRequest(ChangeEvent.ChangeType.ADD, fullObj.getEntry().getAll());        
        cb.addUpdateDv((byte[])dirObj.getAvp(AttributeVirtualization.UUID).getValue(), dvEntry);
        cb.incrNumEntriesToSvc();
      } else { //userPrevDvEntry == null && dvEntry == null //nothing to do... just continue
        log.debug("prevDvEntry null and dvEntry null. Ignore ..." + newMember);
        continue;
      }
      
      sb.append("<arg name=\"memberAdded\" type=\"string\">").append(newMember).append("</arg>");
    }
    
    // removeMembers
    for( String removedMember : removedMembers) {
      log.debug("processObligation: removedMember " + removedMember);
      Entry dirObj = idStore.findLocalObject(event.getAccount(), event.getConnector(), removedMember);
      if (dirObj == null) {
        log.debug("Know nothing about the object " + removedMember);
        continue;
      }
    
      //check if it is a user
      AVP ocAvp = dirObj.getAvp(AttributeVirtualization.OBJECTTYPE);
      assert(ocAvp != null);
      Iterable<String> ocValues = (Iterable<String>) ocAvp.getValues();
      if (ocSpec.isPerson(ocValues) == false) {
        log.debug("This object is not a person. Ignoring ... : " + removedMember);
        continue;
      }
      
      //check if he is in scope
      AVP prevDvUuidAvp = dirObj.getAvp(AttributeVirtualization.DV_ENTRY);
      DitVirtualizationEntry userPrevDvEntry = null;
      if (prevDvUuidAvp != null) {
        byte[] dvUuid = (byte[])prevDvUuidAvp.getValue();
        if (dvUuid == null)
          throw new ObligationProcessingException("DvEntry AVP returned with no value");
        userPrevDvEntry = idVirtualization.getDvEntry(event.getConnector().getDvEntries(), dvUuid);
        if (userPrevDvEntry == null) 
          throw new ObligationProcessingException("dvEntry uuid has no matching dvEntry : " + Util.byteArrayToHexString(dvUuid));
      } else {
        log.debug("This object was not in scope. Will check scope now ... : " + removedMember);
      }
      
      AVP dnAvp = dirObj.getAvp(AttributeVirtualization.DN);
      assert(dnAvp != null);
      IdEntry tempEntry = new IdEntry();
      tempEntry.addAv(dnAvp);
      
      List<AVP> userAvps = new ArrayList<AVP>();
      Iterable<? extends Object> memberOf = memberOfAt.virtualize(cb.getIdDao(), tempEntry);
      AVP avp = new IdAVP(new IdAttribute(AttributeVirtualization.RV_ENTRY,
          new IdAttributeTypeDef(AttributeTypeDef.Type.BINARY)));
      if (memberOf != null) {
        Iterator<? extends Object> it = memberOf.iterator();
        while (it.hasNext()) {
          byte[] r = (byte[])it.next();
          avp.addValue(r);
        }
      }
      userAvps.add(avp);
      userAvps.add(dirObj.getAvp(AttributeVirtualization.DN));
      userAvps.add(dirObj.getAvp(AttributeVirtualization.UUID));
      userAvps.add(ocAvp);

      // Calculate the new dvEntry
      dirObj.addAv(avp); // rv entries
      DitVirtualizationEntry dvEntry = idVirtualization.isObjectMapped(cb.getConnector().getDvEntries(),
          new IdObjectImpl(dirObj, IdObjectType.PERSON));
      if (userPrevDvEntry != null && dvEntry != null) { // modify the user
        log.debug("prevDvEntry !null and dvEntry !null" + removedMember);
        IdAVP dvAvp = new IdAVP(new IdAttribute(AttributeVirtualization.DV_ENTRY));
        dvAvp.addValue(dvEntry.getUuid());
        userAvps.add(dvAvp);
        cb.getSynchronizationService().addChangeToChangeRequest(ChangeEvent.ChangeType.MODIFY, userAvps);
        cb.addUpdateDv((byte[])dirObj.getAvp(AttributeVirtualization.UUID).getValue(), dvEntry);
        cb.incrNumEntriesToSvc();
      } else if (userPrevDvEntry != null && dvEntry == null) { // delete the user
        log.debug("prevDvEntry !null and dvEntry null" + removedMember);
        IdAVP dvAvp = new IdAVP(new IdAttribute(AttributeVirtualization.DV_ENTRY));
        dvAvp.addValue(userPrevDvEntry.getUuid());
        userAvps.add(dvAvp);
        IdAVP delAvp = new IdAVP(new IdAttribute(AttributeVirtualization.DELETED));
        delAvp.addValue("true");
        userAvps.add(delAvp);
        cb.getSynchronizationService().addChangeToChangeRequest(ChangeEvent.ChangeType.DELETE, userAvps);
        cb.addUpdateDv((byte[])dirObj.getAvp(AttributeVirtualization.UUID).getValue(), null);
        cb.incrNumEntriesToSvc();
      } else if (userPrevDvEntry == null && dvEntry != null) { // add the user
        log.debug("prevDvEntry null and dvEntry !null" + removedMember);
        IdObject fullObj = null;
        try {
          fullObj = cb.getIdDao().getCompleteObject(removedMember);
        } catch (Exception e) {
          log.warn("Failed to get full object for " + event.getRemoteObj().getDn()
              + " This could be because the object was deleted while we were processing it. ", e);
          continue;
        }
        DitVirtualizationEntry foDvEntry = idVirtualization.isObjectMapped(cb.getConnector().getDvEntries(), fullObj);
        IdAVP dvAvp = new IdAVP(new IdAttribute(AttributeVirtualization.DV_ENTRY));
        dvAvp.addValue(foDvEntry.getUuid());
        fullObj.getEntry().addAv(dvAvp);
        cb.getSynchronizationService()
            .addChangeToChangeRequest(ChangeEvent.ChangeType.ADD, fullObj.getEntry().getAll());
        cb.addUpdateDv((byte[])dirObj.getAvp(AttributeVirtualization.UUID).getValue(), dvEntry);
        cb.incrNumEntriesToSvc();
      } else { // userPrevDvEntry == null && dvEntry == null //nothing to do...
               // just continue
        log.debug("prevDvEntry null and dvEntry null. Ignore ..." + removedMember);
        continue;
      }

      sb.append("<arg name=\"memberRemoved\" type=\"string\">").append(removedMember).append("</arg>");
    }
    
    audit(obligation, event, cb, sb.toString());
  }
}
