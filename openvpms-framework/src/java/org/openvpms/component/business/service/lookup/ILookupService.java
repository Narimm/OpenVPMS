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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.lookup;

import org.openvpms.component.model.lookup.Lookup;


/**
 * Service for accessing {@link Lookup}s.
 *
 * @author Tim Anderson
 */
public interface ILookupService extends org.openvpms.component.service.lookup.LookupService {

    /**
     * Replaces one lookup with another.
     * <p/>
     * Each lookup must be of the same archetype.
     *
     * @param source the lookup to replace
     * @param target the lookup to replace {@code source} with
     */
    void replace(Lookup source, Lookup target);

}
