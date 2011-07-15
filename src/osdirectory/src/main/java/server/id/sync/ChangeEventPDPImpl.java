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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.sync.ChangeEvent;
import server.id.sync.ChangeEventEvaluationCtx;
import server.id.sync.ChangeEventPDP;
import server.id.sync.EventProcessingObligation;

import com.sun.xacml.Indenter;
import com.sun.xacml.Obligation;
import com.sun.xacml.PDP;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.finder.AttributeFinder;

public class ChangeEventPDPImpl implements ChangeEventPDP {

  private final Log log = LogFactory.getLog(getClass());
  private PDP pdp;
  private AttributeFinder finder;
  
  public ChangeEventPDPImpl(PDP pdp, AttributeFinder finder) {
    this.pdp = pdp;
    this.finder = finder;
  }
  
  public List<EventProcessingObligation> getObligationsForEvent(ChangeEvent event) {
    log.trace("getObligationsForEvent");
    ResponseCtx response = null;
    ChangeEventEvaluationCtx ctx = new ChangeEventEvaluationCtx(event, finder);
    response = pdp.evaluate(ctx);

    @SuppressWarnings("unchecked")
    Set<Result> results = response.getResults();
    //There should be only one result
    assert(results.size() == 1);
    
    Result result = results.iterator().next();
    log.debug("Result : " + Result.DECISIONS[result.getDecision()] + " Resource : " + result.getResource() + " Status : " + result.getStatus().getMessage());
    @SuppressWarnings("unchecked")
    Set<Obligation> obligations = result.getObligations();
    List<EventProcessingObligation> epos = new ArrayList<EventProcessingObligation>();
    for (Obligation o : obligations) {
      EventProcessingObligation epo = getObligation(o);
      if (epo == null) {
        log.error("Could not convert XACML obligation to internal format");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        o.encode(bos, new Indenter());
        log.error("The obligation was : \n" + bos.toString());
      } else {
        epos.add(epo);
      }
    }
    Collections.sort(epos, new Comparator<EventProcessingObligation>() {
      public int compare(EventProcessingObligation o1, EventProcessingObligation o2) {
        if (o1.getOrdinalId() < o2.getOrdinalId()) {
          return -1;
        } else if (o1.getOrdinalId() > o2.getOrdinalId()) {
          return 1;
        } else
          return 0;
      }      
    });
    printObligations(epos);
    return epos;
  }

  private void printObligations(List<EventProcessingObligation> epos) {
    if (log.isDebugEnabled()) {
      log.debug("Returning Obligations");
      for (EventProcessingObligation epo : epos) {
        log.debug(epo);
      }
    }
  }

  private EventProcessingObligation getObligation(Obligation o) {
    @SuppressWarnings("unchecked")
    List<Attribute> assignments = o.getAssignments();
    assert(assignments.size() == 3);
    
    String description = null;
    EventProcessingObligation.ObligationType changeType = null;
    Integer ordinalId = null;
   
    for (Attribute a : assignments) {
      log.trace(a.getId() + " : " + a.getValue().encode());
      if (a.getId().toString().endsWith("ordinalId")) {
        ordinalId = Integer.parseInt(a.getValue().encode());
      } else if (a.getId().toString().endsWith("description")) {
        description = a.getValue().encode();
      } else if (a.getId().toString().endsWith("type")) {
        changeType = Enum.valueOf(EventProcessingObligation.ObligationType.class, a.getValue().encode());
      } else {
        log.error("Unknown attribute type in obligation");
        return null;
      }
    }
    
    return new EventProcessingObligation(changeType, description, ordinalId);
  }

}
