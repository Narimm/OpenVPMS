package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.Component;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;

import java.util.List;

/**
 * Layout strategy for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public abstract class AbstractClaimLayoutStrategy extends AbstractInsuranceLayoutStrategy {

    /**
     * The message node name.
     */
    protected static final String MESSAGE = "message";

    /**
     * Gap claim node name.
     */
    protected static final String GAP_CLAIM = "gapClaim";

    /**
     * Constructs an {@link AbstractInsuranceLayoutStrategy}.
     *
     * @param showGapClaim if {@code true}, show the gap claim node
     */
    public AbstractClaimLayoutStrategy(boolean showGapClaim) {
        ArchetypeNodes nodes = ArchetypeNodes.all().exclude("policy").excludeIfEmpty(MESSAGE, "insurerId");
        setArchetypeNodes(nodes);
        if (!showGapClaim) {
            nodes.exclude(GAP_CLAIM);
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
        Property message = properties.get(MESSAGE);
        if (!StringUtils.isEmpty(message.getString())) {
            addComponent(createTextArea(message, object, context));
        }

        addComponent(createNotes(object, properties, context));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties, Component container,
                                  LayoutContext context) {
        ArchetypeNodes.insert(properties, "endTime", getInsurer(), getPolicyNumber());
        super.doSimpleLayout(object, parent, properties, container, context);
    }

    /**
     * Returns the insurer property.
     *
     * @return the insurer
     */
    protected abstract Property getInsurer();

    /**
     * Returns the policy number property.
     *
     * @return the policy number
     */
    protected abstract Property getPolicyNumber();
}
