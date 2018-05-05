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

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.web.component.im.delete.AbstractEntityDeletionHandler;
import org.openvpms.web.component.im.delete.IMObjectDeletionHandler;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * An {@link IMObjectDeletionHandler} for <em>entity.documentTemplate</em>.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateDeletionHandler extends AbstractEntityDeletionHandler<Entity> {

    /**
     * Constructs a {@link DocumentTemplateDeletionHandler}.
     *
     * @param object             the object to delete
     * @param factory            the editor factory
     * @param transactionManager the transaction manager
     * @param service            the archetype service
     */
    public DocumentTemplateDeletionHandler(Entity object, IMObjectEditorFactory factory,
                                           PlatformTransactionManager transactionManager,
                                           IArchetypeRuleService service) {
        super(object, null, factory, transactionManager, service);
    }

    /**
     * Determines if an object can be deleted.
     *
     * @return {@code true} if the object can be deleted
     */
    @Override
    public boolean canDelete() {
        return TypeHelper.isA(getObject(), DocumentArchetypes.DOCUMENT_TEMPLATE) && super.canDelete();
    }

    /**
     * Returns the participation archetypes to check.
     *
     * @return the participation archetypes to check
     */
    @Override
    protected String[] getParticipations() {
        return new String[]{DocumentArchetypes.DOCUMENT_TEMPLATE_PARTICIPATION};
    }
}
