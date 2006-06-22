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
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Collections;
import java.util.Comparator;
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
     * @param sortNode   the sort node. May be <code>null</code>
     */
    public IMObjectCollectionDataSource(IMObject parent,
                                        NodeDescriptor descriptor,
                                        IArchetypeService service,
                                        String sortNode) {
        super(service);
        List<IMObject> values = descriptor.getChildren(parent);
        if (sortNode != null) {
            sort(values, sortNode);
        }
        _iter = values.iterator();
        _descriptor = descriptor;
    }

    private void sort(List<IMObject> objects, String sortNode) {
        Comparator comparator = ComparatorUtils.naturalComparator();
        comparator = ComparatorUtils.nullLowComparator(comparator);

        Transformer transformer
                = new NodeTransformer(sortNode, getArchetypeService());
        TransformingComparator transComparator
                = new TransformingComparator(transformer, comparator);
        Collections.sort(objects, transComparator);
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

    private static class NodeTransformer implements Transformer {

        /**
         * The field name.
         */
        private final String _name;

        /**
         * The archetype service.
         */
        private final IArchetypeService _service;


        /**
         * Construct a new <code>NodeTransformer</code>.
         *
         * @param name    the field name
         * @param service the archetype service
         */
        public NodeTransformer(String name, IArchetypeService service) {
            _name = name;
            _service = service;
        }

        /**
         * Transforms the input object (leaving it unchanged) into some output
         * object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         * @throws ClassCastException       (runtime) if the input is the wrong
         *                                  class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws FunctorException         (runtime) if the transform cannot be
         *                                  completed
         */
        public Object transform(Object input) {
            Object result;
            IMObject object = (IMObject) input;
            NodeResolver resolver = new NodeResolver(object, _service);
            try {
                result = resolver.getObject(_name);
            } catch (JRException exception) {
                throw new FunctorException(exception);
            }
            if (!(result instanceof Comparable)) {
                // not comparable so null to avoid class cast exceptions
                result = null;
            }
            return result;
        }

    }


}
