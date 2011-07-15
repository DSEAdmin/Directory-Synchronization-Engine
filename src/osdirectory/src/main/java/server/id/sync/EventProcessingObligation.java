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

public class EventProcessingObligation {
  public static enum ObligationType {
    BATCH_SEND_START,
    BATCH_SEND_END,
    POLICY_NAME,
    IGNORE_EVENT, 
    FORWARD_EVENT, 
    FORWARD_EVENT_WITH_ALL_ATTRIBUTES,
    FORWARD_MODIFIED_MEMBERS,
    FORWARD_AS_ADD_EVENT,
    FORWARD_AS_DELETE_EVENT,
    FORWARD_PREV_INSCOPE_CHILDREN_AS_RENAME,
    FORWARD_PREV_INSCOPE_AND_INSCOPE_CHILDREN_AS_RENAME_EVENT,
    FORWARD_PREV_INSCOPE_AND_NOT_INSCOPE_CHILDREN_AS_DELETE_EVENT,
    DELETE_LOCAL_ENTRY, 
    DELETE_LOCAL_ENTRY_CHILDREN,
    ADD_LOCAL_ENTRY,
    MODIFY_LOCAL_ENTRY,
    RENAME_LOCAL_ENTRY,
    RENAME_MODIFY_LOCAL_ENTRY,
    RENAME_LOCAL_CHILDREN,
    MIGRATE_LOCAL_ENTRY,
    UPDATE_CONNECTOR_RV_ENTRY,
    UPDATE_CONNECTOR_DV_ENTRY,
    UPDATE_MEMBER_DV_ENTRIES,
    LOG_ERROR
  }
  
  public static enum ObligationGroup {
    UNKNOWN,
    REMOTE,
    DB,
    DIRECTIVE,
    INFORMATIONAL
  }
  
  private ObligationType type;
  private String description;
  private int ordinalId;
  private ChangeEvent changeEvent; //set later
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(type.name());
    sb.append(" [");
    sb.append(description);
    sb.append("] ");
    sb.append(ordinalId);
    return new String(sb);
  }
  
  public EventProcessingObligation() {
    type = ObligationType.IGNORE_EVENT;
    changeEvent = null;
  }
  
  public EventProcessingObligation(ObligationType type, String description, int ordinalId) {
    this.type = type;
    this.description = description;
    this.ordinalId = ordinalId;
    this.changeEvent = null;
  }
  
  public ObligationType getType() {
    return type;
  }
  
  public void setType(ObligationType type) {
    this.type = type;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }

  public int getOrdinalId() {
    return ordinalId;
  }

  public void setOrdinalId(int ordinalId) {
    this.ordinalId = ordinalId;
  }

  public ChangeEvent getChangeEvent() {
    return changeEvent;
  }

  public void setChangeEvent(ChangeEvent changeEvent) {
    this.changeEvent = changeEvent;
  }

  public ObligationGroup getObligationGroup() {
    switch (type) {
    case BATCH_SEND_START:
    case BATCH_SEND_END:
    case IGNORE_EVENT:
      return ObligationGroup.DIRECTIVE;
    case FORWARD_EVENT:
    case FORWARD_MODIFIED_MEMBERS:
    case FORWARD_EVENT_WITH_ALL_ATTRIBUTES:
    case FORWARD_AS_ADD_EVENT:
    case FORWARD_AS_DELETE_EVENT:
    case FORWARD_PREV_INSCOPE_CHILDREN_AS_RENAME:
    case FORWARD_PREV_INSCOPE_AND_INSCOPE_CHILDREN_AS_RENAME_EVENT:
    case FORWARD_PREV_INSCOPE_AND_NOT_INSCOPE_CHILDREN_AS_DELETE_EVENT:
      return ObligationGroup.REMOTE;
    case DELETE_LOCAL_ENTRY:
    case DELETE_LOCAL_ENTRY_CHILDREN:
    case ADD_LOCAL_ENTRY:
    case MODIFY_LOCAL_ENTRY:
    case RENAME_LOCAL_ENTRY:
    case RENAME_MODIFY_LOCAL_ENTRY:
    case RENAME_LOCAL_CHILDREN:
    case MIGRATE_LOCAL_ENTRY:
    case UPDATE_CONNECTOR_RV_ENTRY:
    case UPDATE_CONNECTOR_DV_ENTRY:
    case UPDATE_MEMBER_DV_ENTRIES:
      return ObligationGroup.DB;
    case POLICY_NAME:
    case LOG_ERROR:
      return ObligationGroup.INFORMATIONAL;
    default:
      return ObligationGroup.UNKNOWN;
    }
  }
}
