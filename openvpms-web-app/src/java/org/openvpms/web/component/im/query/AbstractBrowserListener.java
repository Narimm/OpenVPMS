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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Abstract implementation of  {@link BrowserListener} that provides stubs for each method.
 * This enables subclasses to only provide the methods they need.
 *
 * @author Tim Anderson
 */
public abstract class AbstractBrowserListener<T extends IMObject> implements BrowserListener<T> {

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    public void selected(T object) {

    }

    /**
     * Invoked when an object is browsed.
     *
     * @param object the browsed object
     */
    public void browsed(T object) {
    }

    /**
     * Invoked when a query is performed.
     */
    public void query() {
    }
}
