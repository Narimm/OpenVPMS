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
package org.openvpms.component.business.service.archetype.assertion;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ProperCaseRules {

    /**
     * Returns a list of strings that force capitalisation of the next character when they are encountered at the start 
     * of a word.
     *
     * @return a list of strings
     */
    String[] getStartsWith();

    /**
     * Returns a list of strings that force capitalisation of the next character when they are encountered within a
     * word.
     *
     * @return a list of strings
     */
    String[] getContains();

    /**
     * Returns a list of strings that must appear with the specified case at the end of a word.
     *
     * @return a list of strings
     */
    String[] getEndsWith();
    
    /**
     * Returns a list of strings that are exceptions to the above rules.
     *
     * @return a list of strings that should appear as is
     */
    String[] getExceptions();

    /**
     * Returns the version of the case rules.
     * <p/>
     * These can be used to detect when the rules change.
     *
     * @return the version
     */
    int getVersion();
}
