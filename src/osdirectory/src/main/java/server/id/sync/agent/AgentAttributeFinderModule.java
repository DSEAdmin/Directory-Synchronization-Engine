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
package server.id.sync.agent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.sync.ChangeEvent;
import server.id.sync.ChangeEventEvaluationCtx;
import server.id.sync.PolicyBuilder;
import server.id.sync.SyncAttributeFinder;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.EvaluationResult;

public class AgentAttributeFinderModule extends SyncAttributeFinder {

  private final Log log = LogFactory.getLog(getClass());
  
  public AgentAttributeFinderModule() {
  }

  private EvaluationResult getChangeType(URI attributeType, URI attributeId, URI issuer, URI subjectCategory,
      EvaluationCtx context, int designatorType) {
    if (context instanceof ChangeEventEvaluationCtx) {
      ChangeEventEvaluationCtx ctx = (ChangeEventEvaluationCtx)context;
      ChangeEvent ce = ctx.getChangeEvent();
      return stringAttributeValue(attributeType, attributeId, ce.getChangeType().name());
    } else {
      log.error("Incorrect context received");
      return new EvaluationResult(BagAttribute.
          createEmptyBag(attributeType));    
    }
  }

  private EvaluationResult getIdObjectType(URI attributeType, URI attributeId, URI issuer, URI subjectCategory,
      EvaluationCtx context, int designatorType) {
    if (context instanceof ChangeEventEvaluationCtx) {
      ChangeEventEvaluationCtx ctx = (ChangeEventEvaluationCtx)context;
      ChangeEvent ce = ctx.getChangeEvent();
      return stringAttributeValue(attributeType, attributeId, ce.getRemoteObj().getObjectType().name());
    } else {
      log.error("Incorrect context received");
      return new EvaluationResult(BagAttribute.
          createEmptyBag(attributeType));    
    }
  }
  
  private EvaluationResult getInScope(URI attributeType, URI attributeId, URI issuer, URI subjectCategory,
      EvaluationCtx context, int designatorType) {
    if (context instanceof ChangeEventEvaluationCtx) {
      ChangeEventEvaluationCtx ctx = (ChangeEventEvaluationCtx)context;
      ChangeEvent ce = ctx.getChangeEvent();
      return booleanAttributeValue(attributeType, attributeId, (ce.getDvEntry() != null));
    } else {
      log.error("Incorrect context received");
      return new EvaluationResult(BagAttribute.
          createEmptyBag(attributeType));    
    }
  }
  
  private EvaluationResult getELD(URI attributeType, URI attributeId, URI issuer, URI subjectCategory,
      EvaluationCtx context, int designatorType) {
    if (context instanceof ChangeEventEvaluationCtx) {
      ChangeEventEvaluationCtx ctx = (ChangeEventEvaluationCtx)context;
      ChangeEvent ce = ctx.getChangeEvent();
      return booleanAttributeValue(attributeType, attributeId, (ce.getLocalObj() != null));
    } else {
      log.error("Incorrect context received");
      return new EvaluationResult(BagAttribute.
          createEmptyBag(attributeType));    
    }
  }

//  private EvaluationResult getWasMapped(URI attributeType, URI attributeId,
//      URI issuer, URI subjectCategory, EvaluationCtx context, int designatorType) {
//    if (context instanceof ChangeEventEvaluationCtx) {
//      ChangeEventEvaluationCtx ctx = (ChangeEventEvaluationCtx)context;
//      ChangeEvent ce = ctx.getChangeEvent();
//      return booleanAttributeValue(attributeType, attributeId, (ce.getPrevRvEntry() != null));
//    } else {
//      log.error("Incorrect context received");
//      return new EvaluationResult(BagAttribute.
//          createEmptyBag(attributeType));    
//    }
//  }

  private EvaluationResult getIsMapped(URI attributeType, URI attributeId,
      URI issuer, URI subjectCategory, EvaluationCtx context, int designatorType) {
    if (context instanceof ChangeEventEvaluationCtx) {
      ChangeEventEvaluationCtx ctx = (ChangeEventEvaluationCtx)context;
      ChangeEvent ce = ctx.getChangeEvent();
      return booleanAttributeValue(attributeType, attributeId, (ce.getRvEntry() != null));
    } else {
      log.error("Incorrect context received");
      return new EvaluationResult(BagAttribute.
          createEmptyBag(attributeType));    
    }
  }

