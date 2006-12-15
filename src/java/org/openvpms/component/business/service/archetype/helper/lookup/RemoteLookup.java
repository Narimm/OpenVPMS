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
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;

import java.util.Arrays;
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
 *   <node name="country" path="/details/attributes/country"
 *         type="java.lang.String">
 *     <assertion name="lookup">
 * 	     <property name="type" value="lookup" />
 *       <property name="source" value="lookup.country" />
 *     </assertion>
 *   </node>
 * </pre>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class RemoteLookup extends AbstractLookupAssertion {

    /**
     * The lookup type.
     */
    public static final String TYPE = "lookup";

    /**
     * The lookup shortname.
     */
    private String source;


    /**
     * Constructs a new <code>BasicLookup</code>.
     *
     * @param assertion the assertion descriptor
     * @param service   the archetype service
     */
    public RemoteLookup(AssertionDescriptor assertion,
                        IArchetypeService service) {
        super(assertion, TYPE, service);
        source = getProperty("source");
        if (StringUtils.isEmpty(source)) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.SourceNotSpecified,
                    new Object[]{assertion.getName(), "lookup"});
        }
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
        return LookupHelper.getLookups(getArchetypeService(), source, 0, -1);
    }

    /**
     * Returns partially populated lookups for this assertion.
     *
     * @param nodes the nodes to populate
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Lookup> getLookups(Collection<String> nodes) {
        ArchetypeQuery query = new ArchetypeQuery(source, false, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        return getLookups(nodes, query);
    }

    /**
     * Returns partially populated lookups for this assertion.
     *
     * @param context the context.
     * @param nodes   the nodes to populate
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public List<Lookup> getLookups(IMObject context, Collection<String> nodes) {
        return getLookups(nodes);
    }

    /**
     * Returns the lookup with the specified code.
     * This implementation throws {@link LookupHelperException}.
     *
     * @return the lookup matching <code>code</code>, or <code>null</code> if
     *         none is found
     * @throws LookupHelperException if invoekd
     */
    @Override
    public Lookup getLookup(String code) {
        return LookupHelper.getLookup(getArchetypeService(), source, code);
    }

    /**
     * Returns the name of the lookup with the specified code.
     *
     * @param code the lookup code
     * @return the name of the lookup matching <code>code</code>, or
     *         <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the
     *                                   lookup type
     */
    @Override
    public String getName(String code) {
        ArchetypeQuery query = new ArchetypeQuery(source, false, true)
                .add(new NodeConstraint("code", code))
                .setFirstResult(0)
                .setMaxResults(1);
        List<Lookup> lookups = getLookups(Arrays.asList("name"), query);
        if (!lookups.isEmpty()) {
            Lookup lookup = lookups.get(0);
            return lookup.getName();
        }
        return null;
    }
}
