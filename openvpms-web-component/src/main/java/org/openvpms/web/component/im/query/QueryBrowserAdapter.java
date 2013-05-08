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

/**
 * Adapts the results of one {@link QueryBrowser} to another.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class QueryBrowserAdapter<A, T> extends BrowserAdapter<A, T> implements QueryBrowser<T> {

    /**
     * Constructs a <tt>ResultSetBrowserAdapter</tt>.
     * <p/>
     * The browser to adapt from must be set using {@link #setBrowser}.
     */
    public QueryBrowserAdapter() {
    }

    /**
     * Constructs a <tt>ResultSetBrowserAdapter</tt>.
     *
     * @param browser the browser to adapt from
     */
    public QueryBrowserAdapter(QueryBrowser<A> browser) {
        super(browser);
    }

    /**
     * Returns the underlying browser.
     *
     * @return the underlying browser
     */
    @Override
    public QueryBrowser<A> getBrowser() {
        return (QueryBrowser<A>) super.getBrowser();
    }

    /**
     * Sets the underlying browser.
     *
     * @param browser the browser. Must be an instanceof of {@link QueryBrowser}.
     */
    protected void setBrowser(Browser<A> browser) {
        if (!(browser instanceof QueryBrowser)) {
            throw new IllegalArgumentException("Argument 'browser' must be a QueryBrowser: " + browser);

        }
        super.setBrowser(browser);
    }
}
