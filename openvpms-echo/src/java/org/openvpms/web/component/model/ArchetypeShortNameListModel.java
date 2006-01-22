package org.openvpms.web.component.model;

import java.util.List;

import nextapp.echo2.app.list.AbstractListModel;
import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.Messages;


/**
 * Archetype short name list model.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class ArchetypeShortNameListModel extends AbstractListModel {

    /**
     * Short name indicating that all values apply.
     */
    public static final String ALL = "all";

    /**
     * Localised displlay name for "all".
     */
    private final String ALL_LOCALISED = Messages.get("selectfield.all");

    /**
     * The short names. The first column is the short name, the second the
     * corresponding display name.
     */
    private final String[][] _shortNames;

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     */
    public ArchetypeShortNameListModel(String[] shortNames) {
        this(shortNames, false);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code>, add a localised "All"
     */
    public ArchetypeShortNameListModel(String[] shortNames, boolean all) {
        this(shortNames, all, ServiceHelper.getArchetypeService());
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code>, add a localised "All"
     */
    public ArchetypeShortNameListModel(List<String> shortNames, boolean all) {
        this(shortNames.toArray(new String[0]), all,
                ServiceHelper.getArchetypeService());
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code> add a localised "All"
     * @param service    the archetype service
     */
    public ArchetypeShortNameListModel(String[] shortNames,
                                       boolean all,
                                       IArchetypeService service) {
        int size = shortNames.length;
        int index = 0;
        if (all) {
            ++size;
        }
        _shortNames = new String[size][2];
        if (all) {
            _shortNames[index][0] = ALL;
            _shortNames[index][1] = ALL_LOCALISED;
            ++index;
        }
        for (int i = 0; i < shortNames.length; ++i, ++index) {
            String shortName = shortNames[i];
            ArchetypeDescriptor descriptor
                    = service.getArchetypeDescriptor(shortName);
            _shortNames[index][0] = shortName;
            String displayName = descriptor.getDisplayName();
            if (StringUtils.isEmpty(displayName)) {
                displayName = shortName;
            }
            _shortNames[index][1] = displayName;
        }
    }

    /**
     * Returns the value at the specified index in the list.
     *
     * @param index the index
     * @return the value
     */
    public Object get(int index) {
        return _shortNames[index][1];
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    public int size() {
        return _shortNames.length;
    }

    /**
     * Returns the short name at the specified index in the list
     *
     * @param index the index
     * @return the short name
     */
    public String getShortName(int index) {
        return _shortNames[index][0];
    }

}
