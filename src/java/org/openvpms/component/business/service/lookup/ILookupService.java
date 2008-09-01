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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.lookup;

import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.Collection;


/**
 * Service for accessing {@link Lookup}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ILookupService {

    /**
     * Returns the lookup with the specified lookup archetype short name and
     * code.
     *
     * @param shortName the lookup archetype short name. May contain wildcards
     * @param code      the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     */
    Lookup getLookup(String shortName, String code);

    /**
     * Returns all lookups with the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name. May contain wildcards
     * @return a collection of lookups with the specified short name
     */
    Collection<Lookup> getLookups(String shortName);

    /**
     * Returns the default lookup for the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name. May contain wildcards
     * @return the default lookup, or <Tt>null</tt> if none is found
     */
    Lookup getDefaultLookup(String shortName);

    /**
     * Returns the lookups that are the source of any lookup relationship where
     * the supplied lookup is the target.
     *
     * @param lookup the target lookup
     * @return a collection of source lookups
     */
    Collection<Lookup> getSourceLookups(Lookup lookup);

    /**
     * Returns the lookups that are the source of specific lookup relationships
     * where the supplied lookup is the target.
     *
     * @param lookup                the target lookup
     * @param relationshipShortName the relationship short name. May contain
     *                              wildcards
     * @return a collection of source lookups
     */
    Collection<Lookup> getSourceLookups(Lookup lookup,
                                        String relationshipShortName);

    /**
     * Returns the lookups that are the target of any lookup relationship where
     * the supplied lookup is the source.
     *
     * @param lookup the source lookup
     * @return a collection of target lookups
     */
    Collection<Lookup> getTargetLookups(Lookup lookup);

    /**
     * Returns the lookups that are the target of specific lookup relationships
     * where the supplied lookup is the source.
     *
     * @param lookup                the source lookup
     * @param relationshipShortName the relationship short name. May contain
     *                              wildcards
     * @return a collection of target lookups
     */
    Collection<Lookup> getTargetLookups(Lookup lookup,
                                        String relationshipShortName);
}
