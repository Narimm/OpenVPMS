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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Implementation of the <code>JRDataSource</code> interface, for a single
 * <code>IMObject</code>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectDataSource extends AbstractIMObjectDataSource {

    /**
     * The source object.
     */
    private final IMObject _object;

    /**
     * The archetype descriptor.
     */
    private final ArchetypeDescriptor _archetype;

    /**
     * Determines if there is another record.
     */
    private boolean _next = true;


    /**
     * Construct a new <code>IMObjectDataSource</code>.
     *
     * @param object  the source object
     * @param service the archetype service
     */
    public IMObjectDataSource(IMObject object, IArchetypeService service) {
        super(service);
        _object = object;
        _archetype = service.getArchetypeDescriptor(object.getArchetypeId());
    }

    /**
     * Tries to position the cursor on the next element in the data source.
     *
     * @return true if there is a next record, false otherwise
     * @throws JRException if any error occurs while trying to move to the next
     *                     element
     */
    public boolean next() throws JRException {
        boolean result = _next;
        _next = false;
        return result;
    }

    /**
     * Returns a data source for a collection node.
     *
     * @param name the collection node name
     * @throws JRException for any error
     */
    public JRDataSource getDataSource(String name) throws JRException {
        NodeDescriptor descriptor = _archetype.getNodeDescriptor(name);
        if (descriptor == null) {
            throw new JRException("No node found for field=" + name);
        }
        return new IMObjectCollectionDataSource(_object, descriptor,
                                                getArchetypeService());
    }

    /**
     * Gets the field value for the current position.
     *
     * @return an object containing the field value.
     *         The object type must be the field object type.
     * @throws JRException for any error
     */
    public Object getFieldValue(JRField field) throws JRException {
        Object result;
        String name = field.getName();
        NodeDescriptor node = _archetype.getNodeDescriptor(name);
        if (name.equals("displayName") && node == null) {
            result = _archetype.getDisplayName();
        } else {
            if (node == null) {
                throw new JRException("No node found for field=" + field);
            }
            result = node.getValue(_object);
        }
        return result;
    }


}
