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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;


@SuppressWarnings("unused")
public class AdUserTest extends ActiveDirectoryTest{
  @Override
  protected void onSetUp() throws Exception {
    super.onSetUp();
    //newAdUser = new AdUser("rahul");
    newAdUser = new AdUser("sachin");
  }    
  
  //TEST CASE: Add User with all atributes.(First name, last name, email, sAMAccountName(username) )
  public void atestAddUser() throws UnsupportedEncodingException{
	newAdUser.setStatus("active");
	ldapContact.insertContact(newAdUser);
	SyncData();
	//Verify the synced user data.
	verifyUserData();
  }

  
  //TEST CASE: Delete User
  public void atestDeleteUser() throws Exception{
	  ldapContact.deleteContact(newAdUser);
	  SyncData();
	  User user = getUser();
	  assertTrue("User Deleted",user == null);
  }
  
  //TEST CASE: Add User with few atributes.(First name, last name)
  @SuppressWarnings("unchecked")
  public void atestAddUserWithFewAttributes() throws Exception{
	Hashtable <String, String> updateFields = new Hashtable();
	updateFields.put("sn",newAdUser.getLastName());
	updateFields.put("givenName",newAdUser.getFirstName());
	newAdUser.setStatus("active");
	ldapContact.insertContact(newAdUser,updateFields);
	SyncData();
	User user = getUser();
	if(user != null){
	  System.out.println("Checking user_invite table..");
	  Set<UserInvite> userInvites = user.getUserInvites();
	  // Iterating over the elements in the set
	  Iterator<UserInvite> ui = userInvites.iterator();
	  while (ui.hasNext()) {
	      // Get element
	      UserInvite uInvite = (UserInvite) ui.next();
	      assertEquals("",uInvite.getEmail());
	      assertEquals(newAdUser.getFirstName(),uInvite.getFirst());
	      assertEquals(newAdUser.getLastName(),uInvite.getLast());
	  }
	  System.out.println("Everything is fine.");
	}
  }
  
  //TEST CASE: Add Inactive User with all atributes.(First name, last name, email, sAMAccountName(username) )
  public void atestAddInactiveUser() throws UnsupportedEncodingException{
	ldapContact.insertContact(newAdUser);
	SyncData();
	verifyUserData();
  }
  
  //TEST CASE: Rename 
  public void atestRenameAttribs() throws Exception {
	  String oldCn = "Rahul New Dravid";
	  newAdUser.setCommonName("Rahul Dravid");
	  ldapContact.renameAttributes("CN="+oldCn+",CN=Users,DC=vijay,dc=com","CN="+newAdUser.getCommonName()+",CN=Users,DC=vijay,dc=com");
  }

  //TEST CASE: Add/Modify Attributes
  @SuppressWarnings("unchecked")
  public void atestUpdateUserAttribs() throws Exception{
	Hashtable <String, String> updateFields = new Hashtable();
	String sn = "Test";
	String givenName = "Abcd";
	String mail = "rdravid@india.com";
	
	//updateFields.put("sn",sn);
	//updateFields.put("givenName",givenName);
	updateFields.put("userPrincipalName",mail);
	String cn = newAdUser.getCommonName();
	ldapContact.updateAttributes(cn, updateFields,"update");
	
	//Sync Data
	SyncData();
	
	//Verify the updated data
	User user = getUser(); 
	if(user != null){
		List userInvites = serviceDB.getUserInviteByUserName(newAdUser.getUserName());
		Object[] row =  (Object[]) userInvites.get(0);
		String firstName = (String) row[0];
		String lastName = (String) row[1];
		String email = (String) row[2];
		String status = (String) row[3];
		String statuShortName= (String) row[4];
		//System.out.println(Arrays.toString(row));
		//assertEquals(sn, lastName);
		//assertEquals(givenName, firstName);
		assertEquals(mail, email);
	}else{
		new AssertionError("User Not Found");	
	}
  }

  //TEST CASE: Remove Attributes
  @SuppressWarnings("unchecked")
  public void atestRemoveUserAttribs() throws Exception{
	Hashtable <String, String> updateFields = new Hashtable();
	
	String sn = "Test";
	String givenName = "Abcd";
	String mail = "";
	
	//updateFields.put("sn",sn);
	//updateFields.put("givenName",givenName);
	updateFields.put("mail",mail);
	ldapContact.removeAttributes(newAdUser.getCommonName(), updateFields);
	
	//Sync Data
	SyncData();
	
	//Verify the updated data
	User user = getUser(); 
	if(user != null){
		List userInvites = serviceDB.getUserInviteByUserName(newAdUser.getUserName());
		Object[] row =  (Object[]) userInvites.get(0);
		String firstName = (String) row[0];
		String lastName = (String) row[1];
		String email = (String) row[2];
		String status = (String) row[3];
		String statuShortName= (String) row[4];
		//System.out.println(Arrays.toString(row));
		//assertEquals(sn, lastName);
		//assertEquals(givenName, firstName);
		assertEquals(mail, email);
	}else{
		new AssertionError("User Not Found");	
	}
  }
 
