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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.archetype.rules.act.ActCopyHandler;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.*;
import static org.openvpms.archetype.rules.product.ProductArchetypes.PRODUCT_PARTICIPATION;
import static org.openvpms.archetype.rules.product.ProductArchetypes.TEMPLATE;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.MultipleRelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Editor for collections of {@link ActRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class ActRelationshipCollectionEditor
        extends MultipleRelationshipCollectionTargetEditor {


    /**
     * Construct a new <tt>ActRelationshipCollectionEditor</tt>.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public ActRelationshipCollectionEditor(CollectionProperty property,
                                           Act act, LayoutContext context) {
        super(new ActRelationshipCollectionPropertyEditor(property, act),
              act, context);
    }

    /**
     * Returns the set of acts being edited.
     *
     * @return the set of acts being edited.
     */
    public List<Act> getActs() {
        ActRelationshipCollectionPropertyEditor collection = getEditor();
        return new ArrayList<Act>(collection.getActs().keySet());
    }

    /**
     * Returns the set of acts being edited, including that of the
     * {@link #getCurrentEditor()}.
     *
     * @return the set of acts being edited
     */
    public List<Act> getCurrentActs() {
        ActRelationshipCollectionPropertyEditor collection = getEditor();
        Set<Act> result = new LinkedHashSet<Act>();
        for (IMObject object : collection.getObjects()) {
            result.add((Act) object);
        }
        IMObjectEditor current = getCurrentEditor();
        if (current != null) {
            result.add((Act) current.getObject());
        }
        return new ArrayList<Act>(result);
    }

    /**
     * Adds the object being edited to the collection, if it doesn't exist.
     *
     * @param editor the editor
     * @return <code>true</code> if the object was added, otherwise
     *         <code>false</code>
     */
    @Override
    public boolean addEdited(IMObjectEditor editor) {
        boolean result = false;
        Act act = (Act) editor.getObject();
        if (editor instanceof ActItemEditor
                && hasProductTemplate((ActItemEditor) editor)) {
            IMObjectReference product = ((ActItemEditor) editor).getProductRef();
            if (TypeHelper.isA(product, TEMPLATE)) {
                result = expandTemplate((ActItemEditor) editor, act, product);
                if (result) {
                    populateTable();
                    // template act is replaced with the first product in
                    // the template, so try and select it in the table
                    IMObject object = editor.getObject();
                    getTable().getTable().setSelected(object);
                }
            }
        } else {
            result = super.addEdited(editor);
        }
        return result;
    }

    /**
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    protected ActRelationshipCollectionPropertyEditor getEditor() {
        return (ActRelationshipCollectionPropertyEditor)
                getCollectionPropertyEditor();
    }

    /**
     * Determines if an act contains a product template.
     *
     * @param act the act
     * @return <code>true</code> if the act contains a product template,
     *         otherwise <code>false</code>
     */
    protected IMObjectReference getProductTemplate(Act act) {
        Participation participant = IMObjectHelper.getObject(
                PRODUCT_PARTICIPATION, act.getParticipations());
        if (participant != null) {
            IMObjectReference ref = participant.getEntity();
            if (TypeHelper.isA(ref, TEMPLATE)) {
                return ref;
            }
        }
        return null;
    }

    /**
     * Copies an act item for each product referred to in its template.
     *
     * @param editor      the editor
     * @param act         the act
     * @param templateRef a reference to the template
     * @return <code>true</code> if the template was expanded; otherwise
     *         <code>false</code>
     */
    protected boolean expandTemplate(ActItemEditor editor, Act act,
                                     IMObjectReference templateRef) {
        boolean result = false;
        IMObject template = IMObjectHelper.getObject(templateRef);
        if (template != null) {
            ActRelationshipCollectionPropertyEditor collection = getEditor();

            IMObjectCopier copier = new IMObjectCopier(
                    new ActItemCopyHandler());
            IMObjectBean bean = new IMObjectBean(template);
            List<IMObject> values = bean.getValues("includes");
            Act copy = act; // replace the existing act with the first
            Date startTime = act.getActivityStartTime();
            // templated product
            for (IMObject value : values) {
                EntityRelationship relationship = (EntityRelationship) value;
                IMObjectReference product = relationship.getTarget();

                if (copy == null) {
                    // copy the act, and associate the product
                    List<IMObject> objects = copier.apply(act);
                    copy = (Act) objects.get(0);
                    LayoutContext context = new DefaultLayoutContext();
                    context.setComponentFactory(
                            new ReadOnlyComponentFactory(context));
                    editor = (ActItemEditor) createEditor(copy, context);

                    // reset the start-time, which may have been set by
                    // the editor
                    copy.setActivityStartTime(startTime);
                }
                editor.setProductRef(product);

                IMObjectBean relationshipBean = new IMObjectBean(relationship);
                if (relationshipBean.hasNode("includeQty")) {
                    BigDecimal quantity = relationshipBean.getBigDecimal(
                            "includeQty");
                    if (quantity != null) {
                        editor.setQuantity(quantity);
                    }
                }

                collection.add(copy);
                collection.setEditor(copy, editor);
                copy = null;
                result = true;
            }
        }
        return result;
    }

    /**
     * Helper to determine if an editor has a template product that needs
     * expanding.
     *
     * @param editor the editor
     */
    private boolean hasProductTemplate(ActItemEditor editor) {
        IMObjectReference product = editor.getProductRef();
        return TypeHelper.isA(product, TEMPLATE);
    }

    private class ActItemCopyHandler extends ActCopyHandler {


        /**
         * Determines how {@link IMObjectCopier} should treat an object.
         *
         * @param object  the source object
         * @param service the archetype service
         * @return <tt>object</tt> if the object shouldn't be copied,
         *         <tt>null</tt> if it should be replaced with
         *         <tt>null</tt>, or a new instance if the object should be
         *         copied
         */
        public IMObject getObject(IMObject object, IArchetypeService service) {
            IMObject result;
            if (object instanceof Participation) {
                Participation participant = (Participation) object;
                if (TypeHelper.isA(participant.getEntity(), TEMPLATE)) {
                    result = null;
                } else {
                    result = super.getObject(object, service);
                }
            } else {
                result = super.getObject(object, service);
            }
            return result;
        }

        /**
         * Helper to determine if a node is copyable.
         * <p/>
         * For charge items, this only copies the <em>quantity</em>,
         * <em>patient</em>, <em>product</em>, <em>author</em> and
         * <em>clinician<em> nodes.
         *
         * @param archetype the archetype descriptor
         * @param node      the node descriptor
         * @param source    if <tt>true</tt> the node is the source; otherwise its
         *                  the target
         * @return <tt>true</tt> if the node is copyable; otherwise <tt>false</tt>
         */
        @Override
        protected boolean isCopyable(ArchetypeDescriptor archetype,
                                     NodeDescriptor node, boolean source) {
            boolean result = super.isCopyable(archetype, node, source);
            if (result && TypeHelper.isA(archetype, INVOICE_ITEM, CREDIT_ITEM,
                                         COUNTER_ITEM)) {
                String name = node.getName();
                result = "quantity".equals(name) || "patient".equals(name)
                        || "product".equals(name) || "author".equals(name)
                        || "clinician".equals(name);
            }
            return result;
        }
    }
}
