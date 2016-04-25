/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;

import java.util.List;
import java.util.TimeZone;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link ConfigurationService}.
 *
 * @author benjamincharlton on 11/11/2015.
 */
public class ConfigurationServiceTestCase extends ArchetypeServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(Options.DYNAMIC_PORT);

    /**
     * Tests the {@link ConfigurationService#getTreatmentTemplates()} method.
     */
    @Test
    public void testGetTemplates() {
        String response = "[{\"objectType\":\"treatmenttemplate\",\"name\":\"FLUTD\"},{\"objectType\":" +
                          "\"treatmenttemplate\",\"name\":\"Seizure\"}]";
        stubFor(WireMock.get(urlEqualTo("/treatmenttemplates")).willReturn(aResponse()
                                                                                   .withStatus(201)
                                                                                   .withHeader("Content-Type", "application/json; charset=utf-8")
                                                                                   .withBody(response)));
        ConfigurationService service = createService();
        List<String> templates = service.getTreatmentTemplates();
        assertNotNull(templates);
        assertEquals(2, templates.size());
        assertEquals("FLUTD", templates.get(0));
        assertEquals("Seizure", templates.get(1));
    }

    /**
     * Creates a new {@link ConfigurationService}.
     *
     * @return a new service
     */
    private ConfigurationService createService() {
        String url = "http://localhost:" + wireMockRule.port() + "/";
        return new ConfigurationService(url, "foo", "bar", TimeZone.getTimeZone("Australia/Sydney"));
    }
}
