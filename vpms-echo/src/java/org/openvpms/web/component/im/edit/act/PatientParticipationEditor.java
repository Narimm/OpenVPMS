package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.ObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Participation editor for patients. This updates {@link Context#setPatient}
 * when a patient is selected.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PatientParticipationEditor extends ParticipationEditor {

    /**
     * Construct a new <code>PatientParticipationEditor</code>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param descriptor    the parent descriptor
     * @param context       the layout context. May be <code>null</code>
     */
    protected PatientParticipationEditor(Participation participation, Act parent,
                                         NodeDescriptor descriptor,
                                         LayoutContext context) {
        super(participation, parent, descriptor, context);

        if (participation.isNew() && participation.getEntity() == null) {
            IMObject patient = Context.getInstance().getPatient();
            getObjectReferenceEditor().setObject(patient);
        }
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object     the object to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param context       the layout context. May be <code>null</code>
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        NodeDescriptor descriptor,
                                        LayoutContext context) {
        IMObjectEditor result = null;
        if (object instanceof Participation
            && parent instanceof Act) {
            Participation participation = (Participation) object;
            if (participation.getArchetypeId().getShortName().equals(
                    "participation.patient")) {
                result = new PatientParticipationEditor(
                        participation, (Act) parent, descriptor, context);
            }
        }
        return result;
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property   the reference property
     * @param descriptor the reference descriptor
     * @return a new object reference editor
     */
    @Override
    protected ObjectReferenceEditor createObjectReferenceEditor(
            Property property, NodeDescriptor descriptor) {
        return new ObjectReferenceEditor(property, descriptor) {
            @Override
            protected void onSelected(IMObject object) {
                super.onSelected(object);
                Party patient = (Party) object;
                Context.getInstance().setPatient(patient);
            }
        };
    }
}
