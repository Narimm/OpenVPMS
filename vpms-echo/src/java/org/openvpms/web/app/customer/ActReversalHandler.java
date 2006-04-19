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

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.util.AbstractIMObjectCopyHandler;
import org.openvpms.web.component.im.util.IMObjectCopier;
import org.openvpms.web.component.im.util.IMObjectCopyHandler;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * {@link IMObjectCopyHandler} that creates reversals for the acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
class ActReversalHandler extends AbstractIMObjectCopyHandler {

    /**
     * Invoice act short name.
     */
    private static final String INVOICE_TYPE
            = "act.customerAccountChargesInvoice";

    /**
     * Invoice act item short name.
     */
    private static final String INVOICE_ITEM_TYPE
            = "act.customerAccountInvoiceItem";

    /**
     * Invoice act item relationship short name.
     */
    private static final String INVOICE_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountInvoiceItem";

    /**
     * Counter act short name.
     */
    private static final String COUNTER_TYPE
            = "act.customerAccountChargesCounter";

    /**
     * Counter act item short name.
     */
    private static final String COUNTER_ITEM_TYPE
            = "act.customerAccountChargesCounterItem";

    /**
     * Counter act item relationship type.
     */
    private static final String COUNTER_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountChargesCounterItem";

    /**
     * Credit act type.
     */
    private static final String CREDIT_TYPE
            = "act.customerAccountChargesCredit";

    /**
     * Credit item act type.
     */
    private static final String CREDIT_ITEM_TYPE
            = "act.customerAccountCreditItem";

    /**
     * Credit item act relationship type.
     */
    private static final String CREDIT_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountCreditItem";

    /**
     * Payment act type.
     */
    private static final String PAYMENT_TYPE = "act.customerAccountPayment";

    /**
     * Payment act relationship item type.
     */
    private static final String PAYMENT_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountPaymentItem";

    /**
     * Cash payment type.
     */
    private static final String PAYMENT_CASH_TYPE
            = "act.customerAccountPaymentCash";

    /**
     * Cheque payment type.
     */
    private static final String PAYMENT_CHEQUE_TYPE
            = "act.customerAccountPaymentCheque";

    /**
     * Credit payment type.
     */
    private static final String PAYMENT_CREDIT_TYPE
            = "act.customerAccountPaymentCredit";

    /**
     * EFT payment type.
     */
    private static final String PAYMENT_EFT_TYPE
            = "act.customerAccountPaymentEFT";

    /**
     * Refund act type.
     */
    private static final String REFUND_TYPE = "act.customerAccountRefund";

    /**
     * Refund act relationship item type.
     */
    private static final String REFUND_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountRefundItem";

    /**
     * Cash refund type.
     */
    private static final String REFUND_CASH_TYPE
            = "act.customerAccountRefundCash";

    /**
     * Cheque refund type.
     */
    private static final String REFUND_CHEQUE_TYPE
            = "act.customerAccountRefundCheque";

    /**
     * Credit refund type.
     */
    private static final String REFUND_CREDIT_TYPE
            = "act.customerAccountRefundCredit";

    /**
     * EFT refund type.
     */
    private static final String REFUND_EFT_TYPE = "act.customerAccountRefundEFT";

    /**
     * Determines if the act is a debit or a credit.
     */
    private final boolean _debit;


    /**
     * Map of debit types to their corresponding credit types.
     */
    private static final String[][] TYPE_MAP = {
            {INVOICE_TYPE, CREDIT_TYPE},
            {INVOICE_ITEM_TYPE, CREDIT_ITEM_TYPE},
            {INVOICE_ITEM_RELATIONSHIP_TYPE, CREDIT_ITEM_RELATIONSHIP_TYPE},
            {COUNTER_TYPE, CREDIT_TYPE},
            {COUNTER_ITEM_TYPE, CREDIT_ITEM_TYPE},
            {COUNTER_ITEM_RELATIONSHIP_TYPE, CREDIT_ITEM_RELATIONSHIP_TYPE},
            {PAYMENT_TYPE, REFUND_TYPE},
            {PAYMENT_ITEM_RELATIONSHIP_TYPE, REFUND_ITEM_RELATIONSHIP_TYPE},
            {PAYMENT_CASH_TYPE, REFUND_CASH_TYPE},
            {PAYMENT_CHEQUE_TYPE, REFUND_CHEQUE_TYPE},
            {PAYMENT_CREDIT_TYPE, REFUND_CREDIT_TYPE},
            {PAYMENT_EFT_TYPE, REFUND_EFT_TYPE}
    };


    /**
     * Construct a new <code>ActReversalHandler</code>.
     *
     * @param act the act to reverse
     */
    public ActReversalHandler(Act act) {
        _debit = !IMObjectHelper.isA(act, CREDIT_TYPE, REFUND_TYPE);
    }

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
        if (object instanceof Act || object instanceof ActRelationship
            || object instanceof Participation) {
            String shortName = object.getArchetypeId().getShortName();
            for (String[] map : TYPE_MAP) {
                String debitType = map[0];
                String creditType = map[1];
                if (_debit) {
                    if (debitType.equals(shortName)) {
                        shortName = creditType;
                        break;
                    }
                } else {
                    if (creditType.equals(shortName)) {
                        shortName = debitType;
                        break;
                    }
                }
            }
            result = service.create(shortName);
            if (result == null) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToCreateArchetype,
                        new String[]{shortName});
            }
        } else {
            result = object;
        }
        return result;
    }
}
