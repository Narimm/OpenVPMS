/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.workflow;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.patient.visit.VisitCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;


/**
 * A test {@link VisitCRUDWindow}.
 *
 * @author Tim Anderson
 */
public class TestVisitCRUDWindow extends VisitCRUDWindow {

    private final List<Act> saved = new ArrayList<Act>();

    public TestVisitCRUDWindow(Context context) {
        super(context, new HelpContext("foo", null));
    }

    /**
     * Performs the "Add Visit & Note" operation.
     *
     * @return the added note
     */
    public Act addVisitAndNote() {
        saved.clear();
        onAddNote();
        assertEquals(1, saved.size());
        assertTrue(TypeHelper.isA(saved.get(0), PatientArchetypes.CLINICAL_NOTE));
        return saved.get(0);
    }

    /**
     * Adds a note to the current visit.
     */
    public void addNote() {
        Act act = (Act) IMObjectCreator.create(PatientArchetypes.CLINICAL_NOTE);
        assertNotNull(act);
        LayoutContext context = createLayoutContext(getHelpContext());
        IMObjectEditor editor = createEditor(act, context);
        edit(editor);
    }

    /**
     * Edits an object.
     *
     * @param editor the object editor
     * @return the edit dialog
     */
    @Override
    protected EditDialog edit(IMObjectEditor editor) {
        EditDialog dialog = super.edit(editor);
        fireDialogButton(dialog, PopupDialog.OK_ID);
        return dialog;
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param act   the object
     * @param isNew determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act act, boolean isNew) {
        super.onSaved(act, isNew);
        if (!saved.contains(act)) {
            saved.add(act);
        }
    }
}
