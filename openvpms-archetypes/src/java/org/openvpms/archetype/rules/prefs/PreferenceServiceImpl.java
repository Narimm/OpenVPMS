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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.prefs;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Default implementation of the {@link PreferenceService}.
 *
 * @author Tim Anderson
 */
public class PreferenceServiceImpl implements PreferenceService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * Constructs a {@link PreferenceServiceImpl}.
     *
     * @param service            the archetype service
     * @param transactionManager the transaction manager
     */
    public PreferenceServiceImpl(IArchetypeService service, PlatformTransactionManager transactionManager) {
        this.service = service;
        this.transactionManager = transactionManager;
    }

    /**
     * Returns preferences for a user.
     * <p/>
     * Any changes are made persistent.
     *
     * @param user the user
     * @param save if {@code true}, changes will be made persistent
     * @return the user preferences
     */
    @Override
    public Preferences getPreferences(User user, boolean save) {
        Preferences result;
        IMObjectReference reference = user.getObjectReference();
        if (save) {
            Entity entity = PreferencesImpl.getPreferences(reference, service, transactionManager);
            result = new PreferencesImpl(reference, entity, service, transactionManager);
        } else {
            result = PreferencesImpl.getPreferences(reference, service);
        }
        return result;
    }

    /**
     * Returns the root preference entity for a user, creating them if they don't exist.
     *
     * @param user the user
     * @return the root preference entity
     */
    @Override
    public Entity getEntity(User user) {
        return PreferencesImpl.getPreferences(user.getObjectReference(), service, transactionManager);
    }

    /**
     * Resets the preferences for a user.
     *
     * @param user the user
     */
    @Override
    public void reset(User user) {
        IMObjectReference reference = user.getObjectReference();
        PreferencesImpl.remove(reference, service, transactionManager);
    }
}
