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

package org.openvpms.web.component.prefs;

import echopointng.TabbedPane;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractEditableIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityLinkCollectionTargetPropertyEditor;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;
import org.openvpms.web.system.ServiceHelper;

/**
 * Editor for a group of preferences.
 *
 * @author Tim Anderson
 */
class PreferenceGroupCollectionEditor extends AbstractEditableIMObjectCollectionEditor {

    /**
     * The focus group.
     */
    private final FocusGroup focus = new FocusGroup("PreferenceGroupCollection");

    /**
     * The tab pane model.
     */
    private ObjectTabPaneModel<IMObjectEditor> model;

    /**
     * The tab pane.
     */
    private TabbedPane pane;

    /**
     * Constructs a {@link PreferenceGroupCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public PreferenceGroupCollectionEditor(CollectionProperty property, Entity object, LayoutContext context) {
        super(new PreferenceGroupCollectionPropertyEditor(property, object), object, context);

        EntityLinkCollectionTargetPropertyEditor editor = getCollectionPropertyEditor();
        for (String linkShortName : property.getArchetypeRange()) {
            if (IMObjectHelper.getObject(linkShortName, editor.getRelationships()) == null) {
                String[] shortNames = RelationshipHelper.getTargetShortNames(linkShortName);
                for (String shortName : shortNames) {
                    if (IMObjectHelper.getObject(shortName, editor.getObjects()) == null) {
                        IMObject group = IMObjectCreator.create(shortName);
                        if (group != null) {
                            editor.setRelationshipShortName(linkShortName);
                            // TODO. Ideally would infer the relationship type from the target
                            editor.add(group);
                        }

                    }
                }
            }
        }
    }

    /**
     * Creates a new object.
     * <p/>
     * The object is not automatically added to the collection.
     *
     * @return {@code null} - this method is not supported
     */
    @Override
    public IMObject create() {
        return null;
    }

    /**
     * Refreshes the collection display.
     */
    @Override
    public void refresh() {
        // no-op
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been rendered
     */
    @Override
    public FocusGroup getFocusGroup() {
        return focus;
    }

    /**
     * Returns the selected editor.
     *
     * @return the selected editor, or {@code null} if no editor is selected
     */
    public IMObjectEditor getSelected() {
        return pane != null ? model.getObject(pane.getSelectedIndex()) : null;
    }

    /**
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    @Override
    protected EntityLinkCollectionTargetPropertyEditor getCollectionPropertyEditor() {
        return (EntityLinkCollectionTargetPropertyEditor) super.getCollectionPropertyEditor();
    }

    /**
     * Lays out the component.
     *
     * @param context the layout context
     * @return the component
     */
    @Override
    protected Component doLayout(LayoutContext context) {
        Component container = ColumnFactory.create(Styles.INSET_Y);
        model = new ObjectTabPaneModel<>(container);
        EntityLinkCollectionTargetPropertyEditor propertyEditor = getCollectionPropertyEditor();
        for (IMObject object : propertyEditor.getObjects()) {
            IMObjectEditor editor = propertyEditor.getEditor(object);
            if (editor == null) {
                HelpContext help = context.getHelpContext().topic(object.getArchetypeId().getShortName() + "/edit");
                LayoutContext subContext = new DefaultLayoutContext(context, help);
                editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(object, getObject(), subContext);
                propertyEditor.setEditor(object, editor);
            }
            model.addTab(editor, editor.getDisplayName(), editor.getComponent());
            focus.add(editor.getFocusGroup());
        }
        pane = TabbedPaneFactory.create(model);
        container.add(pane);
        return container;
    }

    private static class PreferenceGroupCollectionPropertyEditor extends EntityLinkCollectionTargetPropertyEditor {

        /**
         * Constructs an {@link PreferenceGroupCollectionPropertyEditor}.
         *
         * @param property the property to edit
         * @param parent   the parent object
         */
        public PreferenceGroupCollectionPropertyEditor(CollectionProperty property, Entity parent) {
            super(property, parent);
        }

        /**
         * Sequences the relationships.
         */
        @Override
        protected void sequence() {
            // no-op. The relationships must define their sequence.
        }

        /**
         * Sequences a relationship.
         *
         * @param relationship the relationship
         */
        @Override
        protected void sequence(EntityLink relationship) {
            // no-op. The relationships must define their sequence.
        }
    }
}
