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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.system.cache;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionListener;
import org.ehcache.core.spi.service.StatisticsService;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.workflow.CalendarService;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.roster.RosterArchetypes;
import org.openvpms.archetype.rules.workflow.roster.RosterService;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.cache.EhCacheable;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Browser for appointment, task, calendar, roster and lookup caches.
 *
 * @author Tim Anderson
 */
class CacheBrowser {

    /**
     * The total amount of memory avilable to the JVM.
     */
    private final SimpleProperty totalMemory = new SimpleProperty("totalmemory", null, String.class,
                                                                  Messages.get("admin.system.cache.totalmemory"), true);

    /**
     * The free memory.
     */
    private final SimpleProperty freeMemory = new SimpleProperty("freememory", null, String.class,
                                                                 Messages.get("admin.system.cache.freememory"), true);

    /**
     * Memory use.
     */
    private final SimpleProperty memoryUse = new SimpleProperty("memoryuse", null, String.class,
                                                                Messages.get("admin.system.cache.memoryuse"), true);

    /**
     * The caches.
     */
    private final List<CacheState> caches;

    /**
     * The statistics service.
     */
    private final StatisticsService statistics;

    /**
     * The table of caches.
     */
    private final PagedIMTable<CacheState> table;

    /**
     * The appointment cache name.
     */
    private static final String APPOINTMENT_CACHE = "appointmentCache";

    /**
     * The task cache name.
     */
    private static final String TASK_CACHE = "taskCache";

    /**
     * The calendar cache name.
     */
    private static final String CALENDAR_CACHE = "calendarCache";

    /**
     * The roster area cache name.
     */
    private static final String ROSTER_AREA_CACHE = "rosterAreaCache";

    /**
     * The roster user cache name.
     */
    private static final String ROSTER_USER_CACHE = "rosterUserCache";

    /**
     * The lookup cache name.
     */
    private static final String LOOKUP_CACHE = "lookupCache";

    /**
     * Multiplier for schedule cache sizes. ~2 months per schedule.
     */
    private static final int SCHEDULE_MULTIPLIER = 2 * 30;

    /**
     * Multiplier for roster area cache sizes. 2 weeks per area * double the no. of current areas.
     */
    private static final int ROSTER_AREA_MULTIPLIER = 2 * 7 * 2;

    /**
     * Multiplier for roster user cache sizes. 2 weeks per user * double the no. of current users.
     * Roster events by user is cached on a weekly basis.
     */
    private static final int ROSTER_USER_MULTIPLIER = 2 * 2;

    /**
     * Multiplier for lookup cache size. 2 * no. of frequently used lookups
     */
    private static final int LOOKUP_MULTIPLIER = 2;


    /**
     * Constructs a {@link CacheBrowser}.
     */
    CacheBrowser() {
        caches = new ArrayList<>();
        statistics = ServiceHelper.getBean(StatisticsService.class);
        addCache((EhCacheable) ServiceHelper.getAppointmentService(), APPOINTMENT_CACHE,
                 "admin.system.cache.appointment", caches);
        addCache((EhCacheable) ServiceHelper.getTaskService(), TASK_CACHE, "admin.system.cache.task", caches);
        addCache(ServiceHelper.getBean(CalendarService.class), CALENDAR_CACHE, "admin.system.cache.calendar", caches);

        RosterService rosterService = ServiceHelper.getBean(RosterService.class);
        addCache(rosterService, ROSTER_AREA_CACHE, "admin.system.cache.rosterarea", caches);
        addCache(rosterService.getUserCache(), ROSTER_USER_CACHE, "admin.system.cache.rosteruser", caches);

        addCache((EhCacheable) ServiceHelper.getLookupService(), "lookupCache", "admin.system.cache.lookup", caches);

        table = new PagedIMTable<>(new CacheTableModel());
        ListResultSet<CacheState> set = new ListResultSet<CacheState>(caches, 20) {
            @Override
            public void sort(SortConstraint[] sort) {
                super.sort(sort);
                IMObjectSorter.sort(getObjects(), sort, input -> input);
            }
        };
        table.setResultSet(set);
    }

    /**
     * Returns the selected cache.
     *
     * @return the selected cache, or {@code null} if none is selected
     */
    public CacheState getSelected() {
        return table.getSelected();
    }

