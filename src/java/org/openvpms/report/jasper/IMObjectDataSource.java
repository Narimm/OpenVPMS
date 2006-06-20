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
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
     * The archetype service.
     */
    private final IArchetypeService _service;

    /**
     * The archetype descriptor.
     */
    private final ArchetypeDescriptor _archetype;

    /**
     * Determines if there is another record.
     */
    private boolean _next = true;

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(IMObjectDataSource.class);


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
        _service = service;
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
        Object result = null;
        String name = field.getName();
        int index;
        IMObject object = _object;
        ArchetypeDescriptor archetype = _archetype;
        while ((index = name.indexOf(".")) != -1) {
            String nodeName = name.substring(0, index);
            NodeDescriptor node = archetype.getNodeDescriptor(nodeName);
            if (node.isObjectReference()) {
                object = getObject(object, node);
            } else {
                throw new JRException(
                        "Field doesn't refer to an object refenerence: "
                                + field);
            }
            name = name.substring(index + 1);
            if (object != null) {
                archetype = _service.getArchetypeDescriptor(
                        object.getArchetypeId());
            }
        }
        NodeDescriptor node = archetype.getNodeDescriptor(name);
        if (name.equals("displayName") && node == null) {
            result = archetype.getDisplayName();
        } else {
            if (node == null) {
                throw new JRException("No node found for field=" + field);
            }
            if (node.isObjectReference()) {
                object = getObject(object, node);
                if (object != null) {
                    result = object.getName();
                }
            } else if (node.isCollection()) {
                StringBuffer descriptions = new StringBuffer();
                for (IMObject value : node.getChildren(object)) {
                    descriptions.append(value.getName());
                    descriptions.append('\n');
                }
                result = descriptions.toString();
            } else {
                result = node.getValue(object);
            }
        }
        return result;
    }

    /**
     * Resolve a reference.
     *
     * @param parent     the parent object
     * @param descriptor the reference descriptor
     */
    protected IMObject getObject(IMObject parent, NodeDescriptor descriptor) {
        IMObjectReference ref = (IMObjectReference) descriptor.getValue(parent);
        if (ref != null) {
            try {
                return ArchetypeQueryHelper.getByObjectReference(_service, ref);
            } catch (OpenVPMSException exception) {
                _log.warn(exception, exception);
            }
        }
        return null;
    }

}
