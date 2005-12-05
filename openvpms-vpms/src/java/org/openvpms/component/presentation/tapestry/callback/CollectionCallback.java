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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.presentation.tapestry.callback;


// apache-hivemind
import org.apache.tapestry.IRequestCycle;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.presentation.tapestry.page.EditPage;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CollectionCallback extends EditCallback {
    private static final long serialVersionUID = 1L;

    private NodeDescriptor descriptor;

    /**
     * @param pageName
     * @param model
     */
    public CollectionCallback(String pageName, Object model,
            NodeDescriptor descriptor) {
        super(pageName, model);
        this.descriptor = descriptor;
    }

    public void add(Object newObject) {
        descriptor.addChildToCollection((IMObject)model, newObject);
    }

    public void remove(Object object) {
        descriptor.removeChildFromCollection((IMObject)model, object);
    }

    public void performCallback(IRequestCycle cycle) {
        // Find the Page for the stored Page Name
        EditPage editPage = (EditPage) cycle.getPage(pageName);
        // Set the model
        editPage.setModel(model);
        // Set the Active tabe
        editPage.setCurrentActiveTab(descriptor.getDisplayName());
        // Activate the Page
        cycle.activate(editPage);
    }
}
