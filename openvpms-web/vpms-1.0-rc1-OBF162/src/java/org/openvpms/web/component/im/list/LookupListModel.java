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

import nextapp.echo2.app.list.AbstractListModel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.util.FastLookupHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * List model for {@link Lookup}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupListModel extends AbstractListModel {

    /**
     * Dummy lookup indicating that all values apply.
     */
    public static final Lookup ALL = new Lookup(null, null, "all");

    /**
     * Dummy lookup indicating that no value is required.
     */
    public static final Lookup NONE = new Lookup(null, null, "none");

    /**
     * The lookups.
     */
    private List<Lookup> lookups;

    /**
     * Determines if "all" should be included.
     */
    private boolean all;

    /**
     * Determines if "none" should be included.
     */
    private boolean none;

    /**
     * The source object. May be <code>null</code>.
     */
    private IMObject object;

    /**
     * The lookup node descriptor. May be <code>null</code>.
     */
    private NodeDescriptor descriptor;


    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(LookupListModel.class);


    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param lookups the lookups to populate the list with.
     * @param all     if <code>true</code>, add a localised "All"
     */
    public LookupListModel(List<Lookup> lookups, boolean all) {
        this.all = all;
        this.lookups = getLookups(lookups);
    }

    /**
     * Construct a new <code>LookupListModel</code>. This may be refreshed.
     *
     * @param object     the source object
     * @param descriptor the lookup node descriptor
     */
    public LookupListModel(IMObject object, NodeDescriptor descriptor) {
        this.object = object;
        this.descriptor = descriptor;
        none = !descriptor.isRequired();
        lookups = getLookups();
    }

    /**
     * Returns the value at the specified index in the list.
     *
     * @param index the index
     * @return the lookup code
     */
    public Object get(int index) {
        return getLookup(index).getCode();
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    public int size() {
        return lookups.size();
    }

    /**
     * Returns the lookup at the specified index.
     *
     * @param index the index
     * @return the lookup
     */
    public Lookup getLookup(int index) {
        return lookups.get(index);
    }

    /**
     * Returns the index of the specified lookup.
     *
     * @param lookup the lookup
     * @return the index of <code>lookup</code>, or <code>-1</code> if it
     *         doesn't exist
     */
    public int indexOf(String lookup) {
        int result = -1;
        for (int i = 0; i < lookups.size(); ++i) {
            if (StringUtils.equals(lookup, lookups.get(i).getCode())) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Refreshes the model, if needed.
     */
    public void refresh() {
        if (object != null) {
            List<Lookup> lookups = getLookups();
            if (!this.lookups.equals(lookups)) {
                this.lookups = lookups;
                int last = this.lookups.isEmpty() ? 0 : this.lookups.size() - 1;
                fireContentsChanged(0, last);
            }
        }
    }

    /**
     * Retrieves the lookups from the lookup service.
     *
     * @return a list of lookups
     */
    protected List<Lookup> getLookups() {
        try {
            List<Lookup> lookups = FastLookupHelper.getLookups(descriptor,
                                                               object);
            return getLookups(lookups);
        } catch (OpenVPMSException exception) {
            log.error(exception, exception);
            return new ArrayList<Lookup>();
        }
    }

    /**
     * Helper to prepend the lookups "all" or "none" when required.
     *
     * @param lookups the lookups
     * @return a copy of <code>lookups</code> preprended with "all" and/or
     *         "none" added when required
     */
    protected List<Lookup> getLookups(List<Lookup> lookups) {
        if (all || none) {
            lookups = new ArrayList<Lookup>(lookups);
            if (all) {
                lookups.add(0, ALL);
            }
            if (none) {
                lookups.add(0, NONE);
            }
        }
        return lookups;
    }

}
