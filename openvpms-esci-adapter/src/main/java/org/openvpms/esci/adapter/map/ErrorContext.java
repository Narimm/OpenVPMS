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
package org.openvpms.esci.adapter.map;

import org.openvpms.esci.adapter.util.ESCIAdapterException;


/**
 * Helper to determine the context of an error in an UBL document.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ErrorContext {

    /**
     * The path to the UBL element.
     */
    private String path;


    /**
     * The type of the root element.
     */
    private String type;

    /**
     * The identifier of the root element.
     */
    private String id;

    /**
     * Constructs an <tt>ErrorContext</tt>.
     *
     * @param type the UBL type
     */
    public ErrorContext(UBLType type) {
        this(type, null);
    }

    /**
     * Constructs an <tt>ErrorContext</tt>.
     *
     * @param type the UBL type
     * @param path the path to the element, relative to <tt>type</tt>
     */
    public ErrorContext(UBLType type, String path) {
        UBLType root = type;
        String fullPath = (path != null) ? root.getPath() + "/" + path : root.getPath();
        while (!root.useForErrorReporting() && root.getParent() != null) {
            root = root.getParent();
            fullPath = root.getPath() + "/" + fullPath;
        }
        this.path = fullPath;
        this.type = root.getType();
        try {
            id = root.getID();
        } catch (ESCIAdapterException ignore) {
            // id is invalid, so can't report it
        }
    }

    /**
     * Returns the path to the element.
     *
     * @return the path to the element
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the type of the parent element.
     *
     * @return the type of the parent element
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the ID of the parent element
     *
     * @return the ID of the parent element. May be <tt>null</tt>
     */
    public String getID() {
        return id;
    }

}
