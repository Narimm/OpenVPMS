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

package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.Component;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectTabPane;
import org.openvpms.web.component.im.layout.IMObjectTabPaneModel;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.history.PatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;

import java.util.List;

/**
 * Layout strategy for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimLayoutStrategy extends AbstractClaimLayoutStrategy {

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The items editor.
     */
    private final ClaimItemCollectionEditor items;

    /**
     * The attachments editor.
     */
    private final AttachmentCollectionEditor attachments;

    /**
     * The tab pane.
     */
    private IMObjectTabPane pane;

    /**
     * The message node name.
     */
    private static final String MESSAGE = "message";


    /**
     * The nodes to display
     */
    private static final ArchetypeNodes NODES = ArchetypeNodes.all().excludeIfEmpty(MESSAGE, "insurerId");

    /**
     * Default constructor, used for viewing claims.
     */
    public ClaimLayoutStrategy() {
        this(null, null, null);
    }

    /**
     * Constructor, used for editing claims.
     *
     * @param patient     the patient
     * @param items       the claim items
     * @param attachments the attachments editor
     */
    public ClaimLayoutStrategy(Party patient,
                               ClaimItemCollectionEditor items, AttachmentCollectionEditor attachments) {
        super(NODES);
        this.items = items;
        this.attachments = attachments;
        this.patient = patient;
    }

    /**
     * Selects the attachments tab.
     */
    public void selectAttachments() {
        if (pane != null) {
            pane.setSelectedIndex(1);
        }
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
        IMObjectComponentFactory factory = context.getComponentFactory();

        if (context.isEdit()) {
            addComponent(new ComponentState(items));
            addComponent(new ComponentState(attachments));
        }

        Property message = properties.get(MESSAGE);
        if (!StringUtils.isEmpty(message.getString())) {
            addComponent(createTextArea(message, object, context));
        }

        // render the policy
        Act policy = getPolicy((Act) object);
        addComponent(createPolicy(policy, object, properties, factory));

        addComponent(createNotes(object, properties, context));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lays out each child component in a tabbed pane.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doComplexLayout(IMObject object, IMObject parent, List<Property> properties, Component container,
                                   LayoutContext context) {
        IMObjectTabPaneModel model = doTabLayout(object, properties, container, context, false);
        if (patient != null) {
            PatientHistoryQuery query = new PatientHistoryQuery(patient, context.getPreferences());
            PatientHistoryBrowser history = new PatientHistoryBrowser(query, context);
            String label = Messages.get("patient.insurance.history");
            label = getShortcut(label, model.size() + 1);
            model.addTab(label, history.getComponent());
        }
        pane = new IMObjectTabPane(model);

        pane.setSelectedIndex(0);
        container.add(pane);
    }

    /**
     * Creates a place-holder for the insurerId node when the collection is empty.
     * <p>
     * This is a workaround for the default single collection rendering that displays a short field containing 'None'.
     *
     * @param object    the parent object
     * @param insurerId the insurerId node
     * @param factory   the component factory
     * @return the place-holder
     */
    private ComponentState createDummyInsurerId(IMObject object, CollectionProperty insurerId,
                                                IMObjectComponentFactory factory) {
        SimpleProperty dummy = new SimpleProperty("dummy", String.class);
        dummy.setMaxLength(insurerId.getMaxLength());
        dummy.setReadOnly(true);
        ComponentState state = factory.create(dummy, object);
        return new ComponentState(state.getComponent(), insurerId);
    }

    /**
     * Creates a component representing the policy.
     *
     * @param policy     the policy. May be {@code null}
     * @param object     the parent object
     * @param properties the properties
     * @param factory    the component factory
     * @return the policy component
     */
    private ComponentState createPolicy(Act policy, IMObject object, PropertySet properties,
                                        IMObjectComponentFactory factory) {
        String value;
        if (policy != null) {
            ActBean bean = new ActBean(policy);
            Party insurer = (Party) bean.getNodeParticipant("insurer");
            String insurerName = (insurer != null) ? insurer.getName() : null;
            ActIdentity identity = bean.getObject("insurerId", ActIdentity.class);
            String insurerId = (identity != null) ? identity.getIdentity() : null;
            value = Messages.format("patient.insurance.policy", insurerName, insurerId);
        } else {
            value = Messages.get("imobject.none");
        }
        SimpleProperty dummy = new SimpleProperty("dummy", value, String.class);
        dummy.setReadOnly(true);
        Component component = factory.create(dummy, object).getComponent();
        return new ComponentState(component, properties.get("policy"));
    }

    /**
     * Returns the claim policy.
     *
     * @param claim the claim
     * @return the policy, or {@code null} if none exists
     */
    private Act getPolicy(Act claim) {
        IMObjectBean bean = new IMObjectBean(claim);
        return bean.getTarget("policy", Act.class);
    }

}
