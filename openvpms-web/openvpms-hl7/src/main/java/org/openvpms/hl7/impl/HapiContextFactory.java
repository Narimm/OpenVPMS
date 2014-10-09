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

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ParserConfiguration;
import ca.uhn.hl7v2.util.idgenerator.IDGenerator;
import ca.uhn.hl7v2.util.idgenerator.UUIDGenerator;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;

/**
 * Factory for {@link HapiContext} instances.
 *
 * @author Tim Anderson
 */
class HapiContextFactory {

    /**
     * Default ID generator.
     */
    private static final IDGenerator ID_GENERATOR = new UUIDGenerator();

    /**
     * Creates a {@code HapiContext}, with a {@code UUIDGenerator}.
     *
     * @return a new context
     */
    public static HapiContext create() {
        return create(ID_GENERATOR);
    }

    /**
     * Creates a {@code HapiContext}, with the specified {@code IDGenerator}.
     *
     * @param generator the ID generator
     * @return a new context
     */
    public static HapiContext create(IDGenerator generator) {
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setIdGenerator(generator);
        return new DefaultHapiContext(configuration, ValidationContextFactory.defaultValidation(),
                                      new DefaultModelClassFactory());
    }
}
