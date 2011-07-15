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
/*
 * Copyright 2008 the original author or authors.
 */

package server.id.ldap;

import java.security.PrivilegedActionException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;

import server.id.AttributeVirtualization;
import server.id.net.NoVerifySSLSocketFactory;


/**
 * @author Shirish Rai
 */
public class LdapContextSource extends LdapContextSource {

  private final Log log = LogFactory.getLog(getClass());

  protected Connector connector;
  protected AttributeVirtualization av;
  protected String saslMechinism;

  public LdapContextSource(Connector connector, AttributeVirtualization av) throws Exception {
    this.connector = connector;
    this.av = av;
    // Since we only have AD for now, we can do this
    saslMechinism = new String("GSSAPI");
    setUrls(getLdapURLs());
    setAuthenticationSource(new MyAuthenticationSource());
    if (connector.getProtocol().equals(Connector.LDAP_WITH_KRB)) {
      setAuthenticationStrategy(new KrbAuthStrategy());
    } else if (connector.getProtocol().equals(Connector.LDAPS)) {
      setAuthenticationStrategy(new LdapsAuthStrategy());
    } else {
      setAuthenticationStrategy(new LdapAuthStrategy());
    }
    setCacheEnvironmentProperties(false);
    super.afterPropertiesSet();
  }

  class LdapsAuthStrategy implements DirContextAuthenticationStrategy {

    public DirContext processContextAfterCreation(DirContext ctx, String userDn, String password)
        throws NamingException {
      return ctx;
    }
    
    @SuppressWarnings("unchecked")
    public void setupEnvironment(Hashtable env, String userDn, String password) throws NamingException {
      log.trace("LdapsAuthStrategy.setupEnvironment");
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.SECURITY_PRINCIPAL, userDn);
      env.put(Context.SECURITY_CREDENTIALS, password);
      env.put("java.naming.ldap.factory.socket", NoVerifySSLSocketFactory.class.getName());
      log.debug("Set auth mechanism in setupAuthenticationStrategy");      
    }    
  }
  
  class LdapAuthStrategy implements DirContextAuthenticationStrategy {

    public DirContext processContextAfterCreation(DirContext ctx, String userDn, String password)
        throws NamingException {
      return ctx;
    }
    
    @SuppressWarnings("unchecked")
    public void setupEnvironment(Hashtable env, String userDn, String password) throws NamingException {
      log.trace("LdapAuthStrategy.setupEnvironment");
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.SECURITY_PRINCIPAL, userDn);
      env.put(Context.SECURITY_CREDENTIALS, password);
      log.debug("Set auth mechanism in setupAuthenticationStrategy");      
    }    
  }
  
  class KrbAuthStrategy implements DirContextAuthenticationStrategy {
    
    public KrbAuthStrategy() {}
    
    public DirContext processContextAfterCreation(DirContext ctx, String userDn, String password)
        throws NamingException {
      return ctx;
    }

    @SuppressWarnings("unchecked")
    public void setupEnvironment(Hashtable env, String userDn, String password) throws NamingException {
      log.trace("KrbAuthStrategy.setupEnvironment");
      env.put(Context.SECURITY_AUTHENTICATION, saslMechinism);
      env.put("javax.security.sasl.qop", "auth-conf");
      log.debug("Set auth mechanism in setupAuthenticationStrategy");      
    }
    
  }
  
  // System.setProperty("java.security.krb5.realm", keberosRealm.toUpperCase());
  // System.setProperty("java.security.krb5.kdc", keberosKdc);
  // System.setProperty("java.security.auth.login.config",
  // (getClass().getResource("jaas.conf")).toExternalForm());

  private String[] getLdapURLs() {
    String[] urls;
    boolean ssl = false;
    if (connector.getProtocol().equals(Connector.LDAPS)) {
      ssl = true;
    }
    if (connector.getBackupHost() == null || connector.getBackupHost().length() == 0) {
      urls = new String[1];
      urls[0] = LdapUtils.buildLDAPUrl(connector.getPrimaryHost(), connector.getPrimaryPort(), ssl);
      log.debug("Setting ldap url to " + urls[0]);
    } else {
      urls = new String[2];
      urls[0] = LdapUtils.buildLDAPUrl(connector.getPrimaryHost(), connector.getPrimaryPort(), ssl);
      urls[0] = LdapUtils.buildLDAPUrl(connector.getBackupHost(), connector.getBackupPort(), ssl);
      log.debug("Setting ldap url to " + urls[0]);
      log.debug("Setting ldap backup url to " + urls[1]);
    }
    return urls;
  }

  @SuppressWarnings("unchecked")
  protected DirContext getDirContextInstance(Hashtable environment) throws NamingException {
    log.trace("getDirContextInstance");
    InitialLdapContext ctx = null;
    
    Set<String> binAttrs = av.getAllBinaryRemoteAttributes();
    if (binAttrs != null && binAttrs.size() > 0) {
      Iterator<String> it = binAttrs.iterator();
      String envVal = it.next();
      while(it.hasNext())
        envVal += " " + it.next();
      log.debug("Set env java.naming.ldap.attributes.binary to : \"" + envVal + "\"");
      environment.put("java.naming.ldap.attributes.binary", envVal);
    }
    
    if (connector.getProtocol().equals(Connector.LDAP_WITH_KRB)) {
      LoginContext lc = null;
      try {
        log.debug("Setting up AuthenticationSource");

        lc = new LoginContext(getClass().getName(), new JaasCallbackHandler(getAuthenticationSource().getPrincipal(),
            getAuthenticationSource().getCredentials()));
        log.debug("Created loginContext");

        lc.login();
        log.debug("Login successful");
      } catch (LoginException le) {
        log.error("Exception while performing Kerberos Authentication", le);
        throw new AuthenticationException(le.toString());
      }

      // 2. Perform JNDI work as logged in subject
      try {
        ctx = (InitialLdapContext) Subject.doAs(lc.getSubject(), new JndiAction(environment));
      } catch (PrivilegedActionException e) {
        NamingException ne = (NamingException) e.getException();
        throw ne;
      }
    } else {
      ctx = createInitialContext(environment);
    }
    log.debug("Successfully craeted ldap context");
    return ctx;
  }

  private InitialLdapContext createInitialContext(Hashtable<?,?> env) throws NamingException {
    return new InitialLdapContext(env, null);
  }

  class MyAuthenticationSource implements AuthenticationSource {

    public MyAuthenticationSource() {
    }

    public String getCredentials() {
      return connector.getPassword();
    }

    public String getPrincipal() {
      return connector.getAdminName();
    }
  }

  class JndiAction implements java.security.PrivilegedExceptionAction<InitialLdapContext> {

    private Hashtable<?,?> env;

    public JndiAction(Hashtable<?,?> env) {
      this.env = env;
    }

    public InitialLdapContext run() throws NamingException {
      try {
        log.trace("Creating ldap context");
        return createInitialContext(env);
      } catch (NamingException e) {
        log.error("NamingException while creating LdapContext", e);
        throw e;
      }
    }
  }
}
