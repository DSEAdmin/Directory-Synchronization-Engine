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
package server.id.ldap.ad;

import java.io.IOException;

import com.sun.jndi.ldap.BasicControl;
import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerEncoder;

public class DirSyncControl extends BasicControl {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5335472570863208424L;

	public static final java.lang.String OID = "1.2.840.113556.1.4.841";

	public static final int LDAP_DIRSYNC_OBJECT_SECURITY = 0x00000001;
	public static final int LDAP_DIRSYNC_ANSISTORS_FIRST_ORDER = 0x00000800;
	public static final int LDAP_DIRSYNC_PUBLIC_DATA_ONLY = 0x00002000;
	public static final int LDAP_DIRSYNC_INCREMENTAL_VALUES = 0x80000000;
	
    private static final byte[] EMPTY_COOKIE = new byte[0];
	
	private int flags;

	private int maxReturnLength;

	private byte[] cookie;

	public DirSyncControl(int pageSize) throws IOException {
		super(OID, true, null);
		flags = LDAP_DIRSYNC_OBJECT_SECURITY;
		maxReturnLength = pageSize;
		this.cookie = EMPTY_COOKIE;
		value = setEncodedValue();
	}
	
	public DirSyncControl(byte[] cookie, int pageSize) throws IOException {
		super(OID, true, null);
		flags = LDAP_DIRSYNC_OBJECT_SECURITY;
		maxReturnLength = pageSize;
		this.cookie = cookie;		
		if (this.cookie == null) {
		  this.cookie = EMPTY_COOKIE;
		}
		value = setEncodedValue();
	}
	
	private byte[] setEncodedValue() throws IOException {
		  BerEncoder be = new BerEncoder();
		  be.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
		  be.encodeInt(flags);
		  be.encodeInt(maxReturnLength);
		  be.encodeOctetString(cookie, Ber.ASN_OCTET_STR);
		  be.endSeq();
		  return be.getTrimmedBuf();
	}
}
