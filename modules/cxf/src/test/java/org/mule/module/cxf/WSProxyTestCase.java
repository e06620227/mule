/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.cxf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.config.ConfigResource;
import org.mule.module.client.MuleClient;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

public class WSProxyTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort dynamicPort1 = new DynamicPort("port1");

    @Rule
    public DynamicPort dynamicPort2 = new DynamicPort("port2");

    @Rule
    public DynamicPort dynamicPort3 = new DynamicPort("port3");

    @Override
    protected String getConfigResources()
    {
        return "mule-proxy-config.xml";
    }

    @Test
    public void testDirectRequest() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("wsdl-cxf:http://localhost:" + dynamicPort1.getNumber() + "/WebService?wsdl&method=echo",
            new DefaultMuleMessage("mule", muleContext));
        assertEquals ("mule", result.getPayloadAsString());
    }

    @Test
    public void testWsdlProxyRequest() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("http.method", "GET");
        MuleMessage replyMessage = client.send("http://localhost:" + dynamicPort2.getNumber() + "/webServiceProxy?wsdl",
            "/services/webServiceProxy?WSDL", props);
        assertNotNull(replyMessage);

        String wsdl = replyMessage.getPayloadAsString();
        assertNotNull(wsdl);
        System.out.println(wsdl);
        assertTrue(wsdl.indexOf("<wsdl:definitions") != -1);
        assertTrue(wsdl.indexOf("<wsdl:message name=\"echoResponse\">") != -1);
        assertTrue(wsdl.indexOf("<wsdl:message name=\"echo\">") != -1);
    }

    @Test
    public void testProxyRequest() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("wsdl-cxf:http://localhost:" + dynamicPort2.getNumber() + "/webServiceProxy?wsdl&method=echo",
            new DefaultMuleMessage("mule", muleContext));
        assertEquals ("mule", result.getPayloadAsString());
    }

    @Test
    public void testWsdlFileRequest() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("http.method", "GET");
        MuleMessage replyMessage = client.send("http://localhost:" + dynamicPort3.getNumber() + "/webServiceProxy?wsdl",
            "/services/webServiceProxy?WSDL", props);
        assertNotNull(replyMessage);

        String wsdl = replyMessage.getPayloadAsString();
        assertNotNull(wsdl);
        assertTrue(wsdl.indexOf("<wsdl:definitions") != -1);
        assertTrue(wsdl.indexOf("<wsdl:message name=\"echoResponse\">") != -1);
        assertTrue(wsdl.indexOf("<wsdl:message name=\"echo\">") != -1);
    }

    @Test
    public void testWsdlFileProxyRequest() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("wsdl-cxf:http://localhost:" + dynamicPort3.getNumber() + "/webServiceProxy?wsdl&method=echo",
            new DefaultMuleMessage("mule", muleContext));
        assertEquals ("mule", result.getPayloadAsString());
    }

    @Test
    public void testWsdlFileNotReloaded() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("wsdl-cxf:http://localhost:" + dynamicPort3.getNumber() + "/webServiceProxy?wsdl&method=echo",
            new DefaultMuleMessage("mule", muleContext));
        assertEquals ("mule", result.getPayloadAsString());

        String wsdlFileName = "wsproxyservice-localWsdl.wsdl";
        ConfigResource wsdlFileResource = new ConfigResource(wsdlFileName);
        String wsdlFilePathname = wsdlFileResource.getUrl().getFile();
        String tmpWsdlFilePathname = wsdlFilePathname + ".tmp";
        File wsdlFile = new File(wsdlFilePathname);
        assertTrue(wsdlFile.exists());
        assertTrue(wsdlFile.renameTo(new File(tmpWsdlFilePathname)));

        try
        {
            assertFalse((new File(wsdlFilePathname)).exists());

            // if initialization will occur a second time, this will throw initialization exception
            result = client.send("wsdl-cxf:http://localhost:" + dynamicPort3.getNumber() + "/webServiceProxy?wsdl&method=echo",
                new DefaultMuleMessage("mule", muleContext));
            assertEquals ("mule", result.getPayloadAsString());
        }
        finally
        {
            // put the file back
            (new File(tmpWsdlFilePathname)).renameTo(new File(wsdlFilePathname));
        }
    }

}
