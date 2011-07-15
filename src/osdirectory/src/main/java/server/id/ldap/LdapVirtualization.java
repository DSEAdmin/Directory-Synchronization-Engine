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

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.AVP;
import server.id.AttributeVirtualization;
import server.id.Group;
import server.id.IdObject;
import server.id.IdObjectType;
import server.id.IdVirtualization;
import server.id.Util;
import server.id.AttributeTypeDef.Type;
import server.id.sync.agent.domain.Constraint;


public class LdapVirtualization implements IdVirtualization {

  private final Log log = LogFactory.getLog(getClass());
  
  public RoleVirtualizationEntry isGroupMapped(Set<? extends RoleVirtualizationEntry> rvEntries, Group group) {
    String dnstr = (String)group.getAttributeValue(AttributeVirtualization.DN);
    return isGroupMapped(rvEntries, dnstr);
  }

  //This should only be called from the agent. So we will use that knowledge
  public DitVirtualizationEntry isObjectMapped(SortedSet<? extends DitVirtualizationEntry> dvEntries, IdObject obj) {
    String dnstr = (String)obj.getAttributeValue(AttributeVirtualization.DN);
    DitVirtualizationEntry dvEntry = isObjectMapped(dvEntries, dnstr);
    if (obj.getObjectType() != IdObjectType.PERSON)
      return dvEntry;
    if ( dvEntry instanceof server.id.sync.agent.domain.DitVirtualizationEntry) {
      server.id.sync.agent.domain.DitVirtualizationEntry dv = (server.id.sync.agent.domain.DitVirtualizationEntry)dvEntry;
      Set<Constraint> constraints = dv.getConstraints();
      if (constraints == null || constraints.size() == 0) {
        return dv;
      } else {
        log.debug("isObjectMapped: checking constraints");
        for (Constraint constraint : constraints) {
          AVP avp = obj.getAVP(constraint.getAttributeName());
          if (avp != null) {
            log.debug("isObjectMapped: got attribute " + constraint.getAttributeName());
            Iterable<? extends Object> values = avp.getValues();
            if (values != null) {
              if (constraint.getAttributeType() == Type.BINARY) {
                for (Object value : values) {
                  if (value instanceof byte[]) {
                    byte[] binValue = (byte[]) value;
                    log.debug("isObjectMapped: binary compare "
                        + Util.byteArrayToHexString(constraint.getAttributeValue()) + " with "
                        + Util.byteArrayToHexString(binValue));
                    if (Arrays.equals(constraint.getAttributeValue(), binValue) == true) {
                      return dv;
                    }
                  } else {
                    log.warn("Attribute type set to binary but got type " + value.getClass().getCanonicalName());
                  }
                }
              } else {
                for (Object value : values) {
                  String strValue = value.toString();
                  log.debug("isObjectMapped: string compare " + new String(constraint.getAttributeValue()) + " with "
                      + strValue);
                  if (strValue.equalsIgnoreCase(new String(constraint.getAttributeValue())) == true) {
                    return dv;
                  }
                }
              }
            }
          }
        }
      }
      return null;
    } else {
      log.warn("isObjectMapped called with illegal parameters");
      return null;
    }
  }

  private DitVirtualizationEntry isObjectMapped(SortedSet<? extends DitVirtualizationEntry> dvEntries, String dnstr) {
    if (dvEntries == null) {
      log.debug("No containers mapped");
      return null;
    }
    if (dnstr == null) {
      log.warn("IdObject did not have a DN attribute, cannot be mapped");
      return null;
    }

    LdapName dn = null;
    try {
      dn = new LdapName(dnstr);
    } catch (InvalidNameException e) {
      log.warn("Invalid DN for object : " + dnstr, e);
      return null;
    }
    
    for (DitVirtualizationEntry ve : dvEntries) {
      try {
        LdapName mapping = new LdapName(ve.getRemoteContainer().getIdentifier());
        log.debug("isObjectMapped: checking " + dnstr + " against " + ve.getRemoteContainer().getIdentifier());
        if (dn.startsWith(mapping)) {
          return ve;
        }
      } catch (InvalidNameException e) {
        log.error("Invalid DN name in configuration of DitVirtualizationEntry : " + 
            ve.getRemoteContainer(), e);
        log.error("Maping skipped");
      }
    }
    log.debug("isObjectMapped: no match found. returning null");
    return null;
  }
    
