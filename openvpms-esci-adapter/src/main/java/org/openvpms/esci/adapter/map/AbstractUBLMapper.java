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
package org.openvpms.esci.adapter.map;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.util.ESCIAdapterException;

import javax.annotation.Resource;


/**
 * Base class for mappers between UBL to OpenVPMS types.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractUBLMapper {

    /**
     * Expected UBL version.
     */
    protected static final String UBL_VERSION = "2.0";

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Registers the archetype service.
     *
     * @param service the archetype service
     */
    @Resource
    public void setArchetypeService(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Verifies that the UBL version matches that expected.
     *
     * @param document the UBL document
     * @throws ESCIAdapterException if the UBL identifier is <tt>null</tt> or not the expected value
     */
    protected void checkUBLVersion(UBLDocument document) {
        String version = document.getUBLVersionID();
        if (!UBL_VERSION.equals(version)) {
            throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue("UBLVersionID", document.getType(),
                                                                               document.getID(), UBL_VERSION, version));
        }
    }

    /**
     * Verifies that an order has a relationship to the expected supplier and stock location.
     *
     * @param order         the order
     * @param supplier      the suppplier
     * @param stockLocation the stock location
     * @param document      the invoice  @throws ESCIAdapterException      if the order wasn't submitted by the supplier
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void checkOrder(FinancialAct order, Party supplier, Party stockLocation, UBLDocument document) {
        ActBean bean = new ActBean(order, service);
        IMObjectReference supplierRef = bean.getNodeParticipantRef("supplier");
        IMObjectReference locationRef = bean.getNodeParticipantRef("stockLocation");
        if (!ObjectUtils.equals(supplierRef, supplier.getObjectReference())
            || !ObjectUtils.equals(locationRef, stockLocation.getObjectReference())) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidOrder(document.getType(), document.getID(),
                                                                            Long.toString(order.getId())));
        }
    }

}
