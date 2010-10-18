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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ubl.InvoiceType;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.exception.ESCIException;
import org.openvpms.esci.service.InvoiceService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;


/**
 * Implementation of the {@link InvoiceService} that maps invoices to <em>act.supplierDelivery</em> acts using
 * {@link InvoiceMapper}.
 * <p/>
 * UBL invoices are mapped to deliveries rather than <em>act.supplierAccountChargesInvoice</em> to reflect the fact
 * that practices may not use suplier invoices. An invoice can be created from the delivery if required.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceServiceAdapter implements InvoiceService {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The invoice mapper.
     */
    private InvoiceMapper mapper;

    /**
     * The listener to notify when an invoice is received. May be <tt>null</tt>
     */
    private InvoiceListener listener;

    /**
     * The user rules.
     */
    private UserRules rules;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(InvoiceServiceAdapter.class);


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
     * Registers the invoice mapper.
     *
     * @param mapper the invoice mapper
     */
    @Resource
    public void setInvoiceMapper(InvoiceMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Registers a listener to be notified when an invoice is received.
     *
     * @param listener the listener to notify. May be <tt>null</tt>
     */
    @Resource
    public void setInvoiceListener(InvoiceListener listener) {
        this.listener = listener;
    }

    /**
     * Registers the user rules.
     *
     * @param rules the user rules
     */
    @Resource
    public void setUserRules(UserRules rules) {
        this.rules = rules;
    }

    /**
     * Submits an invoice.
     *
     * @param invoice the invoice to submit.
     * @throws ESCIException for any error
     */
    public void submitInvoice(InvoiceType invoice) throws ESCIException {
        try {
            User user = getUser();
            if (user == null) {
                Message message = ESCIAdapterMessages.noESCIUser();
                throw new ESCIException(message.toString());
            }
            Delivery delivery = mapper.map(invoice, user);
            service.save(delivery.getActs());
            notifyListener(delivery.getDelivery());
        } catch (ESCIException exception) {
            throw exception;
        } catch (Throwable exception) {
            Message message = ESCIAdapterMessages.failedToSubmitInvoice(exception.getMessage());
            throw new ESCIException(message.toString(), exception);
        }
    }

    /**
     * Returns the current ESCI user.
     *
     * @return the user, or <tt>null</tt> if none is found
     */
    protected User getUser() {
        User result = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            User user = rules.getUser(auth.getName());
            if (TypeHelper.isA(user, UserArchetypes.ESCI_USER)) {
                result = user;
            }
        }
        return result;
    }

    /**
     * Notifies the registered listener that an invoice has been received and processed.
     *
     * @param delivery the delivery that the invoice was mapped to
     */
    private void notifyListener(Act delivery) {
        InvoiceListener l = listener;
        if (l != null) {
            try {
                l.receivedInvoice(delivery);
            } catch (Throwable exception) {
                log.error("InvoiceListener threw exception", exception);
            }
        }
    }

}
