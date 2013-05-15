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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.admin.archetype;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractIMObjectFactory;
import org.openvpms.component.business.service.archetype.IMObjectFactory;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.tools.archetype.loader.Change;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditResultSetDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.AbstractLayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;

import java.util.Arrays;


/**
 * An {@link EditDialog} for archetype descriptors, that provides the facility to test the archetype descriptor.
 *
 * @author Tim Anderson
 */
public class ArchetypeEditDialog extends EditResultSetDialog<ArchetypeDescriptor> {

    /**
     * The object factory. This uses the archetype descriptor being edited when creating test objects.
     */
    private final IMObjectFactory factory;


    /**
     * Constructs an {@code ArchetypeEditDialog}.
     *
     * @param title   the window title
     * @param first   the first object to edit
     * @param set     the set of results to edit
     * @param context the context
     * @param help    the help context
     */
    public ArchetypeEditDialog(String title, ArchetypeDescriptor first, ResultSet<ArchetypeDescriptor> set,
                               Context context, HelpContext help) {
        super(title, first, set, context, help);
        factory = new ObjectFactory();
        addButton("test", new ActionListener() {
            public void onAction(ActionEvent e) {
                onTest();
            }
        });
    }

    /**
     * Saves the current object.
     *
     * @return {@code true} if the object was saved
     */
    @Override
    protected boolean doSave() {
        IMObjectEditor editor = getEditor();
        boolean saved = false;
        if (editor != null) {
            ArchetypeDescriptor current = (ArchetypeDescriptor) editor.getObject();
            ArchetypeDescriptor old = IMObjectHelper.reload(current);
            saved = super.doSave();
            if (saved && old != null) {
                Change change = new Change(current, old);
                boolean updateDerived = change.hasChangedDerivedNodes();
                boolean updateAssertions = change.hasAddedAssertions(BatchArchetypeUpdater.ASSERTIONS);
                if (updateDerived || updateAssertions) {
                    ConfirmingBatchArchetypeUpdater updater = new ConfirmingBatchArchetypeUpdater();
                    updater.confirmUpdate(Arrays.asList(change));
                }
            }
        }
        return saved;
    }

    /**
     * Invoked when the "test" button is pressed.
     */
    private void onTest() {
        try {
            Validator validator = new Validator();
            if (getEditor().validate(validator)) {
                ArchetypeDescriptor descriptor = (ArchetypeDescriptor) getEditor().getObject();
                String shortName = descriptor.getShortName();
                IMObject object = factory.create(shortName);
                TestLayoutContext context = new TestLayoutContext(new LocalContext(), getHelpContext());
                IMObjectEditor editor = IMObjectEditorFactory.create(object, context);
                EditDialog dialog = new TestEditDialog(editor, context.getContext());
                dialog.show();
            } else {
                ValidationHelper.showError(validator);
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Returns the archetype descriptor being edited.
     *
     * @return the archetype descriptor
     */
    private ArchetypeDescriptor getArchetype() {
        return (ArchetypeDescriptor) getEditor().getObject();
    }

    /**
     * An {@link IMObjectFactory} that uses the archetype descriptor being edited, where applicable.
     */
    private class ObjectFactory extends AbstractIMObjectFactory {

        /**
         * Returns an archetype descriptor for the specified archetype short name.
         * <p/>
         * If the short name matches that of the archetype being edited, this will be returned, otherwise
         * that held by the archetype service will be used.
         *
         * @param shortName the archetype short name
         * @return the archetype descriptor or null if there is no corresponding archetype descriptor for
         *         {@code shortName}
         */
        protected ArchetypeDescriptor getArchetypeDescriptor(String shortName) {
            ArchetypeDescriptor descriptor = getArchetype();
            if (descriptor.getShortName().equals(shortName)) {
                return descriptor;
            }
            return DescriptorHelper.getArchetypeDescriptor(shortName);
        }
    }

    /**
     * Layout context that uses the archetype descriptor being edited, where applicable.
     */
    private class TestLayoutContext extends AbstractLayoutContext {

        /**
         * Constructs a {@code TestLayoutContext}.
         *
         * @param context the context
         * @param help    the help context
         */
        public TestLayoutContext(Context context, HelpContext help) {
            super(context, help);
        }

        /**
         * Returns an archetype descriptor for an object.
         *
         * @param object the object
         * @return an archetype descriptor for the object, or {@code null} if none can be found
         */
        @Override
        public ArchetypeDescriptor getArchetypeDescriptor(IMObject object) {
            ArchetypeDescriptor archetype = getArchetype();
            if (object.getArchetypeId().getShortName().equals(archetype.getShortName())) {
                return archetype;
            }
            return super.getArchetypeDescriptor(object);
        }
    }

    /**
     * Edit dialog that tests objects created by the archetype descriptor being edited.
     */
    private class TestEditDialog extends EditDialog {

        /**
         * Creates a new {@code TestEditDialog}.
         *
         * @param editor  the editor
         * @param context the context
         */
        public TestEditDialog(IMObjectEditor editor, Context context) {
            super(editor, false, false, false, false, context);
            addButton("validate", new ActionListener() {
                public void onAction(ActionEvent e) {
                    onCheck();
                }
            });
        }

        /**
         * Validates the object.
         */
        private void onCheck() {
            Validator validator = new Validator();
            if (!getEditor().validate(validator)) {
                ValidationHelper.showError(validator);
            }
        }
    }

}