    /**
     * Adds a listener to be notified when a cache is selected.
     *
     * @param listener the listener to add
     */
    public void addActionListener(ActionListener listener) {
        table.getTable().addActionListener(listener);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        TextField total = BoundTextComponentFactory.create(totalMemory, 10);
        total.setAlignment(Alignment.ALIGN_RIGHT);
        total.setEnabled(false);
        TextField free = BoundTextComponentFactory.create(freeMemory, 10);
        free.setEnabled(false);
        free.setAlignment(Alignment.ALIGN_RIGHT);
        TextField use = BoundTextComponentFactory.create(memoryUse, 10);
        use.setEnabled(false);
        use.setAlignment(Alignment.ALIGN_RIGHT);
        updateMemory();

        ComponentGrid grid = new ComponentGrid();
        grid.add(new ComponentState(total, totalMemory));
        grid.add(new ComponentState(free, freeMemory));
        grid.add(new ComponentState(use, memoryUse));
        return ColumnFactory.create(Styles.WIDE_CELL_SPACING, grid.createGrid(), table.getComponent());
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return table.getFocusGroup();
    }

    /**
     * Refreshes statistics for each of the caches
     * and updates memory use.
     */
    public void refresh() {
        for (CacheState cache : caches) {
            cache.refreshStatistics();
        }
        updateTable();
        updateMemory();
    }

    /**
     * Resets statistics for each of the caches.
     */
    public void resetStatistics() {
        for (CacheState cache : caches) {
            cache.resetStatistics();
        }
        refresh();
    }

    /**
     * Clears the selected cache.
     */
    public void clear() {
        CacheState cache = table.getSelected();
        if (cache != null) {
            cache.clear();
            cache.resetStatistics();
            cache.refreshStatistics();
            updateTable();
        }
    }

    /**
     * Calculates a suggested cache size.
     *
     * @param cache the cache
     * @return the suggested cache size
     */
    public long getSuggestedSize(CacheState cache) {
        long result = 0;
        if (APPOINTMENT_CACHE.equals(cache.getName())) {
            result = getSuggestedCacheSize(SCHEDULE_MULTIPLIER, ScheduleArchetypes.ORGANISATION_SCHEDULE);
        } else if (TASK_CACHE.equals(cache.getName())) {
            result = getSuggestedCacheSize(SCHEDULE_MULTIPLIER, ScheduleArchetypes.ORGANISATION_WORKLIST);
        } else if (CALENDAR_CACHE.equals(cache.getName())) {
            result = getSuggestedCacheSize(SCHEDULE_MULTIPLIER, ProductArchetypes.SERVICE_RATIO_CALENDAR);
        } else if (ROSTER_AREA_CACHE.equals(cache.getName())) {
            result = getSuggestedCacheSize(ROSTER_AREA_MULTIPLIER, RosterArchetypes.ROSTER_AREA);
        } else if (ROSTER_USER_CACHE.equals(cache.getName())) {
            result = getSuggestedCacheSize(ROSTER_USER_MULTIPLIER, UserArchetypes.USER);
        } else if (LOOKUP_CACHE.equals(cache.getName())) {
            result = getSuggestedCacheSize(LOOKUP_MULTIPLIER, PatientArchetypes.SPECIES, PatientArchetypes.BREED,
                                           "lookup.state", "lookup.suburb", "lookup.diagnosis*",
                                           "lookup.visitReason*", "lookup.presentingComplaint*");
        }
        return result;
    }

    /**
     * Updates memory use.
     */
    private void updateMemory() {
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;
        int percent = (total != 0) ? (int) Math.round(100.0 * used / total) : 0;

        totalMemory.setValue(NumberFormatter.getSize(total));
        freeMemory.setValue(NumberFormatter.getSize(free));
        memoryUse.setValue(percent + "%");
    }

    /**
     * Calculates a suggested cache size by counting instances of one or more archetypes, and then multiplying it by
     * a factor.
     *
     * @param factor     the factor to multiply by
     * @param archetypes the archetypes to count
     * @return the suggested cache size
     */
    private long getSuggestedCacheSize(int factor, String... archetypes) {
        ArchetypeQuery query = new ArchetypeQuery(archetypes, false, true);
        query.setCountResults(true);
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        IPage<IMObject> results = service.get(query);
        int count = results.getTotalResults();
        if (count == 0) {
            count = 1;
        }
        return count * factor;
    }

    /**
     * Updates the table.
     */
    private void updateTable() {
        ResultSet<CacheState> set = table.getResultSet();
        set.sort(set.getSortConstraints());
        table.getModel().refresh();
    }

    /**
     * Adds a cache to the list of caches.
     *
     * @param cache  the cache
     * @param name   the cache name
     * @param key    the localisation key for the cache display name
     * @param caches the list to add to
     */
    private void addCache(EhCacheable cache, String name, String key, List<CacheState> caches) {
        caches.add(new CacheState(cache, name, Messages.get(key), statistics));
    }
}
