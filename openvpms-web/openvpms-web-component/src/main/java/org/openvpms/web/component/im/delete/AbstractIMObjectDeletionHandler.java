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

package org.openvpms.web.component.im.delete;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Abstract implementation of the {@link IMObjectDeletionHandler} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectDeletionHandler<T extends IMObject> implements IMObjectDeletionHandler<T> {

    /**
     * The object to delete.
     */
    private final T object;

    /**
     * The editor factory.
     */
    private final IMObjectEditorFactory factory;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * Constructs an {@link AbstractIMObjectDeletionHandler}.
     *
     * @param object             the object to delete
     * @param factory            the editor factory
     * @param transactionManager the transaction manager
     * @param service            the archetype service
     */
    public AbstractIMObjectDeletionHandler(T object, IMObjectEditorFactory factory,
                                           PlatformTransactionManager transactionManager,
                                           IArchetypeRuleService service) {
        this.object = object;
        this.factory = factory;
        this.transactionManager = transactionManager;
        this.service = service;
    }

    /**
     * Returns the object to delete.
     *
     * @return the object to delete
     */
    @Override
    public T getObject() {
        return object;
    }

    /**
     * Deletes the {@link IMObject}.
     *
     * @param context     the context
     * @param helpContext the help context
     * @throws IllegalStateException if the object cannot be deleted
     */
    @Override
    public void delete(final Context context, final HelpContext helpContext) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (!canDelete()) {
                    throw new IllegalStateException(DescriptorHelper.getDisplayName(object, service)
                                                    + " cannot be deleted");
                }
                remove(object, context, helpContext);
            }
        });
    }

    /**
     * Determines if the object can be deactivated.
     *
     * @return {@code true} if the object can be deactivated
     */
    @Override
    public boolean canDeactivate() {
        return object.isActive();
    }

    /**
     * Deactivates the object.
     *
     * @throws IllegalStateException if the object cannot be deleted
     */
    @Override
    public void deactivate() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (!canDeactivate()) {
                    throw new IllegalStateException(DescriptorHelper.getDisplayName(object, service)
                                                    + " cannot be deactivated");
                }
                object.setActive(false);
                service.save(object);
            }
        });
    }

    /**
     * Uses an editor to perform deletion.
     *
     * @param object  the object to delete
     * @param context the context
     * @param help    the help context
     */
    protected void remove(T object, Context context, HelpContext help) {
        DefaultLayoutContext layout = new DefaultLayoutContext(true, context, help);
        final IMObjectEditor editor = factory.create(object, layout);
        editor.getComponent();
        editor.delete();
    }

    /**
     * Determines if a query has matches.
     *
     * @param query the query
     * @return {@code true} if the query has matches
     */
    protected boolean hasMatches(ArchetypeQuery query) {
        query.setMaxResults(1);
        return !service.get(query).getResults().isEmpty();
    }

}
