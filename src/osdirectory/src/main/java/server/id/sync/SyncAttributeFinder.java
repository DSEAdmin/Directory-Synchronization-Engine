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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.finder.AttributeFinderModule;

public abstract class SyncAttributeFinder extends AttributeFinderModule {
  private final Log log = LogFactory.getLog(getClass());

  public SyncAttributeFinder() {
    super();
  }

  protected EvaluationResult stringAttributeValue(URI attributeType, URI attributeId, String value) {
    if (! attributeType.toString().equals(StringAttribute.identifier)) {
      log.warn("Incompatible attribute type for " + attributeId + " : " + attributeType.toString());
      return new EvaluationResult(BagAttribute.
                                    createEmptyBag(attributeType));
    }
    List<AttributeValue> values = new ArrayList<AttributeValue>();
    AttributeValue av = new StringAttribute(value);
    values.add(av);
    BagAttribute bag = null;
    try {
        bag = new BagAttribute(new URI(StringAttribute.identifier), values);
        log.trace("Returning " + attributeId + " : " + value);
        return new EvaluationResult(bag);
    } catch (URISyntaxException e) {    }
  
    //should never reach here
    return new EvaluationResult(BagAttribute.
            createEmptyBag(attributeType));    
  }

  protected EvaluationResult booleanAttributeValue(URI attributeType, URI attributeId, boolean value) {
    if (! attributeType.toString().equals(BooleanAttribute.identifier)) {
      log.warn("Incompatible attribute type for " + attributeId+ " : " + attributeType.toString());
      return new EvaluationResult(BagAttribute.
                                    createEmptyBag(attributeType));
    }
    List<AttributeValue> values = new ArrayList<AttributeValue>();
    AttributeValue av = BooleanAttribute.getInstance(value);
    values.add(av);
    BagAttribute bag = null;
    try {
        bag = new BagAttribute(new URI(BooleanAttribute.identifier), values);
        log.trace("Returning " + attributeId + " : " + value);
        return new EvaluationResult(bag);
    } catch (URISyntaxException e) {    }
  
    //should never reach here
    return new EvaluationResult(BagAttribute.
            createEmptyBag(attributeType));    
  }

  @Override
  public Set<Integer> getSupportedDesignatorTypes() {
    Set<Integer> types = new HashSet<Integer>();
  
    types.add(new Integer(AttributeDesignator.SUBJECT_TARGET));
  
    return types;
  }

  @Override
  public boolean isDesignatorSupported() {
    return true;
  }

}
