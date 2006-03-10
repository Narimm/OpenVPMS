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
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.create.IMObjectCreatorListener;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.IMObjectCopier;
import org.openvpms.web.component.im.util.IMObjectCopyHandler;
import org.openvpms.web.component.util.ButtonFactory;


/**
 * CRUD window for invoices.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class InvoiceCRUDWindow extends ActCRUDWindow {

    /**
     * The reverse button.
     */
    private Button _reverse;

    /**
     * Reverse button identifier.
     */
    private static final String REVERSE_ID = "reverse";

    /**
     * Invoice act type.
     */
    private static final String INVOICE_TYPE = "act.customerInvoice";

    /**
     * Credit act type.
     */
    private static final String CREDIT_TYPE = "act.customerCredit";


    /**
     * Create a new <code>EstimationCRUDWindow</code>.
     *
     * @param type         display name for the types of objects that this may
     *                     create
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public InvoiceCRUDWindow(String type, String refModelName,
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
        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
        buttons.add(_reverse);
        buttons.add(getPrintButton());
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
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            buttons.add(_reverse);
            buttons.add(getPrintButton());
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Invoked when the 'new' button is pressed.
     * <p/>
     * Need to override the default, as the archetypes cannot be specified using
     * wildcard.
     */
    public void onCreate() {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated(object);
            }
        };

        String[] shortNames = {INVOICE_TYPE, CREDIT_TYPE};
        IMObjectCreator.create(getTypeDisplayName(), shortNames, listener);
    }


    /**
     * Invoked when the 'reverse' button is pressed.
     */
    protected void onReverse() {
        Act act = (Act) getObject();
        String status = act.getStatus();
        if (POSTED_STATUS.equals(status)) {
            reverse(act);
        } else {
            showStatusError(act, "customer.invoice.noreverse.title",
                            "customer.invoice.noreverse.message");
        }
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
            setObject(reversal);
            CRUDWindowListener listener = getListener();
            if (listener != null) {
                listener.saved(reversal, false);
            }
        } catch (ArchetypeServiceException exception) {
            ErrorDialog.show(exception);
        } catch (DescriptorException exception) {
            ErrorDialog.show(exception);
        }
    }


    private static class ReversalHandler implements IMObjectCopyHandler {

        /**
         * Determines if the act is an invoice or a credit.
         */
        private final boolean _invoice;

        /**
         * Map of invoice types to their corresponding credit types.
         */
        private static final String[][] TYPE_MAP = {
                {INVOICE_TYPE, CREDIT_TYPE},
                {"act.customerInvoiceItem", "act.customerCreditItem"},
                {"actRelationship.customerInvoiceItem",
                 "actRelationship.customerCreditItem"}};


        /**
         * Construct a nedw <code>ReversalHandler</code>.
         *
         * @param act the act to reverse
         */
        public ReversalHandler(Act act) {
            if (act.getArchetypeId().getShortName().equals(INVOICE_TYPE)) {
                _invoice = true;
            } else {
                _invoice = false;
            }
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
                String replace = null;
                String shortName = object.getArchetypeId().getShortName();
                for (String[] map : TYPE_MAP) {
                    String invoiceType = map[0];
                    String creditType = map[1];
                    if (_invoice) {
                        if (invoiceType.equals(shortName)) {
                            replace = creditType;
                        }
                    } else {
                        if (creditType.equals(shortName)) {
                            replace = invoiceType;
                        }
                    }
                }
                if (replace != null) {
                    result = service.create(replace);
                } else {
                    result = service.create(shortName);
                }
            } else {
                result = object;
            }
            return result;
        }
    }

}
