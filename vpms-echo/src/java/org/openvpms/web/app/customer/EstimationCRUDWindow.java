package org.openvpms.web.app.customer;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.ValidationHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.spring.ServiceHelper;

/**
 * CRUD window for estimation acts.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class EstimationCRUDWindow extends CRUDWindow {

    /**
     * The edit button.
     */
    private Button _edit;

    /**
     * The new button.
     */
    private Button _create;

    /**
     * The delete button.
     */
    private Button _delete;

    /**
     * The print button.
     */
    private Button _print;

    /**
     * The copy button.
     */
    private Button _copy;

    /**
     * Edit button identifier.
     */
    private static final String EDIT_ID = "edit";

    /**
     * New button identifier.
     */
    private static final String NEW_ID = "new";

    /**
     * Delete button identifier.
     */
    private static final String DELETE_ID = "delete";

    /**
     * Print button identifier.
     */
    private static final String PRINT_ID = "print";

    /**
     * Copy button identifier.
     */
    private static final String COPY_ID = "copy";


    /**
     * Create a new <code>EstimationCRUDWindow</code>.
     *
     * @param type         display name for the types of objects that this may
     *                     create
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public EstimationCRUDWindow(String type, String refModelName,
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
        _edit = ButtonFactory.create(EDIT_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onEdit();
            }
        });
        _create = ButtonFactory.create(NEW_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onCreate();
            }
        });
        _delete = ButtonFactory.create(DELETE_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });
        _print = ButtonFactory.create(PRINT_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onPrint();
            }
        });
        _copy = ButtonFactory.create(COPY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onCopy();
            }
        });
        buttons.add(_edit);
        buttons.add(_create);
        buttons.add(_delete);
        buttons.add(_print);
        buttons.add(_copy);
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
            buttons.add(_edit);
            buttons.add(_create);
            buttons.add(_delete);
            buttons.add(_print);
            buttons.add(_copy);
        } else {
            buttons.add(_create);
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        super.onSaved(object, isNew);
        if (isNew) {
            Act act = (Act) object;
            Party customer = Context.getInstance().getCustomer();
            if (customer != null) {
                try {
                    IArchetypeService service
                            = ServiceHelper.getArchetypeService();
                    Participation participation
                            = (Participation) service.create("participation.customer");
                    participation.setEntity(new IMObjectReference(customer));
                    participation.setAct(new IMObjectReference(act));
                    act.addParticipation(participation);

                    if (ValidationHelper.isValid(act)) {
                        service.save(act);
                    }
                } catch (ArchetypeServiceException exception) {
                    ErrorDialog.show(exception);
                } catch (ValidationException exception) {
                    ErrorDialog.show(exception);
                }
            }
        }
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    protected void onPrint() {

    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {

    }


}
