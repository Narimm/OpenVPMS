package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Default editor for {@link Participation} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class DefaultParticipationEditor extends AbstractParticipationEditor {

    /**
     * Construct a new <code>AbstractParticipationEditor</code>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param descriptor    the parent descriptor
     * @param context       the layout context. May be <code>null</code>
     */
    protected DefaultParticipationEditor(Participation participation,
                                         Act parent, NodeDescriptor descriptor,
                                         LayoutContext context) {
        super(participation, parent, descriptor, context);
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object     the object to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param context    the layout context. Nay be <code>null</code>
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static DefaultParticipationEditor create(IMObject object,
                                                    IMObject parent,
                                                    NodeDescriptor descriptor,
                                                    LayoutContext context) {
        DefaultParticipationEditor result = null;
        if (object instanceof Participation
            && parent instanceof Act) {
            result = new DefaultParticipationEditor((Participation) object,
                                                    (Act) parent, descriptor,
                                                    context);
        }
        return result;
    }

}
