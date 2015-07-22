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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

/**
 * Lookup mapping read by {@link LookupMappingCSVReader}.
 *
 * @author Tim Anderson
 */
public class LookupMapping {

    /**
     * The 'from lookup' archetype short name.
     */
    private final String fromType;

    /**
     * The 'from lookup' code.
     */
    private final String fromCode;

    /**
     * The 'from lookup' name.
     */
    private final String fromName;

    /**
     * The 'to lookup' archetype short name.
     */
    private final String toType;

    /**
     * The 'to lookup' code.
     */
    private final String toCode;

    /**
     * The 'to lookup' name.
     */
    private final String toName;

    /**
     * The line that the mapping was read from.
     */
    private final int line;

    /**
     * Processing error message.
     */
    private String error;


    /**
     * Constructs a {@link LookupMapping}.
     *
     * @param fromType the 'from lookup' archetype short name
     * @param fromCode the 'from lookup' code
     * @param fromName the 'from lookup' name
     * @param toType   the 'to lookup' archetype short name
     * @param toCode   the 'to lookup' code
     * @param toName   the 'to lookup' name
     * @param line     the line that the mapping was read from, or {@code -1} if it is not known
     */
    public LookupMapping(String fromType, String fromCode, String fromName, String toType, String toCode, String toName,
                         int line) {
        this.fromType = fromType;
        this.fromCode = fromCode;
        this.fromName = fromName;
        this.toType = toType;
        this.toCode = toCode;
        this.toName = toName;
        this.line = line;
    }

    /**
     * Returns the archetype of the lookup to map from.
     *
     * @return the 'from lookup' archetype short name
     */
    public String getFromType() {
        return fromType;
    }

    /**
     * Returns the code of the lookup to map from.
     *
     * @return the 'from lookup' code.
     */
    public String getFromCode() {
        return fromCode;
    }

    /**
     * Returns the name of the lookup to map from.
     *
     * @return the 'from lookup' name.
     */
    public String getFromName() {
        return fromName;
    }

    /**
     * Returns the archetype of the lookup to map to.
     *
     * @return the 'to lookup' archetype short name
     */
    public String getToType() {
        return toType;
    }

    /**
     * Returns the code of the lookup to map to.
     *
     * @return the 'to lookup' code
     */
    public String getToCode() {
        return toCode;
    }

    /**
     * Returns the name of the lookup to map to.
     *
     * @return the 'to lookup' name
     */
    public String getToName() {
        return toName;
    }

    /**
     * Returns the line number that the mapping was read from.
     *
     * @return the line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Sets an error message to indicate that the mapping is invalid.
     *
     * @param error the error message
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Returns any error message generated while processing the mapping.
     *
     * @return the error message, or {@code null} if there is no error
     */
    public String getError() {
        return error;
    }

}
