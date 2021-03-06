/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.siemens.sw360.components;

import com.siemens.sw360.datahandler.TestUtils;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.siemens.sw360.datahandler.TestUtils.*;
import static com.siemens.sw360.datahandler.thrift.components.Component._Fields.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.fail;

/**
 * @author daniele.fognini@tngtech.com
 */
public class ComponentHandlerTest {

    private ComponentHandler componentHandler;
    private User adminUser = TestUtils.getAdminUser(getClass());

    @Before
    public void setUp() throws Exception {
        assertTestDbNames();
        deleteAllDatabases();
        componentHandler = new ComponentHandler();
    }

    @After
    public void tearDown() throws Exception {
        deleteAllDatabases();
    }

    @Test
    public void testGetByUploadId() throws Exception {

        Component originalComponent = new Component("name").setDescription("a desc");
        String componentId = componentHandler.addComponent(originalComponent, adminUser);

        Release release = new Release("name", "version", componentId).setFossologyId("id");
        String releaseId = componentHandler.addRelease(release, adminUser);

        Component component = componentHandler.getComponentForReportFromFossologyUploadId("id");

        assertThat(component, is(not(nullValue())));
        assertThat(component, is(equalTo(originalComponent, restrictedToFields(ID, NAME, DESCRIPTION))));

        assertThat(componentHandler.getReleaseById(releaseId, adminUser), is(not(nullValue())));
        assertThat(componentHandler.getComponentById(componentId, adminUser), is(not(nullValue())));

        assertThat(componentHandler.deleteComponent(componentId, adminUser), is(RequestStatus.SUCCESS));

        try {
            componentHandler.getReleaseById(releaseId, adminUser);
            fail("expected exception not thrown");
        } catch (SW360Exception e) {
            assertThat(e.getWhy(), containsString("Could not fetch"));
        }
        try {
            componentHandler.getComponentById(componentId, adminUser);
            fail("expected exception not thrown");
        } catch (SW360Exception e) {
            assertThat(e.getWhy(), containsString("Could not fetch"));
        }
    }
}