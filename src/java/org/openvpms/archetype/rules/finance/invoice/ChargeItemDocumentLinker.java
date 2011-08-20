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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.archetype.rules.finance.invoice;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Manages documents associated with a charge item.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
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
     * The acts to remove.
     */
    private List<Act> toRemove = new ArrayList<Act>();

    /**
     * The acts to save.
     */
    private List<Act> toSave = new ArrayList<Act>();


    /**
     * Constructs a <tt>ChargeItemDocumentLinker</tt>.
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
     * <li>gets all document templates associated with the product's <tt>document</tt> node</li>
     * <li>iterates through the acts associated with the invoice item's <tt>document</tt> node and:</li>
     * <ol>
     * <li>removes acts that don't have participation to any of the document templates</li>
     * <li>retains acts which have participations to the document templates</li>
     * </ol>
     * <li>creates acts for each document template that doesn't yet have an act</li>
     * </ol>
     * This is the same as invoking:
     * <pre>
     * prepare();
     * commit();
     * </pre>
     * Use {@link #prepare} and {@link #commit(boolean)} if the charge item must be saved independently.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void link() {
        prepare();
        commit();
    }

    /**
     * Prepares the charge item and documents for commit.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void prepare() {
        // map of template references to their corresponding entity relationship, obtained from the product
        Map<IMObjectReference, EntityRelationship> productTemplates
                = new HashMap<IMObjectReference, EntityRelationship>();

        // template references associated with the current document acts
        Set<IMObjectReference> templateRefs = new HashSet<IMObjectReference>();

        // determine the templates associated with the item's product
        ActBean itemBean = new ActBean(item, service);
        Product product = (Product) itemBean.getParticipant(ProductArchetypes.PRODUCT_PARTICIPATION);
        if (product != null) {
            EntityBean productBean = new EntityBean(product, service);
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
        List<Act> documents = itemBean.getNodeActs("documents");

        // for each document, determine if the product, patient, author or clinician has changed. If so, remove it
        for (Act document : documents.toArray(new Act[documents.size()])) {
            ActBean bean = new ActBean(document, service);
            if (productChanged(bean, product) || patientChanged(bean, itemBean) || authorChanged(bean, itemBean)
                || clinicianChanged(bean, itemBean)) {
                toRemove.add(document);
                ActRelationship r = itemBean.getRelationship(document);
                itemBean.removeRelationship(r);
                document.removeActRelationship(r);
                documents.remove(document);
            } else {
                IMObjectReference templateRef = bean.getNodeParticipantRef("documentTemplate");
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
                    addDocument(itemBean, entity);
                }
            }
        }
    }

    /**
     * Commits the changes, including the charge item.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void commit() {
        commit(true);
    }

    /**
     * Commits the changes, optionally including the charge item.
     *
     * @param saveItem if <tt>true</tt> save the charge item as well
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void commit(boolean saveItem) {
        if (saveItem) {
            toSave.add(0, item);
        }
        if (!toSave.isEmpty()) {
            service.save(toSave);
        }
        for (IMObject object : toRemove) {
            service.remove(object);
        }
        toSave.clear();
        toRemove.clear();
    }

    /**
     * Adds an <em>act.patientDocument*</em> to the invoice item.
     *
     * @param itemBean the invoice item
     * @param document the document template
     * @throws ArchetypeServiceException for any error
     */
    private void addDocument(ActBean itemBean, Entity document) {
        EntityBean bean = new EntityBean(document, service);
        String shortName = bean.getString("archetype");
        if (StringUtils.isEmpty(shortName)) {
            shortName = PatientArchetypes.DOCUMENT_FORM;
        }
        if (TypeHelper.matches(shortName, "act.patientDocument*")) {
            Act act = (Act) service.create(shortName);
            if (act == null) {
                throw new IllegalStateException("Failed to create :" + shortName);
            }
            act.setActivityStartTime(itemBean.getAct().getActivityStartTime());
            ActBean documentAct = new ActBean(act, service);
            IMObjectReference patient = itemBean.getParticipantRef(PatientArchetypes.PATIENT_PARTICIPATION);
            documentAct.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
            documentAct.addParticipation(DocumentArchetypes.DOCUMENT_TEMPLATE_PARTICIPATION, document);
            IMObjectReference author = itemBean.getParticipantRef(UserArchetypes.AUTHOR_PARTICIPATION);
            if (author != null) {
                documentAct.addParticipation(UserArchetypes.AUTHOR_PARTICIPATION, author);
            }
            IMObjectReference clinician = itemBean.getParticipantRef(UserArchetypes.CLINICIAN_PARTICIPATION);
            if (clinician != null) {
                documentAct.addParticipation(UserArchetypes.CLINICIAN_PARTICIPATION, clinician);
            }

            if (TypeHelper.isA(act, PatientArchetypes.DOCUMENT_FORM)) {
                IMObjectReference product = itemBean.getParticipantRef(ProductArchetypes.PRODUCT_PARTICIPATION);
                documentAct.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, product);
            }
            toSave.add(act);
            itemBean.addRelationship("actRelationship.invoiceItemDocument", documentAct.getAct());
        }
    }

    /**
     * Determines if the product has changed.
     *
     * @param bean    the document act bean
     * @param product the item product
     * @return <tt>true</tt> if the product has changed
     */
    private boolean productChanged(ActBean bean, Product product) {
        return bean.hasNode("product") &&
               !ObjectUtils.equals(bean.getNodeParticipantRef("product"), product.getObjectReference());
    }

    /**
     * Determines if the patient has changed.
     *
     * @param docBean  the document act bean
     * @param itemBean the item bean
     * @return <tt>true</tt> if the patient has changed
     */
    private boolean patientChanged(ActBean docBean, ActBean itemBean) {
        return checkParticipant(docBean, itemBean, "patient");
    }

    /**
     * Determines if the author has changed.
     *
     * @param docBean  the document act bean
     * @param itemBean the item bean
     * @return <tt>true</tt> if the author has changed
     */
    private boolean authorChanged(ActBean docBean, ActBean itemBean) {
        return checkParticipant(docBean, itemBean, "author");
    }

    /**
     * Determines if the clinician has changed.
     *
     * @param docBean  the document act bean
     * @param itemBean the item bean
     * @return <tt>true</tt> if the clinician has changed
     */
    private boolean clinicianChanged(ActBean docBean, ActBean itemBean) {
        return checkParticipant(docBean, itemBean, "clinician");
    }

    /**
     * Determines if a participant has changed.
     *
     * @param docBean  the document act bean
     * @param itemBean the item bean
     * @param node     the participant node to check
     * @return <tt>true</tt> if the participant has changed
     */
    private boolean checkParticipant(ActBean docBean, ActBean itemBean, String node) {
        return docBean.hasNode(node) &&
               !ObjectUtils.equals(docBean.getNodeParticipantRef(node), itemBean.getNodeParticipantRef(node));
    }

    /**
     * Helper to retrieve an object given its reference.
     *
     * @param ref the reference
     * @return the object corresponding to the reference, or <tt>null</tt>
     *         if it can't be retrieved
     * @throws ArchetypeServiceException for any error
     */
    private IMObject getObject(IMObjectReference ref) {
        return (ref != null) ? service.get(ref) : null;
    }

}
