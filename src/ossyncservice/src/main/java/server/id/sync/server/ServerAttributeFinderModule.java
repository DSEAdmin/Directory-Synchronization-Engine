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
package server.id.sync.server;

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

public class ServerAttributeFinderModule extends SyncAttributeFinder {
  private final Log log = LogFactory.getLog(getClass());

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
    } else if (attributeId.toString().endsWith(PolicyBuilder.ELD)) {
      return getELD(attributeType, attributeId, issuer, subjectCategory, context, designatorType);
    }
    
    return new EvaluationResult(BagAttribute.
            createEmptyBag(attributeType));
  }

  @Override
  public Set<URI> getSupportedIds() {
    Set<URI> ids = new HashSet<URI>();

    try {
        ids.add(new URI(PolicyBuilder.CHANGE_TYPE));
        ids.add(new URI(PolicyBuilder.ID_OBJ_TYPE));
        ids.add(new URI(PolicyBuilder.ELD));
    } catch (URISyntaxException se) {
        // this won't actually happen in this case
        return null;
    }

    return ids;
  }

}
