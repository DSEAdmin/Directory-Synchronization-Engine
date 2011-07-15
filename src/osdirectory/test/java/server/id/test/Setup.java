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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

public class Setup {
	public static void Start() throws IOException {
		
		try{
			Runtime r = Runtime.getRuntime();
		
		
			Process mysqlproc = r.exec("c:\\xampp\\mysql\\bin\\mysqld.exe");
	        StreamGobbler errorGobbler = new 
	            StreamGobbler(mysqlproc.getErrorStream(), "ERROR");            
	        
	        // any output?
	        StreamGobbler outputGobbler = new 
	            StreamGobbler(mysqlproc.getInputStream(), "OUTPUT");
	            
	        // kick them off
	        errorGobbler.start();
	        outputGobbler.start();
	                                
	        // any error???
	        //int exitVal = mysqlproc.waitFor();
	        System.out.println("Service DB MySQL Started");  
	        
	        Process jbossproc = r.exec("c:\\jboss-4.2.2.GA\\bin\\run.bat -c default -Djava.endorsed.dirs=\"C:\\jboss-4.2.2.GA\\bin\\..\\lib\\endorsed\" -Dsyncservice.properties=\"...syncservice\\src\\main\\test\\server\\id\\sync\\test\\syncservice.properties");
	        StreamGobbler jerrorGobbler = new 
	        StreamGobbler(jbossproc.getErrorStream(), "ERROR");            
    
		    // any output?
		    StreamGobbler joutputGobbler = new 
		        StreamGobbler(jbossproc.getInputStream(), "OUTPUT");
		        
		    // kick them off
		    jerrorGobbler.start();
		    joutputGobbler.start();
		                            
		    // any error???
		    //int jexitVal = jbossproc.waitFor();
		    System.out.println("JBOSS Started");
		    
		
		
		    Process hsqlproc = r.exec("java -classpath ..\\..\\extjavalibs\\hsqldb\\1.8.0.10\\hsqldb.jar org.hsqldb.Server");
		    StreamGobbler herrorGobbler = new 
		        StreamGobbler(hsqlproc.getErrorStream(), "ERROR");            
		    
		    // any output?
		    StreamGobbler houtputGobbler = new 
		        StreamGobbler(hsqlproc.getInputStream(), "OUTPUT");
		        
		    // kick them off
		    herrorGobbler.start();
		    houtputGobbler.start();
		                            
		    // any error???
		    System.out.println("ExitValue: Agent DB HSQL Started");
		    
		    
	    } catch (Throwable t){
	        t.printStackTrace();
	    }
	}
	
	public static void Stop() throws IOException {
		
		try{
			Runtime r = Runtime.getRuntime();
			Process proc = r.exec("c:\\jboss-4.2.2.GA\\bin\\shutdown.bat -S");
			// any error message?
	        StreamGobbler errorGobbler = new 
	            StreamGobbler(proc.getErrorStream(), "ERROR");            
	        
	        // any output?
	        StreamGobbler outputGobbler = new 
	            StreamGobbler(proc.getInputStream(), "OUTPUT");
	            
	        // kick them off
	        errorGobbler.start();
	        //outputGobbler.start();
	                                
	        // any error???
	        System.out.println("JBOSS ShutDown!");
	        
	        
	        Process mysqlproc = r.exec("c:\\xampp\\mysql\\bin\\mysqladmin.exe -u root shutdown");
			// any error message?
	        StreamGobbler merrorGobbler = new 
	            StreamGobbler(mysqlproc.getErrorStream(), "ERROR");            
	        
	        // any output?
	        StreamGobbler moutputGobbler = new 
	            StreamGobbler(mysqlproc.getInputStream(), "OUTPUT");
	            
	        // kick them off
	        merrorGobbler.start();
	        //moutputGobbler.start();
	                                
	        System.out.println("Serive DB ShutDown!");
	        
	        Process hsqlproc = r.exec("c:\\apache-ant-1.7.1\\bin\\ant.bat hsqldb-stop");
			// any error message?
	        StreamGobbler herrorGobbler = new 
	            StreamGobbler(hsqlproc.getErrorStream(), "ERROR");            
	        
	        // any output?
	        StreamGobbler houtputGobbler = new 
	            StreamGobbler(hsqlproc.getInputStream(), "OUTPUT");
	            
	        // kick them off
	        herrorGobbler.start();
	        //houtputGobbler.start();
	                                
	        System.out.println("Agent DB ShutDown!");
	        
	    } catch (Throwable t){
	        t.printStackTrace();
	    }
	}
}
class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    
    StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                System.out.println(type + ">" + line);    
        } catch (IOException ioe){
                ioe.printStackTrace();  
        }
    }
}


