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

import org.pentaho.di.i18n.BaseMessages;


/**
 * Internationalisation support.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Messages {

    private static final String PACKAGE = Messages.class.getPackage().getName();

    /**
     * Returns a formatted, localised message.
     *
     * @param key  the message key.
     * @param args message arguments
     * @return a formatted message
     */
    public static String get(String key, Object... args) {
        switch (args.length) {
            case 0:
                return BaseMessages.getString(PACKAGE, key);
            case 1:
                return BaseMessages.getString(PACKAGE, key, getArg(args[0]));
            case 2:
                return BaseMessages.getString(PACKAGE, key, getArg(args[0]), getArg(args[1]));
            case 3:
                return BaseMessages.getString(PACKAGE, key, getArg(args[0]), getArg(args[1]), getArg(args[2]));
            case 4:
                return BaseMessages.getString(PACKAGE, key, getArg(args[0]), getArg(args[1]), getArg(args[2]),
                                              getArg(args[3]));
            case 5:
                return BaseMessages.getString(PACKAGE, key, getArg(args[0]), getArg(args[1]), getArg(args[2]),
                                              getArg(args[3]), getArg(args[4]));
            default:
                return BaseMessages.getString(PACKAGE, key, getArg(args[0]), getArg(args[1]), getArg(args[2]),
                                              getArg(args[3]), getArg(args[4]), getArg(args[5]));
        }
    }

    /**
     * Helper to convert an argument to a string.
     *
     * @param arg the argument
     * @return the string form of <tt>arg</tt>, or <tt>null</tt>
     */
    private static String getArg(Object arg) {
        return (arg != null) ? arg.toString() : null;
    }
}