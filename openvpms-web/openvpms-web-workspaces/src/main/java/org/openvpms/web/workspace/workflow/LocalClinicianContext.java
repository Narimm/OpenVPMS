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
package org.openvpms.web.workspace.workflow;

import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DelegatingContext;


/**
 * A {@link DelegatingContext} that maintains a clinician locally, not using that provided by the delegate.
 *
 * @author Tim Anderson
 */
public class LocalClinicianContext extends DelegatingContext {

    /**
     * The clinician. May be {@code null}
     */
    private User clinician;

    /**
     * Constructs a {@link LocalClinicianContext}.
     *
     * @param context the context to delegate to
     */
    public LocalClinicianContext(Context context) {
        super(context);
    }

    /**
     * Sets the current clinician.
     *
     * @param clinician the current clinician. May be {@code null}
     */
    @Override
    public void setClinician(User clinician) {
        this.clinician = clinician;
    }

    /**
     * Returns the current clinician.
     *
     * @return the current clinician, or {@code null} if there is no current clinician
     */
    @Override
    public User getClinician() {
        return clinician;
    }
}
