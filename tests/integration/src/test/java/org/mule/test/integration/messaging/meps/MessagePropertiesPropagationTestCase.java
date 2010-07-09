/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.messaging.meps;

import org.mule.api.MuleMessage;
import org.mule.api.config.MuleProperties;
import org.mule.api.transport.PropertyScope;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.tck.util.WebServiceOnlineCheck;
import org.mule.transport.http.HttpConstants;

import java.util.HashMap;
import java.util.Map;

public class MessagePropertiesPropagationTestCase extends FunctionalTestCase
{
    public MessagePropertiesPropagationTestCase()
    {
        super();
        
        // Do not fail test case upon timeout because this probably just means
        // that the 3rd-party web service is off-line.
        setFailOnTimeout(false);
    }
    
    /**
     * If a simple call to the web service indicates that it is not responding properly,
     * we disable the test case so as to not report a test failure which has nothing to do
     * with Mule.
     */
    @Override
    protected boolean isDisabledInThisEnvironment()
    {
        return !WebServiceOnlineCheck.isWebServiceOnline();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/test/integration/messaging/meps/message-properties-propagation.xml";
    }

    /**
     * As per EE-1613, the Correlation-related properties _should_ be propagated to the response message by default.
     */
    public void testPropagatedPropertiesWithHttpTransport() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Content-Type", "application/x-www-form-urlencoded");
        props.put(MuleProperties.MULE_CORRELATION_ID_PROPERTY, "TestID");
        props.put(MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY, "TestGroupSize");
        props.put(MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY, "TestSequence");
        
        MuleMessage response = client.send("vm://httpService1", "symbol=IBM", props);
        assertNotNull(response);
        assertTrue(response.getPayloadAsString().contains("PreviousClose"));
        assertEquals("TestID", response.getProperty(MuleProperties.MULE_CORRELATION_ID_PROPERTY, PropertyScope.OUTBOUND));
        assertEquals("TestGroupSize", response.getProperty(MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY, PropertyScope.OUTBOUND));
        assertEquals("TestSequence", response.getProperty(MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY, PropertyScope.OUTBOUND));
    }

    /**
     * As per EE-1613, the Correlation-related properties _should_ be propagated to the response message by default.
     */
    public void testPropagatedPropertiesWithCxfTransport() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);

        Map<String, Object> props = new HashMap<String, Object>();
        props.put(MuleProperties.MULE_CORRELATION_ID_PROPERTY, "TestID");
        props.put(MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY, "TestGroupSize");
        props.put(MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY, "TestSequence");
        
        MuleMessage response = client.send("vm://cxfService1", "IBM", props);
        assertNotNull(response);
        assertTrue(response.getPayloadAsString().contains("PreviousClose"));
        assertEquals("TestID", response.getProperty(MuleProperties.MULE_CORRELATION_ID_PROPERTY, PropertyScope.OUTBOUND));
        assertEquals("TestGroupSize", response.getProperty(MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY, PropertyScope.OUTBOUND));
        assertEquals("TestSequence", response.getProperty(MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY, PropertyScope.OUTBOUND));
    }

    /**
     * As per EE-1611/MULE-4302, custom properties and non-Correlation-related properties _should not_ be propagated 
     * to the response message by default.
     */
    public void testNotPropagatedPropertiesWithHttpTransport() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Content-Type", "application/x-www-form-urlencoded");
        props.put("some", "thing");
        props.put("other", "stuff");
        props.put(HttpConstants.HEADER_CONTENT_TYPE, "text/bizarre;charset=utf-16");
        
        MuleMessage response = client.send("vm://httpService1", "symbol=IBM", props);
        assertNotNull(response);
        assertNull(response.getProperty("some", PropertyScope.OUTBOUND));
        assertNull(response.getProperty("other", PropertyScope.OUTBOUND));
        assertEquals("text/plain; charset=utf-8", response.getProperty(HttpConstants.HEADER_CONTENT_TYPE, PropertyScope.OUTBOUND));
        assertEquals("utf-8", response.getEncoding());
    }

    /**
     * As per EE-1611/MULE-4302, custom properties and non-Correlation-related properties _should not_ be propagated 
     * to the response message by default.
     */
    public void testNotPropagatedPropertiesWithCxfTransport() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("some", "thing");
        props.put("other", "stuff");
        props.put(HttpConstants.HEADER_CONTENT_TYPE, "text/bizarre;charset=utf-16");
        
        MuleMessage response = client.send("vm://cxfService1", "IBM", props);
        assertNotNull(response);
        assertNull(response.getProperty("some", PropertyScope.OUTBOUND));
        assertNull(response.getProperty("other", PropertyScope.OUTBOUND));
        assertEquals("text/xml; charset=utf-8", response.getProperty(HttpConstants.HEADER_CONTENT_TYPE, PropertyScope.OUTBOUND));
        assertEquals("utf-8", response.getEncoding());
    }

    /**
     * Force the properties to be propagated to the response message.
     */
    public void testForcePropagatedPropertiesWithHttpTransport() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Content-Type", "application/x-www-form-urlencoded");
        props.put("some", "thing");
        props.put("other", "stuff");
        
        MuleMessage response = client.send("vm://httpService2", "symbol=IBM", props);
        assertNotNull(response);
        assertTrue(response.getPayloadAsString().contains("PreviousClose"));
        assertEquals("thing", response.getProperty("some", PropertyScope.OUTBOUND));
        assertEquals("stuff", response.getProperty("other", PropertyScope.OUTBOUND));
    }

    /**
     * Force the properties to be propagated to the response message.
     */
    public void testForcePropagatedPropertiesWithCxfTransport() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("some", "thing");
        props.put("other", "stuff");
        
        MuleMessage response = client.send("vm://cxfService2", "symbol=IBM", props);
        assertNotNull(response);
        assertTrue(response.getPayloadAsString().contains("PreviousClose"));
        assertEquals("thing", response.getProperty("some", PropertyScope.OUTBOUND));
        assertEquals("stuff", response.getProperty("other", PropertyScope.OUTBOUND));
    }
}
