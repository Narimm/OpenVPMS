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
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.business.service.lookup.ILookupService;


/**
 * Factory for {@link LookupAssertion}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupAssertionFactory {

    /**
     * Creates a new {@link LookupAssertion} given a node descriptor.
     * Examines the descriptor for assertions named <em>"lookup"</em>,
     * <em>"lookup.local"</em> or <em>"lookup.assertionType"</em>
     *
     * @param descriptor the node descriptor
     * @param service the archetype service
     * @param lookupService the lookup service
     * @return a new lookup assertion
     * @throws LookupHelperException if the assertion is incorrectly specified
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public static LookupAssertion create(NodeDescriptor descriptor,
                                         IArchetypeService service,
                                         ILookupService lookupService) {
        LookupAssertion result;
        if (descriptor.containsAssertionType("lookup")) {
            AssertionDescriptor assertion
                    = descriptor.getAssertionDescriptor("lookup");
            String type = LookupAssertionHelper.getValue(assertion, "type");
            if (StringUtils.isEmpty(type)) {
                throw new LookupHelperException(
                        LookupHelperException.ErrorCode.TypeNotSpecified,
                        new Object[]{assertion.getName()});
            }
            if (RemoteLookup.TYPE.equals(type)) {
                result = new RemoteLookup(assertion, service, lookupService);
            } else if (TargetLookup.TYPE.equals(type)) {
                result = new TargetLookup(assertion, service, lookupService);
            } else if (SourceLookup.TYPE.equals(type)) {
                result = new SourceLookup(assertion, service, lookupService);
            } else {
                throw new LookupHelperException(
                        LookupHelperException.ErrorCode.InvalidLookupType,
                        new Object[]{type});
            }
        } else if (descriptor.containsAssertionType(LocalLookup.TYPE)) {
            AssertionDescriptor assertion
                    = descriptor.getAssertionDescriptor(LocalLookup.TYPE);
            result = new LocalLookup(assertion, service, lookupService);
        } else if (descriptor.containsAssertionType(LookupAssertionType.TYPE)) {
            AssertionDescriptor assertion
                    = descriptor.getAssertionDescriptor(
                    LookupAssertionType.TYPE);
            result = new LookupAssertionType(assertion, service, lookupService);
        } else {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.InvalidLookupAssertion,
                    new Object[]{descriptor.getName()});
        }
        return result;
    }
}
