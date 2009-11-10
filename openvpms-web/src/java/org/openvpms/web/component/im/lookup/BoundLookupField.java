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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.Binder;
import org.openvpms.web.component.bound.SelectFieldBinder;
import org.openvpms.web.component.property.Property;


/**
 * Binds a lookup {@link Property} to a {@link LookupField}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BoundLookupField extends LookupField {

    /**
     * The binder.
     */
    private Binder binder;


    /**
     * Creates a new <tt>BoundLookupField</tt>.
     *
     * @param property the property to bind
     * @param parent   the parent object
     */
    public BoundLookupField(Property property, IMObject parent) {
        this(property, new NodeLookupQuery(parent, property.getDescriptor()));
    }

    /**
     * Creates a new <tt>BoundLookupField</tt>.
     *
     * @param property the property to bind
     * @param source   the source of the lookups to display
     */
    public BoundLookupField(Property property, LookupQuery source) {
        this(property, source, false);
    }

    /**
     * Creates a new <tt>BoundLookupField</tt>.
     *
     * @param property the property to bind
     * @param source   the source of the lookups to display
     * @param all      if <tt>true</tt>, add a localised "All"
     */
    public BoundLookupField(Property property, LookupQuery source, boolean all) {
        super(source, all, !property.isRequired());
        binder = new SelectFieldBinder(this, property);
        binder.setField();
        if (!StringUtils.isEmpty(property.getDescription())) {
            setToolTipText(property.getDescription());
        }
        if (getSelected() == null) {
            setDefaultSelection();
        }
    }

    /**
     * Refreshes the model if required.
     * <p/>
     * If the model refreshes, the selection will be cleared.
     *
     * @return <tt>true</tt> if the model refreshed
     */
    @Override
    public boolean refresh() {
        boolean result = super.refresh();
        if (result) {
            binder.setProperty();
        }
        return result;
    }

    /**
     * Life-cycle method invoked when the <tt>Component</tt> is removed from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        binder.dispose();
    }
}
