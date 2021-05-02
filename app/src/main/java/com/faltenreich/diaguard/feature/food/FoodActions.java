package com.faltenreich.diaguard.feature.food;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.shared.data.database.dao.FoodDao;
import com.faltenreich.diaguard.shared.data.database.dao.FoodEatenDao;
import com.faltenreich.diaguard.shared.data.database.entity.Food;
import com.faltenreich.diaguard.shared.event.Events;
import com.faltenreich.diaguard.shared.event.data.FoodDeletedEvent;

public class FoodActions {

    public static void deleteFoodIfConfirmed(@NonNull Context context, @NonNull Food food, @Nullable Callback callback) {
        long foodEaten = FoodEatenDao.getInstance().count(food);
        String message = String.format(context.getString(R.string.food_eaten_placeholder), foodEaten);
        new AlertDialog.Builder(context)
            .setTitle(R.string.food_delete)
            .setMessage(message)
            .setNegativeButton(R.string.cancel, (dialog, id) -> {})
            .setPositiveButton(R.string.delete, (dialog, id) -> deleteFood(food, callback))
            .create()
            .show();
    }

    private static void deleteFood(@NonNull Food food, @Nullable Callback callback) {
        FoodDao.getInstance().softDelete(food);
        Events.post(new FoodDeletedEvent(food));
        if (callback != null) {
            callback.onFoodDeleted(food);
        }
    }

    public interface Callback {
        void onFoodDeleted(Food food);
    }
}
