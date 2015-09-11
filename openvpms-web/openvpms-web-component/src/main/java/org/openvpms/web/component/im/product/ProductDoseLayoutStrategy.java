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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.edit.SelectFieldIMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Layout strategy for <em>entity.productDose</em>.
 *
 * @author Tim Anderson
 */
public class ProductDoseLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The nodes to display.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude(ProductDoseEditor.MAX_WEIGHT,
                                                                             ProductDoseEditor.WEIGHT_UNITS);


    /**
     * Constructs a {@link ProductDoseLayoutStrategy}.
     */
    public ProductDoseLayoutStrategy() {
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
        ComponentState species;
        if (context.isEdit()) {
            species = createSpeciesEditor(properties, object, context);
        } else {
            species = createSpeciesViewer(properties);
        }
        addComponent(species);
        ComponentState minWeight = createComponent(properties.get(ProductDoseEditor.MIN_WEIGHT), object, context);
        ComponentState maxWeight = createComponent(properties.get(ProductDoseEditor.MAX_WEIGHT), object, context);
        ComponentState weightUnits = createComponent(properties.get(ProductDoseEditor.WEIGHT_UNITS), object, context);
        Label label = LabelFactory.create();
        label.setText("-");
        Row row = RowFactory.create(Styles.CELL_SPACING, minWeight.getComponent(), label, maxWeight.getComponent(),
                                    weightUnits.getComponent());
        FocusGroup weight = new FocusGroup("weight", minWeight.getComponent(), maxWeight.getComponent(),
                                           weightUnits.getComponent());
        String displayName = Messages.get("product.weight");
        addComponent(new ComponentState(row, minWeight.getProperty(), weight, displayName));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Creates a select field to select the species.
     * <p>
     * This provides an All option, indicating that the dose applies to all species. This is selected if no
     * other species has been selected previously.
     *
     * @param properties the properties
     * @param object     the parent object
     * @param context    the layout context
     * @return a new component
     */
    private ComponentState createSpeciesEditor(PropertySet properties, IMObject object, LayoutContext context) {
        CollectionProperty property = (CollectionProperty) properties.get(ProductDoseEditor.SPECIES);
        PropertyEditor editor = new SelectFieldIMObjectCollectionEditor(property, object, context) {
            @Override
            protected SelectField createSelectField(CollectionProperty property, List<IMObject> objects) {
                SelectField field = super.createSelectField(property, objects);
                if (property.getValues().isEmpty()) {
                    field.setSelectedIndex(0); // select All
                }
                return field;
            }

            @Override
            protected IMObjectListModel createModel(CollectionProperty property, List<IMObject> objects) {
                // create a model with 'All'
                return new IMObjectListModel(objects, true, false);
            }
        };
        return new ComponentState(editor);
    }

    /**
     * Creates a component to view the species.
     * TODO - there should be a read-only equivalent of SelectFieldIMObjectCollectionEditor
     *
     * @param properties the properties
     * @return a new component to view the species
     */
    private ComponentState createSpeciesViewer(PropertySet properties) {
        CollectionProperty property = (CollectionProperty) properties.get(ProductDoseEditor.SPECIES);
        List values = property.getValues();
        String name;
        if (!values.isEmpty()) {
            Lookup value = (Lookup) values.get(0);
            name = value.getName();
        } else {
            name = Messages.get("list.all");
        }
        TextComponent text = ReadOnlyComponentFactory.getText(name, 20, ReadOnlyComponentFactory.MAX_DISPLAY_LENGTH,
                                                              Styles.DEFAULT);
        return new ComponentState(text, property);
    }
}

