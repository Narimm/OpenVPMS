package org.openvpms.web.component.im.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import echopointng.DateField;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Query component for {@link Act} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActQuery implements Query {

    /**
     * The id of the entity to search for.
     */
    private long _entityId;

    /**
     * The component representing the query.
     */
    private Component _component;

    private DateField _startFromField;
    private DateField _startToField;
    private DateField _endFromField;
    private DateField _endToField;

    private Date _startFrom;
    private Date _startTo;
    private Date _endFrom;
    private Date _endTo;

    /**
     * Row style name.
     */
    private static final String ROW_STYLE = "ControlRow";


    /**
     * Construct a new <code>ActQuery</code>.
     *
     * @param entity the entity to search for
     */
    public ActQuery(Entity entity) {
        setEntity(entity);
    }

    /**
     * Sets the entity to search for.
     *
     * @param entity the entity to search for. May be <code>null</code>
     */
    public void setEntity(Entity entity) {
        _entityId = (entity != null) ? entity.getUid() : -1;
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (_component == null) {
            doLayout();
        }
        return _component;
    }

    private void doLayout() {
        Row row = RowFactory.create(ROW_STYLE);
        _startFromField = new DateField();
        _startFromField.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateStartFrom();
                    }
                });

        _startToField = new DateField();
        _startToField.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateStartTo();
                    }
                });
        _endFromField = new DateField();
        _endFromField.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateEndFrom();
                    }
                });
        _endToField = new DateField();
        _endToField.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateEndTo();
                    }
                });
/*
        row.add(LabelFactory.create("actquery.start.from"));
        row.add(_startFromField);
        row.add(LabelFactory.create("actquery.start.to"));
        row.add(_startToField);
        row.add(LabelFactory.create("actquery.end.from"));
        row.add(_endFromField);
        row.add(LabelFactory.create("actquery.end.to"));
        row.add(_endToField);
*/
        _component = row;
    }

    /**
     * Performs the query, returning the matching objects.
     *
     * @return the matching objects
     */
    public List<IMObject> query() {
        List<IMObject> result = Collections.emptyList();

        if (_entityId != -1) {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            List<Act> acts = service.getActs(_entityId, null, null, null, _startFrom,
                    _startTo, _endFrom, _endTo, null, true);
            result = new ArrayList<IMObject>(acts);
        }
        return result;
    }

    private void updateStartFrom() {
        _startFrom = getDate(_startFromField);
        if (_startTo != null && _startFrom.compareTo(_startTo) > 0) {
            setStartTo(_startFrom);
        }
    }

    private void updateStartTo() {
        _startTo = getDate(_startToField);
        if (_startFrom != null && _startFrom.compareTo(_startTo) > 0) {
            setStartFrom(_startTo);
        }
    }

    private void updateEndFrom() {
        _endFrom = getDate(_endFromField);
        if (_endTo != null && _endFrom.compareTo(_endTo) > 0) {
            setEndTo(_endFrom);
        }
    }

    private void updateEndTo() {
        _endTo = getDate(_endToField);
        if (_endFrom != null && _endFrom.compareTo(_endTo) > 0) {
            setEndFrom(_endTo);
        }
    }

    private void setStartFrom(Date date) {
        _startFrom = date;
        setDate(_startFromField, _startFrom);
    }

    private void setStartTo(Date date) {
        _startTo = date;
        setDate(_startToField, _startTo);
    }

    private void setEndFrom(Date date) {
        _endFrom = date;
        setDate(_endFromField, _endFrom);
    }

    private void setEndTo(Date date) {
        _endTo = date;
        setDate(_endToField, _endTo);
    }

    private Date getDate(DateField field) {
        return field.getDateChooser().getSelectedDate().getTime();
    }

    private void setDate(DateField field, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        field.getDateChooser().setSelectedDate(calendar);
    }
}

