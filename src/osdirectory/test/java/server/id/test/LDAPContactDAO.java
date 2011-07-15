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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

public class LDAPContactDAO {
	private LdapTemplate ldapTemplate;
	
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public List getAllContactNames() {
		return ldapTemplate.search("", "(objectclass=person)",
				new AttributesMapper() {
					public Object mapFromAttributes(Attributes attrs)
							throws NamingException {
						return attrs.get("cn").get();
					}
				});
	}

	public AdUser findUser( String attributeName, String attributeVal) {
        List users = ldapTemplate.search("cn=Users,dc=vijay,dc=com", attributeName+"="+attributeVal,new ContactAttributeMapper() );
        if( users.size() > 0 )
        	return (AdUser) users.get(0);
        return null;
    }
	public AdUser findUserInGroup( String userCn, String groupCn) {
        List users = ldapTemplate.search("cn=Users,dc=vijay,dc=com", "(&(cn="+userCn+")(memberOf=CN="+groupCn+", OU=Test, dc=vijay, dc=com))",new ContactAttributeMapper() );
        if( users.size() > 0 )
        	return (AdUser) users.get(0);
        return null;
    }
	public boolean ifGroupExists( String group) {
        List groups= ldapTemplate.search("OU=Test,dc=vijay,dc=com", "cn="+group , new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs)
			throws NamingException {
		return attrs.get("cn").get();
	}
});
        if( groups.size() > 0 )
        	return true;
        return false;
    }

	public List getContactDetails(String commonName,String lastName){
		AndFilter andFilter = new AndFilter();
		//andFilter.and(new EqualsFilter("objectClass","person"));
		//andFilter.and(new EqualsFilter("objectclass","user"));
		andFilter.and(new EqualsFilter("cn",commonName));
		andFilter.and(new EqualsFilter("sn",lastName));
		System.out.println("LDAP Query " + andFilter.encode());
		return ldapTemplate.search("CN=Users,DC=vijay,DC=com", andFilter.encode(),new ContactAttributeMapper());
		
	}
	
	public void deleteContact(AdUser adUser) throws Exception {
		try {
			DistinguishedName newContactDN = new DistinguishedName("cn="+adUser.getCommonName()+",cn=Users,dc=vijay,dc=com");
			ldapTemplate.unbind(newContactDN);
		} catch ( Exception exc ) {
			throw exc;
		}
	}
	private byte[] encodePassword(String password) throws UnsupportedEncodingException {
		String newQuotedPassword = "\"" + password + "\"";
		return newQuotedPassword.getBytes("UTF-16LE");
	}	
	public void insertContact(AdUser adUser) throws UnsupportedEncodingException {
		Attributes personAttributes = new BasicAttributes();
		personAttributes.put( "objectclass", "person" );
	    personAttributes.put( "objectclass", "user" );
	    personAttributes.put("cn", adUser.getCommonName());
	    personAttributes.put( "givenName", adUser.getFirstName() );
	    personAttributes.put( "userPrincipalName", adUser.getEmailAddress() );
	    personAttributes.put( "mail", adUser.getEmailAddress() );
	    personAttributes.put( "sn", adUser.getLastName());
	    personAttributes.put( "description", adUser.getDescription());
	    personAttributes.put( "sAMAccountName", adUser.getUserName());
	    if(adUser.getStatus() == "active"){
	    	personAttributes.put( "userAccountControl", "512" ); /// 512 = normal luser
	    	//PASSWORD stuff.....
	    	personAttributes.put("unicodepwd", encodePassword("XXX007") );
	    }
	    DistinguishedName newContactDN = new DistinguishedName("cn="+adUser.getCommonName()+",cn=Users,dc=vijay,dc=com");
		ldapTemplate.bind(newContactDN, null, personAttributes);
	}

	public void insertContact(AdUser adUser,Hashtable<String, String> updateFields) throws Exception {
		Attributes personAttributes = new BasicAttributes();
		personAttributes.put( "objectclass", "person" );
	    personAttributes.put( "objectclass", "user" );
	    personAttributes.put("cn", adUser.getCommonName());
	    
	    try {
			Iterator<Entry<String, String>> updateFieldsIt = updateFields.entrySet().iterator();
			 while( updateFieldsIt.hasNext() ){
				 Entry<String, String> entry = updateFieldsIt.next();
			 String key = entry.getKey();
			 String val = entry.getValue();
			 personAttributes.put(key,val);
			 }
		} catch ( Exception exc ) {
			throw exc;
		}
		personAttributes.put( "sAMAccountName", adUser.getUserName());
	    if(adUser.getStatus() == "active"){
	    	personAttributes.put( "userAccountControl", "512" ); /// 512 = normal luser
	    	//PASSWORD stuff.....
	    	personAttributes.put("unicodepwd", encodePassword("XXX007") );
	    }
	    DistinguishedName newContactDN = new DistinguishedName("cn="+adUser.getCommonName()+",cn=Users,dc=vijay,dc=com");
		ldapTemplate.bind(newContactDN, null, personAttributes);
	}
	
	public void updateAttributes(String cn, Hashtable<String, String> updateFields, String action) throws Exception {
		try {
			ModificationItem[] items = new ModificationItem[updateFields.size()];
			int i = 0;
			Iterator<Entry<String, String>> updateFieldsIt = updateFields.entrySet().iterator();
			int modifier = 0;
			if(action == "add"){
				modifier = DirContext.ADD_ATTRIBUTE;
			}else{
				modifier = DirContext.REPLACE_ATTRIBUTE;
			}
			
			 while( updateFieldsIt.hasNext() ){
				 Entry<String, String> entry = updateFieldsIt.next();
			 String key = entry.getKey();
			 String val = entry.getValue();
			 items[i] = new ModificationItem( modifier, new BasicAttribute(key, val) );
				i++;
			 }
		    
		    DistinguishedName newContactDN = new DistinguishedName("cn="+cn+",cn=Users,dc=vijay,dc=com");
		    //ldapTemplate.modifyAttributes( newContactDN, new ModificationItem[] { repitem1, repitem2, repitem3, repitem4, repitem5} );
		    ldapTemplate.modifyAttributes( newContactDN, items);
		} catch ( Exception exc ) {
			throw exc;
		}
	}
	public void removeAttributes(String cn, Hashtable<String, String> updateFields) throws Exception {
		try {
			ModificationItem[] items = new ModificationItem[updateFields.size()];
			int i = 0;
			Iterator<Entry<String, String>> updateFieldsIt = updateFields.entrySet().iterator();
			 while( updateFieldsIt.hasNext() ){
				 Entry<String, String> entry = updateFieldsIt.next();
			 String key = entry.getKey();
			 items[i] = new ModificationItem( DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(key) );
				i++;
			 }
		    
		    DistinguishedName newContactDN = new DistinguishedName("cn="+cn+",cn=Users,dc=vijay,dc=com");
		    //ldapTemplate.modifyAttributes( newContactDN, new ModificationItem[] { repitem1, repitem2, repitem3, repitem4, repitem5} );
		    ldapTemplate.modifyAttributes( newContactDN, items);
		} catch ( Exception exc ) {
			throw exc;
		}
	}
	public void renameAttributes(String fromCn, String toCn) throws Exception {
		try {
		    ldapTemplate.rename( fromCn, toCn);
		} catch ( Exception exc ) {
			throw exc;
		}
	}
	public void addToGroup(AdUser adUser,String group_cn) throws Exception {
		
		try {
		    ModificationItem repitem1 = new ModificationItem( DirContext.ADD_ATTRIBUTE, new BasicAttribute("member", "cn="+adUser.getCommonName()+",cn=Users,dc=vijay,dc=com") );
		    //DistinguishedName newContactDN = new DistinguishedName("cn="+adUser.getCommonName()+",cn=Users,dc=vijay,dc=com");
		    DistinguishedName newContactDN = new DistinguishedName(group_cn);
		    ldapTemplate.modifyAttributes( newContactDN, new ModificationItem[] { repitem1} );
		} catch ( Exception exc ) {
			throw exc;
		}
	}
	public void DeleteFromGroup(AdUser adUser,String from_group_cn) throws Exception {
		//Delete from the group
		try {
		    ModificationItem repitem1 = new ModificationItem( DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("member", "cn="+adUser.getCommonName()+",cn=Users,dc=vijay,dc=com") );
		    //DistinguishedName newContactDN = new DistinguishedName("cn="+adUser.getCommonName()+",cn=Users,dc=vijay,dc=com");
		    DistinguishedName newContactDN = new DistinguishedName(from_group_cn);
		    ldapTemplate.modifyAttributes( newContactDN, new ModificationItem[] { repitem1} );
		} catch ( Exception exc ) {
			throw exc;
		}
	}
	public void ReplaceToGroup(AdUser adUser,String from_group_cn, String to_group_cn) throws Exception {
		//Delete from the group
		DeleteFromGroup(adUser,from_group_cn);
		
		//Add to the group
		addToGroup(adUser, to_group_cn);
	}
	public void AddNewGroup(String groupName) throws Exception {
		Attributes personAttributes = new BasicAttributes();
		personAttributes.put( "objectclass", "group" );
	    personAttributes.put("cn", groupName);
	    personAttributes.put( "name", groupName);
	    personAttributes.put( "sAMAccountName", groupName);
	    DistinguishedName newContactDN = new DistinguishedName("cn="+groupName+",ou=Test,dc=vijay,dc=com");
		ldapTemplate.bind(newContactDN, null, personAttributes);
	}
	
	public void removeGroup(String cn) throws Exception {
		DistinguishedName newContactDN = new DistinguishedName(cn);
		ldapTemplate.unbind(newContactDN);
	}

}