  public boolean effectsInScopeObjects(SortedSet<? extends DitVirtualizationEntry> dvEntries, IdObject obj) {
    if (dvEntries == null) {
      log.debug("No containers mapped");
      return false;
    }
    String dnstr = (String)obj.getAttributeValue(AttributeVirtualization.DN);
    if (dnstr == null) {
      log.warn("IdObject did not have a DN attribute");
      return false;
    }
    LdapName dn = null;
    try {
      dn = new LdapName(dnstr);
    } catch (InvalidNameException e) {
      log.warn("Invalid DN for object : " + dnstr, e);
      return false;
    }
    
    for (DitVirtualizationEntry ve : dvEntries) {
      try {
        LdapName mapping = new LdapName(ve.getRemoteContainer().getIdentifier());
        if (dn.startsWith(mapping) || dn.endsWith(mapping)) {
          return true;
        }
      } catch (InvalidNameException e) {
        log.error("Invalid DN name in configuration of DitVirtualizationEntry : " + 
            ve.getRemoteContainer(), e);
        log.error("Maping skipped");
      }
    }

    return false;
  }

  public RoleVirtualizationEntry isGroupMapped(Set<? extends RoleVirtualizationEntry> rvEntries, String dnstr) {
    if (rvEntries == null) {
      log.debug("No roles mapped");
      return null;
    }
    if (dnstr == null) {
      log.warn("Group did not have a DN attribute, cannot be mapped");
      return null;
    }
    LdapName dn = null;
    try {
      dn = new LdapName(dnstr);
    } catch (InvalidNameException e) {
      log.warn("Invalid DN for group : " + dnstr, e);
      return null;
    }

    for (RoleVirtualizationEntry rv : rvEntries) {
      try {
        LdapName mapping = new LdapName(rv.getRemoteRole().getIdentifier());
        if (dn.equals(mapping)) {
          return rv;
        }
      } catch (InvalidNameException e) {
        log.error("Invalid DN name in configuration of RoleVirtualizationEntry : " + 
            rv.getRemoteRole(), e);
        log.error("Maping skipped");
      }
    }
    return null;    
  }

  public RoleVirtualizationEntry isGroupMapped(Set<? extends RoleVirtualizationEntry> rvEntries, byte[] uuid) {
    for (RoleVirtualizationEntry rv : rvEntries) {
      if (log.isDebugEnabled()) {
        log.debug("Checking mapping for internal role " + rv.getLocalRole().getIdentifier() + " remote role " + rv.getRemoteRole().getIdentifier());
        log.debug("isGroupMapped: comparing UUID " + Util.byteArrayToHexString(rv.getRemoteRole().getUuid()) + " and " + Util.byteArrayToHexString(uuid));
      }
      if (Arrays.equals(rv.getRemoteRole().getUuid(), uuid) == true) {
        log.debug("isGroupMapped: returning rvEntry for local role " + rv.getLocalRole().getIdentifier());
        return rv;
      }
    }
    return null;
  }

  public DitVirtualizationEntry getDvEntry(SortedSet<? extends DitVirtualizationEntry> dvEntries, byte[] uuid) {
    for (DitVirtualizationEntry dv : dvEntries) {
      if (log.isDebugEnabled() == true) {
        log.debug("getDvEntry: comparing UUID " + Util.byteArrayToHexString(dv.getUuid())+ " and " + Util.byteArrayToHexString(uuid));
      }
      if (Arrays.equals(dv.getUuid(), uuid) == true) {
        log.debug("getDvEntry: returning dvEntry for local container " + dv.getLocalContainer().getIdentifier());
        return dv;
      }
    }
    return null;
  }
  
  public RoleVirtualizationEntry getRvEntry(Set<? extends RoleVirtualizationEntry> rvEntries, byte[] uuid) {
    for (RoleVirtualizationEntry rv : rvEntries) {
      if (log.isDebugEnabled() == true) {
        log.debug("getRvEntry: comparing UUID " + Util.byteArrayToHexString(rv.getUuid())+ " and " + Util.byteArrayToHexString(uuid));
      }
      if (Arrays.equals(rv.getUuid(), uuid) == true) {
        log.debug("getRvEntry: returning dvEntry for local role " + rv.getLocalRole().getIdentifier());
        return rv;
      }      
    }
    return null;
  }
}