  public void atestFindUser() throws Exception{
	  //AdUser fuser = ldapContact.findUser("userPrincipalName",newAdUser.getEmailAddress());
	  AdUser fuser = ldapContact.findUserInGroup(newAdUser.getCommonName(),"TG1");
	  //List us = ldapContact.getContactDetails("Rahul Dravid","Dravid");
	  //boolean x = ldapContact.ifGroupExists("TG3");
	  //System.out.println("RES: "+ x);
  }
  //TEST CASE: Add to a Mapped object. TG1&TG2 are mapped roles.
  public void testAddToMappedGroup() throws Exception{
	  String group = "TG1";
	  AdUser fuser = ldapContact.findUser("userPrincipalName",newAdUser.getEmailAddress());
	  if(fuser != null){
		ldapContact.addToGroup(newAdUser, "CN="+group+",OU=Test,DC=Vijay,DC=com");
	  
		SyncData();
	  
		//Verify the updated data
		User user = getUser(); 
		if(user != null){
			List userRoles =serviceDB.getUserToRole(user.getUserId(), group);
			
			//if it's a Mapped group the we should find the entry.
			assertTrue("Role Synced for the user", userRoles.size() > 0 );
		}else{
			assertFalse("User Not Found",1==1);	
		}
	  }else{
		  System.out.println("User not found in AD. Something is wrong");
	  }
  }

  //TEST CASE: Add to out of scope object. TG3 is an unmapped role. 
  public void atestAddToUnmappedGroup() throws Exception{
	  String group = "TG3";
	  if(ldapContact.ifGroupExists(group)){
		 ldapContact.addToGroup(newAdUser, "CN="+group+",OU=Test,DC=Vijay,DC=com");
	  
		 SyncData();
	  
		//Verify the updated data
		User user = getUser(); 
		if(user != null){
			List userRoles =serviceDB.getUserToRole(user.getUserId(), group);
			//if it's a Un Mapped group the we should not find the entry.
			assertTrue("Role Synced for the user", userRoles.size() == 0 );
		}else{
			new AssertionError("User Not Found");	
		}
	  }else{
		  System.out.println("Group " + group + "doesn't exist. Please create the group!");
		  assertFalse("Group doesn't exist",1==1);
	  }
  }
  
  //TEST CASE: Delete from a Mapped role TG1 or TG2 (Make sure user is assigned that role)
  public void atestDeleteFromGroup() throws Exception{
	  String group="TG2";
	  //Check if user belongs to the group
	  assertFalse("Group provided doesn't exist." , ldapContact.ifGroupExists(group) == false);
	  AdUser fuser = ldapContact.findUserInGroup(newAdUser.getCommonName(),group);
	  assertFalse("User doesn't belong to the group "+group,fuser == null);
	  
	  //Now do a delete
	  ldapContact.DeleteFromGroup(newAdUser, "CN="+group+",OU=Test,DC=Vijay,DC=com");
	  //Now do a search again
	  AdUser user = ldapContact.findUserInGroup(newAdUser.getCommonName(),group);
	  assertTrue("User Successfully deleted from group "+group, user == null);
	  
	  SyncData();
	  
	  //Verify the updated data
	  User suser = getUser();
	  assertFalse("User not found on the Services", suser == null);
	  
	  List userRoles =serviceDB.getUserToRole(suser.getUserId(), group);
	  
	  assertTrue("Role Synced for the user", userRoles.size() == 0 );
  }

