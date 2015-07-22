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

import java.util.List;

/**
 * Lookup mappings read by {@link LookupMappingCSVReader}.
 *
 * @author Tim Anderson
 */
public class LookupMappings {

    /**
     * The mappings.
     */
    private final List<LookupMapping> mappings;

    /**
     * The erroneous mappings.
     */
    private final List<LookupMapping> errors;

    /**
     * Constructs a {@link LookupMappings}.
     *
     * @param mappings the mappings
     * @param errors   the erroneous mappings
     */
    public LookupMappings(List<LookupMapping> mappings, List<LookupMapping> errors) {
        this.mappings = mappings;
        this.errors = errors;
    }

    /**
     * Returns the mappings.
     *
     * @return the mappings
     */
    public List<LookupMapping> getMappings() {
        return mappings;
    }

    /**
     * Returns the erroneous mappings.
     *
     * @return the erroneous mappings
     */
    public List<LookupMapping> getErrors() {
        return errors;
    }
}
