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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MethodNotSupportedException;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.support.AbstractContextMapper;

import server.id.AttributeVirtualization;
import server.id.Entry;
import server.id.IdObject;
import server.id.IdObjectFactory;
import server.id.IdObjectType;
import server.id.Util;
import server.id.dao.ChangePoller;
import server.id.dao.InvalidConfigurationException;
import server.id.dao.LocalIdentityStore;
import server.id.dao.State;
import server.id.ldap.DirectorySpec;
import server.id.ldap.ExcludedEntries;
import server.id.ldap.LdapTemplate;
import server.id.ldap.LdapEntry;
import server.id.ldap.LdapUtils;

import com.sun.jndi.ldap.ctl.DirSyncResponseControl;
import com.unboundid.ldap.sdk.LDAPException;

public class ADChangePoller implements ChangePoller {  
  private final Log log = LogFactory.getLog(getClass());
  private LdapTemplate template;
  private byte[] userCookie;
  private State userState;
  private DirectorySpec ds;
  private AttributeVirtualization av;
  private Connector connector;
  private LocalIdentityStore lIdStore;
  private IdObjectFactory idObjectFactory;
  private ControlProcessor userCp = new ControlProcessor(IdObjectType.PERSON);
  private ObjectMapper om;
  
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

  private void setState() {
    if (userCookie != null) {
      userState = State.POLL;
    } else {
      userState = State.INITIAL;
    }
  }
  
  private byte[] getCookie(IdObjectType type) {
    return userCookie;
  }

  private void setCookie(IdObjectType type, byte[] cookie) {
    userCookie = cookie;
  }

  private State getState(IdObjectType type) {
    return userState;
  }
  
  private void setState(IdObjectType type, State state) {
    userState = state;
  }
  
  public void init() {
   om = new ObjectMapper();
   userCookie = connector.getUserCookie();
   setState();
  }
  
  class ControlProcessor implements DirContextProcessor {
    IdObjectType idObjectType;
    
    public ControlProcessor(IdObjectType type) {
      this.idObjectType = type;
    }
    
    public void postProcess(DirContext ctx) throws NamingException {      
      LdapContext ldapContext;
      if (ctx instanceof LdapContext) {
          ldapContext = (LdapContext) ctx;
      }
      else {
          throw new IllegalArgumentException("Request Control operations require LDAPv3 - "
                  + "Context must be of type LdapContext");
      }

      log.trace("Retrieve AD dirsync control cookie");
      
      Control[] responseControls = ldapContext.getResponseControls();
      
      if (responseControls == null) {
        log.warn("DirSync Response Control not received");
        return;
      }

      for (Control c : responseControls) {
        if (c instanceof DirSyncResponseControl) {
          DirSyncResponseControl rspCtl = (DirSyncResponseControl)c;
          setCookie(idObjectType, rspCtl.getCookie());
          if (rspCtl.getFlag() != 0)
            setState(idObjectType, State.MORE_AVAILABLE);
          else 
            setState(idObjectType, State.POLL);
          if (log.isDebugEnabled()) {
            log.debug("Value of cookie for " + connector.getName() + "is : " + 
               Util.byteArrayToHexString(getCookie(idObjectType)));
            log.debug("Value of Flags for " + connector.getName() 
                + " is : " + rspCtl.getFlag());
            log.debug("Value of State for " + connector.getName() + " is : " 
                + getState(idObjectType));
          }
        }
      }      
    }

    public void preProcess(DirContext ctx) throws NamingException {
      LdapContext ldapContext;
      if (ctx instanceof LdapContext) {
          ldapContext = (LdapContext) ctx;
      }
      else {
          throw new IllegalArgumentException("Request Control operations require LDAPv3 - "
                  + "Context must be of type LdapContext");
      }

      log.trace("Adding AD deleted objects and dirsync controls");
      Control[] requestControls = ldapContext.getRequestControls();
      if (requestControls == null) {
          requestControls = new Control[0];
      }
      Control[] newControls = new Control[requestControls.length + 2];
      for (int i = 0; i < requestControls.length; ++i) {
        newControls[i] = requestControls[i];
      }
      newControls[newControls.length - 1] = new DeletedObjectsControl();
      try {
        newControls[newControls.length - 2] = 
          new DirSyncControl(getCookie(idObjectType), (int)connector.getPageSize());
      } catch (IOException e) {
        log.warn("Could not create DirSync control", e);
      }
      
      if (log.isDebugEnabled()) {
        log.debug("Using cookie for search for " + connector.getName() +
            " for " + idObjectType + " : " + 
            Util.byteArrayToHexString(getCookie(idObjectType)));
      }
      
      ldapContext.setRequestControls(newControls);
    }    
  }
  
