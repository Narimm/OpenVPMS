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

package org.openvpms.web.component.im.list;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.lookup.LookupQuery;

import java.util.ArrayList;
import java.util.List;


/**
 * List model for {@link Lookup}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupListModel extends AbstractIMObjectListModel<Lookup> {

    /**
     * The lookup source. May be <tt>null</tt>.
     */
    private LookupQuery source;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(LookupListModel.class);


    /**
     * Constructs a new <tt>LookupListModel</tt>.
     *
     * @param source the lookup source
     */
    public LookupListModel(LookupQuery source) {
        this(source, false);
    }


    /**
     * Constructs a new <tt>LookupListModel</tt>. This may be refreshed.
     *
     * @param source the lookup source
     * @param all    if <tt>true</tt>, add a localised "All"
     */
    public LookupListModel(LookupQuery source, boolean all) {
        this(source, all, false);
    }

    /**
     * Constructs a new <tt>LookupListModel</tt>. This may be refreshed.
     *
     * @param source the lookup source
     * @param all    if <tt>true</tt>, add a localised "All"
     * @param none   if <tt>true</tt>, add a localised "None"
     */
    public LookupListModel(LookupQuery source, boolean all, boolean none) {
        this.source = source;
        setObjects(getLookups(), all, none);
    }

    /**
     * Returns the value at the specified index in the list.
     *
     * @param index the index
     * @return the lookup code, or <tt>null</tt> if it represents 'All' or
     *         'None'
     */
    public Object get(int index) {
        Lookup lookup = getLookup(index);
        return (lookup != null) ? lookup.getCode() : null;
    }

    /**
     * Returns the lookup at the specified index.
     *
     * @param index the index
     * @return the lookup code, or <tt>null</tt> if it represents 'All' or
     *         'None'
     */
    public Lookup getLookup(int index) {
        return getObject(index);
    }

    /**
     * Returns the index of the specified lookup.
     *
     * @param lookup the lookup. May be <tt>null</tt>
     * @return the index of <tt>lookup</tt>, or <tt>-1</tt> if it
     *         doesn't exist
     */
    public int indexOf(String lookup) {
        int result = -1;
        List<Lookup> lookups = getObjects();
        for (int i = 0; i < lookups.size(); ++i) {
            Lookup other = lookups.get(i);
            if ((other != null && StringUtils.equals(lookup, other.getCode()))
                    || (other == null && lookup == null)) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Refreshes the model, if needed.
     *
     * @return <tt>true</tt> if the model was refreshed
     */
    public boolean refresh() {
        boolean refreshed = false;
        if (source != null) {
            List<Lookup> current = getCurrentLookups();
            List<Lookup> lookups = getLookups();
            if (!current.equals(lookups)) {
                boolean all = getAllIndex() != -1;
                boolean none = getNoneIndex() != -1;
                setObjects(lookups, all, none);
                refreshed = true;
            }
        }
        return refreshed;
    }

    /**
     * Returns the default lookup.
     *
     * @return the default lookup, or <tt>null</tt> if none is defined
     */
    public Lookup getDefaultLookup() {
        return source.getDefault();
    }

    /**
     * Retrieves the lookups from the lookup source.
     *
     * @return a list of lookups
     */
    private List<Lookup> getLookups() {
        try {
            return source.getLookups();
        } catch (OpenVPMSException exception) {
            log.error(exception, exception);
            return new ArrayList<Lookup>();
        }
    }

    /**
     * Returns a list of the current lookups.
     *
     * @return the current lookups, minus any nulls.
     */
    private List<Lookup> getCurrentLookups() {
        List<Lookup> result = new ArrayList<Lookup>();
        for (Lookup lookup : getObjects()) {
            if (lookup != null) {
                result.add(lookup);
            }
        }
        return result;
    }

}
