/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.system.smartflow;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.smartflow.client.FlowSheetException;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.event.EventStatus;
import org.openvpms.smartflow.event.SmartFlowSheetEventService;
import org.openvpms.web.component.error.ErrorFormatter;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * Smart Flow Sheet status at a practice location.
 *
 * @author Tim Anderson
 */
class Status {

    public enum ComponentStatus {
        NOT_CONFIGURED,
        CONNECTED,
        ERROR,
    }

    /**
     * The location.
     */
    private final Party location;

    /**
     * The event service.
     */
    private final SmartFlowSheetEventService service;

    /**
     * The flow sheet service factory.
     */
    private final FlowSheetServiceFactory factory;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The clinic API key
     */
    private final String key;

    /**
     * The overall status message.
     */
    private String status;

    /**
     * The API status.
     */
    private ComponentStatus apiStatus = ComponentStatus.NOT_CONFIGURED;

    /**
     * The API error message.
     */
    private String apiError;

    /**
     * The queue status.
     */
    private ComponentStatus queueStatus = ComponentStatus.NOT_CONFIGURED;

    /**
     * The queue error message.
     */
    private String queueError;

    /**
     * The event service status.
     */
    private EventStatus eventStatus;

    /**
     * Constructs a {@link Status}.
     *
     * @param location        the practice location
     * @param service         the Smart Flow Sheet event service
     * @param factory         the factory
     * @param practiceService the practice service
     */
    public Status(Party location, SmartFlowSheetEventService service, FlowSheetServiceFactory factory,
                  PracticeService practiceService) {
        this.location = location;
        this.factory = factory;
        this.service = service;
        this.practiceService = practiceService;
        key = factory.getClinicAPIKey(location);
        refresh();
    }

    /**
     * Returns the location identifier.
     *
     * @return the location identifier
     */
    public long getId() {
        return location.getId();
    }

    /**
     * Returns the location name.
     *
     * @return the location name
     */
    public String getName() {
        return location.getName();
    }

    /**
     * Returns a shortened version of the Clinic API Key, for security purposes.
     *
     * @return the key
     */
    public String getDisplayKey() {
        String result = null;
        if (key != null) {
            result = (key.length() >= 10) ? StringUtils.abbreviate(key, 10) : key;
        }
        return result;
    }

    /**
     * Returns an overall status message.
     *
     * @return the status message
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the API status.
     *
     * @return the API status
     */
    public ComponentStatus getAPIStatus() {
        return apiStatus;
    }

    /**
     * Returns the API error.
     *
     * @return the API error, or {@code null} if the {@link #getAPIStatus()} {@code != ERROR}
     */
    public String getAPIError() {
        return apiError;
    }

    /**
     * Returns the queue status.
     *
     * @return the queue status
     */
    public ComponentStatus getQueueStatus() {
        return queueStatus;
    }

    /**
     * Returns the queue error.
     *
     * @return the queue error, or {@code null} if the {@link #getQueueStatus()} {@code != ERROR}
     */
    public String getQueueError() {
        return queueError != null ? queueError : eventStatus.getErrorMessage();
    }

    /**
     * Returns the time when an event was last received.
     *
     * @return the time when an event was last received, or {@code null} if SFS is not configured or no event has
     * been received
     */
    public Date getEventReceived() {
        return eventStatus != null ? eventStatus.getReceived() : null;
    }

    /**
     * Refreshes the status.
     */
    public void refresh() {
        apiStatus = ComponentStatus.NOT_CONFIGURED;
        queueStatus = ComponentStatus.NOT_CONFIGURED;
        status = null;
        apiError = null;
        queueError = null;
        eventStatus = null;
        if (key != null) {
            try {
                // try and get departments to determine if the API connection is working
                factory.getReferenceDataService(location).getDepartments();
                apiStatus = ComponentStatus.CONNECTED;
            } catch (Throwable exception) {
                apiStatus = ComponentStatus.ERROR;
                apiError = getError(exception);
            }
            try {
                factory.getReferenceDataService(location).getServiceBusConfig();
                eventStatus = service.getStatus(location);
                if (eventStatus.getError() == null) {
                    if (practiceService.getServiceUser() == null) {
                        queueStatus = ComponentStatus.ERROR;
                        queueError = Messages.get("admin.system.smartflow.noserviceuser");
                    } else {
                        queueStatus = ComponentStatus.CONNECTED;
                    }
                } else {
                    queueStatus = ComponentStatus.ERROR;
                    queueError = eventStatus.getErrorMessage();
                }
            } catch (Throwable exception) {
                queueStatus = ComponentStatus.ERROR;
                queueError = getError(exception);
            }
        }

        if (apiStatus == ComponentStatus.ERROR || queueStatus == ComponentStatus.ERROR) {
            status = Messages.get("admin.system.smartflow.error");
        } else if (apiStatus == ComponentStatus.NOT_CONFIGURED || queueStatus == ComponentStatus.NOT_CONFIGURED) {
            status = Messages.get("admin.system.smartflow.notconfigured");
        } else {
            status = Messages.get("admin.system.smartflow.connected");
        }
    }

    /**
     * Returns the API status name.
     *
     * @return the API status name
     */
    public String getAPIStatusName() {
        return getStatusName(apiStatus);
    }

    /**
     * Returns the queue status name.
     *
     * @return the queue status name
     */
    public String getQueueStatusName() {
        return getStatusName(queueStatus);
    }

    /**
     * Returns the status name.
     *
     * @param status the component status
     * @return the status name
     */
    private String getStatusName(ComponentStatus status) {
        String result;
        switch (status) {
            case NOT_CONFIGURED:
                result = Messages.get("admin.system.smartflow.notconfigured");
                break;
            case CONNECTED:
                result = Messages.get("admin.system.smartflow.connected");
                break;
            default:
                result = Messages.get("admin.system.smartflow.error");
        }
        return result;
    }

    /**
     * Helper to return an appropriate message from an exception.
     *
     * @param exception the exception
     * @return the exception message
     */
    private String getError(Throwable exception) {
        String result;
        if (exception instanceof FlowSheetException) {
            result = exception.getMessage();
        } else {
            result = ErrorFormatter.format(exception);
        }
        return result;
    }

}
