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
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.IMObjectCopier;
import org.openvpms.web.component.im.util.IMObjectCopyHandler;
import org.openvpms.web.component.util.ButtonFactory;


/**
 * CRUD window for estimation acts.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class EstimationCRUDWindow extends ActCRUDWindow {

    /**
     * The copy button.
     */
    private Button _copy;

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
        _copy = ButtonFactory.create(COPY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onCopy();
            }
        });
        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
        buttons.add(getPrintButton());
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
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            buttons.add(getPrintButton());
            buttons.add(_copy);
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {
        IMObject object = getObject();
        try {
            IMObjectCopier copier = new IMObjectCopier(new ActCopyHandler());
            Act act = (Act) copier.copy(object);
            act.setStatus(INPROGRESS_STATUS);
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
