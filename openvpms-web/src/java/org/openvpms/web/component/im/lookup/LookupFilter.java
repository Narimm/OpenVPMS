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

package org.openvpms.web.component.im.lookup;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.NotPredicate;
import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper to filter lookups returned by another {@link LookupQuery}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupFilter extends AbstractLookupQuery {

    /**
     * The underlying query.
     */
    private final LookupQuery query;

    /**
     * The codes to filter.
     */
    private String[] codes;

    /**
     * The predicate to filter lookups.
     */
    private Predicate predicate;


    /**
     * Creates a new <tt>LookupFilter</tt>.
     *
     * @param query   the source to filter from
     * @param include determines if the lookups should be included or excluded
     * @param codes   the codes to include or exclude
     */
    public LookupFilter(LookupQuery query, boolean include,
                        String ... codes) {
        this.query = query;
        if (include) {
            predicate = new Match();
        } else {
            predicate = new NotPredicate(new Match());
        }
        this.codes = codes;
    }

    /**
     * Sets the codes to filter.
     *
     * @param codes the codes to filter
     */
    public void setCodes(String ... codes) {
        this.codes = codes;
    }

    /**
     * Returns the codes being filtered.
     *
     * @return the codes being filtered
     */
    public String[] getCodes() {
        return codes;
    }

    /**
     * Returns the filtered lookups.
     *
     * @return the filtered lookups
     */
    public List<Lookup> getLookups() {
        List<Lookup> result = new ArrayList<Lookup>();
        CollectionUtils.select(query.getLookups(), predicate, result);
        return result;
    }

    /**
     * Predicate that evaluates true if a lookup matches one of the codes.
     */
    private class Match implements Predicate {
        public boolean evaluate(Object object) {
            Lookup lookup = (Lookup) object;
            for (String code : codes) {
                if (code.equals(lookup.getCode())) {
                    return true;
                }
            }
            return false;
        }
    }

}
