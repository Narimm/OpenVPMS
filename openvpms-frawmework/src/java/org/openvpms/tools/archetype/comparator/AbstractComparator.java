package org.openvpms.tools.archetype.comparator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for archetype comparator.
 *
 * @author Tim Anderson
 */
public class AbstractComparator {

    /**
     * Determines the keys that have been added between two versions of a map.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @return the added keys
     */
    protected <T> Set<String> getAdded(Map<String, T> oldVersion, Map<String, T> newVersion) {
        Set<String> result = new HashSet<String>(newVersion.keySet());
        result.removeAll(oldVersion.keySet());
        return result;
    }

    /**
     * Determines the keys that have been deleted between two versions of a map.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @return the deleted keys
     */
    protected <T> Set<String> getDeleted(Map<String, T> oldVersion, Map<String, T> newVersion) {
        Set<String> result = new HashSet<String>(oldVersion.keySet());
        result.removeAll(newVersion.keySet());
        return result;
    }

    /**
     * Determines the keys that have been retained between two versions of a map.
     *
     * @param oldVersion the old version
     * @param newVersion the new version
     * @return the retained keys
     */
    protected <T> Set<String> getRetained(Map<String, T> oldVersion, Map<String, T> newVersion) {
        Set<String> result = new HashSet<String>(oldVersion.keySet());
        result.retainAll(newVersion.keySet());
        return result;
    }

}
