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
import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.report.ExpressionEvaluator;
import org.openvpms.report.IMObjectExpressionEvaluator;

import java.util.Iterator;


/**
 * Implementation of the <tt>JRDataSource</tt> interface, for a single
 * <tt>IMObject</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectDataSource extends AbstractIMObjectDataSource {

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
     * Construct a new <tt>IMObjectDataSource</tt>.
     *
     * @param object   the source object
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public IMObjectDataSource(IMObject object, IArchetypeService service,
                              DocumentHandlers handlers) {
        super(service, handlers);
        this.object = object;
        resolver = new NodeResolver(object, service);
        evaluator = new IMObjectExpressionEvaluator(object, resolver);
        this.handlers = handlers;
    }

    /**
     * Tries to position the cursor on the next element in the data source.
     *
     * @return true if there is a next record, false otherwise
     * @throws JRException if any error occurs while trying to move to the next
     *                     element
     */
    public boolean next() throws JRException {
        boolean result = next;
        next = false;
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
        ArchetypeDescriptor archetype = resolver.getArchetype();
        NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
        if (descriptor == null) {
            throw new JRException("No node found for field=" + name);
        }
        return new IMObjectCollectionDataSource(
                object, descriptor, getArchetypeService(),
                getDocumentHandlers(), sortNodes);
    }

    /**
     * Returns a data source for the given jxpath expression.
     *
     * @param expression the expression. Must return an <tt>Iterable<tt> or <tt>Iterator</tt> returning
     *                   <tt>IMObjects</tt>
     * @return the data source
     * @throws JRException for any error
     */
    @Override
    @SuppressWarnings("unchecked")
    public JRDataSource getExpressionDataSource(String expression) throws JRException {
        JXPathContext context = JXPathHelper.newContext(object);
        Object value = context.getValue(expression);
        Iterator<IMObject> iterator;
        if (value instanceof Iterable) {
            Iterable<IMObject> iterable = (Iterable<IMObject>) value;
            iterator = iterable.iterator();
        } else if (value instanceof Iterator) {
            iterator = (Iterator<IMObject>) value;
        } else {
            throw new JRException("Unsupported value type=" + ((value != null) ? value.getClass() : null)
                                  + " returned by expression=" + expression);
        }
        return new IMObjectCollectionDataSource(iterator, getArchetypeService(), getDocumentHandlers());
    }

    /**
     * Gets the field value for the current position.
     *
     * @return an object containing the field value.
     *         The object type must be the field object type.
     * @throws JRException for any error
     */
    public Object getFieldValue(JRField field) throws JRException {
        Object value = evaluator.getValue(field.getName());
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

}
