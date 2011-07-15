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
package server.id.sync.server;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import server.id.sync.agent.ClientChangeEventFactory;
import server.id.sync.messages.v1.ChangeRequest;
import server.id.sync.messages.v1.ConnectorConfiguration;
import server.id.sync.messages.v1.ConnectorConfigurationRequest;
import server.id.sync.messages.v1.FullSyncMetaDataRequest;


public class FileSyncService extends AbstractSyncServiceImpl {  
  private Log log = LogFactory.getLog(getClass());
  private String file;
  
  public FileSyncService(ClientChangeEventFactory changeFactory) {
    super(changeFactory);
  }

  public FileSyncService(ClientChangeEventFactory changeFactory, String file) {
    super(changeFactory);
    this.file = file;
  }
  
  String getFile(){
    return file;
  }
  
  void setFile(String file) {
    this.file = file;
  }
  
  synchronized public ChangeResult sendChangeRequest(boolean more) throws JAXBException, FileNotFoundException,
  IllegalArgumentException, IOException {
    if (changeRequest == null)
      throw new IllegalArgumentException("No changeRequest available to send");
    changeRequest.setMoreFragments(more);
    ChangeResult result = null;
    OutputStream os = null;
    try {
      JAXBContext context = JAXBContext.newInstance("server.id.sync.messages.v1");
      os = new FileOutputStream(file);
      Marshaller marshaller = context.createMarshaller();
      marshaller.marshal(new JAXBElement<ChangeRequest>(_ChangeRequest_QNAMEv1, ChangeRequest.class, changeRequest), os);
      result = new ChangeResult(changeRequest, Result.SUCCESS, null);
      fragmentNumber = 0;
      changeRequest = null;
    } catch (JAXBException e) {
      log.error("JAXBException", e);
      throw e;
    } catch (FileNotFoundException e) {
      log.error("FileNotFoundException", e);
      throw e;
    } finally {
      if (os != null) {
        try { os.close(); } catch (IOException ex) {
          log.error("Exception while closing file " + file, ex);
          throw ex;
        }
      }
    }
    return result;
  }

  public ConnectorConfigurationResult pushConnectorConfiguration(Connector connector) throws JAXBException, 
  FileNotFoundException, IllegalArgumentException, IOException {
    if (connector == null) {
      throw new IllegalArgumentException("connector is null");
    }
    
    ConnectorConfigurationResult result = null;
    OutputStream os = null;
    try {
      JAXBContext context = JAXBContext.newInstance("server.id.sync.messages.v1");
      os = new FileOutputStream(file);
      ConnectorConfigurationRequest request = new ConnectorConfigurationRequest();
      request.setVersion(1);
      ConnectorConfiguration cc = getConnectorConfiguration(connector);
      request.setConnectorConfiguration(cc);
      Marshaller marshaller = context.createMarshaller();
      marshaller.marshal(new JAXBElement<ConnectorConfigurationRequest>(_ConnectorConfigurationRequest_QNAMEv1, 
          ConnectorConfigurationRequest.class, request), os);
      result = new ConnectorConfigurationResult(Result.SUCCESS, "");
    } catch (JAXBException e) {
      log.error("JAXBException", e);
      throw e;
    } catch (FileNotFoundException e) {
      log.error("FileNotFoundException", e);
      throw e;
    } finally {
      if (os != null) {
        try { os.close(); } catch (IOException ex) {
          log.error("Exception while closing file " + file, ex);
          throw ex;
        }
      }
    }
    return result;
  }

  public ConnectorStatusResult pushStatus(Connector connector) throws Exception {
    throw new UnsupportedOperationException("pushStatus not supported in " + getServiceType());
  }

  public List<? extends Group> getLocalContainers() {
    throw new UnsupportedOperationException("getLocalContainers not supported in " + getServiceType());
  }

  public List<? extends Role> getLocalRoles() {
    throw new UnsupportedOperationException("getLocalRoles not supported in " + getServiceType());
  }

  public FullSyncMetaDataResult pushFullSyncMetaData(Connector connector, List<byte[]> uuids)throws JAXBException, 
  FileNotFoundException, IllegalArgumentException, IOException {
    if (connector == null) {
      throw new IllegalArgumentException("connector is null");
    }
    
    FullSyncMetaDataResult result;
    OutputStream os = null;
    try {
      JAXBContext context = JAXBContext.newInstance("server.id.sync.messages.v1");
      os = new FileOutputStream(file);
      FullSyncMetaDataRequest request = getFullSyncMetaDataRequest(connector, uuids);
      Marshaller marshaller = context.createMarshaller();
      marshaller.marshal(new JAXBElement<FullSyncMetaDataRequest>(_FullSyncMetaDataRequest_QNAMEv1, 
          FullSyncMetaDataRequest.class, request), os);
      result = new FullSyncMetaDataResult(Result.SUCCESS, "");
    } catch (JAXBException e) {
      log.error("JAXBException", e);
      throw e;
    } catch (FileNotFoundException e) {
      log.error("FileNotFoundException", e);
      throw e;
    } finally {
      if (os != null) {
        try { os.close(); } catch (IOException ex) {
          log.error("Exception while closing file " + file, ex);
          throw ex;
        }
      }
    }
    return result;
  }

}
