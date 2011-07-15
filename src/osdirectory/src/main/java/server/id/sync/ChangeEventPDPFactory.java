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
package server.id.sync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.AttributeFinderModule;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.impl.CurrentEnvModule;
import com.sun.xacml.finder.impl.SelectorModule;

public class ChangeEventPDPFactory {
  private final Log log = LogFactory.getLog(getClass());
  protected String policySetName;
  protected SyncAttributeFinder attributeModule;

  protected List<Map<String, Object>> policies;

  public ChangeEventPDPFactory() {
    super();
  }

  public void setPolicies(List<Map<String, Object>> policies) {
    this.policies = policies;
  }

  public List<Map<String, Object>> getPolicies() {
    return policies;
  }

  public String getPolicySetName() {
    return policySetName;
  }

  public void setPolicySetName(String name) {
    this.policySetName = name;
  }

  public void setAttributeModule(SyncAttributeFinder attributeModule) {
    this.attributeModule = attributeModule;
  }

  public SyncAttributeFinder getAttributeModule() {
    return attributeModule;
  }
  
  public ChangeEventPDP getChangeEventPDP() {
    log.trace("getChangeEventPDP");
    SyncPolicyFinderModule apfm = new SyncPolicyFinderModule(policySetName, policies);
    Set<PolicyFinderModule> pmods = new HashSet<PolicyFinderModule>();
    pmods.add(apfm);
    PolicyFinder pf = new PolicyFinder();
    pf.setModules(pmods);
    
    CurrentEnvModule envAttributeModule = new CurrentEnvModule();
    SelectorModule selectorAttributeModule = new SelectorModule();
    AttributeFinder af = new AttributeFinder();
    List<AttributeFinderModule> afmods = new ArrayList<AttributeFinderModule>();
    afmods.add(envAttributeModule);
    afmods.add(selectorAttributeModule);
    afmods.add(attributeModule);
    af.setModules(afmods);
    
    PDP pdp = new PDP(new PDPConfig(af, pf, null));
    return new ChangeEventPDPImpl(pdp, af);
  }
}
