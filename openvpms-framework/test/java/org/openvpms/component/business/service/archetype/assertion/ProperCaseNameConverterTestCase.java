/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service.archetype.assertion;

import junit.framework.TestCase;


/**
 * Tests the {@link ProperCaseNameConverter} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProperCaseNameConverterTestCase extends TestCase {

    private ProperCaseNameConverter capitaliser;

    /**
     * Tests case conversion of simple names.
     */
    public void testCaseConversion() {
        check("Phillip K Dick", "phillip k dick");
        check("J K Rowling", "J K rowLing");
    }

    /**
     * Tests names that start with <em>Mac</em>, <em>Mc</em> and <em>d'</em>.
     */
    public void testStartsWith() {
        check("Jacques MacDonald", "jacques macdonald");
        check("Joseph R McCarthy", "joseph r mccarthy");
        check("Charles d'Artagnan", "charles d'artagnan");
    }

    /**
     * Tests names that contain <em>-</em>, <em>.</em> and <em>'</em>.
     */
    public void testContains() {
        check("Mary-Anne Fahey", "mary-anne fahey");
        check("Grace O'Malley", "grace o'malley");
        check("J.K. Rowling", "j.k. rowling");
        check("Werner von Braun", "werner von braun");
    }

    /**
     * Tests names that end with <em>'s</em>.
     */
    public void testEndsWith() {
        check("'s", "'s");
        check("Test's", "test's");
        check("Test's Case", "test's case");
        check("Test's Case's", "test's case's");
        check("Test-Hyphen's Case", "test-hyphen's case");
    }

    /**
     * Tests names that contains words that are exceptions to the other rules.
     */
    public void testExceptions() {
        check("La Trobe", "la trobe");
        check("Macquarie Bank", "macquarie bank");
        check("Apple Macintosh", "apple macintosh");
        check("Leonardo di Caprio", "leonardo di caprio");
        check("Leonardo da Vinci", "leonardo da vinci");
        check("Creme de la Creme", "creme de la creme");
    }

    /**
     * Verifies that leading and trailing spaces are removed, and that multiple spaces are replaced with a single space.
     */
    public void testCollapseSpaces() {
        check("Jacques MacDonald", " jacques   macdonald ");
    }

    /**
     * Sets up the fixture.
     */
    @Override
    protected void setUp() throws Exception {
        ProperCaseRules rules = new ProperCaseRules() {
            int version;
            public String[] getStartsWith() {
                return new String[]{"Mac", "Mc", "d'"};
            }

            public String[] getContains() {
                return new String[]{"-", ".", "'"};
            }

            public String[] getEndsWith() {
                return new String[]{"'s"};
            }

            public String[] getExceptions() {
                return new String[]{"von", "van", "de", "la", "da", "di", "Macintosh", "Macquarie", "La Trobe"};
            }

            public int getVersion() {
                return ++version;
            }
        };
        capitaliser = new ProperCaseNameConverter(rules);
    }

    private void check(String expected, String name) {
        assertEquals(expected, capitaliser.convert(name));
    }

}