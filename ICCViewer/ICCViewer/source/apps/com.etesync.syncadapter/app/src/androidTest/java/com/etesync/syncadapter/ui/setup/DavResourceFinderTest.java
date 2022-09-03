/*
 * Copyright © 2013 – 2016 Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package com.etesync.syncadapter.ui.setup;

import com.etesync.syncadapter.App;
import com.etesync.syncadapter.HttpClient;

import org.junit.After;
import org.junit.Before;

import java.net.URI;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static androidx.test.InstrumentationRegistry.getTargetContext;

public class DavResourceFinderTest {

    MockWebServer server = new MockWebServer();

    BaseConfigurationFinder finder;
    OkHttpClient client;
    LoginCredentials credentials;

    private static final String
            PATH_NO_DAV = "/nodav",

            PATH_CALDAV = "/caldav",
            PATH_CARDDAV = "/carddav",
            PATH_CALDAV_AND_CARDDAV = "/both-caldav-carddav",

            SUBPATH_PRINCIPAL = "/principal",
            SUBPATH_ADDRESSBOOK_HOMESET = "/addressbooks",
            SUBPATH_ADDRESSBOOK = "/addressbooks/private-contacts";

    @Before
    public void initServerAndClient() throws Exception {
        server.setDispatcher(new TestDispatcher());
        server.start();

        credentials = new LoginCredentials(URI.create("/"), "mock", "12345");
        finder = new BaseConfigurationFinder(getTargetContext(), credentials);

        client = HttpClient.create(null);
    }

    @After
    public void stopServer() throws Exception {
        server.shutdown();
    }

    // mock server

    public class TestDispatcher extends Dispatcher {

        @Override
        public MockResponse dispatch(RecordedRequest rq) throws InterruptedException {
            if (!checkAuth(rq)) {
                MockResponse authenticate = new MockResponse().setResponseCode(401);
                authenticate.setHeader("WWW-Authenticate", "Basic realm=\"test\"");
                return authenticate;
            }

            String path = rq.getPath();

            if ("OPTIONS".equalsIgnoreCase(rq.getMethod())) {
                String dav = null;
                if (path.startsWith(PATH_CALDAV))
                    dav = "calendar-access";
                else if (path.startsWith(PATH_CARDDAV))
                    dav = "addressbook";
                else if (path.startsWith(PATH_CALDAV_AND_CARDDAV))
                    dav = "calendar-access, addressbook";
                MockResponse response = new MockResponse().setResponseCode(200);
                if (dav != null)
                       response.addHeader("DAV", dav);
                return response;

            } else if ("PROPFIND".equalsIgnoreCase(rq.getMethod())) {
                String props = null;
                switch (path) {
                    case PATH_CALDAV:
                    case PATH_CARDDAV:
                        props = "<current-user-principal><href>" + path + SUBPATH_PRINCIPAL + "</href></current-user-principal>";
                        break;

                    case PATH_CARDDAV + SUBPATH_PRINCIPAL:
                        props = "<CARD:addressbook-home-set>" +
                                "   <href>" + PATH_CARDDAV + SUBPATH_ADDRESSBOOK_HOMESET + "</href>" +
                                "</CARD:addressbook-home-set>";
                        break;
                    case PATH_CARDDAV + SUBPATH_ADDRESSBOOK:
                        props = "<resourcetype>" +
                                "   <collection/>" +
                                "   <CARD:addressbook/>" +
                                "</resourcetype>";
                        break;
                }
                App.Companion.getLog().info("Sending props: " + props);
                return new MockResponse()
                        .setResponseCode(207)
                        .setBody("<multistatus xmlns='DAV:' xmlns:CARD='urn:ietf:params:xml:ns:carddav'>" +
                                "<response>" +
                                "   <href>" + rq.getPath() + "</href>" +
                                "   <propstat><prop>" + props + "</prop></propstat>" +
                                "</response>" +
                                "</multistatus>");
            }

            return new MockResponse().setResponseCode(404);
        }

        private boolean checkAuth(RecordedRequest rq) {
            return "Basic bW9jazoxMjM0NQ==".equals(rq.getHeader("Authorization"));
        }
    }

}
