package com.faltenreich.diaguard.feature.entry.edit.measurement;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.content.ContextCompat;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.feature.preference.data.PreferenceStore;
import com.faltenreich.diaguard.shared.data.database.entity.Category;
import com.faltenreich.diaguard.shared.view.floatingactionbutton.FloatingActionButtonFactory;
import com.faltenreich.diaguard.shared.view.resource.ColorUtils;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;

public class MeasurementFloatingActionMenu extends FloatingActionMenu {

    private static final int MAX_BUTTON_COUNT = 3;

    private List<Category> categoriesToSkip;
    private OnCategorySelectedListener onCategorySelectedListener;
    private OnMiscellaneousSelectedListener onMiscellaneousSelectedListener;

    public MeasurementFloatingActionMenu(Context context) {
        super(context);
        init();
    }

    public MeasurementFloatingActionMenu(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener onCategorySelectedListener) {
        this.onCategorySelectedListener = onCategorySelectedListener;
    }

    public void setOnMiscellaneousSelectedListener(OnMiscellaneousSelectedListener onMiscellaneousSelectedListener) {
        this.onMiscellaneousSelectedListener = onMiscellaneousSelectedListener;
    }

    private void init() {
        categoriesToSkip = new ArrayList<>();
        enableCloseOnClickOutside();
    }

    private void enableCloseOnClickOutside() {
        setOnTouchListener((view, event) -> {
            if (isOpened()) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                    close(true);
                }
                return true;
            } else {
                return false;
            }
        });
    }

    public void ignore(Category category) {
        if (!categoriesToSkip.contains(category)) {
            categoriesToSkip.add(category);
        }
    }

    public void removeIgnore(Category category) {
        categoriesToSkip.remove(category);
    }

    public void restock() {
        removeAllMenuButtons();

        Category[] activeCategories = PreferenceStore.getInstance().getActiveCategories();
        int menuButtonCount = 0;
        int position = 0;
        while (position < activeCategories.length && menuButtonCount < MAX_BUTTON_COUNT) {
            Category category = activeCategories[position];
            if (!categoriesToSkip.contains(category)) {
                addMenuButton(category);
                menuButtonCount++;
            }
            position++;
        }

        FloatingActionButton fabAll = FloatingActionButtonFactory.createFloatingActionButton(
            getContext(),
            getContext().getString(R.string.all),
            R.drawable.ic_more,
            ColorUtils.getBackgroundSecondary(getContext()));

        addMenuButton(fabAll);

        fabAll.setOnClickListener(view -> {
            close(true);
            if (onMiscellaneousSelectedListener != null) {
                onMiscellaneousSelectedListener.onMiscellaneousSelected();
            }
        });
    }

    public void addMenuButton(final Category category) {
        FloatingActionButton fab = FloatingActionButtonFactory.createFloatingActionButton(
            getContext(),
            getContext().getString(category.getStringResId()),
            category.getIconImageResourceId(),
            ContextCompat.getColor(getContext(), R.color.green));
        fab.setOnClickListener(view -> {
            close(true);
            if (onCategorySelectedListener != null) {
                onCategorySelectedListener.onCategorySelected(category);
            }
        });
        addMenuButton(fab);
    }

    public interface OnCategorySelectedListener {

        void onCategorySelected(Category category);
    }

    public interface OnMiscellaneousSelectedListener {

        void onMiscellaneousSelected();
    }
}
