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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

/**
 * Flow Sheet information.
 *
 * @author Tim Anderson
 */
class FlowSheetInfo {

    /**
     * The department identifier.
     */
    private final int departmentId;

    /**
     * The expected no. of days stay.
     */
    private final int expectedStay;

    /**
     * The treatment template.
     */
    private final String template;

    /**
     * Constructs a {@link FlowSheetInfo}.
     *
     * @param departmentId the department id
     * @param expectedStay the expected no. of days stay
     * @param template     the treatment template. May be {@code null}
     */
    public FlowSheetInfo(int departmentId, int expectedStay, String template) {
        this.departmentId = departmentId;
        this.expectedStay = expectedStay;
        this.template = template;
    }

    /**
     * Returns the selected department identifier.
     *
     * @return the selected department identifier, or {@code -1} if none is selected
     */
    public int getDepartmentId() {
        return departmentId;
    }


    /**
     * Returns the selected stay duration.
     *
     * @return the expected no. of days
     */
    public int getExpectedStay() {
        return expectedStay;
    }

    /**
     * Returns the selected treatment template.
     *
     * @return the selected treatment template. May be {@code null}
     */
    public String getTemplate() {
        return template;
    }

}
