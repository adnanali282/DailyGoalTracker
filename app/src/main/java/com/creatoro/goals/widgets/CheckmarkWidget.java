/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.creatoro.goals.widgets;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.creatoro.goals.models.Habit;
import com.creatoro.goals.utils.ColorUtils;
import com.creatoro.goals.widgets.views.CheckmarkWidgetView;

public class CheckmarkWidget extends BaseWidget
{
    @NonNull
    private final Habit habit;

    public CheckmarkWidget(@NonNull Context context,
                           int widgetId,
                           @NonNull Habit habit)
    {
        super(context, widgetId);
        this.habit = habit;
    }

    @Override
    public PendingIntent getOnClickPendingIntent(Context context)
    {
        return pendingIntentFactory.toggleCheckmark(habit, null);
    }

    @Override
    public void refreshData(View v)
    {
        CheckmarkWidgetView view = (CheckmarkWidgetView) v;
        int color = ColorUtils.getColor(getContext(), habit.getColor());
        double score = habit.getScores().getTodayValue();
        float percentage = (float) score;
        int checkmark = habit.getCheckmarks().getTodayValue();

        view.setPercentage(percentage);
        view.setActiveColor(color);
        view.setName(habit.getName());
        view.setCheckmarkValue(checkmark);
        view.refresh();
    }

    @Override
    protected View buildView()
    {
        return new CheckmarkWidgetView(getContext());
    }

    @Override
    protected int getDefaultHeight()
    {
        return 125;
    }

    @Override
    protected int getDefaultWidth()
    {
        return 125;
    }
}
