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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.oasis.ubl.InvoiceType;
import org.oasis.ubl.common.CurrencyCodeContentType;
import org.oasis.ubl.common.aggregate.CustomerPartyType;
import org.oasis.ubl.common.aggregate.MonetaryTotalType;
import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.aggregate.TaxTotalType;
import org.oasis.ubl.common.aggregate.InvoiceLineType;
import org.oasis.ubl.common.aggregate.ItemType;
import org.oasis.ubl.common.aggregate.ItemIdentificationType;
import org.oasis.ubl.common.aggregate.PriceType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.oasis.ubl.common.basic.LineExtensionAmountType;
import org.oasis.ubl.common.basic.PayableAmountType;
import org.oasis.ubl.common.basic.TaxAmountType;
import org.oasis.ubl.common.basic.UBLVersionIDType;
import org.oasis.ubl.common.basic.InvoicedQuantityType;
import org.oasis.ubl.common.basic.BaseQuantityType;
import org.oasis.ubl.common.basic.PriceAmountType;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.supplier.AbstractSupplierTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.ubl.io.UBLDocumentWriter;
import org.openvpms.ubl.io.UBLDocumentContext;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.bind.JAXBException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link InvoiceMapperImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceMapperTestCase extends AbstractSupplierTest {

    private DatatypeFactory factory;

    private Party supplier;

    private Party stockLocation;

    private Product product;

    private CurrencyCodeContentType currency;


    @Test
    public void testMap() throws SAXException, JAXBException {
        InvoiceType invoice = new InvoiceType();
        SupplierPartyType supplierType = createSupplier();
        CustomerPartyType customerType = createCustomer();
        MonetaryTotalType monetaryTotal = createLegalMonetaryTotal();

        invoice.setUBLVersionID(UBLHelper.initID(new UBLVersionIDType(), "2.0"));
        invoice.setID(UBLHelper.createID(12345));
        Date issueDatetime = new Date();
        invoice.setIssueDate(UBLHelper.createIssueDate(issueDatetime, factory));
        invoice.setIssueTime(UBLHelper.createIssueTime(issueDatetime, factory));
        invoice.setAccountingSupplierParty(supplierType);
        invoice.setAccountingCustomerParty(customerType);
        invoice.setLegalMonetaryTotal(monetaryTotal);
        invoice.getTaxTotal().add(createTaxTotal());
        invoice.getInvoiceLine().add(createInvoiceLine());

        UBLDocumentContext context = new UBLDocumentContext();
        UBLDocumentWriter writer = context.createWriter();
        writer.setFormat(true);
        writer.write(invoice, System.out);
        InvoiceMapper mapper = createMapper();
        Delivery delivery = mapper.map(invoice);
        List<FinancialAct> acts = delivery.getActs();
        assertEquals(2, acts.size());
        
    }

    private InvoiceLineType createInvoiceLine() {
        InvoiceLineType result = new InvoiceLineType();
        result.setID(UBLHelper.createID(1)); // TODO - required by UBL
        result.setInvoicedQuantity(UBLHelper.initQuantity(new InvoicedQuantityType(), BigDecimal.ONE, "BX"));
        result.setLineExtensionAmount(UBLHelper.initAmount(new LineExtensionAmountType(), new BigDecimal("100"), currency));
        result.setItem(createItem());
        result.setPrice(createPrice());
        result.getTaxTotal().add(createTaxTotal());
        return result;
    }

    private ItemType createItem() {
        ItemType result = new ItemType();
        ItemIdentificationType buyerId = new ItemIdentificationType();
        buyerId.setID(UBLHelper.createID(product.getId()));
        result.setBuyersItemIdentification(buyerId);
        return result;
    }

    private PriceType createPrice() {
        PriceType result = new PriceType();
        result.setBaseQuantity(UBLHelper.initQuantity(new BaseQuantityType(), BigDecimal.ONE, "BX"));
        result.setPriceAmount(UBLHelper.initAmount(new PriceAmountType(), new BigDecimal("100"), currency));
        return result;
    }


    private TaxTotalType createTaxTotal() {
        TaxTotalType result = new TaxTotalType();
        result.setTaxAmount(UBLHelper.initAmount(new TaxAmountType(), new BigDecimal("10"), currency));
        return result;
    }

    private MonetaryTotalType createLegalMonetaryTotal() {
        MonetaryTotalType result = new MonetaryTotalType();
        result.setPayableAmount(UBLHelper.initAmount(new PayableAmountType(), new BigDecimal("110"), currency));
        result.setLineExtensionAmount(UBLHelper.initAmount(new LineExtensionAmountType(), new BigDecimal("100"), currency));
        return result;
    }

    private SupplierPartyType createSupplier() {
        SupplierPartyType supplierType = new SupplierPartyType();
        CustomerAssignedAccountIDType supplierId = UBLHelper.initID(new CustomerAssignedAccountIDType(),
                                                                    supplier.getId());
        supplierType.setCustomerAssignedAccountID(supplierId);
        return supplierType;
    }

    private CustomerPartyType createCustomer() {
        CustomerPartyType customerType = new CustomerPartyType();
        CustomerAssignedAccountIDType customerId = UBLHelper.initID(new CustomerAssignedAccountIDType(),
                                                                    stockLocation.getId());
        customerType.setCustomerAssignedAccountID(customerId);
        return customerType;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        Party practice = TestHelper.getPractice();
        IMObjectBean bean = new IMObjectBean(practice);
        currency = CurrencyCodeContentType.fromValue(bean.getString("currency"));

        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException error) {
            throw new IllegalStateException(error);
        }
        supplier = TestHelper.createSupplier();
        stockLocation = createStockLocation();
        product = TestHelper.createProduct();
    }

    /**
     * Creates a new invoice mapper.
     *
     * @return a new mapper
     */
    private InvoiceMapperImpl createMapper() {
        InvoiceMapperImpl mapper = new InvoiceMapperImpl();
        mapper.setPracticeRules(new PracticeRules());
        mapper.setLookupService(LookupServiceHelper.getLookupService());
        mapper.setArchetypeService(getArchetypeService());
        mapper.setBeanFactory(new IMObjectBeanFactory(getArchetypeService()));
        return mapper;
    }

}
