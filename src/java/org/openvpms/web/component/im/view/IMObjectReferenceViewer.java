package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Viewer for {@link IMObjectReference}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectReferenceViewer {

    /**
     * The reference to view.
     */
    private final IMObjectReference _reference;


    /**
     * Construct a new <code>IMObjectReferenceViewer</code>.
     *
     * @param reference the reference to view
     */
    public IMObjectReferenceViewer(IMObjectReference reference) {
        _reference = reference;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Component result;
        final IMObject object = IMObjectHelper.getObject(_reference);
        if (object != null) {
            String text = Messages.get("imobject.name", object.getName());
            Button button = ButtonFactory.create(null, "hyperlink");
            button.setText(text);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onView(object);
                }
            });
            result = button;
        } else {
            Label label = LabelFactory.create();
            label.setText(Messages.get("imobject.none"));
            result = label;
        }
        return result;

    }

    /**
     * Views the object.
     */
    protected void onView(IMObject object) {
        ContextApplicationInstance.getInstance().switchTo(object);
    }

}
