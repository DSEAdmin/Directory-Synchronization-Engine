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

public class AdUser{
	String commonName;
	String lastName;
	String firstName;
	String email;
	String description;
	String userName;
	String status="disabled";
	public AdUser(){
		
	}
	public AdUser(String name){
	  if(name == "rahul"){
		  this.commonName="Rahul Dravid";
		  this.lastName="Dravid";
		  this.description = "The Wall";
		  this.email="rdravid@india.com";
		  this.firstName="Rahul";
		  this.userName= "Rahul.Dravid";
	  }else if(name == "sachin"){
		  this.commonName="Sachin Tendulkar";
		  this.lastName="Tendulkar";
		  this.description = "The Master Blaster";
		  this.email="stendulkar@india.com";
		  this.firstName="Sachin";
		  this.userName= "Sachin.Tendulkar";
	  }else{
		  this.commonName="Sourav Ganguly";
		  this.lastName="Ganguly";
		  this.description = "Prince of Culcutta";
		  this.email="sganguly@india.com";
		  this.firstName="Sourav";
		  this.userName= "Sourav.Ganguly";
	  }
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setEmailAddress(String email) {
		this.email = email;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCommonName() {
		return commonName;
	}
	public String getLastName() {
		return lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getEmailAddress() {
		return email;
	}
	public String getDescription() {
		return description;
	}
	public String getUserName() {
		return userName;
	}
	public String getStatus() {
		return status;
	}
	
	public String toString() {
		StringBuffer AdUserStr = new StringBuffer("Person=[");
		AdUserStr.append(" Common Name = " + commonName);
		AdUserStr.append(", Last Name = " + lastName);
		AdUserStr.append(", Description = " + description);
		AdUserStr.append(" ]");
		return AdUserStr.toString();
	}
	
	
	
}