  @Override
  public EvaluationResult findAttribute(URI attributeType, URI attributeId, URI issuer, URI subjectCategory,
      EvaluationCtx context, int designatorType) {
    log.trace("Looking up value for: " + attributeId.toString());
    // check that this is a Subject attribute
    if (designatorType != AttributeDesignator.SUBJECT_TARGET) {
      log.debug("Returning empty bag. Only SUBJECT_TARGET supported");
      return new EvaluationResult(BagAttribute.
                                    createEmptyBag(attributeType));
    }

    if (attributeId.toString().endsWith(PolicyBuilder.CHANGE_TYPE)) {
      return getChangeType(attributeType, attributeId, issuer, subjectCategory, context, designatorType);
    } else if (attributeId.toString().endsWith(PolicyBuilder.ID_OBJ_TYPE)) {
      return getIdObjectType(attributeType, attributeId, issuer, subjectCategory, context, designatorType);
    } else if (attributeId.toString().endsWith(PolicyBuilder.IN_SCOPE)) {
      return getInScope(attributeType, attributeId, issuer, subjectCategory, context, designatorType);
    } else if (attributeId.toString().endsWith(PolicyBuilder.PREV_IN_SCOPE)) {
      return getPrevInScope(attributeType, attributeId, issuer, subjectCategory, context, designatorType);      
    } else if (attributeId.toString().endsWith(PolicyBuilder.ELD)) {
      return getELD(attributeType, attributeId, issuer, subjectCategory, context, designatorType);
    } else if (attributeId.toString().endsWith(PolicyBuilder.EISO)) {
      return getEISO(attributeType, attributeId, issuer, subjectCategory, context, designatorType);
    } else if (attributeId.toString().endsWith(PolicyBuilder.PEISO)) {
      return getPEISO(attributeType, attributeId, issuer, subjectCategory, context, designatorType);      
    } else if (attributeId.toString().endsWith(PolicyBuilder.IS_MAPPED)) {
      return getIsMapped(attributeType, attributeId, issuer, subjectCategory, context, designatorType);
    }
    
    return new EvaluationResult(BagAttribute.
            createEmptyBag(attributeType));
  }

  private EvaluationResult getPEISO(URI attributeType, URI attributeId, URI issuer, URI subjectCategory,
      EvaluationCtx context, int designatorType) {
    if (context instanceof ChangeEventEvaluationCtx) {
      ChangeEventEvaluationCtx ctx = (ChangeEventEvaluationCtx)context;
      ChangeEvent ce = ctx.getChangeEvent();
      return booleanAttributeValue(attributeType, attributeId, ce.isPrevEffectsInScopeObjects());
    } else {
      log.error("Incorrect context received");
      return new EvaluationResult(BagAttribute.
          createEmptyBag(attributeType));    
    }
  }

  private EvaluationResult getEISO(URI attributeType, URI attributeId, URI issuer, URI subjectCategory,
      EvaluationCtx context, int designatorType) {
    if (context instanceof ChangeEventEvaluationCtx) {
      ChangeEventEvaluationCtx ctx = (ChangeEventEvaluationCtx)context;
      ChangeEvent ce = ctx.getChangeEvent();
      return booleanAttributeValue(attributeType, attributeId, ce.isEffectsInScopeObjects());
    } else {
      log.error("Incorrect context received");
      return new EvaluationResult(BagAttribute.
          createEmptyBag(attributeType));    
    }
  }

  private EvaluationResult getPrevInScope(URI attributeType, URI attributeId, URI issuer, URI subjectCategory,
      EvaluationCtx context, int designatorType) {
    if (context instanceof ChangeEventEvaluationCtx) {
      ChangeEventEvaluationCtx ctx = (ChangeEventEvaluationCtx)context;
      ChangeEvent ce = ctx.getChangeEvent();
      return booleanAttributeValue(attributeType, attributeId, (ce.getPrevDvEntry() != null));
    } else {
      log.error("Incorrect context received");
      return new EvaluationResult(BagAttribute.
          createEmptyBag(attributeType));    
    }
  }

  @Override
  public Set<URI> getSupportedIds() {
    Set<URI> ids = new HashSet<URI>();

    try {
        ids.add(new URI(PolicyBuilder.CHANGE_TYPE));
        ids.add(new URI(PolicyBuilder.ID_OBJ_TYPE));
        ids.add(new URI(PolicyBuilder.IN_SCOPE));
        ids.add(new URI(PolicyBuilder.PREV_IN_SCOPE));
        ids.add(new URI(PolicyBuilder.ELD));
        ids.add(new URI(PolicyBuilder.EISO));
        ids.add(new URI(PolicyBuilder.PEISO));
        ids.add(new URI(PolicyBuilder.IS_MAPPED));
    } catch (URISyntaxException se) {
        // this won't actually happen in this case
        return null;
    }

    return ids;
  }

}
