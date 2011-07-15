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

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.IdObjectType;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.Indenter;
import com.sun.xacml.Obligation;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicySet;
import com.sun.xacml.Rule;
import com.sun.xacml.Target;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.FirstApplicablePolicyAlg;
import com.sun.xacml.combine.FirstApplicableRuleAlg;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.combine.RuleCombiningAlgorithm;
import com.sun.xacml.cond.Apply;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.Function;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.Result;

public class PolicyBuilder {
  public static final String NAME = "name";
  public static final String ID_OBJ_TYPE = "idObjectType";
  public static final String CHANGE_TYPE = "changeType";
  public static final String IN_SCOPE = "inScope";
  public static final String PREV_IN_SCOPE = "prevInScope";
  public static final String ELD = "existsInLocalDB";
  public static final String EISO = "effectsInScopeObjects";
  public static final String PEISO = "prevEffectsInScopeObjects";
  public static final String OBLIGATIONS = "obligations";
  public static final String IS_MAPPED = "isMapped";
  public static final String WAS_MAPPED = "wasMapped";
  
  public static final String USER = "User";
  public static final String GROUP = "Group";
  public static final String CONTAINER = "Container";
  public static final String DELETE = "delete";
  public static final String ADD = "add";
  public static final String MODIFY = "modify";
  public static final String RENAME_MODIFY = "rename_modify";

  private final Log log = LogFactory.getLog(getClass());
  private String policySetName;
  private List<Map<String, Object>> policies;

  public PolicyBuilder(String policyName, List<Map<String, Object>> policies) {
    this.policySetName = policyName;
    this.policies = policies;
  }
  
  public AbstractPolicy buildXACMLPolicy() {
    log.trace("buildXACMLPolicy");
    CombiningAlgFactory factory = CombiningAlgFactory.getInstance();
    RuleCombiningAlgorithm ruleCombiningAlg = null;
    PolicyCombiningAlgorithm policyCombiningAlg = null;
    URI policySetURI = null;
    try {
      policySetURI = new URI(policySetName);
      URI ruleCombiningAlgId = new URI(FirstApplicableRuleAlg.algId);
      ruleCombiningAlg = (RuleCombiningAlgorithm) (factory.createAlgorithm(ruleCombiningAlgId));
      URI policyCombiningAlgId = new URI(FirstApplicablePolicyAlg.algId);
      policyCombiningAlg = (PolicyCombiningAlgorithm) factory.createAlgorithm(policyCombiningAlgId);
    } catch (URISyntaxException e) {
      log.error("System Error", e);
    } catch (UnknownIdentifierException e) {
      log.error("System Error", e);
    }
   
    List<Policy> xacmlPolicies = new ArrayList<Policy>();
    for (Map<String, Object> policyDef : policies) {    
      String policyName = (String)policyDef.get(NAME);
      log.trace("Build policy for " + policyName);
      if (policyName == null) {
        log.error("One of the rules in the policy definition does not have a name");
        return null;
      }
      try {
        URI policyId = new URI(policyName);
        List<Rule> rules = getRules(policyDef);
        Set<Obligation> obligations = getObligations(policyDef);
        Policy policy = new Policy(policyId, ruleCombiningAlg, "", new Target(null, null, null), "", rules, obligations);
        log.trace("Adding policy" + policyName + " to policy set");
        xacmlPolicies.add(policy);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        policy.encode(os, new Indenter());
        log.trace(os.toString());
      } catch (URISyntaxException e) {
        log.error("Could not create URI from policy name " + policyName, e);
        return null;
      }
    }

    try {
      Rule defaultRule = new Rule(new URI("defaultFinal"), Result.DECISION_DENY, null, null, null);
      List<Rule> finalRules = new ArrayList<Rule>();
      finalRules.add(defaultRule);
      Policy finalPolicy = new Policy(new URI("finalPolicy"), ruleCombiningAlg, "", new Target(null, null, null), "", finalRules);
      xacmlPolicies.add(finalPolicy);
    } catch (URISyntaxException e) {}
    
    PolicySet policySet = new PolicySet(policySetURI, policyCombiningAlg, "Agent Replication Policy", new Target(null, null, null), xacmlPolicies);
    return policySet;
}
  
  private Set<Obligation> getObligations(Map<String, Object> policyDef) {
    Set<Obligation> obligations = new HashSet<Obligation>();

    @SuppressWarnings("unchecked")
    List<EventProcessingObligation> epos = (List<EventProcessingObligation>)policyDef.get(OBLIGATIONS);
    for (EventProcessingObligation epo : epos) {
      try {
        Attribute ordinalId = new Attribute(new URI("ordinalId"), null, null, new IntegerAttribute(epo.getOrdinalId()));
        Attribute type = new Attribute(new URI("type"), null, null, new StringAttribute(epo.getType().name()));
        Attribute description = new Attribute(new URI("description"), null, null, new StringAttribute(epo.getDescription()));
        List<Attribute> attrs = new ArrayList<Attribute>();
        attrs.add(ordinalId);
        attrs.add(type);
        attrs.add(description);
        Obligation obligation = new Obligation(new URI(epo.getType().toString()), Result.DECISION_PERMIT, attrs);
        obligations.add(obligation);
      } catch (URISyntaxException e) {      }
    }
    return obligations;
  }

  private IdObjectType getIdObjectType(String type) {
    if (type.equalsIgnoreCase(USER)) {
      return IdObjectType.PERSON;
    } else if (type.equalsIgnoreCase(GROUP)) {
      return IdObjectType.GROUP;
    } else if (type.equalsIgnoreCase(CONTAINER)) {
      return IdObjectType.CONTAINER;
    } else return IdObjectType.UNSUPPORTED;
  }
  
