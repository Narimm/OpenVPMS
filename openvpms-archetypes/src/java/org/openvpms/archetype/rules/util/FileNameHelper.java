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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.util;

/**
 * File name helper methods.
 *
 * @author Tim Anderson
 */
public class FileNameHelper {

    /**
     * Characters to exclude from file names.
     */
    private static final String ILLEGAL_CHARACTERS = "\\\\|/|:|\\*|\\?|<|>|\\|";

    /**
     * Replaces illegal file name characters with underscores.
     *
     * @param name the name to clean
     * @return the name with illegal characters replaced with underscores
     */
    public static String clean(String name) {
        return name.replaceAll(ILLEGAL_CHARACTERS, "_");
    }
}