  class ObjectMapper extends AbstractContextMapper {

    ExcludedEntries ee;
    
    public ObjectMapper() {
      ee = new ExcludedEntries(connector, ds);
    }
    
    @Override
    protected Object doMapFromContext(DirContextOperations ctx) {
      log.debug("doMapFromContext: mapping : " + ctx.getDn());
      LdapName entryName;
      try {
        entryName = new LdapName(ctx.getNameInNamespace());
      } catch (InvalidNameException e1) {
        log.error("Returned entry has invalid name" + e1);
        return null;
      }
      if (ee.isSystemBase(entryName)) {
        log.debug("Skipping system base : " + ctx.getNameInNamespace());
        return null;
      }
      if (ee.isSystemEntry(entryName)) {
        log.debug("Skipping system entry : " + ctx.getNameInNamespace());
        return null;        
      }
      
      LdapEntry le = new LdapEntry(ctx);

      Entry e = null;
      try {
        e = av.virtualize(ADChangePoller.this, le);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
      
      IdObject idObj = idObjectFactory.create(connector, e);

      if (idObj != null) {
        log.debug("Created object DN: " + idObj.getDn() != null ? idObj.getDn() : "null");
      } else {
        log.debug("doMapFromContext: null IdObject");
      }
      if (log.isTraceEnabled()) {
        log.trace(e);
      }
      
      return idObj;
    }
  }
  
  public ADChangePoller(LdapTemplate template) {
    this.template = template;
    this.userCookie = null;
    this.userState = State.INITIAL;
  }
  
  @SuppressWarnings("unchecked")
  synchronized private List<IdObject> getNextChangeSet(ControlProcessor cp, String filter) throws Exception {
    SearchControls sc = new SearchControls();
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
    Set<String> remoteAttributes = av.getAllRemoteAttributes();
    sc.setReturningAttributes(remoteAttributes.toArray(new String[remoteAttributes.size()]));
    if (log.isDebugEnabled() == true) {
      log.debug("Search base : [" + connector.getBase() +
          "] filter : [" + filter + "] scope : subtree");
    }
    return template.search(connector.getBase(), filter, sc, om, cp);
  }

  synchronized public List<IdObject> getNextChangeSet() throws Exception {    
    String filter = new String("(|");
    filter += "(" + ds.getFilterSpec().getUserFilter() + ")";
    filter += "(" + ds.getFilterSpec().getGroupFilter() + ")";
    filter += "(" + ds.getFilterSpec().getContainerFilter() + ")";
    filter += ")";
    return getNextChangeSet(userCp, filter);
  }
  
  synchronized public List<IdObject> getNextContainerChangeSet() throws Exception {
    throw new MethodNotSupportedException("Use getNextChage instead");
  }
  
  synchronized public List<IdObject> getNextGroupChangeSet() throws Exception {
    throw new MethodNotSupportedException("Use getNextChage instead");
  }
  
  synchronized public List<IdObject> getNextUserChangeSet() throws Exception {
    throw new MethodNotSupportedException("Use getNextChage instead");
  }

  synchronized public IdObject getCompleteObject(String objectId) throws Exception {
    Set<String> remoteAttributes = av.getAllRemoteAttributes();
    return (IdObject) template.lookup(objectId, 
        remoteAttributes.toArray(new String[remoteAttributes.size()]), om);
  }

  @SuppressWarnings("unchecked")
  synchronized public Iterable<? extends Object> getGroupMembership(String objectId) throws Exception {
    ArrayList<? extends Object> ret = null;
    Set<String> remoteAttributes = new HashSet<String>();
    remoteAttributes.add("memberOf");
    ret = (ArrayList<? extends Object>) template.lookup(objectId, 
        remoteAttributes.toArray(new String[remoteAttributes.size()]), new AbstractContextMapper() {
      protected Object doMapFromContext(DirContextOperations ctx) {
        ArrayList<String>  ret = null;
        String[] memberOfs = ctx.getStringAttributes("memberOf");
        if (memberOfs != null) {
          ret = new ArrayList<String>();
          for ( String memberOf : memberOfs) {
            ret.add(memberOf);
          }
        }
        return ret;
      }      
    });
    return ret;
  }  
  
