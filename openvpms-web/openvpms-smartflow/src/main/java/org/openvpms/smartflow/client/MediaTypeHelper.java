package org.openvpms.smartflow.client;

import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.MediaType;

/**
 * Media type helper.
 *
 * @author Tim Anderson
 */
class MediaTypeHelper {

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
