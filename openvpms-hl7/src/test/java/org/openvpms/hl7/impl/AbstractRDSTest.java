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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v25.message.RDS_O13;
import ca.uhn.hl7v2.model.v25.segment.FT1;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.RXD;
import ca.uhn.hl7v2.model.v25.segment.RXE;
import ca.uhn.hl7v2.util.idgenerator.IDGenerator;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.hl7.MLLPSender;

import java.io.IOException;

/**
 * Tests the {@link RDSProcessor}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractRDSTest extends AbstractMessageTest {

    /**
     * Creates a new product.
     *
     * @return a new product
     */
    protected Product createProduct() {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION, null, false);
        product.setName("Valium 2mg");

        IMObjectBean productBean = new IMObjectBean(product);
        productBean.setValue("dispensingUnits", TestHelper.getLookup("lookup.uom", "TAB", "Tablets", true).getCode());
        productBean.setValue("sellingUnits", TestHelper.getLookup("lookup.uom", "BOX", "Box", true).getCode());
        productBean.setValue("dispInstructions", "Give 1 tablet once daily");
        productBean.save();
        return product;
    }

    /**
     * Creates an RDS O13 message.
     *
     * @param product the dispensed product
     * @return a new message
     * @throws IOException  for any I/O error
     * @throws HL7Exception for any HL7 error
     */
    protected RDS_O13 createRDS(Product product) throws IOException, HL7Exception {
        ILookupService lookups = LookupServiceHelper.getLookupService();
        HapiContext hapiContext = new DefaultHapiContext();
        hapiContext.getParserConfiguration().setIdGenerator(new IDGenerator() {
            @Override
            public String getID() throws IOException {
                return "1200022";
            }
        });

        MessageConfig config = new MessageConfig();
        String fillerOrderNumber = "90032145";
        RDS_O13 message = new RDS_O13(hapiContext.getModelClassFactory());
        message.initQuickstart("RDS", "O13", "P");
        HeaderPopulator header = new HeaderPopulator();
        PIDPopulator pid = new PIDPopulator(getArchetypeService(), LookupServiceHelper.getLookupService());
        PV1Populator pv1 = new PV1Populator();
        header.populate(message, new MLLPSender("107.23.104.102", 2026, "Cubex", "Cubex", "VPMS", "VPMS",
                                                new IMObjectReference("entity.connectorSenderHL7MLLPType", -1)),
                        getDatetime("2014-08-27 09:10:00").getTime(), 7000023, config);
        pid.populate(message.getPATIENT().getPID(), getContext(), config);
        pv1.populate(message.getPATIENT().getPATIENT_VISIT().getPV1(), getContext(), config);
        RXD rxd = message.getORDER().getRXD();
        PopulateHelper.populateProduct(rxd.getDispenseGiveCode(), product);
        rxd.getSubstanceLotNumber(0).setValue("LOT12345678");
        rxd.getSubstanceExpirationDate(0).getTime().setValue(getDatetime("2017-08-24 09:00:00"));
        rxd.getSubstanceManufacturerName(0).getIdentifier().setValue("1234");
        rxd.getSubstanceManufacturerName(0).getText().setValue("Boehringer");
        rxd.getSubstanceManufacturerName(0).getNameOfCodingSystem().setValue("OpenVPMS");
        rxd.getActualDispenseAmount().setValue("2");
        FT1 ft1 = message.getORDER().getFT1();
        ft1.getSetIDFT1().setValue("1");
        ft1.getTransactionDate().getRangeStartDateTime().getTime().setValue(getDatetime("2014-08-25 09:30:00"));
        ft1.getTransactionType().setValue("CG");
        ft1.getTransactionQuantity().setValue("2");
        PopulateHelper.populateClinician(ft1.getOrderedByCode(0), getContext());
        ft1.getFillerOrderNumber().getEntityIdentifier().setValue(fillerOrderNumber);
        PopulateHelper.populateProduct(ft1.getTransactionCode(), product);

        ORC orc = message.getORDER().getORC();
        orc.getOrderControl().setValue("RE");
        orc.getPlacerOrderNumber().getEntityIdentifier().setValue(Long.toString(10231));
        orc.getFillerOrderNumber().getEntityIdentifier().setValue(fillerOrderNumber);
        orc.getDateTimeOfTransaction().getTime().setValue(getDatetime("2014-08-25 09:02:00"));
        PopulateHelper.populateClinician(orc.getEnteredBy(0), getContext());

        RXE rxe = message.getORDER().getENCODING().getRXE();
        PopulateHelper.populateProduct(rxe.getGiveCode(), product);
        IMObjectBean bean = new IMObjectBean(product, getArchetypeService());
        String dispensingCode = bean.getString("dispensingUnits");
        if (dispensingCode != null) {
            String dispensingName = lookups.getName(product, "dispensingUnits");
            PopulateHelper.populateCE(rxd.getActualDispenseUnits(), dispensingCode, dispensingName);
            PopulateHelper.populateCE(rxe.getGiveUnits(), dispensingCode, dispensingName);
        }
        String sellingCode = bean.getString("sellingUnits");
        String dispensingInstructions = bean.getString("dispInstructions");
        if (dispensingInstructions != null) {
            rxe.getProviderSAdministrationInstructions(0).getIdentifier().setValue(dispensingInstructions);
        }
        rxe.getDispenseAmount().setValue("2");
        if (sellingCode != null) {
            String sellingName = lookups.getName(product, "sellingUnits");
            PopulateHelper.populateCE(rxe.getDispenseUnits(), sellingCode, sellingName);
        }

        return message;
    }
}
