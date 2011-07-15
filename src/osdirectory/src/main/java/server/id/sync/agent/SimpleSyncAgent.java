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
package server.id.sync.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SimpleSyncAgent {
  private ClassPathXmlApplicationContext applicationContext;
  private final Log log = LogFactory.getLog(getClass());

  private String[] getConfigLocations() {
    return new String[] { "server/id/IdentityBeans.xml", 
        "server/id/AgentBeans.xml",
        "config/appTestContext.xml" };
  }

  public SimpleSyncAgent() {
    log.info("Creating application context");
    String[] config = getConfigLocations();
    applicationContext = new ClassPathXmlApplicationContext(config);
    log.info("Done creating application context : " + applicationContext);
  }
  
  public static void main(String[] args) throws Exception {
    @SuppressWarnings("unused")
    SimpleSyncAgent ssa = new SimpleSyncAgent();
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    try {
      String input;
      System.out.println("Press any key to exit");
      input = in.readLine();
      System.out.println("Exiting on input " + input);
    } catch (IOException e) {
      System.err.println();
    }
  }
}
