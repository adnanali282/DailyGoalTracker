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

package com.creatoro.goals.activities.habits.list;

import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import com.creatoro.goals.activities.ActivityScope;
import com.creatoro.goals.activities.BaseActivity;
import com.creatoro.goals.activities.BaseMenu;
import com.creatoro.goals.activities.ThemeSwitcher;
import com.creatoro.goals.activities.habits.list.model.HabitCardListAdapter;
import com.creatoro.goals.models.HabitList;
import com.creatoro.goals.models.HabitMatcherBuilder;
import com.creatoro.goals.preferences.Preferences;

import com.creatoro.goals.R;

import javax.inject.Inject;

@ActivityScope
public class ListHabitsMenu extends BaseMenu
{
    @NonNull
    private final ListHabitsScreen screen;

    private final HabitCardListAdapter adapter;

    private boolean showArchived;

    private boolean showCompleted;

    private final Preferences preferences;

    private ThemeSwitcher themeSwitcher;

    @Inject
    public ListHabitsMenu(@NonNull BaseActivity activity,
                          @NonNull ListHabitsScreen screen,
                          @NonNull HabitCardListAdapter adapter,
                          @NonNull Preferences preferences,
                          @NonNull ThemeSwitcher themeSwitcher)
    {
        super(activity);
        this.screen = screen;
        this.adapter = adapter;
        this.preferences = preferences;
        this.themeSwitcher = themeSwitcher;

        showCompleted = preferences.getShowCompleted();
        showArchived = preferences.getShowArchived();
        updateAdapterFilter();
    }

    @Override
    public void onCreate(@NonNull Menu menu)
    {
        MenuItem nightModeItem = menu.findItem(R.id.actionToggleNightMode);
        nightModeItem.setChecked(themeSwitcher.isNightMode());

        MenuItem hideArchivedItem = menu.findItem(R.id.actionHideArchived);
        hideArchivedItem.setChecked(!showArchived);

        MenuItem hideCompletedItem = menu.findItem(R.id.actionHideCompleted);
        hideCompletedItem.setChecked(!showCompleted);
    }

    @Override
    public boolean onItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.actionToggleNightMode:
                screen.toggleNightMode();
                return true;

            case R.id.actionAdd:
                screen.showCreateHabitScreen();
                return true;

            /*case R.id.actionFAQ:
                screen.showFAQScreen();
                return true;*/

            /*case R.id.actionAbout:
                screen.showAboutScreen();
                return true;*/

            case R.id.actionSettings:
                screen.showSettingsScreen();
                return true;

            case R.id.actionHideArchived:
                toggleShowArchived();
                invalidate();
                return true;

            case R.id.actionHideCompleted:
                toggleShowCompleted();
                invalidate();
                return true;

            case R.id.actionSortColor:
                adapter.setOrder(HabitList.Order.BY_COLOR);
                return true;

            case R.id.actionSortManual:
                adapter.setOrder(HabitList.Order.BY_POSITION);
                return true;

            case R.id.actionSortName:
                adapter.setOrder(HabitList.Order.BY_NAME);
                return true;

            case R.id.actionSortScore:
                adapter.setOrder(HabitList.Order.BY_SCORE);
                return true;

            default:
                return false;
        }
    }

    @Override
    protected int getMenuResourceId()
    {
        return R.menu.list_habits;
    }

    private void toggleShowArchived()
    {
        showArchived = !showArchived;
        preferences.setShowArchived(showArchived);
        updateAdapterFilter();
    }

    private void toggleShowCompleted()
    {
        showCompleted = !showCompleted;
        preferences.setShowCompleted(showCompleted);
        updateAdapterFilter();
    }

    private void updateAdapterFilter()
    {
        adapter.setFilter(new HabitMatcherBuilder()
            .setArchivedAllowed(showArchived)
            .setCompletedAllowed(showCompleted)
            .build());
        adapter.refresh();
    }
}
