package org.openvpms.web.app.customer;

import java.util.Date;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.ValidationHelper;
import org.openvpms.web.component.im.util.IMObjectCopier;
import org.openvpms.web.component.im.util.IMObjectCopyHandler;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.resource.util.Messages;
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
     * Posted status type. Acts with this status can't be edited or deleted.
     */
    private static final String POSTED_STATUS = "Posted";


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
     * Invoked when the edit button is pressed. This popups up an {@link
     * EditDialog}.
     */
    @Override
    protected void onEdit() {
        IMObject object = getObject();
        if (object != null) {
            Act act = (Act) object;
            String status = act.getStatus();
            if (!status.equals(POSTED_STATUS)) {
                super.onEdit();
            } else {
                String title = Messages.get("customer.estimation.noedit.title");
                String message = Messages.get("customer.estimation.noedit.message", status);
                ErrorDialog.show(title, message);
            }
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
        super.onSaved(object, isNew);
    }

    /**
     * Invoked when the delete button is pressed.
     */
    @Override
    protected void onDelete() {
        Act act = (Act) getObject();
        String status = act.getStatus();
        if (!POSTED_STATUS.equals(status)) {
            super.onDelete();
        } else {
            String title = Messages.get("customer.estimation.nodelete.title");
            String message = Messages.get("customer.estimation.nodelete.message", status);
            ErrorDialog.show(title, message);
        }
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    protected void onPrint() {
        String title = Messages.get("customer.estimation.print.title");
        String message = Messages.get("customer.estimation.print.message");
        ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addActionListener(ConfirmationDialog.OK_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doPrint();
            }
        });
        dialog.show();
    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {
        IMObject object = getObject();
        try {
            IMObjectCopier copier = new IMObjectCopier(new ActCopyHandler());
            Act act = (Act) copier.copy(object);
            act.setStatus("In Progress");
            act.setActivityStartTime(new Date());
            setPrintStatus(act, false);
            SaveHelper.save(act);
            setObject(act);
            CRUDWindowListener listener = getListener();
            if (listener != null) {
                listener.saved(act, false);
            }
        } catch (ArchetypeServiceException exception) {
            ErrorDialog.show(exception);
        } catch (DescriptorException exception) {
            ErrorDialog.show(exception);
        }
    }

    /**
     * Print the act, updating its status if required.
     */
    private void doPrint() {
        Act act = (Act) getObject();
        String status = act.getStatus();
        if (!POSTED_STATUS.equals(status)) {
            act.setStatus(POSTED_STATUS);
            setPrintStatus(act, true);
            SaveHelper.save(act);
            setObject(act);
            CRUDWindowListener listener = getListener();
            if (listener != null) {
                listener.saved(act, false);
            }
        }
    }

    /**
     * Sets the print status.
     *
     * @param act the act
     * @param printed the print status
     */
    private void setPrintStatus(Act act, boolean printed) {
        ArchetypeDescriptor archetype = getArchetypeDescriptor();
        NodeDescriptor descriptor = archetype.getNodeDescriptor("printed");
        if (descriptor != null) {
            descriptor.setValue(act, printed);
        }
    }


    private class ActCopyHandler implements IMObjectCopyHandler {

        /**
         * Determines if an object should be copied
         *
         * @param object the object to check
         * @param parent the parent of <code>object</code>. May be
         *               <code>null</code>
         * @return <code>true</code> if the object should be copied; otherwise
         *         <code>false</code>
         */
        public boolean copy(IMObject object, IMObject parent) {
            boolean result = false;
            if (object instanceof Act || object instanceof ActRelationship
                || object instanceof Participation) {
                result = true;
            }
            return result;
        }
    }

}
