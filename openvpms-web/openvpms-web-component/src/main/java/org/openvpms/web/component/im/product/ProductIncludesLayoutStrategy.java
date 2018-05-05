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

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import static org.openvpms.web.component.im.product.ProductIncludesEditor.MAX_WEIGHT;
import static org.openvpms.web.component.im.product.ProductIncludesEditor.MIN_WEIGHT;
import static org.openvpms.web.component.im.product.ProductIncludesEditor.WEIGHT_UNITS;

/**
 * Layout strategy for <em>entityLink.productIncludes</em>, used by <em>product.template</em> to include
 * products.
 * <br/>
 * This suppresses the <em>location</em> node, if the practice <em>useLocationProducts</em> flag is unset.
 *
 * @author Tim Anderson
 */
public class ProductIncludesLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Apply the layout strategy.
     * <p/>
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
        boolean useLocationProducts = ProductHelper.useLocationProducts(context.getContext());
        ArchetypeNodes nodes = new ArchetypeNodes();
        nodes.exclude("source");
        nodes.simple("target");
        nodes.first("target");
        if (!useLocationProducts) {
            nodes.exclude("skipIfMissing");
        }

        setArchetypeNodes(nodes);

        ComponentState minWeight = createComponent(properties.get(MIN_WEIGHT), parent, context);
        ComponentState maxWeight = createComponent(properties.get(MAX_WEIGHT), parent, context);
        ComponentState weightUnits = createComponent(properties.get(WEIGHT_UNITS), parent, context);
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
}
