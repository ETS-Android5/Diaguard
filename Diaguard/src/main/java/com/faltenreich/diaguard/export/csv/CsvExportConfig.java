package com.faltenreich.diaguard.export.csv;

import android.content.Context;

import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.export.ExportCallback;
import com.faltenreich.diaguard.export.ExportConfig;
import com.faltenreich.diaguard.export.ExportFormat;

import org.joda.time.DateTime;

public class CsvExportConfig extends ExportConfig {

    private boolean isBackup;

    public CsvExportConfig(
        Context context,
        ExportCallback callback,
        DateTime dateStart,
        DateTime dateEnd,
        Measurement.Category[] categories,
        boolean isBackup
    ) {
        super(context, callback, dateStart, dateEnd, categories, ExportFormat.CSV);
        this.isBackup = isBackup;
    }

    boolean isBackup() {
        return isBackup;
    }
}
