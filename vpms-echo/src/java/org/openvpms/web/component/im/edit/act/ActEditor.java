package org.openvpms.web.component.im.edit.act;

import java.math.BigDecimal;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;


/**
 * An editor for {@link Act}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class ActEditor extends AbstractIMObjectEditor {

    /**
     * The act item editor.
     */
    private ActRelationshipCollectionEditor _editor;


    /**
     * Construct a new <code>ActEditor</code>.
     *
     * @param act        the act to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     */
    protected ActEditor(Act act, IMObject parent,
                        NodeDescriptor descriptor, boolean showAll) {
        super(act, parent, descriptor, showAll);
        NodeDescriptor items = getDescriptor("items");
        _editor = new ActRelationshipCollectionEditor(act, items, showAll);
        _editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateTotals();
            }
        });
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object     the object to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        NodeDescriptor descriptor, boolean showAll) {
        IMObjectEditor result = null;
        if (object instanceof Act) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(object);
            if (archetype != null) {
                NodeDescriptor items = archetype.getNodeDescriptor("items");
                if (items != null) {
                    String[] range = items.getArchetypeRange();
                    if (range.length == 1
                        && range[0].equals("actRelationship.estimationItem")) {
                        result = new ActEditor((Act) object, parent, descriptor, showAll);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Determines if the object has been changed.
     *
     * @return <code>true</code> if the object has been changed
     */
    @Override
    public boolean isModified() {
        return _editor.isModified() || super.isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
        _editor.clearModified();
        super.clearModified();
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    protected boolean doSave() {
        IMObject object = getObject();
        if (object.isNew()) {
            // @todo need to save this before children in order for the children
            // to create relationships to it. May be fixed by OVPMS-175
            if (!SaveHelper.save(object)) {
                return false;
            }
        }
        boolean saved = _editor.save();
        if (saved) {
            saved = super.doSave();
        }
        return saved;
    }

    /**
     * Creates the layout strategy.
     *
     * @param showAll if <code>true</code> show required and optional fields;
     *                otherwise show required fields.
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy(boolean showAll) {
        return new ActLayoutStrategy(_editor.getComponent(), showAll);
    }

    /**
     * Update totals when an act item changes.
     *
     * @todo - workaround for OVPMS-211
     */
    protected void updateTotals() {
        Property highTotal = getProperty("highTotal");
        Property lowTotal = getProperty("lowTotal");

        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor("act.estimationItem");
        NodeDescriptor highDesc = archetype.getNodeDescriptor("highTotal");
        NodeDescriptor lowDesc = archetype.getNodeDescriptor("lowTotal");
        BigDecimal low = new BigDecimal(0);
        BigDecimal high = new BigDecimal(0);
        for (Act act : _editor.getActs()) {
            low = sum(low, act, lowDesc);
            high = sum(high, act, highDesc);
        }
        lowTotal.setValue(low);
        highTotal.setValue(high);
    }

    private BigDecimal sum(BigDecimal value, IMObject object, NodeDescriptor descriptor) {
        // @todo OVPMS-210
        Number tmp = (Number) descriptor.getValue(object);
        if (tmp instanceof BigDecimal) {
            value = value.add((BigDecimal) tmp);
        } else {
            value = value.add(new BigDecimal(tmp.doubleValue()));
        }
        return value;
    }

}
