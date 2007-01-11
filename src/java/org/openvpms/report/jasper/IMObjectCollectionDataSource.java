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
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.report.IMReportException;

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
    private Iterator<IMObject> iter;

    /**
     * The current object.
     */
    private IMObjectDataSource current;

    /**
     * Display name for this collection. May be <code>null</code>
     */
    private String displayName;


    /**
     * Construct a new <code>IMObjectCollectionDataSource</code> for a
     * collection node.
     *
     * @param parent     the parent object
     * @param descriptor the collection desccriptor
     * @param sortNodes  the sort nodes
     */
    public IMObjectCollectionDataSource(IMObject parent,
                                        NodeDescriptor descriptor,
                                        IArchetypeService service,
                                        String ... sortNodes) {
        super(service);
        List<IMObject> values = descriptor.getChildren(parent);
        for (String sortNode : sortNodes) {
            sort(values, sortNode);
        }
        iter = values.iterator();
        displayName = descriptor.getDisplayName();
    }

    /**
     * Construct a new <code>IMObjectCollectionDataSource</code> for a
     * collection of objects.
     *
     * @param objects the objects
     * @param service the archetype service
     */
    public IMObjectCollectionDataSource(Iterator<IMObject> objects,
                                        IArchetypeService service) {
        super(service);
        iter = objects;
    }

    /**
     * Tries to position the cursor on the next element in the data source.
     *
     * @return true if there is a next record, false otherwise
     */
    public boolean next() {
        boolean result = iter.hasNext();
        if (result) {
            current = new IMObjectDataSource(iter.next(),
                                             getArchetypeService());
        }
        return result;
    }

    /**
     * Returns a data source for a collection node.
     *
     * @param name      the collection node name
     * @param sortNodes the list of nodes to sort on
     * @throws JRException for any error
     */
    public JRDataSource getDataSource(String name, String[] sortNodes)
            throws JRException {
        return current.getDataSource(name, sortNodes);
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
        if (current != null) {
            if (field.getName().equals("displayName")) {
                result = displayName;
            } else {
                result = current.getFieldValue(field);
            }
        }
        return result;
    }

    /**
     * Sorts a list of IMObjects on a node in the form specified by
     * {@link NodeResolver}.
     *
     * @param objects  the objects to sort
     * @param sortNode the node to sort on
     */
    private void sort(List<IMObject> objects, String sortNode) {
        Comparator comparator = ComparatorUtils.naturalComparator();
        comparator = ComparatorUtils.nullLowComparator(comparator);

        Transformer transformer
                = new NodeTransformer(sortNode, getArchetypeService());
        TransformingComparator transComparator
                = new TransformingComparator(transformer, comparator);
        Collections.sort(objects, transComparator);
    }

    private static class NodeTransformer implements Transformer {

        /**
         * The field name.
         */
        private final String name;

        /**
         * The archetype service.
         */
        private final IArchetypeService service;


        /**
         * Construct a new <code>NodeTransformer</code>.
         *
         * @param name    the field name
         * @param service the archetype service
         */
        public NodeTransformer(String name, IArchetypeService service) {
            this.name = name;
            this.service = service;
        }

        /**
         * Transforms the input object (leaving it unchanged) into some output
         * object.
         *
         * @param input the object to be transformed, should be left unchanged
         */
        public Object transform(Object input) {
            Object result;
            IMObject object = (IMObject) input;
            NodeResolver resolver = new NodeResolver(object, service);
            try {
                result = resolver.getObject(name);
                if (!(result instanceof Comparable)) {
                    // not comparable so null to avoid class cast exceptions
                    result = null;
                }
            } catch (IMReportException ignore) {
                // node node found
                result = null;
            }
            return result;
        }

    }

}
