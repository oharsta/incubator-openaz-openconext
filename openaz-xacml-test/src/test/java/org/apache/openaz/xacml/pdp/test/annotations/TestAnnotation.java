/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

/*
 *                        AT&T - PROPRIETARY
 *          THIS FILE CONTAINS PROPRIETARY INFORMATION OF
 *        AT&T AND IS NOT TO BE DISCLOSED OR USED EXCEPT IN
 *             ACCORDANCE WITH APPLICABLE AGREEMENTS.
 *
 *          Copyright (c) 2014 AT&T Knowledge Ventures
 *              Unpublished and Not for Publication
 *                     All Rights Reserved
 */
package org.apache.openaz.xacml.pdp.test.annotations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openaz.xacml.api.DataTypeException;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.pdp.test.TestBase;
import org.apache.openaz.xacml.std.annotations.RequestParser;
import org.apache.openaz.xacml.std.annotations.XACMLAction;
import org.apache.openaz.xacml.std.annotations.XACMLAttribute;
import org.apache.openaz.xacml.std.annotations.XACMLEnvironment;
import org.apache.openaz.xacml.std.annotations.XACMLMultiRequest;
import org.apache.openaz.xacml.std.annotations.XACMLRequest;
import org.apache.openaz.xacml.std.annotations.XACMLRequestReference;
import org.apache.openaz.xacml.std.annotations.XACMLResource;
import org.apache.openaz.xacml.std.annotations.XACMLSubject;
import org.apache.openaz.xacml.std.datatypes.HexBinary;
import org.apache.openaz.xacml.std.datatypes.IPAddress;
import org.apache.openaz.xacml.std.datatypes.IPv4Address;
import org.apache.openaz.xacml.std.datatypes.ISO8601DateTime;
import org.apache.openaz.xacml.std.datatypes.ISO8601Time;
import org.apache.openaz.xacml.util.FactoryException;

/**
 * This example application shows how to use annotations for Java classes to create requests to send to the
 * engine.
 */
public class TestAnnotation extends TestBase {
    private static final Log logger = LogFactory.getLog(TestAnnotation.class);

    private int num;

    /**
     * This is a sample class that uses annotations. In addition to demonstrating how to use XACML
     * annotations, it also demonstrates the various Java objects that can be used and how the request parser
     * will resolve each object's datatype.
     */
    @XACMLRequest(ReturnPolicyIdList = true)
    public class MyRequestAttributes {

        public MyRequestAttributes(String user, String action, String resource) {
            this.userID = user;
            this.action = action;
            this.resource = resource;
            this.today = new Date();
            this.yesterday = Calendar.getInstance();
            this.yesterday.add(Calendar.DAY_OF_MONTH, -1);
        }

        @XACMLSubject(includeInResults = true)
        String userID;

        @XACMLSubject(attributeId = "urn:oasis:names:tc:xacml:1.0:subject:subject-id-qualifier")
        boolean admin = false;

        @XACMLSubject(attributeId = "urn:oasis:names:tc:xacml:1.0:subject:key-info", issuer = "com:foo:security")
        HexBinary publicKey = new HexBinary(new byte[] {
            '1', '0'
        });

        @XACMLSubject(attributeId = "urn:oasis:names:tc:xacml:1.0:subject:authentication-time")
        ISO8601Time authenticationTime = new ISO8601Time(8, 0, 0, 0);

        /**
         * Here our base object is "Object", but it is reflected as a Java "String". The parser will then use
         * the XACML http://www.w3.org/2001/XMLSchema#string as the datatype.
         */
        @XACMLSubject(attributeId = "urn:oasis:names:tc:xacml:1.0:subject:authentication-method")
        Object authenticationMethod = new String("RSA Public Key");

        /**
         * Here our base object is "String", but we use the annotation for datatype to clarify that the real
         * XACML data type is http://www.w3.org/2001/XMLSchema#time. The parser will use the data type factory
         * to convert the "String" to a "ISO8601Time" Java object.
         */
        @XACMLSubject(attributeId = "urn:oasis:names:tc:xacml:1.0:subject:request-time", datatype = "http://www.w3.org/2001/XMLSchema#time")
        String requestTime = new String("13:20:00-05:00");

