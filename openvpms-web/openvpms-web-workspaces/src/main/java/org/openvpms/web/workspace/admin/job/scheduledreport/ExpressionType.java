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

package org.openvpms.web.workspace.admin.job.scheduledreport;

/**
 * Scheduled report expression type.
 *
 * @author Tim Anderson
 */
public enum ExpressionType {
    VALUE,               // a static value
    NOW,                 // the current date/time
    TODAY,               // the current date
    YESTERDAY,           // yesterday's date
    TOMORROW,            // tomorrow's date
    START_OF_MONTH,      // the first day of the current month
    END_OF_MONTH,        // the last day of the current month
    START_OF_LAST_MONTH, // the first day of the previous month
    END_OF_LAST_MONTH,   // the last day of the previous month
    START_OF_NEXT_MONTH, // the first day of the next month
    END_OF_NEXT_MONTH,   // the last day of next month
    START_OF_YEAR,       // the first day of the current year
    END_OF_YEAR,         // the last day of current year
    START_OF_LAST_YEAR, // the first day of the previous year
    END_OF_LAST_YEAR,    // the end of the previous year
    START_OF_NEXT_YEAR,  // the first day of next year
    END_OF_NEXT_YEAR,    // the last day of next year
    JUNE_30,             // June 30th of the current year
    LAST_JUNE_30,        // June 30th of the previous year
    NEXT_JUNE_30,        // June 30th of the next year
    JULY_1,              // July 1st of the current year
    LAST_JULY_1,         // July 1st of the previous year
    NEXT_JULY_1,         // July 1st of the next year
}
