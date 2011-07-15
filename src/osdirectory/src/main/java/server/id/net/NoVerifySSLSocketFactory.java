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
package server.id.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NoVerifySSLSocketFactory extends SocketFactory {
  private static SocketFactory socketFactory = null;
  
  /**
   * Builds an all trusting SSL socket factory.
   */
  static {
      // create a trust manager that will purposefully fall down on the
      // job
      TrustManager[] acceptAllTrustMan = new TrustManager[] { new X509TrustManager() {
          public X509Certificate[] getAcceptedIssuers() { return null; }
          public void checkClientTrusted(X509Certificate[] c, String a) { }
          public void checkServerTrusted(X509Certificate[] c, String a) { }
      } };

      // create our no verification SSL socket factory with our accepting trust manager
      try {
          SSLContext sc = SSLContext.getInstance("SSL");
          sc.init(null, acceptAllTrustMan, new java.security.SecureRandom());
          socketFactory = sc.getSocketFactory();
      } catch (GeneralSecurityException e) {
          e.printStackTrace();
      }
  }

  /**
   * @see javax.net.SocketFactory#getDefault()
   */
  public static SocketFactory getDefault() {
      return new NoVerifySSLSocketFactory();
  }

  public Socket createSocket() throws IOException {
    return socketFactory.createSocket();
  }

  @Override
  public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
    return socketFactory.createSocket(arg0, arg1);
  }

  @Override
  public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
    return socketFactory.createSocket(arg0, arg1);
  }

  @Override
  public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException,
      UnknownHostException {
    return socketFactory.createSocket(arg0, arg1, arg2, arg3);
  }

  @Override
  public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
    return socketFactory.createSocket(arg0, arg1, arg2, arg3);
  }

}
