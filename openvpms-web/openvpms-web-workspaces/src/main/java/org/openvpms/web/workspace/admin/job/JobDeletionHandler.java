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

package org.openvpms.web.workspace.admin.job;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.web.component.im.delete.AbstractEntityDeletionHandler;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A deletion handler for <em>entity.job*</em> objects.
 *
 * @author Tim Anderson
 */
public class JobDeletionHandler extends AbstractEntityDeletionHandler<Entity> {

    /**
     * Relationships to exclude when determining if a job can be deleted.
     */
    private static final String[] EXCLUDE = {"entityRelationship.jobUser"};

    /**
     * Constructs a {@link JobDeletionHandler}.
     *
     * @param object             the object to delete
     * @param factory            the editor factory
     * @param transactionManager the transaction manager
     * @param service            the archetype service
     */
    public JobDeletionHandler(Entity object, IMObjectEditorFactory factory,
                              PlatformTransactionManager transactionManager, IArchetypeRuleService service) {
        super(object, EXCLUDE, factory, transactionManager, service);
    }

    /**
     * Determines if an object can be deleted.
     *
     * @return {@code true} if the object can be deleted
     */
    @Override
    public boolean canDelete() {
        return TypeHelper.isA(getObject(), "entity.job*") && super.canDelete();
    }
}
