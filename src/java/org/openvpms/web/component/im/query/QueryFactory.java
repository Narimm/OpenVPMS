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

package org.openvpms.web.component.im.query;

import org.apache.commons.lang.StringUtils;


/**
 * A factory for {@link Query} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class QueryFactory {

    /**
     * Prevent construction.
     */
    private QueryFactory() {
    }

    /**
     * Construct a new {@link Query}.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @return a new query
     */
    public static Query create(String refModelName, String entityName,
                               String conceptName) {
        Query result;
        if ("party".equals(refModelName) && "party".equals(entityName) &&
            StringUtils.indexOf(conceptName, "patient") == 0) {
            result = new PatientQuery(refModelName, entityName, conceptName);
        } else {
            result = new DefaultQuery(refModelName, entityName, conceptName);
        }
        return result;
    }

    /**
     * Construct a new {@link Query}.
     *
     * @param shortNames the archetype short names to query one.
     * @return a new query
     */
    public static Query create(String[] shortNames) {
        Query result;
        if (isPatientQuery(shortNames)) {
            result = new PatientQuery(shortNames);
        } else {
            result = new DefaultQuery(shortNames);
        }
        return result;
    }

    /**
     * Determines if the short names represent a patient query.
     *
     * @param shortNames the short names
     * @return <code>true</code> if the short names represent a patient query;
     *         otherwise <code>false</code>
     */
    private static boolean isPatientQuery(String[] shortNames) {
        boolean patientQuery = true;
        for (String shortName : shortNames) {
            if (!shortName.startsWith("party.patient")) {
                patientQuery = false;
                break;
            }
        }
        return patientQuery;
    }
}
