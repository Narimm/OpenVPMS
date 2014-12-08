/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.ReportException;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * Implementation of the {@code JRDataSource} interface, for collections of {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public class IMObjectCollectionDataSource extends AbstractIMObjectDataSource {

    /**
     * The collection iterator.
     */
    private final Iterator<IMObject> iter;

    /**
     * Additional fields. May be {@code null}
     */
    private final PropertySet fields;

    /**
     * The current object.
     */
    private IMObjectDataSource current;

    /**
     * Display name for this collection. May be {@code null}.
     */
    private String displayName;


    /**
     * Constructs a {@link IMObjectCollectionDataSource} for a collection node.
     *
     * @param parent     the parent object
     * @param fields     additional report fields. These override any in the report. May be {@code null}
     * @param descriptor the collection descriptor
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param handlers   the document handlers
     * @param sortNodes  the sort nodes
     */
    public IMObjectCollectionDataSource(IMObject parent, PropertySet fields, NodeDescriptor descriptor,
                                        IArchetypeService service, ILookupService lookups, DocumentHandlers handlers,
                                        String... sortNodes) {
        super(service, lookups, handlers);
        List<IMObject> values = descriptor.getChildren(parent);
        for (String sortNode : sortNodes) {
            sort(values, sortNode);
        }
        iter = values.iterator();
        this.fields = fields;
        displayName = descriptor.getDisplayName();
    }

    /**
     * Constructs a {@link IMObjectCollectionDataSource} for a collection of objects.
     *
     * @param objects  the objects
     * @param fields   additional report fields. These override any in the report. May be {@code null}
     * @param service  the archetype service
     * @param lookups  the lookup service
     * @param handlers the document handlers
     */
    public IMObjectCollectionDataSource(Iterator<IMObject> objects, PropertySet fields, IArchetypeService service,
                                        ILookupService lookups, DocumentHandlers handlers) {
        super(service, lookups, handlers);
        iter = objects;
        this.fields = fields;
    }

    /**
     * Tries to position the cursor on the next element in the data source.
     *
     * @return true if there is a next record, false otherwise
     */
    public boolean next() {
        boolean result = iter.hasNext();
        if (result) {
            current = new IMObjectDataSource(iter.next(), fields, getArchetypeService(), getLookupService(),
                                             getDocumentHandlers());
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
    public JRDataSource getDataSource(String name, String[] sortNodes) throws JRException {
        return current.getDataSource(name, sortNodes);
    }

    /**
     * Returns a data source for the given jxpath expression.
     *
     * @param expression the expression. Must return an {@code Iterable} or {@code Iterator} returning {@code IMObjects}
     * @return the data source
     * @throws JRException for any error
     */
    @Override
    public JRDataSource getExpressionDataSource(String expression) throws JRException {
        return current.getExpressionDataSource(expression);
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
            if (field.getName().equals("collectionDisplayName")) {
                result = displayName;
            } else {
                result = current.getFieldValue(field);
            }
        }
        return result;
    }

    /**
     * Sorts a list of IMObjects on a node in the form specified by {@link NodeResolver}.
     *
     * @param objects  the objects to sort
     * @param sortNode the node to sort on
     */
    @SuppressWarnings("unchecked")
    private void sort(List<IMObject> objects, String sortNode) {
        Comparator comparator = ComparatorUtils.naturalComparator();
        comparator = ComparatorUtils.nullLowComparator(comparator);

        Transformer transformer = new NodeTransformer(sortNode, getArchetypeService());
        TransformingComparator transComparator = new TransformingComparator(transformer, comparator);
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
         * Constructs a {@code NodeTransformer}.
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
            } catch (ReportException ignore) {
                // node node found
                result = null;
            }
            return result;
        }

    }

}
