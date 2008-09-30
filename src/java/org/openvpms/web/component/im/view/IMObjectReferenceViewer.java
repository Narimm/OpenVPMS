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
    private final IMObjectReference reference;

    /**
     * The name.
     */
    private final String name;

    /**
     * Determines if a hyperlink should be created, to launch a view of the
     * object.
     */
    private final boolean link;

    /**
     * The style name.
     */
    private String style;


    /**
     * Construct a new <code>IMObjectReferenceViewer</code>.
     *
     * @param reference the reference to view. May be <code>null</code>
     * @param link      if <code>true</code> enable an hyperlink to the object
     */
    public IMObjectReferenceViewer(IMObjectReference reference, boolean link) {
        this(reference, null, link);
    }

    /**
     * Construct a new <code>IMObjectReferenceViewer</code>.
     *
     * @param reference the reference to view. May be <code>null</code>
     * @param name      the object name. May be <code>null</code>
     * @param link      if <code>true</code> enable an hyperlink to the object
     */
    public IMObjectReferenceViewer(IMObjectReference reference, String name,
                                   boolean link) {
        this.reference = reference;
        this.name = name;
        this.link = link;
    }

    /**
     * Sets the style name.
     *
     * @param style name
     */
    public void setStyleName(String style) {
        this.style = style;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Component result;
        String text = name;
        if (text == null) {
            text = IMObjectHelper.getName(reference);
            if (text != null) {
                text = Messages.get("imobject.name", text);
            } else {
                text = Messages.get("imobject.none");
            }
        }
        if (text != null) {
            if (link) {
                if (style == null) {
                    style = "hyperlink";
                }
                Button button = ButtonFactory.create(null, style, false);
                button.setText(text);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onView();
                    }
                });
                button.setFocusTraversalParticipant(false);
                // wrap in a row so the button renders to its minimum width
                result = RowFactory.create(button);
            } else {
                Label label = (style != null)
                        ? LabelFactory.create(null, style)
                        : LabelFactory.create();
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
    protected void onView() {
        IMObject object = IMObjectHelper.getObject(reference);
        if (object != null) {
            ContextApplicationInstance.getInstance().switchTo(object);
        }
    }

}
