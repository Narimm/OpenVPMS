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

package org.openvpms.web.component.im.delete;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * An {@link IMObjectDeletionHandler} for {@link Act}s.
 *
 * @author Tim Anderson
 */
public class ActDeletionHandler<T extends Act> extends AbstractIMObjectDeletionHandler<T> {

    /**
     * Constructs a {@link ActDeletionHandler}.
     *
     * @param object             the act to delete
     * @param factory            the editor factory
     * @param transactionManager the transaction manager
     * @param service            the archetype service
     */
    public ActDeletionHandler(T object, IMObjectEditorFactory factory, PlatformTransactionManager transactionManager,
                              IArchetypeRuleService service) {
        super(object, factory, transactionManager, service);
    }

    /**
     * Determines if an object can be deleted.
     *
     * @return {@code true} if the object can be deleted
     */
    @Override
    public boolean canDelete() {
        return !ActStatus.POSTED.equals(getObject().getStatus());
    }

    /**
     * Determines if the object can be deactivated.
     *
     * @return {@code false}. Acts cannot be deactivated
     */
    @Override
    public boolean canDeactivate() {
        return false;
    }
}
