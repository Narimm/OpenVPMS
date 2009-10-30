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
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Returns lookups for assertions that refer to the lookup by archetype short
 * name.
 * <p/>
 * The assertion must be named <em>"lookup"<em>, and contain a <em>"type"</em>
 * property with value <em>"lookup"</em> and a <em>"source"</em> property
 * specifying the lookup shortname.
 * E.g:
 * <pre>
 *   &lt;node name="country" path="/details/country" type="java.lang.String"&gt;
 *     &lt;assertion name="lookup"&gt;
 * 	     &lt;property name="type" value="lookup" /&gt;
 *       &lt;property name="source" value="lookup.country" /&gt;
 *     &lt;/assertion&gt;
 *   &lt;/node&gt;
 * </pre>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class RemoteLookup extends AbstractLookupAssertion {

    /**
     * The lookup type.
     */
    public static final String TYPE = "lookup"; // NON-NLS

    /**
     * The lookup shortname.
     */
    private String source;


    /**
     * Constructs a new <code>RemoteLookup</code>.
     *
     * @param assertion     the assertion descriptor
     * @param service       the archetype service
     * @param lookupService the lookup service
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public RemoteLookup(AssertionDescriptor assertion,
                        IArchetypeService service,
                        ILookupService lookupService) {
        super(assertion, TYPE, service, lookupService);
        source = getProperty("source");
        if (StringUtils.isEmpty(source)) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.SourceNotSpecified,
                    new Object[]{assertion.getName(), "lookup"});
        }
    }

    /**
     * Returns the lookup archetype short name.
     *
     * @return the lookup archetype short name
     */
    public String getShortName() {
        return source;
    }

    /**
     * Returns the lookups for this assertion.
     *
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the
     *                                   lookup type
     */
    public List<Lookup> getLookups() {
        Collection<Lookup> lookups = getLookupService().getLookups(source);
        return new ArrayList<Lookup>(lookups);
    }

    /**
     * Returns the lookup with the specified code.
     *
     * @return the lookup matching <code>code</code>, or <code>null</code> if
     *         none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Lookup getLookup(String code) {
        return getLookupService().getLookup(source, code);
    }

    /**
     * Returns the default lookup.
     *
     * @return the default lookup or <tt>null</tt> if there is no default
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Lookup getDefault() {
        return getLookupService().getDefaultLookup(source);
    }

}
