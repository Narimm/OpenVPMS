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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.contact;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

/**
 * An {@link AddressFormatter} that uses the practice configuration to determine the address format.
 *
 * @author Tim Anderson
 */
public class PracticeAddressFormatter extends AbstractAddressFormatter {

    /**
     * The practice service.
     */
    private PracticeService practiceService;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PracticeAddressFormatter.class);

    /**
     * Constructs a {@link PracticeAddressFormatter}.
     *
     * @param practiceService the practice service
     * @param service         the archetype service
     * @param lookups         the lookup service
     */
    public PracticeAddressFormatter(PracticeService practiceService, IArchetypeService service,
                                    ILookupService lookups) {
        super(service, lookups);
        this.practiceService = practiceService;
    }

    /**
     * Formats an address.
     *
     * @param location   the location
     * @param singleLine if {@code true}, return the address as a single line
     * @return the formatted address. May be {@code null}
     */
    @Override
    public String format(Contact location, boolean singleLine) {
        String result = null;
        try {
            result = super.format(location, singleLine);
        } catch (Exception exception) {
            log.error(exception, exception);
        }
        return result;
    }

    /**
     * Formats an address.
     *
     * @param location   the location contact
     * @param address    the address. May be {@code null}
     * @param suburb     the suburb. May be {@code null}
     * @param stateCode  the state code. May be {@code null}
     * @param stateName  the state name. May be {@code null}
     * @param postcode   the postcode. May be {@code null}
     * @param singleLine if {@code true} formats the address on a single line
     * @return the formatted address
     */
    @Override
    protected String format(Contact location, String address, String suburb, String stateCode, String stateName,
                            String postcode, boolean singleLine) {
        String result = null;
        Party practice = practiceService.getPractice();
        if (practice != null) {
            Lookup lookup = getLookups().getLookup(practice, "addressFormat");
            if (lookup != null) {
                String format = getFormat(lookup, singleLine);
                if (format != null) {
                    JXPathContext context = JXPathHelper.newContext(location);
                    Variables variables = context.getVariables();
                    variables.declareVariable("address", StringUtils.trimToEmpty(address));
                    variables.declareVariable("suburb", StringUtils.trimToEmpty(suburb));
                    variables.declareVariable("state.code", StringUtils.trimToEmpty(stateCode));
                    variables.declareVariable("state", StringUtils.trimToEmpty(stateName));
                    variables.declareVariable("postcode", StringUtils.trimToEmpty(postcode));
                    variables.declareVariable("nl", "\n");
                    Object value = context.getValue(format);
                    result = (value != null) ? value.toString() : null;
                    if (result != null && singleLine) {
                        result = result.replace('\n', ' ');
                    }
                }
            }
        }
        if (result == null) {
            result = formatDefault(address, suburb, stateName, postcode, singleLine);
        }
        return result;
    }

    /**
     * Returns the format to use.
     *
     * @param format     the address format
     * @param singleLine if {@code true}, return the single line format, otherwise return the multi-line format
     * @return the format to use. May be {@code null}
     */
    private String getFormat(Lookup format, boolean singleLine) {
        IMObjectBean bean = new IMObjectBean(format, getService());
        return (singleLine) ? bean.getString("singleLineFormat") : bean.getString("multiLineFormat");
    }

}
