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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.service;

import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.party.Party;
import org.openvpms.insurance.exception.InsuranceException;

/**
 * Locates {@link InsuranceService}.
 *
 * @author Tim Anderson
 */
public interface InsuranceServices {

    /**
     * Determines if claims can be submitted to an insurer via an {@link InsuranceService}.
     *
     * @param insurer the insurer
     * @return {@code true} if insurer accepts claims via an {@link InsuranceService}
     */
    boolean canSubmit(Party insurer);

    /**
     * Returns the insurance service for the specified insurer.
     *
     * @param insurer the insurer
     * @return the insurance service, or {@code null} if the insurer is not associated with an insurance service
     * @throws InsuranceException if the service is unavailable
     */
    InsuranceService getService(Party insurer);

    /**
     * Returns the insurance service for the specified <em>entity.insuranceService*</em> configuration.
     *
     * @param config the service configuration
     * @return the insurance service
     * @throws InsuranceException if the service is unavailable
     */
    InsuranceService getServiceForConfiguration(Entity config);

}
