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

package org.openvpms.web.workspace.workflow.order;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.IntegerStringParser;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.workspace.customer.order.PharmacyTestHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.math.BigDecimal;

/**
 * Tool to generate customer orders.
 *
 * @author Tim Anderson
 */
public class CustomerOrderGenerator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The patient iterator.
     */
    private IMObjectQueryIterator<Party> patientIterator;

    /**
     * The product iterator.
     */
    private IMObjectQueryIterator<Product> productIterator;

    /**
     * The supported products.
     */
    private static final String[] PRODUCTS = new String[]{ProductArchetypes.MEDICATION, ProductArchetypes.MERCHANDISE,
                                                          ProductArchetypes.SERVICE};
    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";

    /**
     * Associates a customer and patient.
     */
    private static class Patient {

        private final Party patient;
        private final Party customer;

        public Patient(Party patient, Party customer) {
            this.patient = patient;
            this.customer = customer;
        }
    }

    /**
     * Constructs an {@link CustomerOrderGenerator}.
     *
     * @param service the archetype service
     * @param rules   the patient rules
     */
    public CustomerOrderGenerator(IArchetypeService service, PatientRules rules) {
        this.service = service;
        this.rules = rules;
    }

    /**
     * Generates customer orders.
     *
     * @param count the number of orders to generate
     */
    public void generate(int count) {
        for (int i = 0; i < count; ++i) {
            Patient patient = nextPatient();
            Product product = nextProduct();
            PharmacyTestHelper.createOrder(patient.customer, patient.patient, product, BigDecimal.ONE, null);
        }
    }

    /**
     * Main line.
     *
     * @param args command line arguments
     * @throws Exception for any error
     */
    public static void main(String[] args) throws Exception {
        JSAP parser = createParser();
        JSAPResult config = parser.parse(args);
        if (!config.success()) {
            displayUsage(parser);
        } else {
            String contextPath = config.getString("context");
            ApplicationContext context;
            if (!new File(contextPath).exists()) {
                context = new ClassPathXmlApplicationContext(contextPath);
            } else {
                context = new FileSystemXmlApplicationContext(contextPath);
            }

            CustomerOrderGenerator generator = new CustomerOrderGenerator(context.getBean(IArchetypeService.class),
                                                                          context.getBean(PatientRules.class)
            );
            generator.generate(config.getInt("count"));
        }
    }

    /**
     * Returns the next patient.
     *
     * @return the next patient
     */
    private Patient nextPatient() {
        boolean all = false;
        if (patientIterator == null || !patientIterator.hasNext()) {
            all = true;
            patientIterator = createPatientIterator();
        }
        Patient result = getNext(patientIterator);
        if (result == null && !all) {
            patientIterator = createPatientIterator();
            result = getNext(patientIterator);
        }
        if (result == null) {
            throw new IllegalStateException("No patients found");
        }

        return result;
    }

    /**
     * Returns the next product.
     *
     * @return the next product
     */
    private Product nextProduct() {
        if (productIterator == null || !productIterator.hasNext()) {
            productIterator = createProductIterator();
            if (!productIterator.hasNext()) {
                throw new IllegalStateException("No products found");
            }
        }
        return productIterator.next();
    }

    /**
     * Returns the next patient.
     *
     * @param patients the patient iterator
     * @return the next patient
     */
    private Patient getNext(IMObjectQueryIterator<Party> patients) {
        Patient result = null;
        while (patients.hasNext()) {
            Party patient = patients.next();
            Party customer = rules.getOwner(patient);
            if (customer != null && customer.isActive()) {
                result = new Patient(patient, customer);
                break;
            }
        }
        return result;
    }

    /**
     * Creates a new patient iterator.
     *
     * @return the patient iterator
     */
    private IMObjectQueryIterator<Party> createPatientIterator() {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.PATIENT);
        query.add(Constraints.sort("id"));
        return new IMObjectQueryIterator<>(service, query);
    }

    /**
     * Creates a new product iterator.
     *
     * @return the product iterator
     */
    private IMObjectQueryIterator<Product> createProductIterator() {
        ArchetypeQuery query = new ArchetypeQuery(PRODUCTS, true, true);
        query.add(Constraints.leftJoin("species"))
                .add(Constraints.isNull("species.code"));
        query.add(Constraints.sort("id"));
        return new IMObjectQueryIterator<>(service, query);
    }

    /**
     * Creates a new product iterator.
     */

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("context")
                                         .setLongFlag("context")
                                         .setDefault(APPLICATION_CONTEXT)
                                         .setHelp("Application context path"));
        parser.registerParameter(new FlaggedOption("count").setShortFlag('c')
                                         .setLongFlag("count")
                                         .setStringParser(IntegerStringParser.getParser())
                                         .setHelp("The no. of orders to generate."));
        return parser;
    }

    /**
     * Prints usage information.
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err
                .println("Usage: java " + CustomerOrderGenerator.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