        @XACMLSubject(attributeId = "urn:oasis:names:tc:xacml:1.0:subject:session-start-time")
        ISO8601DateTime sessionStart = new ISO8601DateTime(TimeZone.getDefault().getID(), 2014, 1, 1, 10, 0,
                                                           0, 0);

        @XACMLSubject(attributeId = "urn:oasis:names:tc:xacml:3.0:subject:authn-locality:ip-address")
        IPAddress ip = new IPv4Address(new short[] {
            123, 134, 156, 255
        }, null, null);

        @XACMLSubject(attributeId = "urn:oasis:names:tc:xacml:3.0:subject:authn-locality:dns-name")
        String dnsName = "localhost";

        @XACMLAction()
        String action;

        @XACMLAction(attributeId = "urn:oasis:names:tc:xacml:1.0:action:implied-action")
        long impliedAction;

        @XACMLResource()
        String resource;

        @XACMLEnvironment()
        Date today;

        @XACMLEnvironment()
        Calendar yesterday;

        /**
         * This field demonstrates how the parser can detect collections and build a bag of values.
         */
        @XACMLAttribute(attributeId = "foo:bar:attribute")
        Collection<Double> fooBar = Arrays.asList(2.5, 3.5);

        /**
         * The XACMLAttribute annotation allows one to specify all the
         */
        @XACMLAttribute(category = "foo:bar:category", attributeId = "foo:bar:attribute2")
        double fooBar2 = 3.999;

        /**
         * This field demonstrates how the parser can detect arrays and build a bag of values.
         */
        @XACMLAttribute(category = "foo:bar:category", attributeId = "foo:bar:attribute:many")
        URI[] fooBarMany = new URI[] {
            URI.create("file://opt/app/test"), URI.create("https://localhost:8443/")
        };

    };

    @XACMLRequest(Defaults = "http://www.w3.org/TR/1999/Rec-xpath-19991116", multiRequest = @XACMLMultiRequest(values = {
                                                           @XACMLRequestReference(values = {
            "subject1", "action", "resource"
        }), @XACMLRequestReference(values = {
            "subject2", "action", "resource"
        })
                  }))
    public class MyMultiRequestAttributes {

        @XACMLSubject(id = "subject1")
        String userID1 = "John";

        @XACMLSubject(id = "subject2")
        String userID2 = "Ringo";

        @XACMLAction(id = "action")
        String action = "access";

        @XACMLResource(id = "resource")
        String resource = "www.mywebsite.com";
    }

    public TestAnnotation(String[] args) throws MalformedURLException, ParseException, HelpException {
        super(args);
    }

    @Override
    public void run() throws IOException, FactoryException {
        //
        // We are not going to iterate any existing request files. So we will override
        // any TestBase code that assumes there are request files present.
        //
        //
        // Configure ourselves
        //
        this.configure();
        //
        // Cycle through creating a few objects
        //
        this.num = 0;
        this.doRequest(new MyRequestAttributes("John", "access", "www.mywebsite.com"));
        this.num++;
        this.doRequest(new MyRequestAttributes("Ringo", "access", "www.mywebsite.com"));
        this.num++;
        this.doRequest(new MyMultiRequestAttributes());
        this.num++;
    }

    private void doRequest(Object info) {
        try {
            Response response = this.callPDP(RequestParser.parseRequest(info));
            Path resultFile;
            if (this.output != null) {
                resultFile = Paths.get(this.output.toString(), "Response." + String.format("%03d", this.num)
                                                               + ".json");
            } else {
                resultFile = Paths.get(this.directory, "results",
                                       "Response." + String.format("%03d", this.num) + ".json");
            }
            //
            // Write the response to the result file
            //
            logger.info("Response is: " + response.toString());
            if (resultFile != null) {
                Files.write(resultFile, response.toString().getBytes());
            }
        } catch (IllegalArgumentException | IllegalAccessException | DataTypeException | IOException e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new TestAnnotation(args).run();
        } catch (ParseException | IOException | FactoryException e) {
            logger.error(e);
        } catch (HelpException e) {
            //
            // ignore this, its thrown just to exit the application
            // after dumping help to stdout.
            //
        }
    }
}
