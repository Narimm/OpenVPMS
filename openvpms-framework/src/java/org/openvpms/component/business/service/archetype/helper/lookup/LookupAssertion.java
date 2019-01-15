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

package org.openvpms.component.business.service.archetype.helper.lookup;

import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.model.archetype.AssertionDescriptor;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.IMObject;

import java.util.Collection;
import java.util.List;


/**
 * Provides access to lookups defined by an {@link AssertionDescriptor}.
 *
 * @author Tim Anderson
 */
public interface LookupAssertion {

    /**
     * Returns the assertion descriptor.
     *
     * @return the assertion descriptor
     */
    AssertionDescriptor getDescriptor();

    /**
     * Returns the type of the assertion.
     *
     * @return the type of the assertion
     */
    String getType();

    /**
     * Returns the lookups for this assertion.
     *
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the lookup type
     */
    List<Lookup> getLookups();

    /**
     * Returns the lookups for this assertion.
     *
     * @param context the context
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the lookup type
     */
    Collection<Lookup> getLookups(IMObject context);

    /**
     * Returns the lookup with the specified code.
     *
     * @param code the lookup code
     * @return the lookup matching {@code code}, or {@code null} none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the lookup type
     */
    Lookup getLookup(String code);

    /**
     * Returns the lookup with the specified code.
     *
     * @param context the context
     * @param code    th lookup code
     * @return the lookup matching {@code code}, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the lookup type
     */
    Lookup getLookup(IMObject context, String code);

    /**
     * Returns the name of the lookup with the specified code.
     *
     * @param code the lookup code
     * @return the name of the lookup matching {@code code}, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the lookup type
     */
    String getName(String code);

    /**
     * Returns the name of the lookup with the specified code.
     *
     * @param context the context. May be {@code null}
     * @return the name of the lookup matching {@code code}, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the lookup type
     */
    String getName(IMObject context, String code);

    /**
     * Returns the default lookup.
     *
     * @return the default lookup or {@code null} if there is no default
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the lookup type
     */
    Lookup getDefault();

}
