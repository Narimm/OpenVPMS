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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.test;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.prefs.PreferenceService;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.prefs.UserPreferences;
import org.openvpms.web.echo.error.ErrorHandler;

import java.util.List;


/**
 * Abstract base class for tests requiring Spring and Echo2 to be set up.
 *
 * @author Tim Anderson
 */
public abstract class AbstractAppTest extends ArchetypeServiceTest {

    /**
     * The practice service.
     */
    private PracticeService practiceService;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        PracticeRules rules = new PracticeRules(getArchetypeService(), applicationContext.getBean(Currencies.class));
        LocationRules locationRules = new LocationRules(getArchetypeService());
        UserRules userRules = new UserRules(getArchetypeService());
        PreferenceService preferences = getPreferenceService();
        practiceService = new PracticeService(getArchetypeService(), rules, null);
        UserPreferences userPreferences = new UserPreferences(preferences, practiceService);
        ContextApplicationInstance app = new ContextApplicationInstance(new GlobalContext(), rules, locationRules,
                                                                        userRules, userPreferences) {
            /**
             * Switches the current workspace to display an object.
             *
             * @param object the object to view
             */
            @Override
            public void switchTo(IMObject object) {
            }

            /**
             * Switches the current workspace to one that supports a particular archetype.
             *
             * @param shortName the archetype short name
             */
            @Override
            public void switchTo(String shortName) {
            }

            @Override
            public Window init() {
                return new Window();
            }

            @Override
            public void lock() {
            }

            @Override
            public void unlock() {
            }
        };
        app.setApplicationContext(applicationContext);
        ApplicationInstance.setActive(app);
        app.doInit();
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown() {
        if (practiceService != null) {
            practiceService.dispose();
        }
        ApplicationInstance instance = ApplicationInstance.getActive();
        if (instance != null) {
            instance.dispose();
        }
    }

    /**
     * Returns the preference service.
     *
     * @return the preference service
     */
    protected PreferenceService getPreferenceService() {
        return Mockito.mock(PreferenceService.class);
    }

    /**
     * Initialises the error handler, so that errors are collected in the supplied array.
     *
     * @param errors the list to correct errors in
     */
    protected void initErrorHandler(List<String> errors) {
        // register an ErrorHandler to collect errors
        ErrorHandler.setInstance(new ErrorHandler() {
            @Override
            public void error(Throwable cause) {
                errors.add(cause.getMessage());
            }

            public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
                errors.add(message);
                if (listener != null) {
                    listener.windowPaneClosing(new WindowPaneEvent(this));
                }
            }
        });
    }
}
