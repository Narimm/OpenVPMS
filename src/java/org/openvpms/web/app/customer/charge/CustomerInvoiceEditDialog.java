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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.customer.charge;

import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * An edit dialog for {@link CustomerChargeActEditor} editors.
 * <p/>
 * This ensures that clinical events exist for each patient, as a workaround for OVPMS-823.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerInvoiceEditDialog extends CustomerChargeActEditDialog {

    /**
     * Constructs a <tt>CustomerInvoiceEditDialog</tt>.
     *
     * @param editor the editor
     */
    public CustomerInvoiceEditDialog(CustomerChargeActEditor editor) {
        super(editor);
    }

    /**
     * Saves the current object.
     *
     * @return <tt>true</tt> if the object was saved
     */
    @Override
    protected boolean doSave() {
        CustomerChargeActEditor editor = (CustomerChargeActEditor) getEditor();
        try {
            // ensure the events are created prior to the invoice being saved
            editor.createClinicalEvents();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
            return false;
        }
        return super.doSave();
    }
}
