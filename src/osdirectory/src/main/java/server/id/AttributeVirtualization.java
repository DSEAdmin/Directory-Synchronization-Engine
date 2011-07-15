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

package server.id;

import java.util.Set;

import server.id.dao.ChangePoller;


/**
 * @author Shirish Rai
 */
public interface AttributeVirtualization {

  public static final String USERID = "user.id";
  public static final String MEMBER = "group.member";
  public static final String MEMBER_OF = "user.memberOf";
  public static final String RV_ENTRY = "object.rvEntryUuid";
  public static final String DV_ENTRY = "object.dvEntryUuid";
  public static final String CREATETIME = "object.createTimestamp";
  public static final String MODIFYTIME = "object.modifyTimestamp";
  public static final String OBJECTTYPE = "object.type";
  public static final String USERSHORTID = "user.shortId";
  public static final String DN = "object.dn";
  public static final String NDN = "object.normalizedDn";
  public static final String EMAIL = "user.email";
  public static final String UUID = "object.uuid";
  public static final String ACCT_CONTROL = "user.accountControl";
  public static final String NAME = "object.name";
  public static final String PARENT_UUID = "object.parentUuid";
  public static final String DELETED = "object.deleted";
  public static final String LAST_PARENT_DN = "object.lastKnownParent";
  public static final String CHANGE_TYPE = "object.changeType";
  public static final String DISABLED = "user.isDisabled";
  public static final String FNAME = "user.firstName";
  public static final String LNAME = "user.lastName";
  
  public AttributeTransform getAttributeTransform(String virtAttr);
  public Set<String> getAllRemoteAttributes();
  public Set<String> getAllBinaryRemoteAttributes();
  public Set<String> getAllVirtualAttributes();
  public Entry virtualize(ChangePoller changePoller, Entry remoteEntry) throws Exception;
}
