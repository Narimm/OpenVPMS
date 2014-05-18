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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.workspace.CRUDWindow;

/**
 * A tab in the {@link VisitEditor}.
 *
 * @author Tim Anderson
 */
public interface VisitEditorTab {

    /**
     * Returns the identifier of this tab.
     *
     * @return the tab identifier
     */
    int getId();

    /**
     * Sets the identifier of this tab.
     *
     * @param id the tab identifier
     */
    void setId(int id);

    /**
     * Invoked when the tab is displayed.
     */
    void show();

    /**
     * Invoked prior to switching to another tab, in order to save state.
     * <p/>
     * If the save fails, then switching is cancelled.
     *
     * @return {@code true} if the save was successful, otherwise {@code false}
     */
    boolean save();

    /**
     * Returns the tab component.
     *
     * @return the tab component
     */
    Component getComponent();

    /**
     * Returns the CRUD window for the tab.
     *
     * @return the CRUD window for the tab, or {@code null} if the tab doesn't provide one
     */
    CRUDWindow<? extends Act> getWindow();

}