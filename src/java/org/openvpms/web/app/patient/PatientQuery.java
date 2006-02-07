package org.openvpms.web.app.patient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.im.query.AbstractQuery;
import org.openvpms.web.component.model.ArchetypeShortNameListModel;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Query implementation that queries patients. The search can be further
 * constrained to only include those patients associated with the current
 * customer.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
class PatientQuery extends AbstractQuery {

    /**
     * The customer to limit the search to. If <code>null</code>, indicates to
     * query all patients.
     */
    private final Party _customer;

    /**
     * The 'all patients' checkbox. If selected, query all patients, otherwise
     * constrain the search to the current customer.
     */
    private CheckBox _allPatients;

    /**
     * All patients label id.
     */
    private static final String ALL_PATIENTS_ID = "allpatients";


    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @param customer     the customer to limit the search to. If
     *                     <code>null</code>, indicates to query all patients
     */
    public PatientQuery(String refModelName, String entityName,
                        String conceptName, Party customer) {
        super(refModelName, entityName, conceptName);
        _customer = customer;
    }


    /**
     * Performs the query, returning the matching objects.
     *
     * @return the matching objects
     */
    @Override
    public List<IMObject> query() {
        List<IMObject> result = null;
        if (_allPatients.isSelected()) {
            result = super.query();
        } else {
            if (_customer != null) {
                result = filterForCustomer();
            }
        }
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Lay out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        addShortNameSelector(container);
        addInstanceName(container);
        addAllPatients(container);
        addInactive(container);
    }

    /**
     * Adds the inactive checkbox to a container.
     *
     * @param container the container
     */
    protected void addAllPatients(Component container) {
        _allPatients = new CheckBox();
        boolean selected = (_customer == null);
        _allPatients.setSelected(selected);
        Label allPatients = LabelFactory.create(ALL_PATIENTS_ID);
        container.add(allPatients);
        container.add(_allPatients);
    }

    /**
     * Filter patients associated with a customer.
     *
     * @return a list of patients associated with the customer that match the
     *         specified criteria
     */
    private List<IMObject> filterForCustomer() {
        List<IMObject> result = null;
        List<IMObject> patients = getPatients(_customer);
        String type = getShortName();
        String name = getInstanceName();
        boolean activeOnly = !includeInactive();

        if (type == null || type.equals(ArchetypeShortNameListModel.ALL)) {
            for (String shortName : _shortNames) {
                List<IMObject> matches
                        = filter(patients, shortName, name, activeOnly);
                if (result == null) {
                    result = matches;
                } else {
                    result.addAll(matches);
                }
            }
        } else {
            result = filter(patients, type, name, activeOnly);
        }
        return result;
    }

    /**
     * Filter a list of objects.
     *
     * @param objects    the objects to filter
     * @param shortName  the archetype shortname to match on
     * @param name       the object instance name to match on
     * @param activeOnly if <code>true</code>, only include active objects
     * @return a list of objects that match the specified criteria
     */
    private List<IMObject> filter(List<IMObject> objects, String shortName,
                                  String name, boolean activeOnly) {
        final String wildcard = "*";
        List<IMObject> result = new ArrayList<IMObject>();
        for (IMObject object : objects) {
            if (!object.getArchetypeId().getShortName().equals(shortName)) {
                continue;
            }
            if (!StringUtils.isEmpty(name)) {
                if (name.startsWith(wildcard) || name.endsWith(wildcard)) {
                    name = StringUtils.strip(name, wildcard);
                }
                if (StringUtils.indexOf(object.getName(), name) == -1) {
                    continue;
                }
            }
            if (activeOnly && !object.isActive()) {
                continue;
            }
            result.add(object);
        }
        return result;
    }

    /**
     * Returns tha patients associated with a customer.
     *
     * @param customer the customer
     * @return a list of patients associated with <code>nustomer</code>
     */
    private List<IMObject> getPatients(Party customer) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<IMObject> result = new ArrayList<IMObject>();
        Set<EntityRelationship> relationships
                = customer.getEntityRelationships();
        IMObjectReference source = new IMObjectReference(customer);

        for (EntityRelationship relationship : relationships) {
            ArchetypeId id = relationship.getArchetypeId();
            if (id.getShortName().equals("entityRelationship.patientOwner")) {
                if (source.equals(relationship.getSource())) {
                    IMObject object = service.get(relationship.getTarget());
                    result.add(object);
                }
            }
        }
        return result;
    }

}
