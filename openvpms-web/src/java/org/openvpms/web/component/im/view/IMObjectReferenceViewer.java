/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

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
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Viewer for {@link IMObjectReference}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectReferenceViewer {

    /**
     * The reference to view.
     */
    private final IMObjectReference _reference;

    /**
     * Determines if a hyperlink should be created, to launch a view of the
     * object.
     */
    private final boolean _link;


    /**
     * Construct a new <code>IMObjectReferenceViewer</code>.
     *
     * @param reference the reference to view
     * @param link      if <code>true</code> enable an hyperlink to the object
     */
    public IMObjectReferenceViewer(IMObjectReference reference, boolean link) {
        _reference = reference;
        _link = link;
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
            if (_link) {
                Button button = ButtonFactory.create();
                button.setStyleName("hyperlink");
                button.setText(text);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onView(object);
                    }
                });
                // wrap in a row so the button renders to its minimum width
                result = RowFactory.create(button);
            } else {
                Label label = LabelFactory.create();
                label.setText(text);
                result = label;
            }
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