  synchronized public void saveStateToDB() {
    lIdStore.setSyncState(null,connector, userCookie, null, null);
  }

  synchronized public void saveOperationResult(OperationType opType, OperationResult opResult, String resultString, 
      long numChangesFromIds, long numChangesToSvc) throws Exception {
    lIdStore.setOperationResult(opType, opResult, resultString, numChangesFromIds, numChangesToSvc, connector);
  }

  synchronized public State getUserState() {
    return userState;
  }

  synchronized public State getGroupState() {
    return State.POLL;
  }
  
  synchronized public State getContainerState() {
    return State.POLL;
  }
  
  synchronized public void resetStateToInitial() {
    init();
    lIdStore.resetStateToInitial(null,connector);
  }
  
  synchronized public void rollbackState() {
    userCookie = connector.getUserCookie();
    setState();
  }
  
  synchronized public Connector getConnector() {
    return connector;
  }

  synchronized public Connector refreshConnector() {
    return lIdStore.refreshConnector(connector);
  }
  
  synchronized public void setDirSpec(DirectorySpec ds) {
    this.ds = ds;
  }

  synchronized public Account getAccount() {
    return null;
  }
  
  synchronized private String getDnForObject(byte[] uuid) {
    String dn = null;
    String hexUuid = Util.byteArrayToHexString(uuid);
    String filterVal =  hexUuid.replaceAll("(.{2})", "\\\\$1");
    log.debug("getDnForObject: using filter objectGUID=" + filterVal);
    @SuppressWarnings("unchecked")
    List<Object> dnlist = template.search(connector.getBase(), "objectGUID="+filterVal, new AbstractContextMapper() {
      @Override
      protected Object doMapFromContext(DirContextOperations ctx) {
        return ctx.getNameInNamespace();
      }      
    });
    if (dnlist != null && dnlist.size() == 1) {
      dn = (String)dnlist.get(0);
    }
    return dn;
  }
  
  synchronized public boolean validateConfiguration() throws MethodNotSupportedException,
      InvalidConfigurationException {
    String errorReport = new String();
    for (DitVirtualizationEntry dvEntry : connector.getDvEntries()) {
      try {
        String dn = getDnForObject(dvEntry.getRemoteContainer().getUuid());
        if (dn == null) {
          errorReport += new String("Remote entry " + dvEntry.getRemoteContainer().getIdentifier() + " no longer exists\n");
        } else {
          if (LdapUtils.dnCompare(dn, dvEntry.getRemoteContainer().getIdentifier()) != 0) {
            errorReport += new String("Remote entry " + dvEntry.getRemoteContainer().getIdentifier() + " has moved\n");
          }
        }
      } catch (DataAccessException ex) {
        log.warn("LDAP search failed: " + ex);
        return false;
      } catch (LDAPException ex) {
        log.warn("LDAPException : " + ex);
        return false;
      }
    }

    for (RoleVirtualizationEntry rvEntry : connector.getRvEntries()) {
      try {
        String dn = getDnForObject(rvEntry.getRemoteRole().getUuid());
        if (dn == null) {
          errorReport += new String("Remote entry " + rvEntry.getRemoteRole().getIdentifier() + " no longer exists\n");
        } else {
          if (LdapUtils.dnCompare(dn, rvEntry.getRemoteRole().getIdentifier()) != 0) {
            errorReport += new String("Remote entry " + rvEntry.getRemoteRole().getIdentifier() + " has moved\n");
          }
        }
      } catch (DataAccessException ex) {
        log.warn("LDAP search failed: " + ex);
        return false;
      } catch (LDAPException ex) {
        log.warn("LDAPException : " + ex);
        return false;
      }
    }
    
    if (errorReport.length() > 0) {
      throw new InvalidConfigurationException(errorReport);
    }
    
    return true;
  }  
}