  //TEST CASE: Replace user from a Mapped role to another Mapped role TG2 to TG1
  public void atestReplaceMappedToMapped() throws Exception{
	  String fromGroup = "TG2"; //or TG1 are the mapped roles
	  String toGroup = "TG1";
	  assertFalse("From group provided doesn't exist." , ldapContact.ifGroupExists(fromGroup) == false);
	  assertFalse("To group provided doesn't exist." , ldapContact.ifGroupExists(toGroup) == false);
	  ldapContact.ReplaceToGroup(newAdUser, "CN="+fromGroup+",OU=Test,DC=Vijay,DC=com","CN="+toGroup+",OU=Test,DC=Vijay,DC=com");
	  //Sync Data
	  SyncData();
	  
	  //Since we have assigned a Mapped role we should see not see the role on services for the user for group 'fromGroup'
	  //Verify the updated data
	  User suser = getUser();
	  assertFalse("User not found on the Services", suser == null);
	  
	  //Since it's replaced from a Mapped role, this entry should be updated in services and we should not see that entry.
	  List userRoles =serviceDB.getUserToRole(suser.getUserId(), fromGroup);
	  assertTrue("Role Synced for the user Mapped to UnMapped", userRoles.size() == 0 );
	  
	  //Since it's replaced to a mapped role we should see it in services.
	  List userToRoles =serviceDB.getUserToRole(suser.getUserId(), toGroup);
	  assertTrue("Role Synced for the user Mapped to UnMapped", userToRoles.size() > 0 );
  }

  //TEST CASE: Replace user from a Mapped role to another UnMapped role TG2 to TG3
  public void atestReplaceMappedToUnmappedGroup() throws Exception{
	  String fromGroup = "TG1";
	  String toGroup = "TG3";
	  assertFalse("From group provided doesn't exist." , ldapContact.ifGroupExists(fromGroup) == false);
	  assertFalse("To group provided doesn't exist." , ldapContact.ifGroupExists(toGroup) == false);
	  ldapContact.ReplaceToGroup(newAdUser, "CN="+fromGroup+",OU=Test,DC=Vijay,DC=com","CN="+toGroup+",OU=Test,DC=Vijay,DC=com");
	  //Sync Data
	  SyncData();
	  
	  //Since we have assigned a Mapped role we should not see the role on services for the user for group 'fromGroup'
	  //Verify the updated data
	  User suser = getUser();
	  assertFalse("User not found on the Services", suser == null);
	  
	  //Since it's replaced from a Mapped role this entry should be updated in services and we should not see that entry.
	  List userRoles =serviceDB.getUserToRole(suser.getUserId(), fromGroup);
	  assertTrue("Role Synced for the user Mapped to UnMapped", userRoles.size() == 0 );
	  
	  //Since it's replaced to an unmapped role we should not see it in services.
	  List userToRoles =serviceDB.getUserToRole(suser.getUserId(), toGroup);
	  assertTrue("Role Synced for the user Mapped to UnMapped", userToRoles.size() == 0 );
  }
  
  //TEST CASE: Replace user from a UnMapped role to another UnMapped role TG3 to TG4
  public void atestReplaceUnmappedToUnmapped() throws Exception{
	  String fromGroup = "TG3";
	  String toGroup = "TG4";
	  assertFalse("From group provided doesn't exist." , ldapContact.ifGroupExists(fromGroup) == false);
	  assertFalse("To group provided doesn't exist." , ldapContact.ifGroupExists(toGroup) == false);
	  ldapContact.ReplaceToGroup(newAdUser, "CN="+fromGroup+",OU=Test,DC=Vijay,DC=com","CN="+toGroup+",OU=Test,DC=Vijay,DC=com");
	  
	  //Sync Data
	  SyncData();
	  
	  //Since we have assigned an UnMapped role we should not see the role on services for the user
	  //Verify the updated data
	  User suser = getUser();
	  assertFalse("User not found on the Services", suser == null);
	  
	  //Since it's replaced from an unmapped role we should not see that entry.
	  List userRoles =serviceDB.getUserToRole(suser.getUserId(), fromGroup);
	  assertTrue("Role Synced for the user Mapped to UnMapped", userRoles.size() == 0 );
	  
	  //Since it's replaced to an unmapped role we should not see it in services.
	  List userToRoles =serviceDB.getUserToRole(suser.getUserId(), toGroup);
	  assertTrue("Role Synced for the user Mapped to UnMapped", userToRoles.size() == 0 );
  }
  
