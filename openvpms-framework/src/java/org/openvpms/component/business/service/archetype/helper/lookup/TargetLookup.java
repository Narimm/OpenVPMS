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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper.lookup;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.Collection;
import java.util.Collections;


/**
 * Lookup assertion denoting the target in a lookup relationship.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class TargetLookup extends AbstractLookupAssertion {

    /**
     * The lookup type.
     */
    public static final String TYPE = "targetLookup";

    /**
     * The lookup relationship short name.
     */
    private final String relationship;

    /**
     * The jxpath to the lookup code.
     */
    private final String value;


    /**
     * Constructs a new <tt>TargetLookup</tt>.
     *
     * @param descriptor    the assertion descriptor
     * @param service       the archetype service
     * @param lookupService the lookup service
     */
    public TargetLookup(AssertionDescriptor descriptor,
                        IArchetypeService service,
                        ILookupService lookupService) {
        super(descriptor, TYPE, service, lookupService);
        relationship = getProperty("relationship");
        value = getProperty("value");
        if (StringUtils.isEmpty(relationship) || StringUtils.isEmpty(value)) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.InvalidTargetLookupSpec);
        }
    }

    /**
     * Returns the lookups for this assertion.
     *
     * @param context the context
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Collection<Lookup> getLookups(IMObject context) {
        Collection<Lookup> lookups;
        Lookup lookup = getSourceLookup(context);
        if (lookup != null) {
            lookups = getLookupService().getTargetLookups(lookup,
                                                          relationship);
        } else {
            lookups = Collections.emptyList();
        }
        return lookups;
    }

    /**
     * Returns the lookup with the specified code.
     *
     * @param context the context
     * @return the lookup matching <tt>code</tt>, or <tt>null</tt> if
     *         none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Lookup getLookup(IMObject context, String code) {
        Lookup result = null;
        Lookup source = getSourceLookup(context);
        if (source != null) {
            Collection<Lookup> lookups
                    = getLookupService().getTargetLookups(source, relationship);
            for (Lookup lookup : lookups) {
                if (code.equals(lookup.getCode())) {
                    result = lookup;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the name of the lookup with the specified code.
     *
     * @param context the context. May be <tt>null</tt>
     * @return the name of the lookup matching <tt>code</tt>, or
     *         <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public String getName(IMObject context, String code) {
        Lookup lookup = getLookup(context, code);
        return (lookup != null) ? lookup.getName() : null;
    }

    /**
     * Returns the source lookup.
     *
     * @param context the context
     * @return the source lookup, or <tt>null</tt> if none can be found
     */
    private Lookup getSourceLookup(IMObject context) {
        return getLookup(context, value, relationship, "source");
    }

}
