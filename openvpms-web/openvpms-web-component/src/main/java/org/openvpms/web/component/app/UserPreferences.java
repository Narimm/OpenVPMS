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

package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * User preferences.
 *
 * @author Tim Anderson
 */
public class UserPreferences {

    /**
     * Determines if product templates should be displayed during charging and estimates.
     */
    private boolean showTemplate;

    /**
     * Determines if product types should be displayed during charging and estimates.
     */
    private boolean showProductType;

    /**
     * Determines if batches should be displayed during charging.
     */
    private boolean showBatch;

    /**
     * Default constructor.
     */
    public UserPreferences() {
        super();
    }

    /**
     * Constructs a {@link UserPreferences} using defaults from a practice.
     *
     * @param practice the practice
     */
    public UserPreferences(Party practice) {
        initialise(practice);
    }

    /**
     * Initialise the preferences from a practice.
     *
     * @param practice the practice
     */
    public void initialise(Party practice) {
        IMObjectBean bean = new IMObjectBean(practice);
        showTemplate = bean.getBoolean("showTemplateDuringCharging");
        showProductType = bean.getBoolean("showProductTypeDuringCharging");
        showBatch = bean.getBoolean("showBatchDuringCharging");
    }

    /**
     * Determines if product templates should be displayed during charging and estimates.
     *
     * @return {@code true} if product templates should be displayed.
     */
    public boolean getShowTemplateDuringCharging() {
        return showTemplate;
    }

    /**
     * Determines if product templates should be displayed during charging and estimates.
     *
     * @param showTemplate if {@code true}, display the product template
     */
    public void setShowTemplateDuringCharging(boolean showTemplate) {
        this.showTemplate = showTemplate;
    }

    /**
     * Determines if product types should be displayed during charging and estimates.
     *
     * @return {@code true} if product types should be displayed.
     */
    public boolean getShowProductTypeDuringCharging() {
        return showProductType;
    }

    /**
     * Determines if product types should be displayed during charging and estimates.
     *
     * @param showProductType if {@code true}, display the product type
     */
    public void setShowProductTypeDuringCharging(boolean showProductType) {
        this.showProductType = showProductType;
    }

    /**
     * Determines if batches should be displayed during charging.
     *
     * @return {@code true} if batches should be displayed.
     */
    public boolean getShowBatchDuringCharging() {
        return showBatch;
    }

    /**
     * Determines if batches should be displayed during charging.
     *
     * @param showBatch if {@code true}, display the batch
     */
    public void setShowBatchDuringCharging(boolean showBatch) {
        this.showBatch = showBatch;
    }
}
