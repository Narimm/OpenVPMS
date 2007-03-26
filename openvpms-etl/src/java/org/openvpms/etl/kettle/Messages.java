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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.kettle;

import be.ibridge.kettle.i18n.BaseMessages;


/**
 * Internationalisation support.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Messages {

    private static final String PACKAGE = Messages.class.getPackage().getName();

    public static String getString(String key) {
        return BaseMessages.getString(PACKAGE, key);
    }

    public static String getString(String key, String param1) {
        return BaseMessages.getString(PACKAGE, key, param1);
    }

    public static String getString(String key, String param1, String param2) {
        return BaseMessages.getString(PACKAGE, key, param1, param2);
    }

    public static String getString(String key, String param1, String param2,
                                   String param3) {
        return BaseMessages.getString(PACKAGE, key, param1, param2, param3);
    }

    public static String getString(String key, String param1, String param2,
                                   String param3, String param4) {
        return BaseMessages.getString(PACKAGE, key, param1, param2, param3,
                                      param4);
    }

    public static String getString(String key, String param1, String param2,
                                   String param3, String param4,
                                   String param5) {
        return BaseMessages.getString(PACKAGE, key, param1, param2, param3,
                                      param4, param5);
    }

    public static String getString(String key, String param1, String param2,
                                   String param3, String param4, String param5,
                                   String param6) {
        return BaseMessages.getString(PACKAGE, key, param1, param2, param3,
                                      param4, param5, param6);
    }
}