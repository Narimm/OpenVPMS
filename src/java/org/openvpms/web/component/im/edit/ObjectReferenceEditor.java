package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.jxpath.Pointer;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.DefaultQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.select.Selector;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Editor for {@link IMObjectReference}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ObjectReferenceEditor {

    /**
     * Pointer to the reference.
     */
    private Pointer _pointer;

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
     * @param pointer    a pointer to the reference
     * @param descriptor the reference descriptor
     */
    public ObjectReferenceEditor(Pointer pointer, NodeDescriptor descriptor) {
        this(pointer, descriptor, false);
    }

    /**
     * Construct a new <code>ObjectReferenceEditor</code>.
     *
     * @param pointer    a pointer to the reference
     * @param descriptor the reference descriptor
     * @param readOnly   if <code>true</code> the reference cannot be edited
     */
    public ObjectReferenceEditor(Pointer pointer, NodeDescriptor descriptor, boolean readOnly) {
        _pointer = pointer;
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
        IMObjectReference reference = (IMObjectReference) _pointer.getValue();
        if (reference != null || (reference == null && !readOnly)) {
            _selector.setObject(getObject(reference, descriptor));
        }
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
                    onSelected(object);
                }
            }
        });

        popup.show();
    }

    /**
     * Invoked when an object is selected.
     *
     * @param object the object
     */
    protected void onSelected(IMObject object) {
        IMObjectReference reference = new IMObjectReference(object);
        _pointer.setValue(reference);
        _selector.setObject(object);
    }

    /**
     * Returns an object given its reference and descriptor. If the reference is
     * null, determines if the descriptor matches that of the current object
     * being edited and returns that instead.
     *
     * @param reference  the object reference. May be <code>null</code>
     * @param descriptor the node descriptor
     * @return the object matching <code>reference</code>, or
     *         <code>descriptor</code>, or <code>null</code> if there is no
     *         match
     */
    private IMObject getObject(IMObjectReference reference,
                               NodeDescriptor descriptor) {
        IMObject result = null;
        if (reference == null) {
            result = match(descriptor);
        } else {
            IMObject edit = Context.getInstance().getEdited();
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
     * Determines if the current object being edited matches the archetype
     * range of the specified descriptor.
     *
     * @param descriptor the node descriptor
     * @return the current object being edited, or <code>null</code> if its type
     *         doesn't match the specified descriptor's archetype range
     */
    private IMObject match(NodeDescriptor descriptor) {
        IMObject result = null;
        String[] range = descriptor.getArchetypeRange();
        IMObject object = Context.getInstance().getEdited();
        if (object != null) {
            String shortName = object.getArchetypeId().getShortName();
            for (int i = 0; i < range.length; ++i) {
                if (range[i].equals(shortName)) {
                    result = object;
                    break;
                }
            }
        }
        return result;
    }

}
