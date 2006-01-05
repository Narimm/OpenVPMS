package org.openvpms.web.component.model;

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.list.AbstractListModel;
import org.openvpms.component.business.domain.im.lookup.Lookup;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class LookupListModel extends AbstractListModel {

    private List<Lookup> _lookups;

    private boolean _allowNone;

    public static String NONE_LABEL = "None";

    public static String NONE_VALUE = "none";


    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param lookups the lookups to populate the list with.
     */
    public LookupListModel(List<Lookup> lookups) {
        this(lookups, false);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param lookups   the lookups to populate the list with.
     * @param allowNone determines if a value is required
     */
    public LookupListModel(List<Lookup> lookups, boolean allowNone) {
        _lookups = lookups;
        _allowNone = allowNone;
        if (allowNone) {
            _lookups = new ArrayList<Lookup>(lookups);
            _lookups.add(0, new Lookup(null, "", ""));
        }
    }

    /**
     * Returns the value at the specified index in the list.
     *
     * @param index the index
     * @return the value
     */
    public Object get(int index) {
        String value;
        if (_allowNone && index == 0) {
            value = NONE_VALUE;
        } else {
            value = _lookups.get(index).getValue();
        }
        return value;
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    public int size() {
        return _lookups.size();
    }

    /**
     * Returns the index of the specified value.
     *
     * @param value the value
     * @return the index of <code>value</code>, or <code>-1</code> if it
     *         doesn't exist
     */
    public int indexOf(String value) {
        int result = -1;
        for (int i = 0; i < _lookups.size(); ++i) {
            Lookup lookup = _lookups.get(i);
            if ((value != null && value.equals(lookup.getValue()))
                || (value == null && lookup.getValue() == null)) {
                result = i;
                break;
            }
        }
        return result;
    }

}
