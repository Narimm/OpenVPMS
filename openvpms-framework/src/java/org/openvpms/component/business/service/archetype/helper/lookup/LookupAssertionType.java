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

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.List;


/**
 * Assertion type lookup.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LookupAssertionType extends AbstractLookupAssertion {

    /**
     * The lookup type.
     */
    public static final String TYPE = "lookup.assertionType";


    /**
     * Constructs a new <code>LookupAssertionType</code>.
     *
     * @param descriptor the assertion descriptor
     * @param service    the archetype service
     */
    public LookupAssertionType(AssertionDescriptor descriptor,
                               IArchetypeService service) {
        super(descriptor, TYPE, service);
    }

    /**
     * Returns the lookups for this assertion.
     *
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public List<Lookup> getLookups() {
        List<Lookup> lookups = new ArrayList<Lookup>();
        List<AssertionTypeDescriptor> descs =
                getArchetypeService().getAssertionTypeDescriptors();
        for (AssertionTypeDescriptor adesc : descs) {
            lookups.add(new Lookup(ArchetypeId.LocalLookupId,
                                   adesc.getName(), adesc.getName()));
        }
        return lookups;
    }

    /**
     * Returns the lookup with the specified code.
     *
     * @param code the lookup code
     * @return the lookup matching <code>code</code>, or <code>null</code> if
     *         none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Lookup getLookup(String code) {
        List<AssertionTypeDescriptor> descs =
                getArchetypeService().getAssertionTypeDescriptors();
        for (AssertionTypeDescriptor adesc : descs) {
            if (adesc.getName().equals(code)) {
                return new Lookup(ArchetypeId.LocalLookupId,
                                  adesc.getName(), adesc.getName());
            }
        }
        return null;
    }

}
