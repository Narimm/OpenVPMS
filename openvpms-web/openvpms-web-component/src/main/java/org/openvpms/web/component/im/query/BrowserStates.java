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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.query;

import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/**
 * Manages the state of {@link Browser Browsers}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BrowserStates {

    /**
     * The browser states.
     */
    private List<BrowserState> states = new ArrayList<BrowserState>();

    /**
     * Adds the state of the specified browser.
     *
     * @param browser the browser
     */
    public synchronized void add(Browser browser) {
        BrowserState state = browser.getBrowserState();
        if (state != null) {
            boolean found = false;
            ListIterator<BrowserState> iter = states.listIterator();
            while (iter.hasNext()) {
                BrowserState existing = iter.next();
                if (existing.supports(browser)) {
                    iter.set(state);
                    found = true;
                    break;
                }
            }
            if (!found) {
                states.add(state);
            }
        }
    }

    /**
     * Populates a browser with a state.
     *
     * @param browser the browser to populate
     * @return <tt>true</tt> if the browser was populated or <tt>false</tt> if no state exists that supports the browser
     */
    public synchronized boolean setBrowserState(Browser browser) {
        boolean result = false;
        for (BrowserState state : states) {
            if (state.supports(browser)) {
                browser.setBrowserState(state);
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Determines if a {@link BrowserState} exists that supports the specified type and archetypes.
     *
     * @param type       the type
     * @param shortNames the archetype short names
     * @return <tt>true</tt> if a state exists
     */
    public synchronized boolean exists(Class type, String[] shortNames) {
        for (BrowserState state : states) {
            if (state.supports(shortNames, type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the singleton instance, scoped to the user's session.
     * <p/>
     * TODO - singletons aren't ideal but there is a lot of refactoring involved to avoid it
     *
     * @return the browser states
     */
    public static BrowserStates getInstance() {
        return (BrowserStates) ServiceHelper.getContext().getBean("browserStates");
    }

}
