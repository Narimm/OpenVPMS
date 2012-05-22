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
package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.act.Act;

/**
 * Default implementation of {@link IMObjectOperations} for acts.
 *
 * @author Tim Anderson
 */
public class DefaultActOperations<T extends Act> extends ActOperations<T> {

    /**
     * The singleton instance.
     */
    private static final DefaultActOperations INSTANCE = new DefaultActOperations();

    /**
     * Default constructor.
     */
    private DefaultActOperations() {
    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends Act> DefaultActOperations<T> getInstance() {
        return INSTANCE;
    }

}
