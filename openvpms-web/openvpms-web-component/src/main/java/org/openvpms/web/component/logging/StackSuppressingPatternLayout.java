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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.logging;

import org.apache.log4j.PatternLayout;


/**
 * A <tt>PatternLayout</tt> that suppresses stack traces.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StackSuppressingPatternLayout extends PatternLayout {

    /**
     * Returns <tt>false</tt> to indicate that this layout handles stack traces.
     * This effectively suppresses printing of the stack trace.
     *
     * @return <tt>false</tt>
     */
    @Override
    public boolean ignoresThrowable() {
        return false;
    }
}
