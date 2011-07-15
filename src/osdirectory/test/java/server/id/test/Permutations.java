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
package server.id.test;

import java.util.List;
import java.util.LinkedList;

public class Permutations {

  public static List<List<String>> getPerm(String elem, List<List<String> > prevPerm) {
    List<List<String>> perm = new LinkedList<List<String>>();
    for (int i = 0; i < prevPerm.size(); ++i) {
      for (int j = 0; j < prevPerm.get(i).size() + 1; ++j) {
        LinkedList<String> next = new LinkedList<String>(prevPerm.get(i));
        next.add(j, elem);
        perm.add(next);
      }
    }
    return perm;
  }
  
  public static void print(List<List<String>> p) {
    for (List<String> pe : p) {
      for (String e : pe) {
        System.out.print(e + " ");
      }
      System.out.println();
    }
  }
  
  public static void main(String[] args) {
    List<List<String>> initial = new LinkedList<List<String>>();
    initial.add(new LinkedList<String>());
    initial.get(0).add("Add");
    
    List<List<String>> next = getPerm("Modify", initial);
    next = getPerm("Rename", next);
    next = getPerm("Delete", next);
    print(next);
  }
}
