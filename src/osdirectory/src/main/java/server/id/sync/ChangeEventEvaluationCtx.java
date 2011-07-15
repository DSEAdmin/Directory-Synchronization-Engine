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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.DateAttribute;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.TimeAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.finder.AttributeFinder;

public class ChangeEventEvaluationCtx implements EvaluationCtx {

  private final Log log = LogFactory.getLog(getClass());
  private ChangeEvent changeEvent;
  private DateAttribute currentDate;
  private TimeAttribute currentTime;
  private DateTimeAttribute currentDateTime;
  private AttributeValue resourceId;
  private AttributeFinder finder;
  
  public ChangeEventEvaluationCtx(ChangeEvent changeEvent, AttributeFinder finder) {
    this.changeEvent = changeEvent;
    this.finder = finder;
    resourceId = new StringAttribute(EvaluationCtx.RESOURCE_ID);
  }
  
  public ChangeEvent getChangeEvent() {
    return changeEvent;
  }
  
  public void setChangeEvent(ChangeEvent changeEvent) {
    this.changeEvent = changeEvent;
  }

  public EvaluationResult getActionAttribute(URI type, URI id, URI issuer) {
    log.trace("getActionAttribute");
    return new EvaluationResult(BagAttribute.createEmptyBag(type));
  }

  public EvaluationResult getAttribute(String contextPath, Node namespaceNode, URI type, String xpathVersion) {
    log.trace("getAttribute");
    return new EvaluationResult(BagAttribute.createEmptyBag(type));
  }

  public AttributeFinder getAttributeFinder() {
    log.trace("getAttributeFinder");
    return finder;
  }

  public DateAttribute getCurrentDate() {
    log.trace("getCurrentDate");
    return currentDate;
  }

  public DateTimeAttribute getCurrentDateTime() {
    log.trace("getCurrentDateTime");
    return currentDateTime;
  }

  public TimeAttribute getCurrentTime() {
    log.trace("getCurrentTime");
    return currentTime;
  }

  public EvaluationResult getEnvironmentAttribute(URI type, URI id, URI issuer) {
    log.trace("getEnvironmentAttribute");
    return new EvaluationResult(BagAttribute.createEmptyBag(type));
  }

  //We don't support XPath queries in the finder
  public Node getRequestRoot() {
    return null;
  }

  public EvaluationResult getResourceAttribute(URI type, URI id, URI issuer) {
    log.trace("getResourceAttribute");
    return new EvaluationResult(BagAttribute.createEmptyBag(type));
  }

  public AttributeValue getResourceId() {
    //log.trace("getResourceId");
    return resourceId;
  }

  public int getScope() {
    return SCOPE_IMMEDIATE;
  }

  public EvaluationResult getSubjectAttribute(URI type, URI id, URI category) {
    //log.trace("getSubjectAttribute1");
    return getSubjectAttribute(type, id, null, category);
  }

  public EvaluationResult getSubjectAttribute(URI type, URI id, URI issuer, URI category) {
    //log.trace("getSubjectAttribute2");
    return finder.findAttribute(type, id, issuer, category, this, AttributeDesignator.SUBJECT_TARGET);
  }

  public void setCurrentDate(DateAttribute currentDate) {
    log.trace("setCurrentDate");
    this.currentDate = currentDate;
  }

  public void setCurrentDateTime(DateTimeAttribute currentDateTime) {
    log.trace("setCurrentDateTime");
    this.currentDateTime = currentDateTime;
  }

  public void setCurrentTime(TimeAttribute currentTime) {
    log.trace("setCurrentTime");
    this.currentTime = currentTime;
  }

  public void setResourceId(AttributeValue resourceId) {
    log.trace("setResourceId");
    this.resourceId = resourceId;
  }
}
