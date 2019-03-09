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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.user;

import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.web.component.im.delete.AbstractEntityDeletionHandler;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A deletion handler for <em>security.user</em> objects.
 *
 * @author Tim Anderson
 */
public class UserDeletionHandler extends AbstractEntityDeletionHandler<User> {

    /**
     * Constructs a {@link UserDeletionHandler}.
     *
     * @param object             the object to delete
     * @param factory            the editor factory
     * @param transactionManager the transaction manager
     * @param service            the archetype service
     */
    public UserDeletionHandler(User object, IMObjectEditorFactory factory,
                               PlatformTransactionManager transactionManager, IArchetypeRuleService service) {
        super(object, factory, transactionManager, service);
    }

    /**
     * Determines if an object can be deleted.
     *
     * @return {@code true} if the object can be deleted
     */
    @Override
    public boolean canDelete() {
        return super.canDelete() && notUsedByJob();
    }

    /**
     * Determines if the object can be deactivated.
     *
     * @return {@code true} if the object can be deactivated
     */
    @Override
    public boolean canDeactivate() {
        return super.canDeactivate() && notUsedByJob();
    }

    /**
     * Checks that the user is not referred to by an active job.
     * <p/>
     * If it is, it cannot be deleted or deactivated, as that would cause the job to fail.
     *
     * @return {@code true} if the user is not referred to by a job
     */
    private boolean notUsedByJob() {
        return ServiceHelper.getBean(UserRules.class).getJobUsedBy(getObject()) == null;
    }
}
