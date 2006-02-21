package org.openvpms.web.component.im.query;

import java.util.List;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.component.im.list.ArchetypeShortNameListModel;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Abstract implementation of the {@link Query} interface that queries {@link
 * IMObject} instances on short name, instance name, and active/inactive
 * status.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractQuery implements Query {

    /**
     * Archetype short names to match on.
     */
    protected final String[] _shortNames;

    /**
     * The instance name. If the text is <code>null</code> or empty, indicates
     * to query all instances.
     */
    private TextField _instanceName;

    /**
     * The inactive check box. If selected, deactived instances will be returned
     * along with the active ones.
     */
    private CheckBox _inactive;

    /**
     * The selected archetype short name. If <code>null</code>, or {@link
     * ArchetypeShortNameListModel#ALL}, indicates to query using all matching
     * short names.
     */
    private String _shortName;

    /**
     * The component representing the query.
     */
    private Component _component;

    /**
     * Type label id.
     */
    private static final String TYPE_ID = "type";

    /**
     * Name label id.
     */
    private static final String NAME_ID = "name";

    /**
     * Deactivated label id.
     */
    private static final String DEACTIVATED_ID = "deactivated";

    /**
     * Button row style name.
     */
    private static final String ROW_STYLE = "ControlRow";

    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified short names.
     *
     * @param shortNames the short names
     */
    public AbstractQuery(String[] shortNames) {
        _shortNames = shortNames;
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public AbstractQuery(String refModelName, String entityName,
                         String conceptName) {
        this(getShortNames(refModelName, entityName, conceptName));
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (_component == null) {
            _component = RowFactory.create(ROW_STYLE);
            doLayout(_component);
        }
        return _component;
    }

    /**
     * Performs the query, returning the matching objects.
     *
     * @return the matching objects
     */
    public List<IMObject> query() {
        String type = getShortName();
        String name = getInstanceName();
        boolean activeOnly = !includeInactive();

        IArchetypeService service = ServiceHelper.getArchetypeService();
        String[] shortNames;
        if (type == null || type.equals(ArchetypeShortNameListModel.ALL)) {
            shortNames = _shortNames;
        } else {
            shortNames = new String[]{type};
        }
        return service.get(shortNames, name, true, activeOnly);
    }

    /**
     * Sets the archetype instance name to query.
     *
     * @param name the archetype instance name. If <code>null</code> indicates
     *             to query all instances
     */
    protected void setInstanceName(String name) {
        _instanceName.setText(name);
    }

    /**
     * Returns the archetype instance name, including wildcards.
     *
     * @return the archetype instance name. Nay be <code>null</code>
     */
    protected String getInstanceName() {
        final String wildcard = "*";
        String name = _instanceName.getText();
        if (!StringUtils.isEmpty(name)) {
            boolean start = name.startsWith(wildcard);
            boolean end = name.endsWith(wildcard);
            if (!start) {
                name = wildcard + name;
            }
            if (!end) {
                name = name + wildcard;
            }
        }
        return name;
    }

    /**
     * Determines if inactive instances should be returned.
     *
     * @return <code>true</code> if inactive instances should be retured;
     *         <code>false</code>
     */
    protected boolean includeInactive() {
        return _inactive.isSelected();
    }

    /**
     * Returns the selected archetype short name.
     *
     * @return the archetype short name. May be <code>null</code>
     */
    protected String getShortName() {
        return _shortName;
    }

    /**
     * Set the archetype short name.
     *
     * @param name the archetype short name. If <code>null</code>, indicates to
     *             query using all matching short names.
     */
    protected void setShortName(String name) {
        _shortName = name;
    }

    /**
     * Lay out the component in a container.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        addShortNameSelector(container);
        addInstanceName(container);
        addInactive(container);
    }

    /**
     * Adds the short name selector to a container.
     *
     * @param container the container
     */
    protected void addShortNameSelector(Component container) {
        // set up the short names select field, iff there is more than
        // one matching short name.
        if (_shortNames.length > 1) {
            final ArchetypeShortNameListModel model
                    = new ArchetypeShortNameListModel(_shortNames, true);
            final SelectField shortNameSelector = SelectFieldFactory.create(model);
            shortNameSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int index = shortNameSelector.getSelectedIndex();
                    String shortName = model.getShortName(index);
                    setShortName(shortName);
                }
            });

            Label typeLabel = LabelFactory.create(TYPE_ID);
            container.add(typeLabel);
            container.add(shortNameSelector);
        }
    }

    /**
     * Adds the instance name field to a container.
     *
     * @param container the container
     */
    protected void addInstanceName(Component container) {
        // instance name text field
        _instanceName = TextComponentFactory.create();
        Label nameLabel = LabelFactory.create(NAME_ID);
        container.add(nameLabel);
        container.add(_instanceName);
    }

    /**
     * Adds the inactive checkbox to a container.
     *
     * @param container the container
     */
    protected void addInactive(Component container) {
        _inactive = new CheckBox();
        _inactive.setSelected(false);
        Label deactivedLabel = LabelFactory.create(DEACTIVATED_ID);
        container.add(deactivedLabel);
        container.add(_inactive);
    }

    /**
     * Helper to return short names matching certain criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @return a list of short names matching the criteria
     */
    private static String[] getShortNames(String refModelName,
                                          String entityName,
                                          String conceptName) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<String> names = service.getArchetypeShortNames(refModelName,
                entityName, conceptName, true);
        return names.toArray(new String[0]);
    }

}
