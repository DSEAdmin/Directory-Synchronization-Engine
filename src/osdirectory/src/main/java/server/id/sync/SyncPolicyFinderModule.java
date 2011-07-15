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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.sync.PolicyBuilder;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;


public class SyncPolicyFinderModule extends PolicyFinderModule {
  private final Log log = LogFactory.getLog(getClass());
  private String policySetName;
  private List<Map<String, Object>> policies;
  private AbstractPolicy compiledPolicy;
  
  public SyncPolicyFinderModule(String policySetName, List<Map<String, Object>> policies) {
    this.policySetName = policySetName;
    this.policies = policies;
  }
  
  @Override
  public PolicyFinderResult findPolicy(EvaluationCtx context) {
    if (compiledPolicy == null) {
      PolicyBuilder builder = new PolicyBuilder(policySetName, policies);
      compiledPolicy = builder.buildXACMLPolicy();
      if (compiledPolicy == null) {
        log.error("Failed to build XACML policy for " + policySetName);
        List<String> codes = new ArrayList<String>();
        codes.add(Status.STATUS_PROCESSING_ERROR);
        return new PolicyFinderResult(new Status(codes));
      }
    }
    return new PolicyFinderResult(compiledPolicy);
  }

  @Override
  public void invalidateCache() {
    compiledPolicy = null;
  }

  @Override
  public boolean isRequestSupported() {
    return true;
  }

  @Override
  public void init(PolicyFinder pf) {
    PolicyBuilder builder = new PolicyBuilder(policySetName, policies);
    compiledPolicy = builder.buildXACMLPolicy();    
    if (compiledPolicy == null) {
      log.error("init: Failed to build XACML policy for " + policySetName);
    }
  }

}
