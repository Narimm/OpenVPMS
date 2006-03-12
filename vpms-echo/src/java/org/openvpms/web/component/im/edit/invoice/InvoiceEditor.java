package org.openvpms.web.component.im.edit.invoice;

import java.math.BigDecimal;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.DescriptorHelper;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerInvoice</em> and <em>act.customerCredit</em>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class InvoiceEditor extends ActEditor {

    /**
     * Construct a new <code>ActEditor</code>.
     *
     * @param act        the act to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param context    the layout context
     */
    protected InvoiceEditor(Act act, IMObject parent,
                            NodeDescriptor descriptor, LayoutContext context) {
        super(act, parent, descriptor, context);
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object     the object to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param context    the layout context
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        NodeDescriptor descriptor,
                                        LayoutContext context) {
        IMObjectEditor result = null;
        if (object instanceof Act) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(object);
            if (archetype != null) {
                NodeDescriptor items = archetype.getNodeDescriptor("items");
                if (items != null) {
                    String[] range = items.getArchetypeRange();
                    if (range.length == 1
                        && (range[0].equals("actRelationship.customerInvoiceItem")
                            || range[0].equals("actRelationship.customerCreditItem")))
                    {
                        result = new InvoiceEditor((Act) object, parent,
                                                   descriptor, context);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Update totals when an act item changes.
     *
     * @todo - workaround for OVPMS-211
     */
    protected void updateTotals() {
        Property total = getProperty("total");

        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor("act.customerInvoiceItem");
        NodeDescriptor descriptor = archetype.getNodeDescriptor("total");
        BigDecimal value = new BigDecimal(0);
        for (Act act : getEditor().getActs()) {
            value = sum(value, act, descriptor);
        }
        total.setValue(value);
    }

}