  //TEST CASE: Replace user from a UnMapped role to another Mapped role TG3 to TG2
  public void atestReplaceUnmappedToMapped() throws Exception{
	  String fromGroup = "TG3";
	  String toGroup = "TG2";
	  assertFalse("From group provided doesn't exist." , ldapContact.ifGroupExists(fromGroup) == false);
	  assertFalse("To group provided doesn't exist." , ldapContact.ifGroupExists(toGroup) == false);
	  ldapContact.ReplaceToGroup(newAdUser, "CN="+fromGroup+",OU=Test,DC=Vijay,DC=com","CN="+toGroup+",OU=Test,DC=Vijay,DC=com");
	  
	  //Sync Data
	  SyncData();
	  
	  //Since we have assigned a Mapped role we should see the role on services for the user
	  //Verify the updated data
	  User suser = getUser();
	  assertFalse("User not found on the Services", suser == null);
	  
	  //Since it's replaced from a unmapped role we should not see that entry.
	  List userRoles =serviceDB.getUserToRole(suser.getUserId(), fromGroup);
	  assertTrue("Role Synced for the user Mapped to UnMapped", userRoles.size() == 0 );
	  
	  //Since it's replaced to a Mapped role we should see it in services.
	  List userToRoles =serviceDB.getUserToRole(suser.getUserId(), toGroup);
	  assertTrue("Role Synced for the user Mapped to UnMapped", userToRoles.size() > 0 );
  }
  
//TEST CASE:Failure- Add User with services down. Make sure JBOSS is down.
  public void atestAddUserServicesDown() throws UnsupportedEncodingException{
	newAdUser.setStatus("active");
	ldapContact.insertContact(newAdUser);
	SyncData();
	//Verify the synced user data.
	User user = getUser();
	assertTrue("User Not Added",user == null);
  }
  //TEST CASE:Failure- Bring the services up and sync. Make sure JBOSS is ON
  public void atestSyncToServicesUp() throws UnsupportedEncodingException{
	SyncData();
	//Verify the synced user data.
	verifyUserData();
  }
  
  public void atestSyncData(){
	//Sync Data
	  SyncData();
  }
  
  
  
 //TEST: Verify the synced user data.
 @SuppressWarnings("unchecked")
 public void verifyUserData(){
	 User user = getUser(); 
	 if(user != null){
		  System.out.println("Checking user table..");
		  assertEquals(newAdUser.getEmailAddress(),user.getEmail());
		  
		  System.out.println("Checking user status..");
		  if(newAdUser.getStatus() == "active"){
			  assertEquals("Pending", user.getUserStatusDef().getName());
		  }else{
			  assertFalse("Invalid user status", user.getUserStatusDef().getName() == "Pending");
		  }
		  System.out.println("User Status: "+user.getUserStatusDef().getName());
		  //Now we found the user. Check all the data related to this user record.
		  
		  //1st Check the user_invite table data
		  System.out.println("Checking user_invite table..");
		  Set<UserInvite> userInvites = user.getUserInvites();
		  // Iterating over the elements in the set
		  Iterator<UserInvite> ui = userInvites.iterator();
		  while (ui.hasNext()) {
		      // Get element
		      UserInvite uInvite = (UserInvite) ui.next();
		      assertEquals(newAdUser.getEmailAddress(),uInvite.getEmail());
		      assertEquals(newAdUser.getFirstName(),uInvite.getFirst());
		      assertEquals(newAdUser.getLastName(),uInvite.getLast());
		  }
	
		  //Check if the user got assigned any new role -- TO DO
//		  System.out.println("Checking user_to_role table..");
//		  Set<UserToRole> userToRole = user.getUserToRoles();
//		  assertTrue("UserToRole",userToRole.size() > 0);
		  
		  //Check user_to_user_group entry
		  System.out.println("Checking user_to_user_group table..");
		  Set<UserToUserGroup> userToUserGroup = user.getUserToUserGroups();
		  assertTrue("UserToUserGroup",userToUserGroup.size() > 0);
	
		  //Check User Prefs
		  System.out.println("Checking user_pref table..");
		  Set<UserPref> userPref = user.getUserPrefs();
		  assertTrue("UserPreferences",userPref.size() > 0);
		  
		  //Check Remote Object Entry
		  System.out.println("Checking remote_object table..");
		  Set<RemoteObject> remoteObject = user.getRemoteObjects();
		  assertTrue("RemoteObject",remoteObject.size() > 0);
		  
		  //Check UserLogs
		  System.out.println("Checking user_log table..");
		  List userLog = serviceDB.getUserLogs(user.getUserId()); 
		  
		  Iterator ulog = userLog.iterator();
		  while(ulog.hasNext()){
			Object[] row =  (Object[]) ulog.next();
			//System.out.println(Arrays.toString(row));
			Integer unixTime = (Integer) row[0];
			long timestamp = (long) unixTime.intValue() * 1000;
			java.util.Date d = new java.util.Date(timestamp); 
			System.out.println("[" + row[1] +"] On Date:  "+ d.toString()  );  
		  }
		  System.out.println("Verification Successful!!");
	  }else{
		  System.out.println("User Not Found. Something is wrong");
	  }
 } 

}
