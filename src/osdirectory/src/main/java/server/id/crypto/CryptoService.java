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
package server.id.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CryptoService {
  private final Log log = LogFactory.getLog(getClass());
  private String jcaProviderName;
  private String jceProviderName;

  public CryptoService() {
  }

  public boolean init() {
    // check if provider is already installed
    Provider prov = Security.getProvider("BC");

    log.debug("BC provider not installed. Loading...");
    // add BouncyCastle provider
    if (prov == null) {
      prov = new org.bouncycastle.jce.provider.BouncyCastleProvider();
      if (prov != null) {
    	log.debug("BC provider instantiated");
      } else {
    	log.warn("BC provider could not be instantiated");
      }
      Security.addProvider(prov);
    }

    jcaProviderName = prov.getName();
    jceProviderName = prov.getName();

    if (Security.getProvider(jcaProviderName) == null) {
      log.error("Failed to add BouncyCastle provider");
      return false;
    } else {
      log.debug("Added BouncyCastle provider : " + jcaProviderName + " , " + jceProviderName);
    }

    return true;
  }

  public byte[] getDigest(String algorithm, byte[] data) throws NoSuchAlgorithmException, NoSuchProviderException {
    MessageDigest md = MessageDigest.getInstance(algorithm, jcaProviderName);
    md.update(data);
    return md.digest();
  }
}
