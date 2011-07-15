/*************************BEGINE LICENSE BLOCK**********************************
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
 *  The Original Code is Directory Synchronization Engine(DSE).
 * 
 *  The Initial Developer of the Original Code is IronKey, Inc..
 *  Portions created by the Initial Developer are Copyright (C) 2011
 *  the Initial Developer. All Rights Reserved.
 * 
 *  Contributor(s): Shirish Rai
 * 
 **************************END LICENSE BLOCK***********************************/
package server.id.sync.server.dao;


import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.RDN;

public class LdapContainerManager implements ContainerManager {
	private final Log log = LogFactory.getLog(getClass());

	//This is only public so that the tests work. I could use reflection in JUnit but that is more work
	//than I want to do.
	public String[] getContainers(String base, String dn) throws LDAPException {
		if (DN.isAncestorOf(base, dn, false) == false) {
			log.warn("getContainers: incorrect arguments base is : " + base + " dn is : " + dn);
			return null;
		}
		RDN[] baserdns = DN.getRDNs(base);
		RDN[] dnrdns = DN.getRDNs(dn);
		int len = dnrdns.length - baserdns.length - 1;
		if (len < 0) {//Something went wrong. 
			log.warn("getContainers: incorrect arguments base is : " + base + " dn is : " + dn);
			len = 0;
		}
		String[] ret = new String[len];
		
		for (int i = len; i > 0 ; --i) { //0th position is not a container
			String[] vals = dnrdns[i].getAttributeValues();
			String c = vals[0];
/*			for (int j = 1; j < vals.length; ++j) {
				c += " + " + vals[j];
			}
*/			ret[len - i] = c;
		}
		
		return ret;
	}

	private boolean matchPath(EntitiesDao sed, UserGroup leaf, UserGroup root, String[] containers, int idx) {
		List<UserGroup> path = sed.getUserGroupPath(leaf);
		//Forward the list to dv
		Iterator<UserGroup> it = path.iterator();
		UserGroup rootInPath = null;
		while (it.hasNext()) {
			UserGroup ug = it.next();
			if (ug.getUserGroupId().intValue() == root.getUserGroupId().intValue()) {
				rootInPath = ug;
				break;
			}
		}
		if (rootInPath != null) {
			int i = 0;
			while(it.hasNext() && i <= idx) {
				UserGroup ug = it.next();
				String name = ug.getNormalizedName();
				if (name == null)
				  name = ug.getName();
				if (name.equals(containers[i]) == false) {
					return false;
				} else {
					i++;
				}
			}
			if (i > idx)
				return true;
			else 
				return false;
		} else
			return false;
	}

	private UserGroup createRemainingPath(EntitiesDao sed, Account account, UserGroup closestUg,
			UserGroup root, String[] containers, int startFrom) {
		UserGroup parent = null;
		if (closestUg == null) {
			parent = root;
			startFrom = 0;
		} else {
			parent = closestUg;
			startFrom += 1;
		}
		
		for (int i = startFrom; i < containers.length; ++i) {
			Integer ugid = sed.addUserGroup(containers[i], (Account) account, parent, UserGroupStatusDefType.remote_active);
			parent = sed.getUserGroup(ugid);
		}
		
		return parent;
	}
	
	public UserGroup createOrGetContainerFor(EntitiesDao sed,
			Account account, 
			DitVirtualizationEntry dvEntry, 
			String dn) throws Exception {		
		DitVirtualization dv = (DitVirtualization)dvEntry;
		//remove the last components of the dn 
		String ndn = DN.normalize(dn);
		String[] containers = getContainers(dvEntry.getRemoteContainer().getIdentifier(), ndn);
		if (containers == null) {
			return null;
		}
		
		//If no more components left, return the dv local container
		if (containers.length == 0) {
			return dv.getUserGroup();
		}
		
		//find the containers by name and check their parents. If not found, create		
		int i = containers.length;
		UserGroup closestUg = null;
		while (closestUg == null && i > 0) {
			i -= 1;
			List<UserGroup> ugs = sed.getAllUserGroupByName((Account) account,
					containers[i]);
			if (ugs != null) {
				for (UserGroup ug : ugs) {
					if (matchPath(sed, ug, dv.getUserGroup(), containers, i)) {
						closestUg = ug;
						break;
					}
				}
			}
		}
		return createRemainingPath(sed, account, closestUg, dv.getUserGroup(), containers, i);
	}

	private boolean noRefs(EntitiesDao sed, UserGroup ug) {
		return sed.isUserGroupRefed(ug) == false;
	}
	
	private boolean noSubgroups(EntitiesDao sed, UserGroup ug) {
	  return sed.hasSubgroups(ug) == false;
	}

	public void removeContainerIfEmptyFor(EntitiesDao sed,
			Account account, 
			DitVirtualizationEntry dvEntry, 
			String dn) throws Exception {
		DitVirtualization dv = (DitVirtualization)dvEntry;
		//remove the last components of the dn 
		String ndn = DN.normalize(dn);
		String[] containers = getContainers(dvEntry.getRemoteContainer().getIdentifier(), ndn);
		if (containers == null) {
			return;
		}
		
		//If no more components left, return the dv local container
		if (containers.length == 0) {
			return;
		}
		
		//find the containers by name and check their parents. If not found, create		
		UserGroup closestUg = null;
		List<UserGroup> ugs = sed.getAllUserGroupByName((Account) account,
			containers[containers.length - 1]);
		if (ugs != null) {
			for (UserGroup ug : ugs) {
				if (matchPath(sed, ug, dv.getUserGroup(), containers, containers.length - 1)) {
					closestUg = ug;
					break;
				}
			}
		}
		
		if (closestUg != null) {
			if (noRefs(sed, closestUg) == true && noSubgroups(sed, closestUg) == true) {
			  UserGroupStatusDef remote_active = sed.getUserGroupStatusDef(UserGroupStatusDefType.remote_active);
			  if (closestUg.getUserGroupStatusDefId().intValue() == remote_active.getUserGroupStatusDefId().intValue())
				sed.deleteUserGroup(closestUg);
			}
		}
	}
		
}
