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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import org.apache.commons.jxpath.Functions;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.ResolvingPropertySet;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.ExpressionEvaluator;
import org.openvpms.report.IMObjectExpressionEvaluator;
import org.openvpms.report.Parameters;

import java.util.Map;


/**
 * Implementation of the {@code JRDataSource} interface, for a single {@link IMObject}.
 *
 * @author Tim Anderson
 */
public class IMObjectDataSource extends AbstractDataSource {

    /**
     * The source object.
     */
    private final IMObject object;

    /**
     * The node resolver.
     */
    private final NodeResolver resolver;

    /**
     * The expression evaluator.
     */
    private final ExpressionEvaluator evaluator;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Determines if there is another record.
     */
    private boolean next = true;

    /**
     * Constructs an {@link IMObjectDataSource}.
     *
     * @param object     the source object
     * @param parameters the report parameters
     * @param fields     a map of additional field names and their values, to pass to the report. May be {@code null}
     * @param service    the archetype service
     * @param handlers   the document handlers
     * @param functions  the JXPath extension functions
     */
    public IMObjectDataSource(IMObject object, Parameters parameters, Map<String, Object> fields,
                              IArchetypeService service, ILookupService lookups, DocumentHandlers handlers,
                              Functions functions) {
        this(object, parameters, fields != null ? new ResolvingPropertySet(fields, service, lookups) : null, service,
             lookups, handlers, functions);
    }

    /**
     * Constructs an {@link IMObjectDataSource}.
     *
     * @param object     the source object
     * @param parameters the report parameters
     * @param fields     fields to pass to the report. May be {@code null}
     * @param service    the archetype service
     * @param handlers   the document handlers
     * @param functions  the JXPath extension functions
     */
    protected IMObjectDataSource(IMObject object, Parameters parameters, PropertySet fields,
                                 IArchetypeService service, ILookupService lookups, DocumentHandlers handlers,
                                 Functions functions) {
        super(parameters, fields, service, lookups, handlers, functions);
        this.object = object;
        resolver = new NodeResolver(object, service, lookups);
        evaluator = new IMObjectExpressionEvaluator(object, resolver, parameters, fields, service, lookups, functions);
        this.handlers = handlers;
    }

    /**
     * Tries to position the cursor on the next element in the data source.
     *
     * @return true if there is a next record, false otherwise
     * @throws JRException if any error occurs while trying to move to the next element
     */
    public boolean next() throws JRException {
        boolean result = next;
        next = false;
        return result;
    }

    /**
     * Returns the field value.
     *
     * @param name the field name
     * @return the field value. May be {@code null}
     */
    @Override
    public Object getFieldValue(String name) {
        Object value = evaluator.getValue(name);
        if (value instanceof Document) {
            Document doc = (Document) value;
            if (doc.getContents() != null && doc.getContents().length != 0) {
                DocumentHandler handler = handlers.get(doc);
                value = handler.getContent(doc);
            } else {
                value = null;
            }
        }
        return value;
    }

    /**
     * Returns a data source for a collection node.
     *
     * @param name      the collection node name
     * @param sortNodes the list of nodes to sort on
     * @throws JRException for any error
     */
    public JRRewindableDataSource getDataSource(String name, String[] sortNodes) throws JRException {
        ArchetypeDescriptor archetype = resolver.getArchetype();
        NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
        if (descriptor == null) {
            throw new JRException("No node found for field=" + name);
        }
        return new IMObjectCollectionDataSource(object, getParameters(), getFields(), descriptor,
                                                getArchetypeService(), getLookupService(), getDocumentHandlers(),
                                                getFunctions(), sortNodes);
    }

    /**
     * Returns a data source for the given jxpath expression.
     *
     * @param expression the expression. Must return an {@code Iterable} or {@code Iterator} returning {@code IMObjects}
     * @return the data source
     * @throws JRException for any error
     */
    @Override
    @SuppressWarnings("unchecked")
    public JRRewindableDataSource getExpressionDataSource(String expression) throws JRException {
        return getExpressionDataSource(object, expression);
    }

    /**
     * Gets the field value for the current position.
     *
     * @param field the field
     * @return an object containing the field value. The object type must be the field object type.
     */
    public Object getFieldValue(JRField field) {
        return getFieldValue(field.getName());
    }

    /**
     * Evaluates an xpath expression.
     *
     * @param expression the expression
     * @return the result of the expression. May be {@code null}
     */
    public Object evaluate(String expression) {
        return evaluator.evaluate(expression);
    }

    /**
     * Evaluates an xpath expression against an object.
     *
     * @param object     the object
     * @param expression the expression
     * @return the result of the expression. May be {@code null}
     */
    @Override
    public Object evaluate(Object object, String expression) {
        return evaluator.evaluate(object, expression);
    }
}
