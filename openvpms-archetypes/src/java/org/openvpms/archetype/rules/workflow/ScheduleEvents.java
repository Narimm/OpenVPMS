package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.List;

/**
 * Schedule events.
 *
 * @author Tim Anderson
 */
public class ScheduleEvents {

    /**
     * The events.
     */
    private final List<PropertySet> events;

    /**
     * The modification hash.
     */
    private final long modHash;

    /**
     * Constructs a {@link ScheduleEvents}.
     *
     * @param events  the events
     * @param modHash the modification hash
     */
    public ScheduleEvents(List<PropertySet> events, long modHash) {
        this.events = events;
        this.modHash = modHash;
    }

    /**
     * Returns the events.
     *
     * @return the events
     */
    public List<PropertySet> getEvents() {
        return events;
    }

    /**
     * Returns the number of events.
     *
     * @return the number of events
     */
    public int size() {
        return events.size();
    }

    /**
     * Returns the modification hash.
     * <p>
     * This can be used to determine if the events have been modified since they were last accessed.
     *
     * @return the modification hash
     */
    public long getModHash() {
        return modHash;
    }

    /**
     * Returns an event given its reference.
     *
     * @param reference the reference
     * @return the event, or {@code null} if none is found
     */
    public PropertySet getEvent(Reference reference) {
        for (PropertySet set : events) {
            if (reference.equals(set.getReference(ScheduleEvent.ACT_REFERENCE))) {
                return set;
            }
        }
        return null;
    }

}
