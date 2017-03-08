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

package org.openvpms.report;

import org.openvpms.component.system.common.util.MapPropertySet;

import java.util.Map;

/**
 * Report parameters.
 * <p/>
 * This prefixes parameters with a {@code "P."} so they can be distinguished from fields.
 * <p/>
 * They can be accessed using {@code $P.<name>} in expressions.
 *
 * @author Tim Anderson
 */
public class Parameters extends MapPropertySet {

    /**
     * Constructs a {@link Parameters}.
     *
     * @param parameters the parameters. May be {@code null}
     */
    public Parameters(Map<String, Object> parameters) {
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                set("P." + entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Determines if the are no parameters.
     *
     * @return {@code true} if there are no parameters, otherwise {@code false}
     */
    public boolean isEmpty() {
        return getProperties().isEmpty();
    }
}
