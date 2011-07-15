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
package server.id.dao;

import java.util.Map;

import org.springframework.beans.factory.BeanFactory;

import server.id.sync.EventProcessor;



public interface IdentityDAOFactory {
  public static final String ldifFileName = "server.id.ldifFileName";
  public static final String changeRequest = "server.id.changeRequest";
  
  public static enum DeploymentType {
    AGENT,
    SERVER,
    SERVICE
  }

  public ChangePoller getChangePoller(BeanFactory bf, Account account, Connector c, Map<String, Object> options) throws
    Exception;
  public EventProcessor getEventProcessor(BeanFactory bf, Account account, Connector c, Map<String, Object> options) throws Exception;
}
