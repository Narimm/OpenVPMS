package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.DefaultQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.select.Selector;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Editor for {@link IMObjectReference}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ObjectReferenceEditor {

    /**
     * The reference property.
     */
    private Property _property;

    /**
     * The node descriptor.
     */
    private NodeDescriptor _descriptor;

    /**
     * The selector.
     */
    private Selector _selector;


    /**
     * Construct a new <code>ObjectReferenceEditor</code>.
     *
     * @param property   the reference property
     * @param descriptor the reference descriptor
     */
    public ObjectReferenceEditor(Property property, NodeDescriptor descriptor) {
        this(property, descriptor, false);
    }

    /**
     * Construct a new <code>ObjectReferenceEditor</code>.
     *
     * @param property   the reference property
     * @param descriptor the reference descriptor
     * @param readOnly   if <code>true</code> the reference cannot be edited
     */
    public ObjectReferenceEditor(Property property, NodeDescriptor descriptor,
                                 boolean readOnly) {
        _property = property;
        _descriptor = descriptor;

        if (readOnly) {
            _selector = new Selector(Selector.ButtonStyle.HIDE);
        } else {
            _selector = new Selector(Selector.ButtonStyle.RIGHT);
            _selector.getSelect().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSelect();
                }
            });
        }
        IMObjectReference reference = (IMObjectReference) _property.getValue();
        if (reference != null || (reference == null && !readOnly)) {
            _selector.setObject(getObject(reference, descriptor));
        }
    }

    /**
     * Sets the value of the reference to the supplied object.
     *
     * @param object the object
     */
    public void setObject(IMObject object) {
        IMObjectReference reference = new IMObjectReference(object);
        _property.setValue(reference);
        _selector.setObject(object);
    }

    /**
     * Returns the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return _descriptor.getDisplayName();
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return _selector.getComponent();
    }

    /**
     * Returns the object reference's descriptor.
     *
     * @return the object reference's descriptor
     */
    public NodeDescriptor getDescriptor() {
        return _descriptor;
    }

    /**
     * Returns an object given its reference and descriptor. If the reference is
     * null, determines if the descriptor matches that of the current object
     * being viewed/edited and returns that instead.
     *
     * @param reference  the object reference. May be <code>null</code>
     * @param descriptor the node descriptor
     * @return the object matching <code>reference</code>, or
     *         <code>descriptor</code>, or <code>null</code> if there is no
     *         matches
     */
    public static IMObject getObject(IMObjectReference reference,
                                     NodeDescriptor descriptor) {
        IMObject result = null;
        if (reference == null) {
            result = match(descriptor);
        } else {
            IMObject edit = Context.getInstance().getCurrent();
            if (edit != null) {
                if (edit.getArchetypeId().equals(reference.getArchetypeId())
                    && edit.getUid() == reference.getUid()) {
                    result = edit;
                }
            }
            if (result == null) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                result = service.get(reference);
            }
        }
        return result;
    }

    /**
     * Pops up a dialog to select an object.
     */
    protected void onSelect() {
        Query query = new DefaultQuery(_descriptor.getArchetypeRange());
        final Browser browser = new Browser(query);
        String title = Messages.get("imobject.select.title",
                                    _descriptor.getDisplayName());
        final BrowserDialog popup = new BrowserDialog(title, browser);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                IMObject object = popup.getSelected();
                if (object != null) {
                    setObject(object);
                }
            }
        });

        popup.show();
    }

    /**
     * Determines if the current object being edited matches archetype range of
     * the specified descriptor.
     *
     * @param descriptor the node descriptor
     * @return the current object being edited, or <code>null</code> if its type
     *         doesn't matches the specified descriptor's archetype range
     */
    private static IMObject match(NodeDescriptor descriptor) {
        IMObject result = null;
        IMObject object = Context.getInstance().getCurrent();
        if (object != null) {
            for (String shortName : descriptor.getArchetypeRange()) {
                if (DescriptorHelper.matches(
                        object.getArchetypeId(), shortName)) {
                    result = object;
                    break;
                }
            }
        }
        return result;
    }

}
