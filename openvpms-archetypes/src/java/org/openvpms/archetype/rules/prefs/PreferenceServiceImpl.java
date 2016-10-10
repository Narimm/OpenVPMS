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
import org.openvpms.component.business.domain.im.party.Party;
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
     * Returns preferences for a user or practice.
     *
     * @param party  the party
     * @param source if non-null, specifies the source to copy preferences from if the party has none
     * @param save   if {@code true}, changes will be made persistent
     * @return the preferences
     */
    @Override
    public Preferences getPreferences(Party party, Party source, boolean save) {
        Preferences result;
        IMObjectReference reference = party.getObjectReference();
        IMObjectReference sourceReference = (source != null) ? source.getObjectReference() : null;
        if (save) {
            Entity entity = PreferencesImpl.getPreferences(reference, sourceReference, service, transactionManager);
            result = new PreferencesImpl(reference, sourceReference, entity, service, transactionManager);
        } else {
            result = PreferencesImpl.getPreferences(reference, sourceReference, service);
        }
        return result;
    }

    /**
     * Returns the root preference entity for a user or practice, creating it if it doesn't exist.
     *
     * @param party  the party
     * @param source if non-null, specifies the party's preferences to copy to the user, if the user has none
     * @return the root preference entity
     */
    @Override
    public Entity getEntity(Party party, Party source) {
        IMObjectReference reference = party.getObjectReference();
        IMObjectReference sourceReference = source != null ? source.getObjectReference() : null;
        return PreferencesImpl.getPreferences(reference, sourceReference, service, transactionManager);
    }

    /**
     * Resets the preferences for a user or practice.
     *
     * @param party  the party
     * @param source if non-null, specifies the party's preferences to copy to the user
     */
    @Override
    public void reset(Party party, Party source) {
        IMObjectReference reference = party.getObjectReference();
        IMObjectReference sourceReference = source != null ? source.getObjectReference() : null;
        PreferencesImpl.reset(reference, sourceReference, service, transactionManager);
    }
}
