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

import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextAdapter;

import server.id.Constants;
import server.id.IdObject;
import server.id.dao.ChangePoller;
import server.id.krb.KrbConfigManager;
import server.id.krb.RealmInfo;
import sun.security.krb5.KrbException;


/**
 * Abstract base class for PersonDao integration tests.
 * 
 * @author Shirish Rai
 */
public class SASLConnectionTest extends AbstractTest {
  private final Log log = LogFactory.getLog(getClass());
  ChangePoller idDao;
  KrbConfigManager krbMgr;

  static class RealmComparator<T extends RealmInfo> implements Comparator<T> {

    public int compare(T o1, T o2) {
      return o1.getKrbRealm().compareToIgnoreCase(o2.getKrbRealm());
    }    
  }
  
  public void testConnectToAD() {
    log.trace("testConnectToAD");
    System.setProperty("server.id.localIdStoreBean", "testLocalIdStore");
    System.setProperty("java.security.krb5.conf", "krb5.conf");
    System.setProperty("java.security.auth.login.config",
     (getClass().getResource("jaas.conf")).toExternalForm());
    log.info("java.security.krb5.conf set to " + System.getProperty("java.security.krb5.conf"));
    log.info("java.security.auth.login.config set to" + System.getProperty("java.security.auth.login.config"));
    try {
      TestConnector c = new TestConnector();
      krbMgr = (KrbConfigManager)applicationContext.getBean(Constants.getKrbManagerBeanName());
      TreeSet<RealmInfo> realms = new TreeSet<RealmInfo>(new RealmComparator<RealmInfo>());
      realms.add(c);
      try {
        krbMgr.updateConfigFile(realms);
      } catch (KrbException e) {
        log.trace("Could not update krb5.conf file", e);
      }
      log.debug("Wrote krb5.conf file");
      idDao = daoFactory.getChangePoller(applicationContext, null, c, null);
      
      log.debug("Got Idenity DAO for connector " + c.getName());
      
      List<IdObject> entries = idDao.getNextUserChangeSet();
      for (IdObject e : entries) {
        if (e == null) continue;
        System.out.println(e.getClass().getName());
        System.out.print(e);
      }
    } catch (RuntimeException ex) {
      log.error("Uncaught runtime exception:", ex);
      throw ex;
    } catch (Exception ex) {
      log.error("Uncaught checked exception:", ex);
    }
    finally {
    }
  }

//  public void testAnonymousConnectToAD() {
//    // AD does not allow anonymous access by default
//    findSASLFromRootDse();
//  }

  protected String[] findSASLFromRootDse() {
    DirContext ctx;
    String[] values = null;
    try {
      String ldap = "ldap://10.8.32.10:3268";
      Hashtable<String, String> env = new Hashtable<String, String>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, ldap);

      ctx = new InitialDirContext(env);
      // String ldap = assembleProviderUrlString(new String[] { getUrls()[0] });         
      try {
        // return c.context.getAttributes("ldap://" + c.host, attrIDs);
        Attributes attrs = ctx.getAttributes("");
        DirContextAdapter adapter = new DirContextAdapter(attrs, null, null);
        values = adapter.getStringAttributes("supportedSASLMechanisms");
        System.out.println(StringUtils.join(values, ' '));
      } catch (NamingException e) {
        // REMIND ttan
        e.printStackTrace();
      } finally {
        ctx.close();
      }
    } catch (NamingException e1) {
      e1.printStackTrace();
    }
    if (values == null) {
      throw new IllegalArgumentException("SASL mechanism cannot be found from RootDse.  Please specify it manually");
    }
    return values;
  }
}
