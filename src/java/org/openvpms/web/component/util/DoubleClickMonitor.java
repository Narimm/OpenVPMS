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
package org.openvpms.web.component.util;

import org.apache.commons.lang.ObjectUtils;

import java.util.Date;


/**
 * Helper to determine if there has been a double click.
 * <p/>
 * This is a workaround for the problem that mouse double clicks are not supported by the Echo Web Framework.
 * <p/>
 * Double clicks are simulated by tracking single clicks on the same object, within a configurable time frame.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DoubleClickMonitor {

    /**
     * The last clicked on object.
     */
    private Object last;

    /**
     * The time of the last click.
     */
    private Date lastClick;

    /**
     * The click interval.
     */
    private final int interval;


    /**
     * Constructs a <tt>DoubleClickMonitor</tt> with no interval.
     * <p/>
     * In this mode, any two clicks on the same object will be considered a double click.
     */
    public DoubleClickMonitor() {
        this(0);
    }

    /**
     * Constructs a <tt>DoubleClickMonitor</tt> with the specified interval.
     *
     * @param interval the maximum interval between clicks, in milliseconds
     */
    public DoubleClickMonitor(int interval) {
        this.interval = interval;
    }

    /**
     * Determines if there has been a double click on an object.
     * <p/>
     * It is considered a double click if the method is called twice with the same (i.e using equals()) object,
     * within the specified interval. If a double click is detected, the monitor resets - two more clicks are required
     * within the interval to be considered another double click.
     *
     * @param object the clicked on object. May be <tt>null</tt>
     * @return true if the object has been clicked twice within the interval
     */
    public boolean isDoubleClick(Object object) {
        boolean result;
        Date now = new Date();
        result = (lastClick != null && (interval == 0 || (lastClick.getTime() + interval) >= now.getTime()));
        result = result && ObjectUtils.equals(last, object);
        if (result) {
            lastClick = null;
            last = null;
        } else {
            lastClick = now;
            last = object;
        }
        return result;

    }

    /**
     * Determines if there has been a double click.
     * <p/>
     * It is considered a double click if the method is called twice within the specified interval.
     *
     * @return <tt>true</tt> if there has been a double click, otherwise <tt>false</tt>
     */
    public boolean isDoubleClick() {
        return isDoubleClick(null);
    }

}
