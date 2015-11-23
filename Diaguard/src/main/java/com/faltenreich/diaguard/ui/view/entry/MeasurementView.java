package com.faltenreich.diaguard.ui.view.entry;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.adapter.SwipeDismissTouchListener;
import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.data.entity.Insulin;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.data.entity.Pressure;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Faltenreich on 24.09.2015.
 */
public class MeasurementView<T extends Measurement> extends LinearLayout {

    @Bind(R.id.image_showcase)
    protected ImageView imageViewShowcase;

    @Bind(R.id.layer_showcase)
    protected View viewLayerShowcase;

    @Bind(R.id.image_category)
    protected ImageView imageViewCategory;

    @Bind(R.id.category)
    protected TextView textViewCategory;

    @Bind(R.id.layout_content)
    protected LinearLayout content;

    private Measurement.Category category;
    private OnCategoryRemovedListener onCategoryRemovedListener;

    @Deprecated
    public MeasurementView(Context context) {
        super(context);
    }

    public MeasurementView(Context context, T measurement) {
        super(context);
        this.category = measurement.getCategory();
        init(measurement);
    }

    public MeasurementView(Context context, Measurement.Category category) {
        super(context);
        this.category = category;
        init(null);
    }

    public void setOnCategoryRemovedListener(OnCategoryRemovedListener onCategoryRemovedListener) {
        this.onCategoryRemovedListener = onCategoryRemovedListener;
    }

    private boolean hasMeasurementViewCallback() {
        return onCategoryRemovedListener != null;
    }

    private void init(@Nullable Measurement measurement) {
        boolean isUpdating = measurement != null;

        LayoutInflater.from(getContext()).inflate(R.layout.list_item_measurement, this);
        ButterKnife.bind(this);

        imageViewShowcase.setImageResource(PreferenceHelper.getInstance().getShowcaseImageResourceId(category));
        viewLayerShowcase.setBackgroundColor(getResources().getColor(PreferenceHelper.getInstance().getCategoryColorResourceId(category)));
        imageViewCategory.setImageResource(PreferenceHelper.getInstance().getCategoryImageResourceId(category));
        textViewCategory.setText(PreferenceHelper.getInstance().getCategoryName(category));

        setOnTouchListener(new SwipeDismissTouchListener(this, null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }
                    @Override
                    public void onDismiss(View view, Object token) {
                        if (hasMeasurementViewCallback()) {
                            onCategoryRemovedListener.onRemove(category);
                        }
                    }
                }));

        switch (category) {
            case INSULIN:
                content.addView(isUpdating ?
                        new MeasurementInsulinView(getContext(), (Insulin) measurement) :
                        new MeasurementInsulinView(getContext()));
                break;
            case PRESSURE:
                content.addView(isUpdating ?
                        new MeasurementPressureView(getContext(), (Pressure) measurement) :
                        new MeasurementPressureView(getContext()));
                break;
            default:
                content.addView(isUpdating ?
                        new MeasurementGenericView<>(getContext(), measurement) :
                        new MeasurementGenericView<>(getContext(), category));
        }
    }

    public Measurement getMeasurement() {
        View childView = content.getChildAt(0);
        if (childView != null && childView instanceof MeasurementAbstractView) {
            return ((MeasurementAbstractView) childView).getMeasurement();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.button_delete)
    protected void remove() {
        if (hasMeasurementViewCallback()) {
            onCategoryRemovedListener.onRemove(category);
        }
    }

    public interface OnCategoryRemovedListener {
        void onRemove(Measurement.Category category);
    }
}