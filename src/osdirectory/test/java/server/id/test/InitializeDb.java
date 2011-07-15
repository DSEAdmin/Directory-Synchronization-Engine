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
package server.id.test;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.safehaus.uuid.UUID;
import org.springframework.orm.hibernate3.HibernateTemplate;

import server.id.AttributeTypeDef;
import server.id.AttributeVirtualization;
import server.id.dao.LocalIdentityStore;
import server.id.sync.agent.domain.Connector;
import server.id.sync.agent.domain.DitVirtualizationEntry;
import server.id.sync.agent.domain.RoleVirtualizationEntry;
import server.id.sync.server.SOAPSyncService;


public class InitializeDb extends AbstractTest {
  private final Log log = LogFactory.getLog(getClass());
  private LocalIdentityStore idStore;
  private SOAPSyncService syncService;
  private List<? extends Group> localContainers;
  private List<? extends Role> localRoles;
  
   static class DomainException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DomainException(String reason) {
      super(reason);
    }
  }
  
  private static class Constraint {    
    public Constraint(String name, String value) {
      super();
      this.name = name;
      this.value = value;
    }
    public String name;
    public String value;
  }
   
  private static class Dv {
    public Dv(String uuid, String lc, String rc, Constraint[] constraint) {
      super();
      this.uuid = uuid;
      this.rc = rc;
      this.lc = lc;
      this.constraint = constraint;
    }

    public String uuid;
    public String rc;
    public String lc;
    public Constraint[] constraint;
  }
  
  private static class Rv {
    public Rv(String uuid, String lr, String rr) {
      super();
      this.uuid = uuid;
      this.lr = lr;
      this.rr = rr;
    }

    public String uuid;
    public String lr;
    public String rr;
  }
  
  private static class CInfo {
    public CInfo(String uuid, String name, String realm, String host, String admin, String pw, String Admin,
         String base, String schedule, long retryCount, long retryInterval, Dv[] dvs, Rv[] rvs) {
      super();
      this.uuid = uuid;
      this.name = name;
      this.realm = realm;
      this.host = host;
      this.admin = admin;
      this.pw = pw;
      this.Admin = Admin;
      this.dvs = dvs;
      this.rvs = rvs;
      this.base = base;
      this.schedule = schedule;
      this.retryCount = retryCount;
      this.retryInterval = retryInterval;
    }

    public String uuid;
    public String name;
    public String realm;
    public String host;
    public String admin;
    public String pw;
    public String Admin;
    String base;
    String schedule;
    long retryCount;
    long retryInterval;
    Dv[] dvs;
    Rv[] rvs;
  }
  
  private CInfo shirishLocalVm = new CInfo("0913d586-e3f9-4874-8b15-d0f1b2e5c7b2", "shirishLocalVm", "SHIRISH.COM",
      "192.168.30.10", "Administrator@SHIRISH.COM", "Secret00", "LocalAdmin", "dc=shirish,dc=com", "0 * * * * ?",
      1, 0, 
      new Dv[] 
             {new Dv("4c857f96-ffac-4e56-82d5-c3e106846d8a", "shirish", "dc=shirish,dc=com", 
                 new Constraint[]
                      {new Constraint(AttributeVirtualization.RV_ENTRY, "783e16cd-effe-49ee-8f3b-c14f25b8824b")
                      }
               )
             },
      new Rv[] 
             {new Rv("72066f85-ec16-4c62-a074-1ca1cc654415", "Administrators", "cn=Administrators, cn=builtin, dc=shirish, dc=com"),
             new Rv("783e16cd-effe-49ee-8f3b-c14f25b8824b", "TG1", "CN=TG1,OU=Test,DC=shirish,DC=com"),
             new Rv("efa6edb1-496f-4639-b71f-d86b2a2897f7", "TG2", "CN=TG2,OU=Test,DC=shirish,DC=com")
             }
  );
  
  private CInfo[] cinfoList = {shirishLocalVm};
  
  private byte[] getLocalContainerUuid(String name) {
    for (Group g : localContainers) {
      if (g.getIdentifier().equalsIgnoreCase(name)) {
        return g.getUuid();
      }
    }
    return null;
  }
  
  private byte[] getLocalRoleUuid(String name) {
    for (Role r : localRoles) {
      if (r.getIdentifier().equalsIgnoreCase(name)) {
        return r.getUuid();
      }
    }
    return null;
  }
  
  private byte[] getUuidForEntry(CInfo ci, String dn) throws NamingException {
    try {
      byte[] uuid = null;
      Hashtable<String, String> env = new Hashtable<String, String>();
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.SECURITY_PRINCIPAL, ci.admin);
      env.put(Context.SECURITY_CREDENTIALS, ci.pw);
      env.put(Context.PROVIDER_URL, "ldap://" + ci.host);
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put("java.naming.ldap.attributes.binary", "objectGUID");
      DirContext ctx = new InitialLdapContext(env, null);
      SearchControls searchCtls = new SearchControls();

      // Specify the attributes to return
      String returnedAtts[] = { "objectGUID" };
      searchCtls.setReturningAttributes(returnedAtts);

      // Specify the search scope
      searchCtls.setSearchScope(SearchControls.OBJECT_SCOPE);

      // specify the LDAP search filter
      String searchFilter = "(objectClass=*)";

      // Specify the Base for the search
      String searchBase = dn;

      // Search for objects using the filter
      NamingEnumeration<?> answer = ctx.search(searchBase, searchFilter, searchCtls);

      // Loop through the search results
      while (answer.hasMoreElements()) {
        SearchResult sr = (SearchResult) answer.next();

        Attribute attr = sr.getAttributes().get("objectGUID");
        if (attr != null) {
          uuid = (byte[]) attr.get();
        }
      }
      return uuid;
    } catch (NamingException e) {
      log.error("NamingExceptin", e);
      throw e;
    }
  }
  
  private Connector getConnector(CInfo ci) throws Exception {
    Connector c = new Connector();
    
    c.setMajorVersion(1);
    c.setMinorVersion(0);
    c.setType(IdentityStoreType.ACTIVE_DIRECTORY);
    c.setName(ci.name);
    c.setUuid(new UUID(ci.uuid).asByteArray());
    c.setRealm(ci.realm);
    c.setPrimaryHost(ci.host);
    c.setPrimaryPort(389);
    c.setBackupHost(null);
    c.setBackupPort(389);
    c.setGc(false);
    c.setAdminName(ci.admin);
    c.setPassword(ci.pw);
    c.setAdminName(ci.Admin);
    c.setProtocol("ldap");
    c.setMode(OperationMode.AGENT_SYNCHRONIZATION);
    if (ci.rvs != null) {
      Set<RoleVirtualizationEntry> rvEntries = new HashSet<RoleVirtualizationEntry>();
      for (Rv rv : ci.rvs) {
        RoleVirtualizationEntry rvEntry = new RoleVirtualizationEntry();
        rvEntry.setConnector(c);
        rvEntry.setLocalRole(rv.lr);
        rvEntry.setLocalRoleUuid(getLocalRoleUuid(rv.lr));
        rvEntry.setRemoteRole(rv.rr);
        rvEntry.setRemoteRoleUuid(getUuidForEntry(ci, rv.rr));
        rvEntry.setUuid(new UUID(rv.uuid).asByteArray());
        rvEntries.add(rvEntry);
      }
      c.setRvEntries(rvEntries);
    }
    if (ci.dvs != null) {
      SortedSet<DitVirtualizationEntry> dvEntries = new TreeSet<DitVirtualizationEntry>();
      int i = 0;
      for (Dv dv : ci.dvs) {
        i += 1;
        DitVirtualizationEntry dvEntry = new DitVirtualizationEntry();        
        dvEntry.setConnector(c);
        dvEntry.setLocalContainer(dv.lc);
        dvEntry.setLocalContainerUuid(getLocalContainerUuid(dv.lc));
        dvEntry.setOrdinalId(i);
        dvEntry.setRemoteContainer(dv.rc);
        dvEntry.setRemoteContainerUuid(getUuidForEntry(ci, dv.rc));
        dvEntry.setUuid(new UUID(dv.uuid).asByteArray());
        dvEntries.add(dvEntry);
      }
      c.setDvEntries(dvEntries);
    }
    c.setBase(ci.base);
    c.setSyncSchedule(ci.schedule);
    c.setUserCookie(null);
    c.setGroupCookie(null);
    c.setContainerCookie(null);
    c.setChangeNumber(0);
    c.setPageSize(0);
    c.setAutoCreateContainers(true);
    c.setRetryCount(ci.retryCount);
    c.setRetryInterval(ci.retryInterval);
    c.setSyncStatus(SyncStatus.FULL_SYNC_IN_PROGRESS);
    return c;
  }

  private void addConstraintsToDv(CInfo ci, Connector c) {
    if (ci.dvs == null)
      return;
    HibernateTemplate hibernateTemplate = (HibernateTemplate)idStore.getDelegate();
    SortedSet<? extends DitVirtualizationEntry> dvEntries = c.getDvEntries();
    for (DitVirtualizationEntry dve : dvEntries) {
      DitVirtualizationEntry dv = (DitVirtualizationEntry)dve;
      for (int i = 0; i < ci.dvs.length; ++i) {
        if (Arrays.equals((new UUID(ci.dvs[i].uuid).asByteArray()), dv.getUuid()) == true) {
          if (ci.dvs[i].constraint.length > 0) {
            Set<server.id.sync.agent.domain.Constraint> constraints = null;
            constraints = new HashSet<server.id.sync.agent.domain.Constraint>();
            for (Constraint con : ci.dvs[i].constraint) {
              server.id.sync.agent.domain.Constraint constraint = new 
                server.id.sync.agent.domain.Constraint(con.name, AttributeTypeDef.Type.BINARY, 
                    new UUID(con.value).asByteArray(), dv);
              hibernateTemplate.save(constraint);
              constraints.add(constraint);
            }
            dv.setConstraints(constraints);
          }          
        }
      }
    }
  }
  
  public void testClearAndInitDb() throws Exception {
    log.trace("testClearAndInitDb");
    System.setProperty("server.id.localIdStoreBean", "testLocalIdStore");
    try {
      idStore = (LocalIdentityStore)applicationContext.getBean("testLocalIdStore");
      if (idStore == null) {
        log.error("Could not get local identity store bean");
        throw new DomainException("Could not get local Identity store bean");
      }
      syncService = (SOAPSyncService)applicationContext.getBean("soapSyncServiceClient");
      if (syncService == null) {
        log.error("Could not get syncClient");
        throw new DomainException("Could not get syncClient");
      }

      localContainers = syncService.getLocalContainers();
      localRoles = syncService.getLocalRoles();
      
      for (CInfo ci : cinfoList) {
        Connector c = getConnector(ci);
        idStore.saveConnector(null, c);
        c = (Connector) idStore.getConnectorByName(null, ci.name);
        addConstraintsToDv(ci, c);
      }
    } catch (RuntimeException ex) {
      log.error("Uncaught runtime exception:", ex);
      throw ex;
    } catch (Exception ex) {
      log.error("Uncaught checked exception:", ex);
      throw ex;
    }
    finally {
    }
  }

}
