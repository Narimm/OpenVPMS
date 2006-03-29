package org.openvpms.web.app.customer;

import nextapp.echo2.app.Row;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.create.IMObjectCreatorListener;


/**
 * CRUD window for invoices.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class InvoiceCRUDWindow extends ActCRUDWindow {

    /**
     * Invoice act type.
     */
    private static final String INVOICE_TYPE = "act.customerAccountChargesInvoice";

    /**
     * Credit act type.
     */
    private static final String CREDIT_TYPE = "act.customerAccountChargesCredit";

    /**
     * Counter Sale act type.
     */
    private static final String COUNTER_TYPE = "act.customerAccountChargesCounter";


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
        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
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

        String[] shortNames = {INVOICE_TYPE, CREDIT_TYPE, COUNTER_TYPE};
        IMObjectCreator.create(getTypeDisplayName(), shortNames, listener);
    }

}
