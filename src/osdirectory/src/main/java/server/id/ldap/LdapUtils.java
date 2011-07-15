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
package server.id.ldap;

import org.apache.commons.lang.StringUtils;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;

public class LdapUtils {

  private LdapUtils() {
    ;
  }

  public static String buildLDAPUrl(String host, int port, boolean ssl) {
    StringBuffer url = new StringBuffer();
    if (ssl == true) url.append("ldaps://"); else url.append("ldap://");
    url.append(host);
    url.append(":");
    url.append(Integer.toString(port));
    return url.toString();
  }
  
  public static boolean isUpn(String address) {
    char c;
    int aidx = -1;

    for (int i = 0; i < address.length(); i++) {
      c = address.charAt(i);
      if (Character.isWhitespace(c)) {
        return false;
      } else if (c == '@') {
        aidx = i;
      }
    }

    if ((aidx == -1) || (aidx == 0) || (aidx == (address.length() - 1))) {
      return false;
    }
    return true;
  }

  public static String getUpnPrefix(String address) {
    return StringUtils.substringBefore(address, "@");
  }
  
  public static String normalizeDn(String dn) throws LDAPException {
    return DN.normalize(dn);
  }
  
  public static int dnCompare(String dn1, String dn2) throws LDAPException {
    return DN.compare(dn1, dn2);
  }
}
