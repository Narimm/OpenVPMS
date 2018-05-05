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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.invoice;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Manages documents associated with a charge item.
 *
 * @author Tim Anderson
 */
public class ChargeItemDocumentLinker {

    /**
     * The charge item.
     */
    private final FinancialAct item;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link ChargeItemDocumentLinker}.
     *
     * @param item    the item
     * @param service the archetype service
     */
    public ChargeItemDocumentLinker(FinancialAct item, IArchetypeService service) {
        this.item = item;
        this.service = service;
    }

    /**
     * Creates or deletes acts related to the invoice item based on the document templates associated with the
     * charge item's product.
     * This:
     * <ol>
     * <li>gets all document templates associated with the product's {@code document} node</li>
     * <li>iterates through the acts associated with the invoice item's {@code document} node and:</li>
     * <ol>
     * <li>removes acts that don't have participation to any of the document templates</li>
     * <li>retains acts which have participations to the document templates</li>
     * </ol>
     * <li>creates acts for each document template that doesn't yet have an act</li>
     * </ol>
     * This is the same as invoking:
     * <pre>
     * prepare(changes);
     * changes.save();
     * </pre>
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void link() {
        PatientHistoryChanges changes = new PatientHistoryChanges(null, null, service);
        prepare(changes);
        changes.save();
    }

    /**
     * Prepares the charge item and documents for save.
     * <p/>
     * Invoke {@link PatientHistoryChanges#save()} to commit the changes.
     *
     * @param changes the patient history changes
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void prepare(PatientHistoryChanges changes) {
        // map of template references to their corresponding entity relationship, obtained from the product
        Map<IMObjectReference, EntityRelationship> productTemplates = new HashMap<>();

        // template references associated with the current document acts
        Set<Reference> templateRefs = new HashSet<>();

        // determine the templates associated with the item's product
        IMObjectBean itemBean = service.getBean(item);
        Product product = itemBean.getTarget("product", Product.class);
        if (product != null) {
            IMObjectBean productBean = service.getBean(product);
            if (productBean.hasNode("documents")) {
                for (EntityRelationship r : productBean.getValues("documents", EntityRelationship.class)) {
                    IMObjectReference target = r.getTarget();
                    if (target != null) {
                        productTemplates.put(target, r);
                    }
                }
            }
        }

        // get document acts associated with the item
        List<Act> documents = itemBean.getTargets("documents", Act.class);

        // for each document, determine if the product, patient, author or clinician has changed. If so, remove it
        for (Act document : documents.toArray(new Act[documents.size()])) {
            IMObjectBean bean = service.getBean(document);
            if (productChanged(bean, product) || patientChanged(bean, itemBean) || authorChanged(bean, itemBean)
                || clinicianChanged(bean, itemBean)) {
                changes.removeItemDocument(item, document);
                documents.remove(document);
            } else {
                Reference templateRef = bean.getTargetRef("documentTemplate");
                if (templateRef != null) {
                    templateRefs.add(templateRef);
                }
            }
        }

        // add any templates associated with the product where there is no corresponding act
        for (Map.Entry<IMObjectReference, EntityRelationship> entry : productTemplates.entrySet()) {
            IMObjectReference typeRef = entry.getKey();
            if (!templateRefs.contains(typeRef)) {
                Entity entity = (Entity) getObject(typeRef);
                if (entity != null) {
                    addDocument(itemBean, entity, changes);
                }
            }
        }
    }

    /**
     * Adds an <em>act.patientDocument*</em> to the invoice item.
     *
     * @param itemBean the invoice item
     * @param document the document template
     * @param changes  the patient history changes
     * @throws ArchetypeServiceException for any error
     */
    private void addDocument(IMObjectBean itemBean, Entity document, PatientHistoryChanges changes) {
        IMObjectBean bean = service.getBean(document);
        String archetype = bean.getString("archetype");
        if (StringUtils.isEmpty(archetype)) {
            archetype = PatientArchetypes.DOCUMENT_FORM;
        }
        if (TypeHelper.matches(archetype, "act.patientDocument*")) {
            Act act = (Act) service.create(archetype);
            if (act == null) {
                throw new IllegalStateException("Failed to create :" + archetype);
            }
            act.setActivityStartTime(((Act) itemBean.getObject()).getActivityStartTime());
            IMObjectBean documentAct = service.getBean(act);
            Reference patient = itemBean.getTargetRef("patient");
            documentAct.setTarget("patient", patient);
            documentAct.setTarget("documentTemplate", document);
            Reference author = itemBean.getTargetRef("author");
            if (author != null) {
                documentAct.setTarget("author", author);
            }
            Reference clinician = itemBean.getTargetRef("clinician");
            if (clinician != null) {
                documentAct.setTarget("clinician", clinician);
            }

            if (documentAct.isA(PatientArchetypes.DOCUMENT_FORM, PatientArchetypes.DOCUMENT_LETTER)) {
                Reference product = itemBean.getTargetRef("product");
                documentAct.setTarget("product", product);
            }
            changes.addItemDocument(item, act);
        }
    }

    /**
     * Determines if the product has changed.
     *
     * @param bean    the document act bean
     * @param product the item product
     * @return {@code true} if the product has changed
     */
    private boolean productChanged(IMObjectBean bean, Product product) {
        return bean.hasNode("product")
               && !ObjectUtils.equals(bean.getTargetRef("product"), product.getObjectReference());
    }

    /**
     * Determines if the patient has changed.
     *
     * @param docBean  the document act bean
     * @param itemBean the item bean
     * @return {@code true} if the patient has changed
     */
    private boolean patientChanged(IMObjectBean docBean, IMObjectBean itemBean) {
        return checkParticipant(docBean, itemBean, "patient");
    }

    /**
     * Determines if the author has changed.
     *
     * @param docBean  the document act bean
     * @param itemBean the item bean
     * @return {@code true} if the author has changed
     */
    private boolean authorChanged(IMObjectBean docBean, IMObjectBean itemBean) {
        return checkParticipant(docBean, itemBean, "author");
    }

    /**
     * Determines if the clinician has changed.
     *
     * @param docBean  the document act bean
     * @param itemBean the item bean
     * @return {@code true} if the clinician has changed
     */
    private boolean clinicianChanged(IMObjectBean docBean, IMObjectBean itemBean) {
        return checkParticipant(docBean, itemBean, "clinician");
    }

    /**
     * Determines if a participant has changed.
     *
     * @param docBean  the document act bean
     * @param itemBean the item bean
     * @param node     the participant node to check
     * @return {@code true} if the participant has changed
     */
    private boolean checkParticipant(IMObjectBean docBean, IMObjectBean itemBean, String node) {
        return docBean.hasNode(node) && !ObjectUtils.equals(docBean.getTargetRef(node), itemBean.getTargetRef(node));
    }

    /**
     * Helper to retrieve an object given its reference.
     *
     * @param ref the reference
     * @return the object corresponding to the reference, or {@code null}if it can't be retrieved
     * @throws ArchetypeServiceException for any error
     */
    private IMObject getObject(IMObjectReference ref) {
        return (ref != null) ? service.get(ref) : null;
    }

}
