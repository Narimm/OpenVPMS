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

package org.openvpms.archetype.function.user;

import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.function.expression.ExpressionFunctions;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link UserFunctions} and {@link CachingUserFunctions} classes.
 *
 * @author Tim Anderson
 */
public class UserFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * The practice rules.
     */
    @Autowired
    PracticeRules rules;

    /**
     * The practice service.
     */
    private PracticeService practiceService;

    /**
     * The JXPath context.
     */
    private Party practice;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        practice = (Party) create(PracticeArchetypes.PRACTICE);
        practiceService = Mockito.mock(PracticeService.class);
        Mockito.when(practiceService.getPractice()).thenReturn(practice);
    }

    /**
     * Tests the {@link UserFunctions#format(User, String)} and {@link UserFunctions#formatById(long, String)} methods.
     */
    @Test
    public void testFormat() {
        User user1 = createUser("Dr Jo Bloggs Name", "Dr Jo Bloggs Desc, BVSc MVS", "Dr", "Jo", "Bloggs", "BVSc MVS");
        User user2 = createUser("Fred Smith Name", "Fred Smith Desc", null, "Fred", "Smith", null);
        User user3 = createUser("M Foo", "Dr M Foo, BVSc MVS", null, null, null, null);
        Lookup shortFormat = createUserNameFormat(
                "NAME", "expr:ifempty(expr:concatIf($firstName, ' ', $lastName), $name)");
        Lookup mediumFormat = createUserNameFormat(
                "TITLE_NAME", "concat(\n" +
                              "     expr:concatIf($title,' '), \n" +
                              "     expr:ifempty(expr:concatIf($firstName, ' ', $lastName), $name))\n");
        Lookup longFormat = createUserNameFormat(
                "TITLE_NAME_QUALIFICATIONS", "concat(\n" +
                                             "     expr:concatIf($title,' '), \n" +
                                             "     expr:ifempty(expr:concatIf($firstName, ' ', $lastName), $name),\n" +
                                             "     expr:concatIf(', ', $qualifications))\n");

        IMObjectBean bean = new IMObjectBean(practice);
        bean.setValue("shortUserNameFormat", shortFormat.getCode());
        bean.setValue("mediumUserNameFormat", mediumFormat.getCode());
        bean.setValue("longUserNameFormat", longFormat.getCode());
        checkFormat(user1, "short", "Jo Bloggs");
        checkFormat(user1, "medium", "Dr Jo Bloggs");
        checkFormat(user1, "long", "Dr Jo Bloggs, BVSc MVS");

        checkFormat(user2, "short", "Fred Smith");
        checkFormat(user2, "medium", "Fred Smith");
        checkFormat(user2, "long", "Fred Smith");

        checkFormat(user3, "short", "M Foo");
        checkFormat(user3, "medium", "M Foo");
        checkFormat(user3, "long", "M Foo");
    }

    /**
     * Verifies that the username node is defined as a variable.
     */
    @Test
    public void testUsername() {
        User user = TestHelper.createUser();

        Lookup shortFormat = createUserNameFormat("USERNAME", "$username");
        IMObjectBean bean = new IMObjectBean(practice);
        bean.setValue("shortUserNameFormat", shortFormat.getCode());
        checkFormat(user, "short", user.getUsername());
    }

    /**
     * Verifies that the description node is defined as a variable.
     */
    @Test
    public void testDescription() {
        User user = createUser("Dr Jo Bloggs Name", "Dr Jo Bloggs Desc, BVSc MVS", "Dr", "Jo", "Bloggs", "BVSc MVS");

        Lookup shortFormat = createUserNameFormat("DESCRIPTION", "$description");
        IMObjectBean bean = new IMObjectBean(practice);
        bean.setValue("longUserNameFormat", shortFormat.getCode());
        checkFormat(user, "long", "Dr Jo Bloggs Desc, BVSc MVS");
    }

    /**
     * Verifies the {@link UserFunctions#format(User, String)} and {@link UserFunctions#formatById(long, String)}
     * return the expected values.
     *
     * @param user     the user
     * @param style    the format style
     * @param expected the expected result
     */
    private void checkFormat(User user, String style, String expected) {
        checkFormat(user, style, expected, false);
        checkFormat(user, style, expected, true);
    }

    /**
     * Verifies the {@link UserFunctions#format(User, String)} and {@link UserFunctions#formatById(long, String)}
     * return the expected values.
     *
     * @param user     the user
     * @param style    the format style
     * @param expected the expected result
     * @param cache    if {@code true}, use {@link CachingUserFunctions} otherwise use {@link UserFunctions}
     */
    private void checkFormat(User user, String style, String expected, boolean cache) {
        FunctionLibrary library = new FunctionLibrary();
        if (cache) {
            library.addFunctions(new CachingUserFunctions(getArchetypeService(), practiceService, getLookupService(),
                                                          library, 1024));
        } else {
            library.addFunctions(new UserFunctions(getArchetypeService(), practiceService, getLookupService(),
                                                   library));
        }
        library.addFunctions(new ExpressionFunctions("expr"));

        // test the user based format
        JXPathContext context1 = JXPathHelper.newContext(user, library);
        assertEquals(expected, context1.getValue("user:format(., '" + style + "')"));

        // test the id based format
        JXPathContext context2 = JXPathHelper.newContext(new Object(), library);
        assertEquals(expected, context2.getValue("user:format(" + user.getId() + ", '" + style + "')"));
    }

    /**
     * Helper to create a new lookup.userNameFormat.
     *
     * @param code       the lookup code
     * @param expression the expression
     * @return a new lookup
     */
    private Lookup createUserNameFormat(String code, String expression) {
        Lookup lookup = TestHelper.getLookup("lookup.userNameFormat", code, false);
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("expression", expression);
        bean.save();
        return lookup;
    }

    /**
     * Helper to create a user.
     *
     * @param name           the name
     * @param description    the description
     * @param title          the title. May be {@code null}
     * @param firstName      the first name. May be {@code null}
     * @param lastName       the last name. May be {@code null}
     * @param qualifications the qualifications. May be {@code null}
     * @return a new user
     */
    private User createUser(String name, String description, String title, String firstName, String lastName,
                            String qualifications) {
        User user = TestHelper.createUser();
        IMObjectBean bean = new IMObjectBean(user);
        if (title != null) {
            Lookup lookup = TestHelper.getLookup("lookup.personTitle", title.toUpperCase(), title, true);
            bean.setValue("title", lookup.getCode());
        } else {
            bean.setValue("title", null);
        }
        bean.setValue("name", name);
        bean.setValue("description", description);
        bean.setValue("firstName", firstName);
        bean.setValue("lastName", lastName);
        bean.setValue("qualifications", qualifications);
        bean.save();
        return user;
    }

}
