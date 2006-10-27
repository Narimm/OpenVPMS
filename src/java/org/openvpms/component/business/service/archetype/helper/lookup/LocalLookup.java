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
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.List;


/**
 * Local lookup assertion. The lookups are defined inline in the assertion.
 * E.g:
 * <pre>
 *   <node name="sex" path="/details/attributes/sex" type="java.lang.String">
 *     <assertion name="lookup.local">
 *       <propertyList name="entries">
 *         <property name="MALE" value="male"/>
 *         <property name="FEMALE" value="female"/>
 *       </propertyList>
 *     </assertion>
 * 	 </node>
 * </pre>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LocalLookup extends AbstractLookupAssertion {

    /**
     * The lookup type.
     */
    public static final String TYPE = "lookup.local";

    /**
     * The lookups.
     */
    private List<Lookup> lookups = new ArrayList<Lookup>();


    /**
     * Constructs a new <code>LocalLookup</code> assertion.
     *
     * @param assertion the assertion descriptor
     * @param service   the archetype service
     */
    public LocalLookup(AssertionDescriptor assertion,
                       IArchetypeService service) {
        super(assertion, TYPE, service);
        PropertyList list = (PropertyList) assertion.getPropertyMap()
                .getProperties().get("entries");
        for (NamedProperty prop : list.getProperties()) {
            AssertionProperty aprop = (AssertionProperty) prop;
            lookups.add(new Lookup(ArchetypeId.LocalLookupId,
                                   aprop.getName(), aprop.getValue()));
        }
    }

    /**
     * Returns the lookups for this assertion.
     *
     * @return a list of lookups
     */
    @Override
    public List<Lookup> getLookups() {
        return lookups;
    }

    /**
     * Returns the lookup with the specified code.
     *
     * @return the lookup matching <code>code</code>, or <code>null</code> if
     *         none is found
     */
    @Override
    public Lookup getLookup(String code) {
        for (Lookup lookup : lookups) {
            if (lookup.getCode().equals(code)) {
                return lookup;
            }
        }
        return null;
    }

}
