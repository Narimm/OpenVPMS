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

package org.openvpms.web.workspace.patient.insurance.policy;

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectTableCollectionViewer;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.style.Styles;

import java.util.ArrayList;
import java.util.List;

/**
 * Layout strategy for <em>act.patientInsurancePolicy</em>.
 *
 * @author Tim Anderson
 */
public class PolicyLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The nodes to display.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().excludeIfEmpty("claims");

    /**
     * Constructs a {@link PolicyLayoutStrategy}.
     */
    public PolicyLayoutStrategy() {
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
        CollectionProperty claims = (CollectionProperty) properties.get("claims");
        if (!claims.isEmpty()) {
            if (context.isEdit()) {
                // if a claim exists that isn't PENDING or POSTED, make the policy number read-only
                IMObjectBean bean = new IMObjectBean(object);
                boolean pendingOrPosted = true;
                for (Act claim : bean.getSources("claims", Act.class)) {
                    String status = claim.getStatus();
                    if (!Claim.Status.PENDING.isA(status) && !Claim.Status.POSTED.isA(status)) {
                        pendingOrPosted = false;
                        break;
                    }
                }
                if (!pendingOrPosted) {
                    Property insurerId = properties.get("insurerId");
                    LayoutContext subContext = new DefaultLayoutContext(context);
                    ReadOnlyComponentFactory factory = new ReadOnlyComponentFactory(subContext, Styles.EDIT);
                    subContext.setComponentFactory(factory);
                    addComponent(factory.create(insurerId, object));
                }
            }
            ClaimViewer viewer = new ClaimViewer(claims, object, context);
            addComponent(new ComponentState(viewer.getComponent(), viewer.getProperty()));
        }
        return super.apply(object, properties, parent, context);
    }

    /**
     * Displays claims.
     */
    private class ClaimViewer extends IMObjectTableCollectionViewer {

        /**
         * Constructs a {@link ClaimViewer}.
         *
         * @param property the collection to view
         * @param parent   the parent object
         * @param layout   the layout context. May be {@code null}
         */
        public ClaimViewer(CollectionProperty property, IMObject parent, LayoutContext layout) {
            super(property, parent, layout);
        }


        /**
         * Returns the objects to display.
         *
         * @return the objects to display
         */
        @Override
        protected List<IMObject> getObjects() {
            List values = getProperty().getValues();
            List<IMObject> objects = new ArrayList<>();
            for (Object value : values) {
                IMObjectRelationship relationship = (IMObjectRelationship) value;
                IMObject claim = IMObjectHelper.getObject(relationship.getSource());
                if (claim != null) {
                    objects.add(claim);
                }
            }
            return objects;
        }

        /**
         * Browse an object.
         *
         * @param object the object to browse.
         */
        @Override
        protected void browse(IMObject object) {
            // no-op
        }

        /**
         * Create a new table model.
         *
         * @param context the layout context
         * @return a new table model
         */
        @Override
        protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
            String[] arcetypes = {InsuranceArchetypes.CLAIM};
            return IMObjectTableModelFactory.create(arcetypes, getObject(), context);
        }

    }
}
