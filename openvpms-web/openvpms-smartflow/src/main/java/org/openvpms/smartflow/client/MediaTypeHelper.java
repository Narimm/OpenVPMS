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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.client;

import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.MediaType;

/**
 * Media type helper.
 *
 * @author Tim Anderson
 */
public class MediaTypeHelper {

    /**
     * PDF media type.
     */
    public static final String APPLICATION_PDF = "application/pdf";

    /**
     * PDF media type.
     */
    public static final MediaType APPLICATION_PDF_TYPE = new MediaType("application", "pdf");

    /**
     * Determines if a media type is "application/json".
     *
     * @param type the media type. May be {@code null}
     * @return {@code true} if the type is "application/json"
     */
    public static boolean isJSON(MediaType type) {
        return type != null && equals(type, MediaType.APPLICATION_JSON_TYPE);
    }

    /**
     * Determines if a media type is "application/pdf".
     *
     * @param type the media type. May be {@code null}
     * @return {@code true} if the type is "application/pdf"
     */
    public static boolean isPDF(MediaType type) {
        return type != null && equals(type, APPLICATION_PDF_TYPE);
    }

    /**
     * Determines if a media type is one of a set of expected types.
     *
     * @param type     the media type. May be {@code null}
     * @param expected the expected types
     * @return {@code true} if the type is one of expected
     */
    public static boolean isA(MediaType type, MediaType... expected) {
        boolean result = false;
        if (type != null && expected.length > 0) {
            result = true;
            for (MediaType t : expected) {
                if (!equals(type, t)) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines if two media types are equal based on their type and subtype i.e. it ignores any parameters.
     *
     * @param type1 the first media type to compare
     * @param type2 the second media type to compare
     * @return {@code true} if they are equal
     */
    private static boolean equals(MediaType type1, MediaType type2) {
        return StringUtils.equalsIgnoreCase(type1.getType(), type2.getType())
               && StringUtils.equalsIgnoreCase(type1.getSubtype(), type2.getSubtype());
    }
}
