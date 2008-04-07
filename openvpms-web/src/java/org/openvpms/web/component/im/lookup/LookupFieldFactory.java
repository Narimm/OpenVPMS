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

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.util.ComponentFactory;

import java.util.List;


/**
 * Factory for {@link LookupField}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupFieldFactory extends ComponentFactory {

    /**
     * Creates a new <tt>LookupField</tt>, selecting the first lookup.
     *
     * @param lookups the lookups
     */
    public static LookupField create(List<Lookup> lookups) {
        return create(lookups, false);
    }

    /**
     * Creates a new <tt>LookupField</tt>, selecting the first lookup.
     *
     * @param lookups the lookups
     * @param all     if <tt>true</tt>, add a localised "All"
     */
    public static LookupField create(List<Lookup> lookups, boolean all) {
        return create(new ListLookupQuery(lookups), all);
    }

    /**
     * Creates a new <tt>LookupField</tt>, selecting the first lookup.
     *
     * @param source the lookup source
     */
    public static LookupField create(LookupQuery source) {
        return create(source, false);
    }

    /**
     * Creates a new <tt>LookupField</tt>, selecting the first lookup.
     *
     * @param source the lookup source
     * @param all    if <tt>true</tt>, add a localised "All"
     */
    public static LookupField create(LookupQuery source, boolean all) {
        LookupField field = new LookupField(source, all);
        setDefaultStyle(field);
        if (field.getModel().size() != 0) {
            field.setSelectedIndex(0);
        }
        return field;
    }
}
