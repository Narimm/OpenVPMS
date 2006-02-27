package org.openvpms.web.component.im.create;

import java.util.List;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.SelectionDialog;
import org.openvpms.web.component.im.list.ArchetypeShortNameListModel;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * {@link IMObject} creator.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public final class IMObjectCreator {

    /**
     * Prevent construction.
     */
    private IMObjectCreator() {
    }

    /**
     * Create a new object of the specified archetype.
     *
     * @param shortName the archetype shortname
     * @return a new object, or <code>null</code> if the short name is not known
     */
    public static IMObject create(String shortName) {
        IMObject result = null;
        IArchetypeService service = ServiceHelper.getArchetypeService();
        try {
            result = service.create(shortName);
        } catch (ArchetypeServiceException exception) {
            ErrorDialog.show(exception);
        }
        return result;
    }

    /**
     * Create a new object, selecting from a list of short names that matches
     * the supplied criteria.
     *
     * @param type         display name for the type of the object
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @param listener     the listener to notify
     */
    public static void create(String type, String refModelName,
                              String entityName, String conceptName,
                              IMObjectCreatorListener listener) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<String> shortNames = service.getArchetypeShortNames(
                refModelName, entityName, conceptName, true);
        if (shortNames.isEmpty()) {
            ErrorDialog.show("Cannot create object",
                    "No archetypes matches reference model="
                            + refModelName + ", entity=" + entityName
                            + ", concept=" + conceptName);
        } else {
            create(type, shortNames, listener);
        }
    }

    /**
     * Create a new object, selected from a list. This implementation pops up a
     * selection dialog if needed.
     *
     * @param shortNames the archetype shortnames
     * @param listener   the listener to notify
     */
    public static void create(String type, List<String> shortNames,
                              final IMObjectCreatorListener listener) {
        create(type, shortNames.toArray(new String[0]), listener);
    }

    /**
     * Create a new object, selected from a list. This implementation pops up a
     * selection dialog if needed.
     *
     * @param shortNames the archetype shortnames
     * @param listener   the listener to notify
     */
    public static void create(String type, String[] shortNames,
                              final IMObjectCreatorListener listener) {
        if (shortNames.length == 0) {
            ErrorDialog.show("Cannot create object of type " + type
                             + " Empty list of short-names supplied");
        } else if (shortNames.length > 1) {
            final ArchetypeShortNameListModel model
                    = new ArchetypeShortNameListModel(shortNames, false);
            String title = Messages.get("imobject.new.title", type);
            String message = Messages.get("imobject.new.message", type);
            final SelectionDialog dialog
                    = new SelectionDialog(title, message, model);
            dialog.addActionListener(
                    SelectionDialog.OK_ID, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int selected = dialog.getSelectedIndex();
                    if (selected != -1) {
                        IMObject object = create(model.getShortName(selected));
                        if (object != null) {
                            listener.created(object);
                        }
                    }
                }

            });
            dialog.show();
        } else {
            IMObject object = create(shortNames[0]);
            if (object != null) {
                listener.created(object);
            }
        }
    }

}
