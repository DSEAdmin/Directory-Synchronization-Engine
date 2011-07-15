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
package server.id.sync.agent.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "agent_operation_result")
public class OperationResult extends EntityBase {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Enumerated(EnumType.STRING)
  @Column(name = "operation_type", unique = false, nullable = false)
  private OperationType opType;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "operation_result", unique = false, nullable = false)
  private OperationResult opResult;
 
  @Column(name = "result_string", unique = false, nullable = true)
  private String resultString;
  
  @Column(name = "num_changes_from_ids", unique = false, nullable = true)
  private long numChangesFromIds;
  
  @Column(name = "num_changes_to_service", unique = false, nullable = true)
  private long numChangesToService;
  
  @ManyToOne(optional = false)
  @JoinColumn(name = "agent_connector_id", nullable = false)
  private Connector connector;

  public OperationResult(OperationType opType, 
      OperationResult opResult, String resultString, long numChangesFromIds,
      long numChangesToService, Connector connector) {
    super();
    this.opType = opType;
    this.opResult = opResult;
    this.resultString = resultString;
    this.numChangesFromIds = numChangesFromIds;
    this.numChangesToService = numChangesToService;
    this.connector = connector;
  }

  public OperationType getOpType() {
    return opType;
  }

  public void setOpType(OperationType opType) {
    this.opType = opType;
  }

  public OperationResult getOpResult() {
    return opResult;
  }

  public void setOpResult(OperationResult opResult) {
    this.opResult = opResult;
  }

  public String getResultString() {
    return resultString;
  }

  public void setResultString(String resultString) {
    this.resultString = resultString;
  }

  public long getNumChangesFromIds() {
    return numChangesFromIds;
  }

  public void setNumChangesFromIds(long numChangesFromIds) {
    this.numChangesFromIds = numChangesFromIds;
  }

  public long getNumChangesToService() {
    return numChangesToService;
  }

  public void setNumChangesToService(long numChangesToService) {
    this.numChangesToService = numChangesToService;
  }

  public Connector getConnector() {
    return connector;
  }

  public void setConnector(Connector connector) {
    this.connector = connector;
  }  
}
