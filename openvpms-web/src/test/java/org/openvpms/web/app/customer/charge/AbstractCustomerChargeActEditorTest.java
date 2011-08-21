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

package org.openvpms.web.app.customer.charge;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ObjectUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Abstract base class for customer charge act editor tests.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractCustomerChargeActEditorTest extends AbstractAppTest {

    /**
     * The practice.
     */
    private Party practice;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        practice = TestHelper.getPractice();
        practice.addClassification(createTaxType());
        save(practice);
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param item       the item to check
     * @param patient    the expected patient
     * @param product    the expected product
     * @param author     the expected author
     * @param clinician  the expected clinician
     * @param quantity   the expected quantity
     * @param unitCost   the expected unit cost
     * @param unitPrice  the expected unit price
     * @param fixedCost  the expected fixed cost
     * @param fixedPrice the expected fixed price
     * @param discount   the expected discount
     * @param tax        the expected tax
     * @param total      the expected total
     */
    protected void checkItem(FinancialAct item, Party patient, Product product, User author, User clinician,
                             BigDecimal quantity, BigDecimal unitCost, BigDecimal unitPrice, BigDecimal fixedCost,
                             BigDecimal fixedPrice, BigDecimal discount, BigDecimal tax, BigDecimal total) {
        ActBean bean = new ActBean(item);
        if (bean.hasNode("patient")) {
            assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        }
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        if (bean.hasNode("clinician")) {
            assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        }
        checkEquals(quantity, bean.getBigDecimal("quantity"));
        checkEquals(fixedCost, bean.getBigDecimal("fixedCost"));
        checkEquals(fixedPrice, bean.getBigDecimal("fixedPrice"));
        checkEquals(unitPrice, bean.getBigDecimal("unitPrice"));
        checkEquals(unitCost, bean.getBigDecimal("unitCost"));
        checkEquals(discount, bean.getBigDecimal("discount"));
        checkEquals(tax, bean.getBigDecimal("tax"));
        checkEquals(total, bean.getBigDecimal("total"));
    }

    /**
     * Verifies a patient medication act matches that expected.
     *
     * @param item      the charge item, linked to the medication
     * @param patient   the expected patient
     * @param product   the expected product
     * @param author    the expected author
     * @param clinician the expected clinician
     * @return the medication act
     */
    protected Act checkMedication(FinancialAct item, Party patient, Product product, User author, User clinician) {
        ActBean itemBean = new ActBean(item);
        List<Act> dispensing = itemBean.getNodeActs("dispensing");
        assertEquals(1, dispensing.size());

        Act medication = dispensing.get(0);
        ActBean bean = new ActBean(medication);
        assertEquals(item.getActivityStartTime(), medication.getActivityStartTime());
        assertEquals(item.getActivityEndTime(), medication.getActivityEndTime());
        assertTrue(bean.isA(PatientArchetypes.PATIENT_MEDICATION));
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        checkEquals(item.getQuantity(), bean.getBigDecimal("quantity"));
        return medication;
    }

    /**
     * Finds an investigation associated with a charge item, given its investigation type.
     *
     * @param item              the item
     * @param investigationType the investigation type
     * @return the corresponding investigation
     */
    protected Act getInvestigation(Act item, Entity investigationType) {
        ActBean itemBean = new ActBean(item);
        List<Act> investigations = itemBean.getNodeActs("investigations");
        for (Act investigation : investigations) {
            ActBean bean = new ActBean(investigation);
            assertTrue(bean.isA(InvestigationArchetypes.PATIENT_INVESTIGATION));
            if (ObjectUtils.equals(bean.getNodeParticipantRef("investigationType"),
                                   investigationType.getObjectReference())) {
                return investigation;
            }
        }
        fail("Investigation not found");
        return null;
    }

    /**
     * Verifies a patient investigation act matches that expected.
     *
     * @param item              the charge item, linked to the investigation
     * @param patient           the expected patient
     * @param investigationType the expected investigation type
     * @param author            the expected author
     * @param clinician         the expected clinician
     * @return the investigation act
     */
    protected Act checkInvestigation(Act item, Party patient, Entity investigationType, User author, User clinician) {
        Act investigation = getInvestigation(item, investigationType);
        ActBean bean = new ActBean(investigation);
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(investigationType.getObjectReference(), bean.getNodeParticipantRef("investigationType"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        return investigation;
    }

    /**
     * Finds a reminder associated with a charge item, given its reminder type.
     *
     * @param item              the item
     * @param reminderType the reminder type
     * @return the corresponding reminder
     */
    protected Act getReminder(Act item, Entity reminderType) {
        ActBean itemBean = new ActBean(item);
        List<Act> reminders = itemBean.getNodeActs("reminders");
        for (Act reminder : reminders) {
            ActBean bean = new ActBean(reminder);
            assertTrue(bean.isA(ReminderArchetypes.REMINDER));
            if (ObjectUtils.equals(bean.getNodeParticipantRef("reminderType"),
                                   reminderType.getObjectReference())) {
                return reminder;
            }
        }
        fail("Reminder not found");
        return null;
    }

    /**
     * Verifies a patient reminder act matches that expected.
     *
     * @param item         the charge item, linked to the reminder
     * @param patient      the expected patient
     * @param product      the expected product
     * @param reminderType the expected reminder type
     * @param author       the expected author
     * @param clinician    the expected clinician
     * @return the reminder act
     */
    protected Act checkReminder(Act item, Party patient, Product product, Entity reminderType, User author,
                                User clinician) {
        Act reminder = getReminder(item, reminderType);
        EntityBean productBean = new EntityBean(product);

        ReminderRules rules = new ReminderRules();
        List<EntityRelationship> rels = productBean.getNodeRelationships("reminders");
        assertEquals(1, rels.size());
        ActBean bean = new ActBean(reminder);
        assertEquals(item.getActivityStartTime(), reminder.getActivityStartTime());
        Date dueDate = rules.calculateProductReminderDueDate(item.getActivityStartTime(), rels.get(0));
        assertEquals(0, DateRules.compareTo(reminder.getActivityEndTime(), dueDate));
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(reminderType.getObjectReference(), bean.getNodeParticipantRef("reminderType"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        return reminder;
    }

    /**
     * Finds a document associated with a charge item, given its document template.
     *
     * @param item              the item
     * @param template the document template
     * @return the corresponding reminder
     */
    protected Act getDocument(Act item, Entity template) {
        ActBean itemBean = new ActBean(item);
        List<Act> documents = itemBean.getNodeActs("documents");
        for (Act document : documents) {
            ActBean bean = new ActBean(document);
            assertTrue(bean.isA("act.patientDocument*"));
            if (ObjectUtils.equals(bean.getNodeParticipantRef("documentTemplate"), template.getObjectReference())) {
                return document;
            }
        }
        fail("Document not found");
        return null;
    }

    /**
     * Verifies a document act matches that expected.
     *
     * @param item      the charge item, linked to the document act
     * @param patient   the expected patient
     * @param product   the expected product
     * @param template  the expected document template
     * @param author    the expected author
     * @param clinician the expected clinician
     * @return the document act
     */
    protected Act checkDocument(Act item, Party patient, Product product, Entity template, User author,
                                User clinician) {
        Act document = getDocument(item, template);
        ActBean bean = new ActBean(document);
        assertTrue(bean.isA(PatientArchetypes.DOCUMENT_FORM));
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(template.getObjectReference(), bean.getNodeParticipantRef("documentTemplate"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        return document;
    }

    /**
     * Saves the current popup editor.
     *
     * @param mgr       the popup editor manager
     * @param shortName the expected archetype short name of the object being edited
     */
    protected void checkSavePopup(EditorManager mgr, String shortName) {
        EditDialog dialog = mgr.getCurrent();
        assertNotNull(dialog);
        IMObjectEditor editor = dialog.getEditor();
        assertTrue(TypeHelper.isA(editor.getObject(), shortName));
        assertTrue(editor.isValid());
        clickDialogOK(dialog);
    }

    /**
     * Helper to click OK on an edit dialog.
     *
     * @param dialog the dialog
     */
    protected void clickDialogOK(EditDialog dialog) {
        Button ok = dialog.getButtons().getButton(PopupDialog.OK_ID);
        assertNotNull(ok);
        assertTrue(ok.isEnabled());
        ok.fireActionPerformed(new ActionEvent(ok, ok.getActionCommand()));
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param fixedPrice the fixed price
     * @return a new product
     */
    protected Product createProduct(String shortName, BigDecimal fixedPrice) {
        Product product = createProduct(shortName);
        product.addProductPrice(createFixedPrice(product, BigDecimal.ZERO, fixedPrice));
        save(product);
        return product;
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param fixedCost  the fixed cost
     * @param fixedPrice the fixed price
     * @param unitCost   the unit cost
     * @param unitPrice  the unit price
     * @return a new product
     */
    protected Product createProduct(String shortName, BigDecimal fixedCost, BigDecimal fixedPrice, BigDecimal unitCost,
                                    BigDecimal unitPrice) {
        Product product = createProduct(shortName);
        product.addProductPrice(createFixedPrice(product, fixedCost, fixedPrice));
        product.addProductPrice(createUnitPrice(product, unitCost, unitPrice));
        save(product);
        return product;
    }

    /**
     * Helper to create a product.
     *
     * @param shortName the product archetype short name
     * @return a new product
     */
    protected Product createProduct(String shortName) {
        return TestHelper.createProduct(shortName, null, true);
    }

    /**
     * Adds an investigation type to a product.
     *
     * @param product the product
     * @return the investigation type
     */
    protected Entity addInvestigation(Product product) {
        Entity investigation = (Entity) create(InvestigationArchetypes.INVESTIGATION_TYPE);
        investigation.setName("X-TestInvestigationType-" + investigation.hashCode());
        EntityBean productBean = new EntityBean(product);
        productBean.addNodeRelationship("investigationTypes", investigation);
        save(investigation, product);
        return investigation;
    }

    /**
     * Adds a document template to a product.
     *
     * @param product the product
     * @return the document template
     */
    protected Entity addTemplate(Product product) {
        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        template.setName("X-TestDocumentTemplate-" + template.hashCode());
        EntityBean productBean = new EntityBean(product);
        productBean.addNodeRelationship("documents", template);
        save(template, product);
        return template;
    }

    /**
     * Adds an interactive reminder type to a product.
     *
     * @param product the product
     * @return the reminder type
     */
    protected Entity addReminder(Product product) {
        Entity reminderType = ReminderTestHelper.createReminderType();
        EntityBean productBean = new EntityBean(product);
        EntityRelationship rel = productBean.addNodeRelationship("reminders", reminderType);
        IMObjectBean relBean = new IMObjectBean(rel);
        relBean.setValue("interactive", true);
        save(product, reminderType);
        return reminderType;
    }

    /**
     * Helper to create a new unit price.
     *
     * @param product the product
     * @param cost    the cost price
     * @param price   the price after markup
     * @return a new unit price
     */
    private ProductPrice createUnitPrice(Product product, BigDecimal cost, BigDecimal price) {
        return createPrice(product, ProductArchetypes.UNIT_PRICE, cost, price);
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param product the product
     * @param cost    the cost price
     * @param price   the price after markup
     * @return a new unit price
     */
    private ProductPrice createFixedPrice(Product product, BigDecimal cost, BigDecimal price) {
        return createPrice(product, ProductArchetypes.FIXED_PRICE, cost, price);
    }

    /**
     * Helper to create a new product price.
     *
     * @param product   the product
     * @param shortName the product price archetype short name
     * @param cost      the cost price
     * @param price     the price after markup
     * @return a new unit price
     */
    private ProductPrice createPrice(Product product, String shortName, BigDecimal cost, BigDecimal price) {
        ProductPrice result = (ProductPrice) create(shortName);
        ProductPriceRules rules = new ProductPriceRules();
        BigDecimal markup = rules.getMarkup(product, cost, price, practice);
        result.setName("XPrice");
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("cost", cost);
        bean.setValue("markup", markup);
        bean.setValue("price", price);
        return result;
    }

    /**
     * Helper to create and save a new tax type classification.
     *
     * @return a new tax classification
     */
    private Lookup createTaxType() {
        Lookup tax = (Lookup) create("lookup.taxType");
        IMObjectBean bean = new IMObjectBean(tax);
        bean.setValue("code", "XTAXRULESTESTCASE_CLASSIFICATION_" + Math.abs(new Random().nextInt()));
        bean.setValue("rate", new BigDecimal(10));
        save(tax);
        return tax;
    }

    protected static class EditorManager extends PopupEditorManager {

        /**
         * The current edit dialog.
         */
        private EditDialog current;

        /**
         * Returns the current popup dialog.
         *
         * @return the current popup dialog. May be <tt>null</tt>
         */
        public EditDialog getCurrent() {
            return current;
        }

        /**
         * Displays an edit dialog.
         *
         * @param dialog the dialog
         */
        @Override
        protected void edit(EditDialog dialog) {
            super.edit(dialog);
            current = dialog;
        }

        /**
         * Invoked when the edit is completed.
         */
        @Override
        protected void editCompleted() {
            super.editCompleted();
            current = null;
        }
    }
}