  private ChangeEvent.ChangeType getChangeType(String type) {
    if (type.equalsIgnoreCase(DELETE)) {
      return ChangeEvent.ChangeType.DELETE;
    } else if (type.equalsIgnoreCase(ADD)) {
        return ChangeEvent.ChangeType.ADD;
    } else if (type.equalsIgnoreCase(MODIFY)) {
        return ChangeEvent.ChangeType.MODIFY;
    } else if (type.equalsIgnoreCase(RENAME_MODIFY)) {
        return ChangeEvent.ChangeType.RENAME_MODIFY;
    }
    return ChangeEvent.ChangeType.UNKNOWN;
  }

  private Apply createAndCondition(List<Apply> args) {
    FunctionFactory factory = FunctionFactory.getGeneralInstance();
    Function andFunc = null;
    try {
        andFunc = factory.createFunction("urn:oasis:names:tc:xacml:1.0:function:and");
    } catch (Exception e) {
      log.error("Internal Error", e);
      return null;
    }
    return new Apply(andFunc, args, true);
  }
  
  private Apply createStringEqExp(String attribute, String value) {
    FunctionFactory factory = FunctionFactory.getConditionInstance();

    // now create the apply section that gets the designator value
    List<Evaluatable> fArgs = new ArrayList<Evaluatable>();
    factory = FunctionFactory.getGeneralInstance();
    Function oneFunc = null;
    try {
        oneFunc =
            factory.createFunction("urn:oasis:names:tc:xacml:1.0:function:"
                                   + "string-one-and-only");
    } catch (Exception e) {    }
    URI designatorType;
    URI designatorId;
    try {
      designatorType = new URI("http://www.w3.org/2001/XMLSchema#string");
      designatorId = new URI(attribute);
    } catch (URISyntaxException e) {
      log.error("Internal Error", e);
      return null;
    }
    AttributeDesignator designator = 
      new AttributeDesignator(AttributeDesignator.SUBJECT_TARGET, designatorType,
        designatorId, true);
    fArgs.add(designator);
    Apply arg1 = new Apply(oneFunc, fArgs, false);
    
    StringAttribute attrValue = new StringAttribute(value);

    // get the function that the expression uses
    Function function = null;
    try {
        function =
            factory.createFunction("urn:oasis:names:tc:xacml:1.0:function:"
                                   + "string-equal");
    } catch (Exception e) {    }
    List<Evaluatable> expArgs = new ArrayList<Evaluatable>();
    expArgs.add(arg1);
    expArgs.add(attrValue);
    return new Apply(function, expArgs, false);
  }
  
  private Apply createBooleanEqExp(String attribute, boolean value) {
    FunctionFactory factory = FunctionFactory.getConditionInstance();

    // now create the apply section that gets the designator value
    List<Evaluatable> fArgs = new ArrayList<Evaluatable>();
    factory = FunctionFactory.getGeneralInstance();
    Function oneFunc = null;
    try {
        oneFunc =
            factory.createFunction("urn:oasis:names:tc:xacml:1.0:function:"
                                   + "boolean-one-and-only");
    } catch (Exception e) {    }
    URI designatorType;
    URI designatorId;
    try {
      designatorType = new URI("http://www.w3.org/2001/XMLSchema#boolean");
      designatorId = new URI(attribute);
    } catch (URISyntaxException e) {
      log.error("Internal Error", e);
      return null;
    }
    AttributeDesignator designator = 
      new AttributeDesignator(AttributeDesignator.SUBJECT_TARGET, designatorType,
        designatorId, true);
    fArgs.add(designator);
    Apply arg1 = new Apply(oneFunc, fArgs, false);
    
    BooleanAttribute attrValue = BooleanAttribute.getInstance(value);

    // get the function that the expression uses
    Function function = null;
    try {
        function =
            factory.createFunction("urn:oasis:names:tc:xacml:1.0:function:"
                                   + "boolean-equal");
    } catch (Exception e) {    }
    List<Evaluatable> expArgs = new ArrayList<Evaluatable>();
    expArgs.add(arg1);
    expArgs.add(attrValue);
    return new Apply(function, expArgs, false);
  }
  
  private List<Rule> getRules(Map<String, Object> policyDef) {
    List<Rule> rules = new ArrayList<Rule>();
    URI ruleId = null;
    try {
      ruleId = new URI("default");
    } catch (URISyntaxException e1) {    }
    int effect = Result.DECISION_PERMIT;
    Target target = new Target(null, null, null);

    List<Apply> args = new ArrayList<Apply>();
    for (java.util.Iterator<Entry<String, Object>> it = policyDef.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, Object> e = it.next(); 
      log.trace("Create expression for entry " + e.getKey());
      if (e.getKey().equalsIgnoreCase(NAME) || e.getKey().equalsIgnoreCase(OBLIGATIONS)) {
        continue;
      } else if (e.getKey().equalsIgnoreCase(ID_OBJ_TYPE)) {
        IdObjectType type = getIdObjectType((String)e.getValue());
        Apply a = createStringEqExp(e.getKey(), type.name());
        args.add(a);
      } else if (e.getKey().equalsIgnoreCase(CHANGE_TYPE)) {        
        ChangeEvent.ChangeType changeType = getChangeType((String)e.getValue());
        Apply a = createStringEqExp(e.getKey(), changeType.name());
        args.add(a);
      } else { //all boolean        
        Apply a = createBooleanEqExp(e.getKey(), Boolean.parseBoolean((String)e.getValue()));
        args.add(a);
      }
    }
    
    Apply condition = createAndCondition(args);
    
    Rule rule = new Rule(ruleId, effect, "", target, condition);
    rules.add(rule);
    return rules;
  }
}
