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

package org.openvpms.archetype.rules.math;

import org.apache.commons.lang.StringUtils;
import static org.openvpms.archetype.rules.math.CurrencyException.ErrorCode.*;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


/**
 * Maintains a cache of {@link Currency} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Currencies {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookupService;


    /**
     * The currency cache.
     */
    private Map<String, Currency> currencies = new HashMap<String, Currency>();


    /**
     * Constructs a new <tt>Currencies</tt>, using the default archetype service
     * and lookup service.
     */
    public Currencies() {
        this(ArchetypeServiceHelper.getArchetypeService(),
             LookupServiceHelper.getLookupService());
    }

    /**
     * Constructs a new <tt>Currencies</tt>.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     */
    public Currencies(IArchetypeService service,
                      ILookupService lookupService) {
        this.service = service;
        this.lookupService = lookupService;
    }

    /**
     * Returns a currency given its ISO 4217 code.
     *
     * @param code the ISO currency code
     * @return the currency corresponding to <tt>code</tt>
     * @throws CurrencyException if the currency code is invalid or no
     *                           <em>lookup.currency</em> is defined for the
     *                           currency
     */
    public synchronized Currency getCurrency(String code) {
        if (StringUtils.isEmpty(code)) {
            throw new CurrencyException(InvalidCurrencyCode, code);
        }
        Currency currency = currencies.get(code);
        if (currency == null) {
            java.util.Currency c = java.util.Currency.getInstance(code);
            if (c == null) {
                throw new CurrencyException(InvalidCurrencyCode, code);
            }
            Lookup lookup = lookupService.getLookup("lookup.currency", code);
            if (lookup == null) {
                throw new CurrencyException(NoLookupForCode, code);
            }
            IMObjectBean bean = new IMObjectBean(lookup, service);
            String roundingMode = bean.getString("roundingMode");
            RoundingMode mode = RoundingMode.valueOf(roundingMode);
            if (mode == null) {
                throw new CurrencyException(InvalidRoundingMode, roundingMode,
                                            code);
            }
            BigDecimal minDenomination = bean.getBigDecimal("minDenomination");
            currency = new Currency(c, mode, minDenomination);
            currencies.put(code, currency);
        }
        return currency;
    }

    /**
     * Refreshes the cache.
     */
    public synchronized void refresh() {
        currencies.clear();
    }

}
