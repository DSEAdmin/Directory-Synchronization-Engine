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

import java.text.ParseException;
import java.util.Random;
import java.util.UUID;

import com.unboundid.util.Base64;

public class Util {
  private static final char[] HEX_CHAR_TABLE = {
    '0', '1', '2', '3',
    '4', '5', '6', '7',
    '8', '9', 'a', 'b',
    'c', 'd', 'e', 'f'
  };    

  //16 x 5 = 80
  private static final int PRINTABLE_ASCII_LEN = 80;
  private static final char[] PRINTABLE_ASCII = {
    'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p',
    'q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F',
    'G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V',
    'W','X','Y','Z','0','1','2','3','4','5','6','7','8','9','!','@',
    '#','$','%','^','&','*','(',')','-','_','+','=','?','!','@','.'
  };
  
  public static String byteArrayToHexString(byte[] data) {
    if (data == null) {
      return "null";
    }
    
    StringBuilder hex = new StringBuilder(2 * data.length);

    for (byte b : data) {
      int v = b & 0xFF;
      hex.append(HEX_CHAR_TABLE[v >>> 4]);
      hex.append(HEX_CHAR_TABLE[v & 0xF]);
    }
    return hex.toString();    
  }

  public static String uuidToString(byte[] uuid) {
    UUID id = UUID.nameUUIDFromBytes(uuid);
    return id.toString();
  }
  
  public static String uuidToString(String uuid) {
    byte[] buuid = uuid.getBytes();
    return uuidToString(buuid);
  }

  //Note that this is easy to break if you know the approximate time when the password was generated
  public static String generateRandomAsciiPassword(int length) {
    StringBuffer sb = new StringBuffer();
    Random rand = new Random(System.currentTimeMillis());
    for (int i = 0; i < length; ++i) {
      int r = rand.nextInt(PRINTABLE_ASCII_LEN);
      sb.append(PRINTABLE_ASCII[r]);
    }
    return new String(sb);
  }
  
  public static byte[] base64Decode(String data) {
    try {
      return Base64.decode(data);
    } catch (ParseException e) {
      return null;
    }
  }
  
  public static String base64Encode(byte[] data) {
    return Base64.encode(data);
  }  
}
