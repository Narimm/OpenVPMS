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

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Default implementation of {@link IMObjectOperations}.
 *
 * @author Tim Anderson
 */
public class DefaultIMObjectOperations<T extends IMObject> extends AbstractIMObjectOperations<T> {

    /**
     * The singleton instance.
     */
    private static final DefaultIMObjectOperations INSTANCE = new DefaultIMObjectOperations();

    /**
     * Default constructor.
     */
    private DefaultIMObjectOperations() {

    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> DefaultIMObjectOperations<T> getInstance() {
        return INSTANCE;
    }
}
