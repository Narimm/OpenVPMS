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

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import static org.openvpms.web.app.patient.mr.PatientRecordTypes.*;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * CRUD Window for patient record acts in 'visit' view.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class VisitRecordCRUDWindow extends PatientRecordCRUDWindow {

    /**
     * Create a new <code>VisitRecordCRUDWindow</code>.
     */
    public VisitRecordCRUDWindow() {
        super(new ShortNameList(CLINICAL_EVENT));
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return a new editor
     */
    @Override
    protected IMObjectEditor createEditor(IMObject object,
                                          LayoutContext context) {
        if (TypeHelper.isA(object, CLINICAL_PROBLEM)) {
            // don't permit editing items node in visit view
            return new ClinicalProblemActEditor((Act) object, null, false,
                                                context);
        }
        return super.createEditor(object, context);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        if (isNew) {
            if (TypeHelper.isA(object, getClinicalEventItemShortNames())) {
                addActRelationship((Act) object, CLINICAL_EVENT,
                                   RELATIONSHIP_CLINICAL_EVENT_ITEM);
            }
        }
        super.onSaved(object, isNew);
    }

}
