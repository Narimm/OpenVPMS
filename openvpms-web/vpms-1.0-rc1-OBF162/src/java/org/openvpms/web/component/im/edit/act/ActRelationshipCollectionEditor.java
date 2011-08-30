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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Editor for collections of {@link ActRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class ActRelationshipCollectionEditor
        extends IMObjectTableCollectionEditor {


    /**
     * Construct a new <code>ActRelationshipCollectionEditor</code>.
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
        List<Act> result = new ArrayList<Act>();
        for (IMObject object : collection.getObjects()) {
            result.add((Act) object);
        }
        return result;
    }

    /**
     * Returns an editor for an object, creating one if it doesn't exist.
     *
     * @param object the object to edit
     * @return an editor for the object
     */
    @Override
    public IMObjectEditor getEditor(IMObject object) {
        return super.getEditor(object);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit <code>object</code>
     */
    @Override
    public IMObjectEditor createEditor(IMObject object,
                                       LayoutContext context) {
        return super.createEditor(object, context);
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
            IMObjectReference product = ((ActItemEditor) editor).getProduct();
            if (TypeHelper.isA(product, "product.template")) {
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
     * Refreshes the table.
     */
    public void refresh() {
        populateTable();
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
                "participation.product", act.getParticipations());
        if (participant != null) {
            IMObjectReference ref = participant.getEntity();
            if (TypeHelper.isA(ref, "product.template")) {
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
            // templated product
            for (IMObject value : values) {
                EntityRelationship relationship = (EntityRelationship) value;
                IMObjectReference product = relationship.getTarget();

                if (copy == null) {
                    // copy the act, and associate the product
                    copy = (Act) copier.copy(act);
                    LayoutContext context = new DefaultLayoutContext();
                    context.setComponentFactory(
                            new ReadOnlyComponentFactory(context));
                    editor = (ActItemEditor) createEditor(copy, context);

                }
                IMObjectBean relationshipBean = new IMObjectBean(relationship);
                if (relationshipBean.hasNode("includeQty")) {
                    BigDecimal quantity = relationshipBean.getBigDecimal(
                            "includeQty");
                    if (quantity != null) {
                        editor.setQuantity(quantity);
                    }
                }

                editor.setProduct(product);

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
        IMObjectReference product = editor.getProduct();
        return TypeHelper.isA(product, "product.template");
    }

    private class ActItemCopyHandler extends ActCopyHandler {

        /**
         * Determines how {@link IMObjectCopier} should treat an object.
         *
         * @param object  the source object
         * @param service the archetype service
         * @return <code>object</code> if the object shouldn't be copied,
         *         <code>null</code> if it should be replaced with
         *         <code>null</code>, or a new instance if the object should be
         *         copied
         */
        public IMObject getObject(IMObject object, IArchetypeService service) {
            IMObject result;
            if (object instanceof Participation) {
                Participation participant = (Participation) object;
                if (TypeHelper.isA(participant.getEntity(),
                                   "product.template")) {
                    result = null;
                } else {
                    result = super.getObject(object, service);
                }
            } else {
                result = super.getObject(object, service);
            }
            return result;
        }
    }
}
