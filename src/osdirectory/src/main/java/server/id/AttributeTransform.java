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
package server.id;

import java.util.List;

import server.id.dao.ChangePoller;


public interface AttributeTransform {
  Iterable<? extends Object> virtualize(ChangePoller changePoller,  Entry remoteEntry) throws Exception;
  List<String> getRemoteAttributes();
  List<String> getBinaryRemoteAttributes();
}
