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
package server.id.krb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.security.krb5.Config;
import sun.security.krb5.KrbException;

public class KrbConfigManager {
  private final Log log = LogFactory.getLog(getClass());
  
  private String configFile;
  private String timeout = "5000";
  
  public KrbConfigManager() {
    configFile = System.getProperty("java.security.krb5.conf", "krb5.conf");
    log.debug("Config file set to " + configFile);
  }
    
  public void setTimeout(String timeout) {
    this.timeout = timeout;
  }
  
  public void updateConfigFile(Set<RealmInfo> realms) throws KrbException, IOException {
    File file = new File(configFile);
    try {
      file.createNewFile();
      log.debug("Config file exists or created a new one");
    } catch (IOException e) {
      log.warn("Could not create krb5.conf", e);
      throw e;
    }
    
    Writer out = null;
    synchronized (Config.getInstance()) {
      try {
        out = new BufferedWriter(new FileWriter(configFile));
        log.debug("Opened config file " + configFile);
        writeLibDefaults(out);
        writeAppDefaults(out);
        writeLogin(out);
        writeRealm(out, realms);
        writeDomainRealm(out);
        writeLogging(out);
        writeCapaths(out);
      } catch (IOException ioe) {
        log.warn("Failed to create krb5.conf", ioe);
        throw ioe;
      } finally {
        if (out != null) {
          try {out.close();} catch (Exception ex) {};
        }
      }
    }
    log.debug("Refreshing kerberos configuration");
    Config.refresh();
  }

  private void writeCapaths(Writer out) {
  }

  private void writeLogging(Writer out) throws IOException {
    out.write("[logging]\n");
    out.write("\tdefault = FILE:krb5.log\n");
  }

  private void writeDomainRealm(Writer out) {
  }

  private void writeRealm(Writer out, Set<RealmInfo> realms) throws IOException {
    out.write("[realms]\n");
    for (RealmInfo realm : realms) {
      out.write("\t" + realm.getKrbRealm().toUpperCase() + " = {\n");
      for (String kdc : realm.getKdc()) {
        out.write("\t\tkdc = " + kdc + "\n");
      }
      out.write("\t\tkdc_timeout = " + timeout + "\n"); //Since we only expect to be on LAN
      //out.write("max_retries = 2\n"); Config does not pay attention to this
      out.write("\t}\n");
      log.debug("Wrote realm " + realm.getKrbRealm().toUpperCase() + " to config file");
    }
    out.write("\n");
  }

  private void writeLogin(Writer out) {
  }

  private void writeAppDefaults(Writer out) {
  }

  private void writeLibDefaults(Writer out) throws IOException {
    out.write("[libdefaults]\n");
    out.write("\n");
  }
}
