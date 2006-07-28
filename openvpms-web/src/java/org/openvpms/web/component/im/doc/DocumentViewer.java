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

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.apache.commons.io.FilenameUtils;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Viewer for {@link IMObjectReference}s of type <em>document.*</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentViewer {

    /**
     * The reference to view.
     */
    private final IMObjectReference _reference;

    /**
     * The parent object. May be <code>null</code>
     */
    private final IMObject _parent;

    /**
     * Determines if a hyperlink should be created, to enable downloads of
     * the document.
     */
    private final boolean _link;


    /**
     * Construct a new <code>DocumentViewer</code>.
     *
     * @param reference the reference to view
     * @param parent    the parent. May be <code>null</code>
     * @param link      if <code>true</code> enable an hyperlink to the object
     */
    public DocumentViewer(IMObjectReference reference, IMObject parent,
                          boolean link) {
        _reference = reference;
        _parent = parent;
        _link = link;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Component result;
        String text = null;
        String styleName = null;
        if (_reference != null) {
            if (_parent instanceof DocumentAct) {
                DocumentAct dact = (DocumentAct)_parent;
                if (dact.getFileName() != null)
                    styleName = "download.".concat(FilenameUtils.getExtension(dact.getFileName()));
                else
                    styleName = "download.default";
                if (ApplicationInstance.getActive().getStyle(Button.class, styleName) == null)
                    styleName = "download.default";
                text = dact.getFileName();
            }
            else {
                styleName="donwload.default";
                text = DescriptorHelper.getDisplayName(_reference.getArchetypeId().getShortName());
            }
            
            if (_link) {
                Button button = ButtonFactory.create();
                button.setStyleName(styleName);
                button.setText(text);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onDownload();
                    }
                });
                result = button;
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
     * Invoked when the link is selected.
     */
    private void onDownload() {
        DownloadHelper.download(_reference);
    }
}
