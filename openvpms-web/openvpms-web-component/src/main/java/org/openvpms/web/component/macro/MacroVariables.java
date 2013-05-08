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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.macro;


import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.AbstractPropertyResolver;
import org.openvpms.component.business.service.archetype.helper.PropertyResolver;
import org.openvpms.component.business.service.archetype.helper.PropertyResolverException;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.macro.IMObjectVariables;
import org.openvpms.web.component.app.Context;

import java.util.HashMap;
import java.util.Map;

import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode
    .InvalidObject;

/**
 * Returns macro variables from an {@link Context}.
 *
 * @author Tim Anderson
 */
public class MacroVariables extends IMObjectVariables {

    /**
     * The practice variable name.
     */
    public static final String PRACTICE = "practice";

    /**
     * The location variable name.
     */
    public static final String LOCATION = "location";

    /**
     * The stock location variable name.
     */
    public static final String STOCK_LOCATION = "stockLocation";

    /**
     * The customer variable name.
     */
    public static final String CUSTOMER = "customer";

    /**
     * The patient variable name.
     */
    public static final String PATIENT = "patient";

    /**
     * The supplier variable name.
     */
    public static final String SUPPLIER = "supplier";

    /**
     * The product variable name.
     */
    public static final String PRODUCT = "product";

    /**
     * The user variable name.
     */
    public static final String USER = "user";

    /**
     * The clinician variable name.
     */
    public static final String CLINICIAN = "clinician";

    /**
     * The till variable name.
     */
    public static final String TILL = "till";

    /**
     * The deposit variable name.
     */
    public static final String DEPOSIT_ACCOUNT = "depositAccount";

    /**
     * The visit variable name.
     */
    public static final String VISIT = "visit";

    /**
     * The invoice variable name.
     */
    public static final String INVOICE = "invoice";

    /**
     * The context.
     */
    private final Context context;

    /**
     * Mapping of variable name to archetype short name.
     */
    public static final Map<String, String> MAPPINGS = new HashMap<String, String>();


    static {
        MAPPINGS.put(PRACTICE, Context.PRACTICE_SHORTNAME);
        MAPPINGS.put(LOCATION, Context.LOCATION_SHORTNAME);
        MAPPINGS.put(STOCK_LOCATION, Context.STOCK_LOCATION_SHORTNAME);
        MAPPINGS.put(CUSTOMER, Context.CUSTOMER_SHORTNAME);
        MAPPINGS.put(PATIENT, Context.PATIENT_SHORTNAME);
        MAPPINGS.put(SUPPLIER, Context.SUPPLIER_SHORTNAME);
        MAPPINGS.put(CLINICIAN, Context.CLINICIAN_SHORTNAME);
        MAPPINGS.put(PRODUCT, Context.PRODUCT_SHORTNAME);
        MAPPINGS.put(TILL, Context.TILL_SHORTNAME);
        MAPPINGS.put(DEPOSIT_ACCOUNT, Context.DEPOSIT_SHORTNAME);
        MAPPINGS.put(USER, UserArchetypes.USER);
        MAPPINGS.put(VISIT, PatientArchetypes.CLINICAL_EVENT);
        MAPPINGS.put(INVOICE, CustomerAccountArchetypes.INVOICE);
    }

    /**
     * Constructs a {@code MacroVariables}.
     *
     * @param context the context
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public MacroVariables(Context context, IArchetypeService service, ILookupService lookups) {
        super(service, lookups);
        this.context = context;
    }

    /**
     * Returns the named object from the context.
     *
     * @param name      the object name
     * @param shortName the object's archetype short name
     * @return the context object. May be {@code null}
     */
    private IMObject getContextObject(String name, String shortName) {
        IMObject result;
        // The user and clinician variables have the same shortName, so call the appropriate context methods rather
        // than using context.getObject()
        if (USER.equals(name)) {
            result = context.getUser();
        } else if (CLINICIAN.equals(name)) {
            result = context.getClinician();
        } else {
            result = context.getObject(shortName);
        }
        return result;
    }

    /**
     * Creates the property resolver.
     * <p/>
     * This implementation will look in the context for a variable before falling back to the supplied variables.
     *
     * @param variables the variables
     * @param service   the archetype service
     * @return a new property resolver
     */
    @Override
    protected PropertyResolver createResolver(final PropertySet variables, IArchetypeService service) {
        return new AbstractPropertyResolver(service) {
            @Override
            protected Object get(String name) {
                String mapping = MAPPINGS.get(name);
                if (mapping == null) {
                    return variables.get(name);
                }
                IMObject object = getContextObject(name, mapping);
                if (object == null) {
                    throw new PropertyResolverException(InvalidObject, name);
                }
                return object;
            }

            @Override
            protected boolean exists(String name) {
                String mapping = MAPPINGS.get(name);
                return (mapping == null) ? variables.exists(name) : getContextObject(name, mapping) != null;
            }

            protected Object resolve(IMObject object, String name) {
                return MacroVariables.this.resolve(object, name);
            }
        };
    }
}
