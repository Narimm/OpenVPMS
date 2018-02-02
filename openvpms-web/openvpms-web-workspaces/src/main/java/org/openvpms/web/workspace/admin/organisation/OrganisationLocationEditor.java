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

package org.openvpms.web.workspace.admin.organisation;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.edit.PropertyComponentEditor;
import org.openvpms.web.component.im.doc.LogoEditor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityLinkCollectionTargetPropertyEditor;
import org.openvpms.web.component.im.relationship.MultipleRelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.print.PrintHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Editor for <em>party.organisationLocation</em>
 * <p>
 * This:
 * <ul>
 * <li>displays a list of a available printers for the "defaultPrinter" node
 * <li>displays an editor for the practice location logo</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class OrganisationLocationEditor extends AbstractIMObjectEditor {

    /**
     * The printer collection.
     */
    private final PrinterCollectionEditor printers;

    /**
     * All available printer names.
     */
    private final List<String> printerNames;

    /**
     * The logo editor.
     */
    private final LogoEditor logoEditor;

    /**
     * The nodes to display.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().simple("pricingGroup");

    /**
     * Constructs an {@link OrganisationLocationEditor}
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public OrganisationLocationEditor(Party object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        DocumentAct logo = getLogo(object);
        logoEditor = new LogoEditor(logo, object, layoutContext);
        printerNames = Arrays.asList(PrintHelper.getPrinters());
        printers = new PrinterCollectionEditor(getCollectionProperty("printers"), object, layoutContext);
        getEditors().add(printers);
        getEditors().add(logoEditor);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LocationLayoutStrategy();
    }

    /**
     * Returns the logo.
     *
     * @param object the location
     * @return the logo
     */
    private DocumentAct getLogo(Party object) {
        DocumentAct result;
        ArchetypeQuery query = new ArchetypeQuery(DocumentArchetypes.LOGO_ACT);
        query.add(Constraints.join("owner").add(Constraints.eq("entity", object)));
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<DocumentAct> iterator = new IMObjectQueryIterator<>(query);
        if (iterator.hasNext()) {
            result = iterator.next();
        } else {
            result = (DocumentAct) IMObjectCreator.create(DocumentArchetypes.LOGO_ACT);
        }
        return result;
    }

    private static class PrinterEditor extends AbstractIMObjectEditor {

        /**
         * The printer selector.
         */
        private final PropertyComponentEditor printer;

        /**
         * Constructs a {@link PrinterEditor}.
         *
         * @param object        the object to edit
         * @param parent        the parent object. May be {@code null}
         * @param printerNames  the available printer names
         * @param layoutContext the layout context
         */
        public PrinterEditor(IMObject object, IMObject parent, Set<String> printerNames, LayoutContext layoutContext) {
            super(object, parent, layoutContext);
            Property property = getProperty("name");
            SelectField field = BoundSelectFieldFactory.create(property, createModel(printerNames));
            printer = new PropertyComponentEditor(property, field);
        }

        /**
         * Sets the available printer names.
         *
         * @param printerNames the available printer names
         */
        public void setAvailablePrinterNames(Set<String> printerNames) {
            ((SelectField) printer.getComponent()).setModel(createModel(printerNames));
        }

        /**
         * Creates a model of available printer names.
         *
         * @param names the available printer names
         * @return a new model
         */
        protected ListModel createModel(Set<String> names) {
            String name = getProperty("name").getString();
            if (name != null) {
                names = new HashSet<>(names);
                names.add(name);
            }
            List<String> list = new ArrayList<>(names);
            Collections.sort(list);
            return new DefaultListModel(list.toArray(new String[list.size()]));
        }

        /**
         * Creates the layout strategy.
         *
         * @return a new layout strategy
         */
        @Override
        protected IMObjectLayoutStrategy createLayoutStrategy() {
            IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
            strategy.addComponent(new ComponentState(printer));
            return strategy;
        }
    }

    private class PrinterCollectionEditor extends MultipleRelationshipCollectionTargetEditor {

        /**
         * Constructs a {@link PrinterCollectionEditor}.
         *
         * @param property the collection property
         * @param entity   the parent entity
         * @param context  the layout context
         */
        public PrinterCollectionEditor(CollectionProperty property, Entity entity, LayoutContext context) {
            super(new EntityLinkCollectionTargetPropertyEditor(property, entity), entity, context);
        }

        /**
         * Creates a new editor.
         *
         * @param object  the object to edit
         * @param context the layout context
         * @return an editor to edit {@code object}
         */
        @Override
        public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
            return new PrinterEditor(object, getObject(), getAvailablePrinterNames(), context);
        }

        /**
         * Sets the current editor.
         *
         * @param editor the editor. May be {@code null}
         */
        @Override
        protected void setCurrentEditor(IMObjectEditor editor) {
            if (editor != null) {
                Set<String> names = getAvailablePrinterNames();
                ((PrinterEditor) editor).setAvailablePrinterNames(names);
            }
            super.setCurrentEditor(editor);
        }

        /**
         * Create a new table model.
         *
         * @param context the layout context
         * @return a new table model
         */
        @Override
        protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
            return new PrinterTableModel();
        }

        /**
         * Enable/disables the buttons.
         * <p>
         * This only enables the add button if there are printers available.
         *
         * @param enable if {@code true} enable buttons (subject to criteria), otherwise disable them
         */
        @Override
        protected void enableNavigation(boolean enable) {
            boolean enableAdd = enable;
            if (enable) {
                Set<String> available = getAvailablePrinterNames();
                enableAdd = !available.isEmpty();
            }
            super.enableNavigation(enable, enableAdd);
        }

        /**
         * Returns printer names that haven't been allocated.
         *
         * @return the available printer names
         */
        protected Set<String> getAvailablePrinterNames() {
            Set<String> allocatedNames = new HashSet<>();
            for (IMObject object : getCurrentObjects()) {
                if (object.getName() != null) {
                    allocatedNames.add(object.getName());
                }
            }
            Set<String> available = new HashSet<>(printerNames);
            available.removeAll(allocatedNames);
            return available;
        }
    }

    private class PrinterTableModel extends BaseIMObjectTableModel<IMObject> {

        private final int statusIndex;

        /**
         * Constructs a new {@code BaseIMObjectTableModel}, using
         * a new column model created by {@link #createTableColumnModel}.
         */
        public PrinterTableModel() {
            super(null);
            DefaultTableColumnModel model = (DefaultTableColumnModel) createTableColumnModel(false, false, true, false,
                                                                                             false);
            statusIndex = getNextModelIndex(model);
            model.addColumn(createTableColumn(statusIndex, "printer.status"));
            setTableColumnModel(model);
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate
         */
        @Override
        protected Object getValue(IMObject object, TableColumn column, int row) {
            Object result;
            if (column.getModelIndex() == statusIndex) {
                if (printerNames.contains(object.getName())) {
                    result = Messages.get("printer.status.available");
                } else {
                    result = Messages.get("printer.status.unknown");
                }
            } else {
                result = super.getValue(object, column, row);
            }
            return result;
        }
    }

    private class LocationLayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Constructs an {@link LocationLayoutStrategy}.
         */
        public LocationLayoutStrategy() {
            super(NODES);
        }

        /**
         * Apply the layout strategy.
         * <p>
         * This renders an object in a {@code Component}, using a factory to create the child components.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param context    the layout context
         * @return the component containing the rendered {@code object}
         */
        @Override
        public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
            Property defaultPrinter = properties.get("defaultPrinter");
            DefaultListModel model = new DefaultListModel(printerNames.toArray());
            SelectField field = BoundSelectFieldFactory.create(defaultPrinter, model);
            addComponent(new ComponentState(field, defaultPrinter));
            addComponent(new ComponentState(printers));
            return super.apply(object, properties, parent, context);
        }

        /**
         * Lays out components in a grid.
         *
         * @param object     the object to lay out
         * @param properties the properties
         * @param context    the layout context
         */
        @Override
        protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context) {
            ComponentGrid grid = super.createGrid(object, properties, context);
            ComponentState logo = new ComponentState(logoEditor.getComponent(), null, logoEditor.getFocusGroup(),
                                                     Messages.get("admin.practice.logo"));
            grid.add(logo, 2);
            return grid;
        }

        /**
         * Creates a component for a property.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display {@code property}
         */
        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
            ComponentState result;
            if (property.getName().equals("defaultPrinter")) {
                DefaultListModel model = new DefaultListModel(PrintHelper.getPrinters());
                SelectField field = BoundSelectFieldFactory.create(property, model);
                result = new ComponentState(field, property);
            } else {
                result = super.createComponent(property, parent, context);
            }
            return result;
        }

    }

}
