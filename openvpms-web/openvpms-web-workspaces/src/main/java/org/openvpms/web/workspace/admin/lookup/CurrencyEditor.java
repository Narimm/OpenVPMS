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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.lookup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An editor for currency lookups.
 * <p/>
 * This defaults the <em>minPrice</em> node to the value returned by {@link Currency#getDefaultRoundingAmount()},
 * if the value is not set.
 *
 * @author Tim Anderson
 */
public class CurrencyEditor extends AbstractLookupEditor {

    /**
     * The currency code node.
     */
    private static final String CODE = "code";

    /**
     * The minimum denomination node.
     */
    private static final String MIN_DENOMINATION = "minDenomination";

    /**
     * The minimum price node.
     */
    private static final String MIN_PRICE = "minPrice";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(CurrencyEditor.class);

    /**
     * Constructs an {@link CurrencyEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public CurrencyEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);

        Property code = getProperty(CODE);
        code.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                initValues(true);
            }
        });
        initValues(object.isNew());
    }

    /**
     * Initialises the minimum denomination and price.
     *
     * @param overwrite if {@code true}, overwrite existing values
     */
    protected void initValues(boolean overwrite) {
        Property code = getProperty(CODE);
        Property minDenomination = getProperty(MIN_DENOMINATION);
        Property minPrice = getProperty(MIN_PRICE);
        if (code.getValue() != null) {
            try {
                Currency currency = new Currency(java.util.Currency.getInstance(code.getString()),
                                                 RoundingMode.HALF_UP);
                if (overwrite || MathRules.isZero(minDenomination.getBigDecimal(BigDecimal.ZERO))) {
                    minDenomination.setValue(currency.getDefaultRoundingAmount());
                }
                if (overwrite || MathRules.isZero(minPrice.getBigDecimal(BigDecimal.ZERO))) {
                    minPrice.setValue(currency.getDefaultRoundingAmount());
                }
            } catch (IllegalArgumentException exception) {
                log.warn("Failed to locate currency=" + code.getString(), exception);
            }
        }
    }

}
