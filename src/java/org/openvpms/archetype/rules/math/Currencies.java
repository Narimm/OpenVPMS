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
import static org.openvpms.archetype.rules.math.CurrencyException.ErrorCode.InvalidCurrencyCode;
import static org.openvpms.archetype.rules.math.CurrencyException.ErrorCode.NoLookupForCode;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

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
     * The currency lookup archeype short name.
     */
    private static final String LOOKUP_CURRENCY = "lookup.currency";


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
        service.addListener(LOOKUP_CURRENCY, new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                add((Lookup) object);
            }

            @Override
            public void removed(IMObject object) {
                delete((Lookup) object);
            }
        });
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
            Lookup lookup = lookupService.getLookup(LOOKUP_CURRENCY, code);
            if (lookup == null) {
                throw new CurrencyException(NoLookupForCode, code);
            }
            currency = add(lookup);
        }
        return currency;
    }

    /**
     * Adds a currency.
     *
     * @param lookup the currency lookup
     * @return the added currency
     */
    private synchronized Currency add(Lookup lookup) {
        Currency currency = new Currency(lookup, service);
        currencies.put(lookup.getCode(), currency);
        return currency;
    }

    /**
     * Removes a currency.
     *
     * @param lookup the currency lookup
     */
    private synchronized void delete(Lookup lookup) {
        currencies.remove(lookup.getCode());
    }

}
