package org.openvpms.web.workspace.admin.system.smartflow;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.smartflow.client.FlowSheetException;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.web.component.error.ErrorFormatter;
import org.openvpms.web.resource.i18n.Messages;

/**
 * .
 *
 * @author Tim Anderson
 */
public class Status {

    private final Party location;

    private final String key;

    private String status;

    public Status(Party location, FlowSheetServiceFactory factory) {
        this.location = location;
        key = factory.getClinicAPIKey(location);
        if (key != null) {
            try {
                factory.getReferenceDataService(location).getDepartments();
                status = Messages.get("admin.system.smartflow.connected");
            } catch (Throwable exception) {
                if (exception instanceof FlowSheetException) {
                    status = exception.getMessage();
                } else {
                    status = ErrorFormatter.format(exception);
                }
            }
        } else {
            status = Messages.get("admin.system.smartflow.notconfigured");
        }
    }

    public long getId() {
        return location.getId();
    }

    public String getName() {
        return location.getName();
    }

    public String getKey() {
        return key;
    }

    public String getStatus() {
        return status;
    }
}
