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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Iterator;
import java.util.List;


/**
 * Implementation of the <code>JRDataSource</code> interface, for collections
 * of <code>IMObject</code>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectCollectionDataSource extends AbstractIMObjectDataSource {

    /**
     * The collection iterator.
     */
    private Iterator<IMObject> _iter;

    /**
     * The collection descriptor.
     */
    private final NodeDescriptor _descriptor;

    /**
     * The current object.
     */
    private IMObjectDataSource _current;


    /**
     * Construct a new <code>IMObjectCollectionDataSource</code>.
     *
     * @param parent     the parent objecft
     * @param descriptor the collection desccriptor
     */
    public IMObjectCollectionDataSource(IMObject parent,
                                        NodeDescriptor descriptor,
                                        IArchetypeService service) {
        super(service);
        List<IMObject> values = descriptor.getChildren(parent);
        _iter = values.iterator();
        _descriptor = descriptor;
    }

    /**
     * Tries to position the cursor on the next element in the data source.
     *
     * @return true if there is a next record, false otherwise
     */
    public boolean next() {
        boolean result = _iter.hasNext();
        if (result) {
            _current = new IMObjectDataSource(_iter.next(),
                                              getArchetypeService());
        }
        return result;
    }

    /**
     * Gets the field value for the current position.
     *
     * @return an object containing the field value. The object type must be the
     *         field object type.
     * @throws JRException for any error
     */
    public Object getFieldValue(JRField field) throws JRException {
        Object result = null;
        if (_current != null) {
            if (field.getName().equals("displayName")) {
                result = _descriptor.getDisplayName();
            } else {
                result = _current.getFieldValue(field);
            }
        }
        return result;
    }

}
