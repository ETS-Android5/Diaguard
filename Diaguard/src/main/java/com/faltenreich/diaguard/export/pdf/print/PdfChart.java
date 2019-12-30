package com.faltenreich.diaguard.export.pdf.print;

import android.content.Context;
import android.util.Log;

import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.data.dao.EntryDao;
import com.faltenreich.diaguard.data.entity.BloodSugar;
import com.faltenreich.diaguard.data.entity.Entry;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.export.pdf.meta.PdfExportCache;
import com.faltenreich.diaguard.export.pdf.view.SizedBox;
import com.faltenreich.diaguard.export.pdf.view.SizedImage;
import com.faltenreich.diaguard.export.pdf.view.SizedTable;
import com.faltenreich.diaguard.ui.list.item.ListItemCategoryValue;
import com.faltenreich.diaguard.util.DateTimeUtils;
import com.pdfjet.Align;
import com.pdfjet.Cell;
import com.pdfjet.Color;
import com.pdfjet.Line;
import com.pdfjet.Point;
import com.pdfjet.TextLine;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PdfChart implements PdfPrintable {

    private static final String TAG = PdfChart.class.getSimpleName();
    private static final float POINT_RADIUS = 5;
    private static final float LABEL_WIDTH = 100;
    private static final float PADDING = 12;
    private static final int HOUR_INTERVAL = 2;

    private PdfExportCache cache;
    private SizedBox chart;
    private SizedTable table;

    private List<BloodSugar> bloodSugars;
    private LinkedHashMap<Measurement.Category, ListItemCategoryValue[]> measurements;

    PdfChart(PdfExportCache cache) {
        float width = cache.getPage().getWidth();
        this.cache = cache;
        this.chart = new SizedBox(width, width / 4);
        this.table = new SizedTable();
        init();
    }

    @Override
    public float getHeight() {
        return chart.getHeight() + table.getHeight() + PdfPage.MARGIN;
    }

    private void init() {
        fetchData();
        setDataForTable();
    }

    private void fetchData() {
        DateTime dateTime = cache.getDateTime();

        List<Entry> entries = EntryDao.getInstance().getEntriesOfDay(dateTime);
        bloodSugars = new ArrayList<>();
        for (Entry entry : entries) {
            List<Measurement> measurements = EntryDao.getInstance().getMeasurements(entry);
            for (Measurement measurement : measurements) {
                if (measurement instanceof BloodSugar) {
                    bloodSugars.add((BloodSugar) measurement);
                }
            }
        }

        List<Measurement.Category> categories = new ArrayList<>();
        for (Measurement.Category category : cache.getConfig().getCategories()) {
            if (category != Measurement.Category.BLOODSUGAR) {
                categories.add(category);
            }
        }
        measurements = EntryDao.getInstance().getAverageDataTable(
            dateTime,
            categories.toArray(new Measurement.Category[0]),
            HOUR_INTERVAL
        );
    }

    private void setDataForTable() {
        Context context = cache.getContext();
        List<List<Cell>> tableData = new ArrayList<>();

        int index = 0;
        for (Map.Entry<Measurement.Category, ListItemCategoryValue[]> entry : measurements.entrySet()) {
            Measurement.Category category = entry.getKey();
            ListItemCategoryValue[] values = entry.getValue();
            List<Cell> row = new ArrayList<>();

            try {
                int imageRes = PreferenceHelper.getInstance().getCategoryImageResourceId(category);
                SizedImage image = new SizedImage(cache.getPdf(), context, imageRes);
                image.setSize(20);
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage());
            }

            Cell titleCell = new Cell(cache.getFontNormal());
            titleCell.setText(category.toLocalizedString(context));
            titleCell.setWidth(LABEL_WIDTH);
            titleCell.setBgColor(index % 2 == 0 ? cache.getColorDivider() : Color.transparent);
            titleCell.setFgColor(Color.gray);
            titleCell.setPenColor(Color.gray);
            row.add(titleCell);

            for (ListItemCategoryValue value : values) {
                // TODO: What to do with multiline values?
                Cell valueCell = new Cell(cache.getFontNormal());
                valueCell.setText(value.print());
                valueCell.setWidth((cache.getPage().getWidth() - LABEL_WIDTH) / (DateTimeConstants.HOURS_PER_DAY / HOUR_INTERVAL));
                valueCell.setBgColor(index % 2 == 0 ? cache.getColorDivider() : Color.transparent);
                valueCell.setFgColor(Color.black);
                valueCell.setPenColor(Color.gray);
                valueCell.setTextAlignment(Align.CENTER);
                row.add(valueCell);
            }

            tableData.add(row);
            index++;
        }

        try {
            // Must be executed early to know the table's height
            table.setData(tableData);
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
        }
    }

    @Override
    public void drawOn(PdfPage page, Point position) throws Exception {
        position = drawChart(page, position, bloodSugars);

        table.setLocation(position.getX(), position.getY());
        table.drawOn(page);
    }

    private Point drawChart(PdfPage page, Point position, List<BloodSugar> bloodSugars) throws Exception {
        chart.setColor(Color.transparent);
        chart.setPosition(position.getX(), position.getY());
        float[] coordinates = chart.drawOn(page);

        TextLine label = new TextLine(cache.getFontNormal());
        label.setColor(Color.gray);

        Line line = new Line();
        line.setColor(Color.gray);

        float chartWidth = chart.getWidth();
        float chartHeight = chart.getHeight();
        float chartStartX = 0;
        float chartEndX = chartStartX + chart.getWidth();
        float chartStartY = 0;
        float chartEndY = chartStartY + chartHeight;

        float contentStartX = LABEL_WIDTH;
        float contentStartY = chartStartY + label.getHeight() + PADDING;
        float contentEndX = chartEndX;
        float contentEndY = chartEndY;
        float contentWidth = contentEndX - contentStartX;
        float contentHeight = contentEndY - contentStartY;

        int xStep = DateTimeConstants.MINUTES_PER_HOUR * HOUR_INTERVAL;
        float xMax = DateTimeConstants.MINUTES_PER_DAY;
        int yStep = 40;
        float yMaxMin = 210;
        float yMax = yMaxMin;
        for (BloodSugar bloodSugar : bloodSugars) {
            if (bloodSugar.getMgDl() > yMax) {
                yMax = bloodSugar.getMgDl();
            }
        }
        if (yMax > 200) {
            // Increased range for exceeding values
            yStep += (int) ((yMax - yMaxMin) / 50) * 20;
        }

        TextLine header = new TextLine(cache.getFontBold());
        header.setText(DateTimeUtils.toWeekDayAndDate(cache.getDateTime()));
        header.setPosition(chartStartX, chartStartY + header.getHeight());
        header.placeIn(chart);
        header.drawOn(page);

        // Labels for x axis
        int minutes = 0;
        while (minutes <= xMax) {
            float x = contentStartX + ((float) minutes / xMax) * contentWidth;

            label.setText(String.valueOf(minutes / 60));
            label.setPosition(x - label.getWidth() / 2, chartStartY + header.getHeight());
            label.placeIn(chart);
            label.drawOn(page);

            line.setStartPoint(x, chartStartY + header.getHeight() + 8);
            line.setEndPoint(x, contentEndY);
            line.placeIn(chart);
            line.drawOn(page);

            minutes += xStep;
        }

        // TODO: Make sure to always set n labels
        // Labels for y axis
        int labelValue = yStep;
        float labelY;
        while ((labelY = contentStartY + contentHeight - ((labelValue / yMax) * contentHeight)) >= contentStartY) {
            label.setText(PreferenceHelper.getInstance().getMeasurementForUi(Measurement.Category.BLOODSUGAR, labelValue));
            label.setPosition(chartStartX, labelY + (label.getHeight() / 4));
            label.placeIn(chart);
            label.drawOn(page);

            line.setStartPoint(chartStartX + label.getWidth() + PADDING, labelY);
            line.setEndPoint(contentEndX, labelY);
            line.placeIn(chart);
            line.drawOn(page);

            labelValue += yStep;
        }

        Point point = new Point();
        point.setFillShape(true);
        point.setRadius(POINT_RADIUS);
        for (BloodSugar bloodSugar : bloodSugars) {
            Entry entry = bloodSugar.getEntry();
            float minute = entry.getDate().getMinuteOfDay();
            float value = bloodSugar.getMgDl();
            float x = contentStartX + ((minute / xMax) * contentWidth);
            float y = contentStartY + (contentHeight - (value / yMax) * contentHeight);

            point.setPosition(x, y);
            int color = Color.black;
            if (cache.getConfig().isHighlightLimits()) {
                if (value > PreferenceHelper.getInstance().getLimitHyperglycemia()) {
                    color = cache.getColorHyperglycemia();
                } else if (value < PreferenceHelper.getInstance().getLimitHypoglycemia()) {
                    color = cache.getColorHypoglycemia();
                }
            }
            point.setColor(color);
            point.placeIn(chart);
            point.drawOn(page);
        }

        return new Point(position.getX(), coordinates[1]);
    }
}
