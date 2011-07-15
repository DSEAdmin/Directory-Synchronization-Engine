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
package server.id.ldap;

public class FilterSpec {
  private String userFilter;
  private String groupFilter;
  private String containerFilter;
    
  public String getUserFilter() {
    return userFilter;
  }


  public void setUserFilter(String userFilter) {
    this.userFilter = userFilter;
  }


  public String getGroupFilter() {
    return groupFilter;
  }


  public void setGroupFilter(String groupFilter) {
    this.groupFilter = groupFilter;
  }


  public String getContainerFilter() {
    return containerFilter;
  }


  public void setContainerFilter(String containerFilter) {
    this.containerFilter = containerFilter;
  }

  public FilterSpec() {}
}
