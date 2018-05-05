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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * The default deletion handler.
 *
 * @author Tim Anderson
 */
public class DefaultDeletionHandler extends AbstractIMObjectDeletionHandler<IMObject> {

    /**
     * Constructs a {@link DefaultDeletionHandler}.
     *
     * @param object             the object to delete
     * @param factory            the editor factory
     * @param transactionManager the transaction manager
     * @param service            the archetype service
     */
    public DefaultDeletionHandler(IMObject object, IMObjectEditorFactory factory,
                                  PlatformTransactionManager transactionManager, IArchetypeRuleService service) {
        super(object, factory, transactionManager, service);
    }

    /**
     * Determines if the object can be deleted.
     *
     * @return {@code true} if the object can be deleted
     */
    @Override
    public boolean canDelete() {
        return true;
    }
}
