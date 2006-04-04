package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;


/**
 * An editor for {@link Act}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public abstract class ActEditor extends AbstractIMObjectEditor {

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
     * @param context    the layout context. May be <code>null</code>
     */
    protected ActEditor(Act act, IMObject parent, NodeDescriptor descriptor,
                        LayoutContext context) {
        super(act, parent, descriptor, context);
        NodeDescriptor items = getDescriptor("items");
        _editor = new ActRelationshipCollectionEditor(act, items,
                                                      getLayoutContext());
        getModifiableSet().add(act, _editor);
        _editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateTotals();
            }
        });
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
        return super.doSave();
    }

    /**
     * Returns the act collection editor.
     *
     * @return the act colleciton editor
     */
    protected ActRelationshipCollectionEditor getEditor() {
        return _editor;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new ActLayoutStrategy(_editor);
    }

    /**
     * Update totals when an act item changes.
     *
     * @todo - workaround for OVPMS-211
     */
    protected abstract void updateTotals();

}
