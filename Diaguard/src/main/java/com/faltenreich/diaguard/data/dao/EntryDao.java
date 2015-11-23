package com.faltenreich.diaguard.data.dao;

import android.util.Log;

import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.data.entity.BaseEntity;
import com.faltenreich.diaguard.data.entity.Entry;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.util.ArrayUtils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Faltenreich on 05.09.2015.
 */
public class EntryDao extends BaseDao<Entry> {

    private static final String TAG = EntryDao.class.getSimpleName();

    private static EntryDao instance;

    public static EntryDao getInstance() {
        if (instance == null) {
            instance = new EntryDao();
        }
        return instance;
    }

    private EntryDao() {
        super(Entry.class);
    }

    public List<Entry> getEntriesOfDay(DateTime day) {
        return getEntriesBetween(day, day);
    }

    public List<Entry> getEntriesBetween(DateTime start, DateTime end) {
        start = start.withTimeAtStartOfDay();
        end = end.withTime(DateTimeConstants.HOURS_PER_DAY - 1,
                DateTimeConstants.MINUTES_PER_HOUR - 1,
                DateTimeConstants.SECONDS_PER_MINUTE - 1,
                DateTimeConstants.MILLIS_PER_SECOND - 1);
        try {
            return getDao().queryBuilder().orderBy(Entry.Column.DATE, true).where().gt(Entry.Column.DATE, start).and().lt(Entry.Column.DATE, end).query();
        } catch (SQLException e) {
            Log.e(TAG, "Could not getEntriesBetween");
            return new ArrayList<>();
        }
    }

    public int deleteMeasurements(Entry entry) {
        int deletedMeasurements = 0;
        for (Measurement.Category category : Measurement.Category.values()) {
            deletedMeasurements += MeasurementDao.getInstance(category.toClass()).deleteMeasurements(entry);
        }
        return deletedMeasurements;
    }

    public List<Measurement> getMeasurements(Entry entry) {
        return getMeasurements(entry, PreferenceHelper.getInstance().getActiveCategories());
    }

    public List<Measurement> getMeasurements(Entry entry, Measurement.Category[] categories) {
        List<Measurement> measurements = new ArrayList<>();
        for (Measurement.Category category : categories) {
            Measurement measurement = MeasurementDao.getInstance(category.toClass()).getMeasurement(entry);
            if (measurement != null) {
                measurements.add(measurement);
            }
        }
        return measurements;
    }

    private <M extends Measurement> QueryBuilder<Entry, Long> join(Class<M> clazz) {
        QueryBuilder<Entry, Long> qbOne = getDao().queryBuilder();
        QueryBuilder<M, Long> qbTwo = MeasurementDao.getInstance(clazz).getDao().queryBuilder();
        try {
            return qbOne.join(qbTwo);
        } catch (SQLException exception) {
            Log.e(TAG, String.format("Could not join with '%s'", clazz.toString()));
            return null;
        }
    }

    public <M extends Measurement> Entry getLatestWithMeasurement(Class<M> clazz) {
        try {
            return join(clazz).orderBy(Entry.Column.DATE, false).queryForFirst();
        } catch (SQLException exception) {
            Log.e(TAG, String.format("Could not getLatestWithMeasurement '%s'", clazz.toString()));
            return null;
        }
    }

    public <M extends Measurement> List<Entry> getAllWithMeasurementFromToday(Class<M> clazz) {
        try {
            return join(clazz).where().gt(Entry.Column.DATE, DateTime.now().withTimeAtStartOfDay()).query();
        } catch (SQLException exception) {
            Log.e(TAG, String.format("Could not getLatestWithMeasurement '%s'", clazz.toString()));
            return null;
        }
    }

    public LinkedHashMap<Measurement.Category, float[]> getAverageDataTable(DateTime day, Measurement.Category[] categories, int hoursToSkip) {
        LinkedHashMap<Measurement.Category, float[]> values = new LinkedHashMap<>();
        for (Measurement.Category category : categories) {
            values.put(category, new float[DateTimeConstants.HOURS_PER_DAY / hoursToSkip]);
        }
        for(Entry entry : getEntriesOfDay(day)) {
            for (Measurement measurement : getMeasurements(entry, categories)) {
                Measurement.Category category = measurement.getCategory();
                boolean valueIsAverage =
                        category == Measurement.Category.BLOODSUGAR ||
                                category == Measurement.Category.HBA1C ||
                                category == Measurement.Category.WEIGHT ||
                                category == Measurement.Category.PULSE ||
                                category == Measurement.Category.PRESSURE;
                int index = measurement.getEntry().getDate().hourOfDay().get() / hoursToSkip;
                boolean valueIsSum = category != Measurement.Category.PRESSURE;
                float value = valueIsSum ? ArrayUtils.sum(measurement.getValues()) : ArrayUtils.avg(measurement.getValues());
                float oldValue = values.get(category)[index];
                // TODO: Divisor is not 2 but count
                float newValue = valueIsAverage ?
                        oldValue > 0 ?
                                (oldValue + value) / 2 :
                                value :
                        oldValue + value;
                values.get(category)[index] = newValue;
            }
        }
        return values;
    }
}