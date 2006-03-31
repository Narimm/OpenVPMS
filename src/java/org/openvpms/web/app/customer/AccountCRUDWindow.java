package org.openvpms.web.app.customer;

import java.util.Date;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.AbstractIMObjectCopyHandler;
import org.openvpms.web.component.im.util.IMObjectCopier;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD window for invoices.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class AccountCRUDWindow extends ActCRUDWindow {

    /**
     * The reverse button.
     */
    private Button _reverse;

    /**
     * The statement button.
     */
    private Button _statement;

    /**
     * The adjust button.
     */
    private Button _adjust;

    /**
     * Reverse button identifier.
     */
    private static final String REVERSE_ID = "reverse";

    /**
     * Statement button identifier.
     */
    private static final String STATEMENT_ID = "statement";

    /**
     * Adjust button identifier.
     */
    private static final String ADJUST_ID = "adjust";

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
     * Create a new <code>EstimationCRUDWindow</code>.
     *
     * @param type         display name for the types of objects that this may
     *                     create
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public AccountCRUDWindow(String type, String refModelName,
                             String entityName, String conceptName) {
        super(type, refModelName, entityName, conceptName);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        _reverse = ButtonFactory.create(REVERSE_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onReverse();
            }
        });
        _statement = ButtonFactory.create(STATEMENT_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onStatement();
            }
        });
        _adjust = ButtonFactory.create(ADJUST_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onAdjust();
            }
        });
        buttons.add(_reverse);
        buttons.add(getPrintButton());
        buttons.add(_statement);
        buttons.add(_adjust);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        Row buttons = getButtons();
        buttons.removeAll();
        if (enable) {
            buttons.add(_reverse);
            buttons.add(getPrintButton());
            buttons.add(_statement);
            buttons.add(_adjust);
        }
    }

    /**
     * Invoked when the 'reverse' button is pressed.
     */
    protected void onReverse() {
        final Act act = (Act) getObject();
        String status = act.getStatus();
        if (POSTED_STATUS.equals(status)) {
            String name = getArchetypeDescriptor().getDisplayName();
            String title = Messages.get("customer.account.reverse.title", name);
            String message = Messages.get("customer.account.reverse.message", name);
            ConfirmationDialog dialog = new ConfirmationDialog(title, message);
            dialog.addActionListener(ConfirmationDialog.OK_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    reverse(act);
                }
            });
            dialog.show();
        } else {
            showStatusError(act, "customer.account.noreverse.title",
                            "customer.account.noreverse.message");
        }
    }

    /**
     * Invoked when the 'statement' button is pressed.
     */
    protected void onStatement() {
    }

    /**
     * Invoked when the 'adjust' button is pressed.
     */
    protected void onAdjust() {
    }

    /**
     * Reverse an invoice or credit act.
     *
     * @param act the act to reverse
     */
    private void reverse(Act act) {
        try {
            IMObjectCopier copier
                    = new IMObjectCopier(new ReversalHandler(act));
            Act reversal = (Act) copier.copy(act);
            reversal.setStatus(INPROGRESS_STATUS);
            reversal.setActivityStartTime(new Date());
            setPrintStatus(reversal, false);
            SaveHelper.save(reversal);
        } catch (ArchetypeServiceException exception) {
            ErrorDialog.show(exception);
        } catch (DescriptorException exception) {
            ErrorDialog.show(exception);
        }
    }


    private static class ReversalHandler extends AbstractIMObjectCopyHandler {

        /**
         * Determines if the act is a charge or a credit.
         */
        private final boolean _charge;


        /**
         * Map of invoice types to their corresponding credit types.
         */
        private static final String[][] TYPE_MAP = {
                {INVOICE_TYPE, CREDIT_TYPE},
                {INVOICE_ITEM_TYPE, CREDIT_ITEM_TYPE},
                {INVOICE_ITEM_RELATIONSHIP_TYPE, CREDIT_ITEM_RELATIONSHIP_TYPE},
                {COUNTER_TYPE, CREDIT_TYPE},
                {COUNTER_ITEM_TYPE, CREDIT_ITEM_TYPE},
                {COUNTER_ITEM_RELATIONSHIP_TYPE, CREDIT_ITEM_RELATIONSHIP_TYPE}
        };


        /**
         * Construct a nedw <code>ReversalHandler</code>.
         *
         * @param act the act to reverse
         */
        public ReversalHandler(Act act) {
            String shortName = act.getArchetypeId().getShortName();
            _charge = !shortName.equals(CREDIT_TYPE);
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
                    String chargeType = map[0];
                    String creditType = map[1];
                    if (_charge) {
                        if (chargeType.equals(shortName)) {
                            shortName = creditType;
                            break;
                        }
                    } else {
                        if (creditType.equals(shortName)) {
                            shortName = chargeType;
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

}
