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

package org.openvpms.web.workspace.customer.charge;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Abstract base class for customer charge act editor tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractCustomerChargeActEditorTest extends AbstractAppTest {

    /**
     * The practice.
     */
    private Party practice;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        // NOTE: need to create the practice prior to the application as it caches the practice in the context 
        practice = TestHelper.getPractice();
        practice.addClassification(TestHelper.createTaxType(BigDecimal.TEN));
        IMObjectBean bean = new IMObjectBean(practice);
        bean.setValue("minimumQuantities", true);
        bean.setValue("minimumQuantitiesOverride", null);
        save(practice);
        super.setUp();
    }

    /**
     * Returns the practice.
     *
     * @return the practice
     */
    protected Party getPractice() {
        return practice;
    }

    /**
     * Adds a charge item.
     *
     * @param editor   the editor
     * @param patient  the patient
     * @param product  the product
     * @param quantity the quantity. If {@code null}, indicates the quantity won't be changed
     * @param queue    the popup editor manager
     * @return the editor for the new item
     */
    protected CustomerChargeActItemEditor addItem(CustomerChargeActEditor editor, Party patient, Product product,
                                                  BigDecimal quantity, EditorQueue queue) {
        return CustomerChargeTestHelper.addItem(editor, patient, product, quantity, queue);
    }

    /**
     * Sets the values of a charge item.
     *
     * @param editor     the charge editor
     * @param itemEditor the charge item editor
     * @param patient    the patient
     * @param product    the product
     * @param quantity   the quantity
     * @param queue      the popup editor manager
     */
    protected void setItem(DefaultCustomerChargeActEditor editor, CustomerChargeActItemEditor itemEditor,
                           Party patient, Product product, BigDecimal quantity, EditorQueue queue) {
        CustomerChargeTestHelper.setItem(editor, itemEditor, patient, product, quantity, queue);
    }

    /**
     * Verifies a charge matches that expected.
     *
     * @param charge    the charge
     * @param customer  the expected customer
     * @param author    the expected author
     * @param clinician the expected clinician
     * @param tax       the expected tax
     * @param total     the expected total
     */
    protected void checkCharge(FinancialAct charge, Party customer, User author, User clinician, BigDecimal tax,
                               BigDecimal total) {
        assertNotNull(charge);
        ActBean bean = new ActBean(charge);
        assertEquals(customer.getObjectReference(), bean.getNodeParticipantRef("customer"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        if (bean.hasNode("clinician")) {
            checkClinician(charge, clinician);
        }
        checkEquals(tax, bean.getBigDecimal("tax"));
        checkEquals(total, bean.getBigDecimal("amount"));
    }

    /**
     * Verifies that an act has the expected clinician.
     *
     * @param act       the act to check
     * @param clinician the expected clinician
     */
    protected void checkClinician(Act act, User clinician) {
        ActBean bean = new ActBean(act);
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param items           the items to search
     * @param patient         the expected patient
     * @param product         the expected product
     * @param template        the expected template. May be {@code null}
     * @param author          the expected author
     * @param clinician       the expected clinician
     * @param minimumQuantity the expected minimum quantity
     * @param quantity        the expected quantity
     * @param unitCost        the expected unit cost
     * @param unitPrice       the expected unit price
     * @param fixedCost       the expected fixed cost
     * @param fixedPrice      the expected fixed price
     * @param discount        the expected discount
     * @param tax             the expected tax
     * @param total           the expected total
     * @param event           the clinical event. May be {@code null}
     * @param childActs       the expected no. of child acts
     * @return the item
     */
    protected ActBean checkItem(List<FinancialAct> items, Party patient, Product product, Product template,
                                User author, User clinician, BigDecimal minimumQuantity, BigDecimal quantity,
                                BigDecimal unitCost, BigDecimal unitPrice, BigDecimal fixedCost,
                                BigDecimal fixedPrice, BigDecimal discount, BigDecimal tax, BigDecimal total,
                                Act event, int childActs) {
        int count = 0;
        FinancialAct item = find(items, patient, product);
        checkItem(item, patient, product, template, author, clinician, minimumQuantity, quantity, unitCost, unitPrice,
                  fixedCost, fixedPrice, discount, tax, total);
        ActBean itemBean = new ActBean(item);
        EntityBean bean = new EntityBean(product);

        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
                // verify there is a medication act that is linked to the event
                Act medication = checkMedication(item, patient, product, author, clinician);
                if (event != null) {
                    checkEventRelationship(event, medication);
                }
                ++count;
            } else {
                assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
            }
            List<Entity> investigations = bean.getNodeTargetEntities("investigationTypes");
            assertEquals(investigations.size(), itemBean.getNodeActs("investigations").size());
            for (Entity investigationType : investigations) {
                // verify there is an investigation for each investigation type, and it is linked to the event
                Act investigation = checkInvestigation(item, patient, investigationType, author, clinician);
                if (event != null) {
                    checkEventRelationship(event, investigation);
                }
                ++count;
            }
            List<Entity> reminderTypes = bean.getNodeTargetEntities("reminders");
            assertEquals(reminderTypes.size(), itemBean.getNodeActs("reminders").size());
            for (Entity reminderType : reminderTypes) {
                checkReminder(item, patient, product, reminderType, author, clinician);
                ++count;
            }
            List<Entity> alertTypes = bean.getNodeTargetEntities("alerts");
            assertEquals(alertTypes.size(), itemBean.getNodeActs("alerts").size());
            for (Entity alertType : alertTypes) {
                checkAlert(item, patient, product, alertType, author, clinician);
                ++count;
            }
            List<Entity> templates = bean.getNodeTargetEntities("documents");
            assertEquals(templates.size(), itemBean.getNodeActs("documents").size());
            for (Entity docTemplate : templates) {
                // verify there is a document for each template, and it is linked to the event
                Act document = checkDocument(item, patient, product, docTemplate, author, clinician);
                if (event != null) {
                    checkEventRelationship(event, document);
                }
                ++count;
            }
        } else {
            // verify there are no medication, investigation, reminder nor document acts
            assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
            assertTrue(itemBean.getActs(InvestigationArchetypes.PATIENT_INVESTIGATION).isEmpty());
            assertTrue(itemBean.getActs(ReminderArchetypes.REMINDER).isEmpty());
            assertTrue(itemBean.getActs(PatientArchetypes.ALERT).isEmpty());
            assertTrue(itemBean.getActs("act.patientDocument*").isEmpty());
        }
        assertEquals(childActs, count);
        return itemBean;
    }

    /**
     * Verifies that a relationship exists between an event and an act.
     *
     * @param event the event
     * @param act   the act
     */
    protected void checkEventRelationship(Act event, Act act) {
        checkEventRelationship(event, act, true);
    }

    /**
     * Verifies that a relationship exists/doesn't exist between an event and an act.
     *
     * @param event  the event
     * @param act    the act
     * @param exists if {@code true} the relationship must exist
     */
    protected void checkEventRelationship(Act event, Act act, boolean exists) {
        ActBean eventBean = new ActBean(event);
        String shortName = TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)
                           ? PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM : PatientArchetypes.CLINICAL_EVENT_ITEM;
        assertEquals(exists, eventBean.hasRelationship(shortName, act));
    }

    /**
     * Verifies that an event has relationships to the specified acts, and that those relationships are the only
     * ones present.
     *
     * @param event the event
     * @param acts  the expected acts
     */
    protected void checkEventRelationships(Act event, Act... acts) {
        assertEquals(event.getSourceActRelationships().size(), acts.length);
        for (Act act : acts) {
            checkEventRelationship(event, act);
        }
    }


    /**
     * Finds an item in a list of items, by patient and product.
     *
     * @param items   the items
     * @param patient the patient. May be {@code null}
     * @param product the product
     * @return the corresponding item
     */
    protected <T extends Act> T find(List<T> items, Party patient, Product product) {
        T result = null;
        IMObjectReference ref = (patient != null) ? patient.getObjectReference() : null;
        for (T item : items) {
            ActBean bean = new ActBean(item);
            if (ObjectUtils.equals(bean.getNodeParticipantRef("product"), product.getObjectReference())) {
                if (bean.hasNode("patient")) {
                    if (ObjectUtils.equals(bean.getNodeParticipantRef("patient"), ref)) {
                        result = item;
                        break;
                    }
                } else {
                    result = item;
                    break;
                }
            }
        }
        assertNotNull(result);
        return result;
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param item            the item to check
     * @param patient         the expected patient
     * @param product         the expected product
     * @param template        the expected template. May be {@code null}
     * @param author          the expected author
     * @param clinician       the expected clinician
     * @param minimumQuantity the expected minimum quantity
     * @param quantity        the expected quantity
     * @param unitCost        the expected unit cost
     * @param unitPrice       the expected unit price
     * @param fixedCost       the expected fixed cost
     * @param fixedPrice      the expected fixed price
     * @param discount        the expected discount
     * @param tax             the expected tax
     * @param total           the expected total
     */
    protected void checkItem(FinancialAct item, Party patient, Product product, Product template, User author,
                             User clinician, BigDecimal minimumQuantity, BigDecimal quantity, BigDecimal unitCost,
                             BigDecimal unitPrice, BigDecimal fixedCost, BigDecimal fixedPrice, BigDecimal discount,
                             BigDecimal tax, BigDecimal total) {
        ActBean bean = new ActBean(item);
        if (bean.hasNode("patient")) {
            assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        }
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        if (template == null) {
            assertNull(bean.getNodeParticipantRef("template"));
        } else {
            assertEquals(template.getObjectReference(), bean.getNodeParticipantRef("template"));
        }
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        if (bean.hasNode("clinician")) {
            if (clinician != null) {
                assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
            } else {
                assertNull(bean.getNodeParticipant("clinician"));
            }
        }
        checkEquals(minimumQuantity, bean.getBigDecimal("minQuantity"));
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
    protected DocumentAct getInvestigation(Act item, Entity investigationType) {
        ActBean itemBean = new ActBean(item);
        List<Act> investigations = itemBean.getNodeActs("investigations");
        for (Act investigation : investigations) {
            ActBean bean = new ActBean(investigation);
            assertTrue(bean.isA(InvestigationArchetypes.PATIENT_INVESTIGATION));
            if (ObjectUtils.equals(bean.getNodeParticipantRef("investigationType"),
                                   investigationType.getObjectReference())) {
                return (DocumentAct) investigation;
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
     * @param item         the item
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

        ReminderRules rules = ServiceHelper.getBean(ReminderRules.class);
        List<EntityRelationship> rels = productBean.getNodeRelationships("reminders");
        assertEquals(1, rels.size());
        ActBean bean = new ActBean(reminder);
        Date createdTime = DateUtils.truncate(bean.getDate("createdTime"), Calendar.SECOND); // details nodes store ms
        assertEquals(0, DateRules.compareTo(item.getActivityStartTime(), createdTime));
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
     * Finds an alert associated with a charge item, given its alert type.
     *
     * @param item      the item
     * @param alertType the alert type
     * @return the corresponding alert
     */
    protected Act getAlert(Act item, Entity alertType) {
        ActBean itemBean = new ActBean(item);
        List<Act> alerts = itemBean.getNodeActs("alerts");
        for (Act alert : alerts) {
            ActBean bean = new ActBean(alert);
            assertTrue(bean.isA(PatientArchetypes.ALERT));
            if (ObjectUtils.equals(bean.getNodeParticipantRef("alertType"), alertType.getObjectReference())) {
                return alert;
            }
        }
        fail("Alert not found");
        return null;
    }

    /**
     * Verifies a patient alert act matches that expected.
     *
     * @param item      the charge item, linked to the alert
     * @param patient   the expected patient
     * @param product   the expected product
     * @param alertType the expected alert type
     * @param author    the expected author
     * @param clinician the expected clinician
     * @return the alert act
     */
    protected Act checkAlert(Act item, Party patient, Product product, Entity alertType, User author, User clinician) {
        Act alert = getAlert(item, alertType);
        ActBean bean = new ActBean(alert);
        assertEquals(item.getActivityStartTime(), alert.getActivityStartTime());
        IMObjectBean alertBean = new IMObjectBean(alertType);
        if (alertBean.getString("durationUnits") != null) {
            int duration = alertBean.getInt("duration");
            DateUnits units = DateUnits.fromString(alertBean.getString("durationUnits"));
            Date endTime = DateRules.getDate(item.getActivityStartTime(), duration, units);
            assertEquals(endTime, alert.getActivityEndTime());
        }
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(alertType.getObjectReference(), bean.getNodeParticipantRef("alertType"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        return alert;
    }

    /**
     * Finds a document associated with a charge item, given its document template.
     *
     * @param item     the item
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
     * Verifies that an event linked to a charge has one instance of the expected note.
     *
     * @param chargeItem the charge item, linked to an event
     * @param patient    the expected patient
     * @param note       the expected note
     */
    protected void checkChargeEventNote(ActBean chargeItem, Party patient, String note) {
        Act event = getEvent(chargeItem);
        checkEventNote(event, patient, note);
    }

    /**
     * Verifies that an event has one instance of the expected note.
     *
     * @param event   the event
     * @param patient the expected patient
     * @param note    the expected note
     */
    protected void checkEventNote(Act event, Party patient, String note) {
        int found = 0;
        ActBean eventBean = new ActBean(event);
        assertEquals(patient, eventBean.getNodeParticipant("patient"));
        for (Act item : eventBean.getNodeActs("items")) {
            if (TypeHelper.isA(item, PatientArchetypes.CLINICAL_NOTE)) {
                ActBean bean = new ActBean(item);
                assertEquals(patient, bean.getNodeParticipant("patient"));
                if (StringUtils.equals(note, bean.getString("note"))) {
                    found++;
                }
            }
        }
        assertEquals(1, found);
    }

    /**
     * Returns an event linked to a charge item.
     *
     * @param item the charge item
     * @return the corresponding event
     */
    protected Act getEvent(ActBean item) {
        Act event = (Act) item.getNodeSourceObject("event");
        assertNotNull(event);
        return event;
    }

    /**
     * Helper to create a product.
     *
     * @param shortName the product archetype short name
     * @return a new product
     */
    public Product createProduct(String shortName) {
        return CustomerChargeTestHelper.createProduct(shortName);
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param fixedPrice the fixed price
     * @return a new product
     */
    protected Product createProduct(String shortName, BigDecimal fixedPrice) {
        return CustomerChargeTestHelper.createProduct(shortName, fixedPrice);
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param fixedPrice the fixed price
     * @param pharmacy   the pharmacy
     * @return a new product
     */
    protected Product createProduct(String shortName, BigDecimal fixedPrice, Entity pharmacy) {
        Product product = CustomerChargeTestHelper.createProduct(shortName, fixedPrice);
        EntityBean bean = new EntityBean(product);
        bean.addNodeTarget("pharmacy", pharmacy);
        bean.save();

        return product;
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param fixedPrice the fixed price, tax-exclusive
     * @param unitPrice  the unit price, tax-exclusive
     * @return a new product
     */
    protected Product createProduct(String shortName, BigDecimal fixedPrice, BigDecimal unitPrice) {
        return CustomerChargeTestHelper.createProduct(shortName, BigDecimal.ZERO, fixedPrice, BigDecimal.ZERO,
                                                      unitPrice);
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param fixedCost  the fixed cost
     * @param fixedPrice the fixed price, tax-exclusive
     * @param unitCost   the unit cost
     * @param unitPrice  the unit price, tax-exclusive
     * @return a new product
     */
    protected Product createProduct(String shortName, BigDecimal fixedCost, BigDecimal fixedPrice, BigDecimal unitCost,
                                    BigDecimal unitPrice) {
        return CustomerChargeTestHelper.createProduct(shortName, fixedCost, fixedPrice, unitCost, unitPrice);
    }

    /**
     * Adds an investigation type to a product.
     *
     * @param product the product
     * @return the investigation type
     */
    protected Entity addInvestigation(Product product) {
        return addInvestigation(product, null);
    }

    /**
     * Adds an investigation type to a product.
     *
     * @param product    the product
     * @param laboratory the laboratory. May be {@code null}
     * @return the investigation type
     */
    protected Entity addInvestigation(Product product, Entity laboratory) {
        Entity investigation = (laboratory != null) ? ProductTestHelper.createInvestigationType(laboratory, "123456789")
                                                    : ProductTestHelper.createInvestigationType();
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
        addReminder(product, reminderType);
        return reminderType;
    }

    /**
     * Adds an interactive reminder type to a product.
     *
     * @param product      the product
     * @param reminderType the reminder type
     */
    protected void addReminder(Product product, Entity reminderType) {
        EntityBean productBean = new EntityBean(product);
        EntityRelationship rel = productBean.addNodeRelationship("reminders", reminderType);
        IMObjectBean relBean = new IMObjectBean(rel);
        relBean.setValue("interactive", true);
        save(product, reminderType);
    }

    /**
     * Adds an interactive alert type to a product.
     *
     * @param product the product
     * @return the alert type
     */
    protected Entity addAlertType(Product product) {
        Entity alertType = ReminderTestHelper.createAlertType("Z Test Alert", null, 1, DateUnits.YEARS, true);
        addAlertType(product, alertType);
        return alertType;
    }

    /**
     * Adds an alert type to a product.
     *
     * @param product   the product
     * @param alertType the alert type
     */
    protected void addAlertType(Product product, Entity alertType) {
        IMObjectBean bean = new IMObjectBean(product);
        bean.addNodeTarget("alerts", alertType);
        bean.save();
    }

    /**
     * Adds a tax exemption to a customer.
     *
     * @param customer the customer
     */
    protected void addTaxExemption(Party customer) {
        IMObjectBean bean = new IMObjectBean(getPractice());
        List<Lookup> taxes = bean.getValues("taxes", Lookup.class);
        assertEquals(1, taxes.size());
        customer.addClassification(taxes.get(0));
        save(customer);
    }

    /**
     * Adds a discount to an entity.
     *
     * @param entity   the entity to add to
     * @param discount the discount
     */
    protected void addDiscount(Entity entity, Entity discount) {
        IMObjectBean bean = new IMObjectBean(entity);
        bean.addNodeTarget("discounts", discount);
        bean.save();
    }

}
