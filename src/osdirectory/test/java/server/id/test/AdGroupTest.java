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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("unused")
public class AdGroupTest extends ActiveDirectoryTest{
  
  private String groupName;
  
  @Override
  protected void onSetUp() throws Exception {
    super.onSetUp();
    newAdUser = new AdUser("rahul");
    groupName = "TG4";
    //newAdUser = new AdUser("sachin");
  }    
  
  public void atestAddMappeddGroup() throws Exception{
	  //Add a new Group
	  ldapContact.AddNewGroup(groupName);
	  SyncData();
	  //Verify
	  List role = serviceDB.getGroupByName(groupName);
	  //we want to see the group coz it's Mapped
	  assertTrue("RoleFound", role.size() > 0);
  }
  public void atestAddMappeddGroupWithUser() throws Exception{
	  //Add a new Group
	  ldapContact.AddNewGroup(groupName);
	  
	  //Add User
	  AdUser fuser = ldapContact.findUser("userPrincipalName",newAdUser.getEmailAddress());
	  assertFalse("User Not Found to add to the group", fuser == null);
	  ldapContact.addToGroup(newAdUser, "CN="+groupName+",OU=Test,DC=Vijay,DC=com");
	  
	  SyncData();
	  //Verify
	  List role = serviceDB.getGroupByName(groupName);
	  //we want to see the group coz it's Mapped
	  assertTrue("RoleFound", role.size() > 0);
	  
	  //Verify the updated data
	  User user = getUser();
	  assertFalse("User Not Found",user == null);
	  List userRoles =serviceDB.getUserToRole(user.getUserId(), groupName);
		
	  //if it's a Mapped group the we should find the entry.
	  assertTrue("Role Synced for the user", userRoles.size() > 0 );
  }
  
  public void atestRemoveGroup() throws Exception{

	  ldapContact.removeGroup("cn="+groupName+",OU=Test,dc=vijay,dc=com");
	  SyncData();
	  List role = serviceDB.getGroupByName(groupName);
	  //we don't want to see the group 
	  assertFalse("RoleFound", role.size() > 0);
  }
  
  public void atestAddUnmappedGroup() throws Exception{
	  //Add a new Group
	  assertFalse("Group already exists", ldapContact.ifGroupExists(groupName) == true);
	  ldapContact.AddNewGroup(groupName);
	  SyncData();
	  List role = serviceDB.getGroupByName(groupName);
	  //we don't want to see the group coz it's not mapped
	  assertFalse("RoleFound", role.size() > 0);
  }
  public void testAddUnmappedGroupWithUser() throws Exception{
	  //Add a new Group
	  assertFalse("Group already exists", ldapContact.ifGroupExists(groupName) == true);
	  ldapContact.AddNewGroup(groupName);
	  
	  //Add User
	  AdUser fuser = ldapContact.findUser("userPrincipalName",newAdUser.getEmailAddress());
	  assertFalse("User Not Found to add to the group", fuser == null);
	  ldapContact.addToGroup(newAdUser, "CN="+groupName+",OU=Test,DC=Vijay,DC=com");
	  
	  SyncData();
	  List role = serviceDB.getGroupByName(groupName);
	  //we don't want to see the group coz it's not mapped
	  assertFalse("RoleFound", role.size() > 0);
	  
	  //Verify the updated data
	  User user = getUser();
	  assertFalse("User Not Found",user == null);
	  List userRoles =serviceDB.getUserToRole(user.getUserId(), groupName);
		
	  //if it's a UnMapped group the we should not find the entry.
	  assertTrue("Role Synced for the user", userRoles.size() == 0 );
  }
  public void atestSyncData(){
	//Sync Data
	  SyncData();
  }
 
}
