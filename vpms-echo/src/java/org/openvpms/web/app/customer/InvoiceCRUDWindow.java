package org.openvpms.web.app.customer;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.Act;
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
     * Reverse an invoice.
     *
     * @param act the invoice act
     */
    private void reverse(Act act) {

    }

}
