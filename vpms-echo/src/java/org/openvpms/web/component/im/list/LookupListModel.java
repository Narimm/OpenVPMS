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

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.list.AbstractListModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.lookup.ILookupService;


/**
 * List model for {@link Lookup}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupListModel extends AbstractListModel {

    /**
     * The lookups.
     */
    private List<Lookup> _lookups;

    /**
     * Dummy short name indicating that all values apply.
     */
    public static final String ALL = "all";

    /**
     * Dummy short name indicating that no value is required.
     */
    public static final String NONE = "none";

    /**
     * Determines if "all" should be included.
     */
    private boolean _all;

    /**
     * Determines if "none" should be included.
     */
    private boolean _none;

    /**
     * The source object. May be <code>null</code>.
     */
    private IMObject _object;

    /**
     * The lookup node descriptor. May be <code>null</code>.
     */
    private NodeDescriptor _descriptor;

    /**
     * The lookup service. May be <code>null</code>.
     */
    private ILookupService _service;


    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param lookups the lookups to populate the list with.
     * @param all     if <code>true</code>, add a localised "All"
     */
    public LookupListModel(List<Lookup> lookups, boolean all) {
        _all = all;
        _lookups = getLookups(lookups);
    }

    /**
     * Construct a new <code>LookupListModel</code>. This may be refreshed.
     *
     * @param object     the source object
     * @param descriptor the lookup node descriptor
     * @param service    the lookup service
     */
    public LookupListModel(IMObject object, NodeDescriptor descriptor,
                           ILookupService service) {
        _object = object;
        _descriptor = descriptor;
        _service = service;
        _none = !descriptor.isRequired();
        _lookups = getLookups();
    }

    /**
     * Returns the value at the specified index in the list.
     *
     * @param index the index
     * @return the value
     */
    public Object get(int index) {
        return _lookups.get(index).getValue();
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    public int size() {
        return _lookups.size();
    }

    /**
     * Returns the lookup at the specified index.
     *
     * @param index gthe index
     * @return the lookup
     */
    public Lookup getLookup(int index) {
        return _lookups.get(index);
    }

    /**
     * Refreshes the model, if needed.
     */
    public void refresh() {
        if (_object != null) {
            List<Lookup> lookups = getLookups();
            if (!_lookups.equals(lookups)) {
                _lookups = lookups;
                int last = _lookups.isEmpty() ? 0 : _lookups.size() - 1;
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
        return getLookups(_service.get(_descriptor, _object));
    }

    /**
     * Helper to prepend the lookups "all" or "none" when required.
     *
     * @param lookups the lookups
     * @return a copy of <code>lookups</code> preprended with "all" and/or
     *         "none" added when required
     */
    protected List<Lookup> getLookups(List<Lookup> lookups) {
        if (_all || _none) {
            lookups = new ArrayList<Lookup>(lookups);
            if (_all) {
                lookups.add(0, new Lookup(null, null, ALL));
            }
            if (_none) {
                lookups.add(0, new Lookup(null, null, NONE));
            }
        }
        return lookups;
    }

}
