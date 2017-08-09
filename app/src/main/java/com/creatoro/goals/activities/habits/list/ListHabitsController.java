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

import com.creatoro.goals.activities.ActivityScope;
import com.creatoro.goals.activities.BaseSystem;
import com.creatoro.goals.activities.habits.list.controllers.HabitCardListController;
import com.creatoro.goals.activities.habits.list.model.HabitCardListAdapter;
import com.creatoro.goals.commands.CommandRunner;
import com.creatoro.goals.commands.CreateRepetitionCommand;
import com.creatoro.goals.commands.ToggleRepetitionCommand;
import com.creatoro.goals.models.CheckmarkList;
import com.creatoro.goals.models.Habit;
import com.creatoro.goals.models.HabitList;
import com.creatoro.goals.preferences.Preferences;
import com.creatoro.goals.tasks.ImportDataTask;
import com.creatoro.goals.tasks.TaskRunner;
import com.creatoro.goals.utils.DateUtils;
import com.creatoro.goals.utils.ReminderScheduler;
import com.creatoro.goals.widgets.WidgetUpdater;

import com.creatoro.goals.R;
import com.creatoro.goals.tasks.ExportCSVTaskFactory;
import com.creatoro.goals.tasks.ExportDBTaskFactory;
import com.creatoro.goals.tasks.ImportDataTaskFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

@ActivityScope
public class ListHabitsController
    implements HabitCardListController.HabitListener
{

    @NonNull
    private final ListHabitsScreen screen;

    @NonNull
    private final BaseSystem system;

    @NonNull
    private final HabitList habitList;

    @NonNull
    private final HabitCardListAdapter adapter;

    @NonNull
    private final Preferences prefs;

    @NonNull
    private final CommandRunner commandRunner;

    @NonNull
    private final com.creatoro.goals.tasks.TaskRunner taskRunner;

    private ReminderScheduler reminderScheduler;

    private WidgetUpdater widgetUpdater;

    private ImportDataTaskFactory importTaskFactory;

    private ExportCSVTaskFactory exportCSVFactory;

    private ExportDBTaskFactory exportDBFactory;

    @Inject
    public ListHabitsController(@NonNull BaseSystem system,
                                @NonNull CommandRunner commandRunner,
                                @NonNull HabitList habitList,
                                @NonNull HabitCardListAdapter adapter,
                                @NonNull ListHabitsScreen screen,
                                @NonNull Preferences prefs,
                                @NonNull ReminderScheduler reminderScheduler,
                                @NonNull TaskRunner taskRunner,
                                @NonNull WidgetUpdater widgetUpdater,
                                @NonNull ImportDataTaskFactory importTaskFactory,
                                @NonNull ExportCSVTaskFactory exportCSVFactory,
                                @NonNull ExportDBTaskFactory exportDBFactory)
    {
        this.adapter = adapter;
        this.commandRunner = commandRunner;
        this.habitList = habitList;
        this.prefs = prefs;
        this.screen = screen;
        this.system = system;
        this.taskRunner = taskRunner;
        this.reminderScheduler = reminderScheduler;
        this.widgetUpdater = widgetUpdater;
        this.importTaskFactory = importTaskFactory;
        this.exportCSVFactory = exportCSVFactory;
        this.exportDBFactory = exportDBFactory;
    }

    public void onExportCSV()
    {
        List<Habit> selected = new LinkedList<>();
        for (Habit h : habitList) selected.add(h);

        taskRunner.execute(exportCSVFactory.create(selected, filename -> {
            if (filename != null) screen.showSendFileScreen(filename);
            else screen.showMessage(R.string.could_not_export);
        }));
    }

    public void onExportDB()
    {
        taskRunner.execute(exportDBFactory.create(filename -> {
            if (filename != null) screen.showSendFileScreen(filename);
            else screen.showMessage(R.string.could_not_export);
        }));
    }

    @Override
    public void onHabitClick(@NonNull Habit h)
    {
        screen.showHabitScreen(h);
    }

    @Override
    public void onHabitReorder(@NonNull Habit from, @NonNull Habit to)
    {
        taskRunner.execute(() -> habitList.reorder(from, to));
    }

    public void onImportData(@NonNull File file,
                             @NonNull OnFinishedListener finishedListener)
    {
        taskRunner.execute(importTaskFactory.create(file, result -> {
            switch (result)
            {
                case ImportDataTask.SUCCESS:
                    adapter.refresh();
                    screen.showMessage(R.string.habits_imported);
                    break;

                case ImportDataTask.NOT_RECOGNIZED:
                    screen.showMessage(R.string.file_not_recognized);
                    break;

                default:
                    screen.showMessage(R.string.could_not_import);
                    break;
            }

            finishedListener.onFinish();
        }));
    }

    @Override
    public void onInvalidEdit()
    {
        screen.showMessage(R.string.long_press_to_edit);
    }

    @Override
    public void onEdit(@NonNull Habit habit, long timestamp)
    {
        CheckmarkList checkmarks = habit.getCheckmarks();
        double oldValue = checkmarks.getValues(timestamp, timestamp)[0];

        screen.showNumberPicker(oldValue / 1000, habit.getUnit(), newValue -> {
            newValue = Math.round(newValue * 1000);
            commandRunner.execute(
                new CreateRepetitionCommand(habit, timestamp, (int) newValue),
                habit.getId());
        });
    }


    @Override
    public void onInvalidToggle()
    {
        screen.showMessage(R.string.long_press_to_toggle);
    }

    public void onRepairDB()
    {
        taskRunner.execute(() -> {
            habitList.repair();
            screen.showMessage(R.string.database_repaired);
        });
    }

    public void onSendBugReport()
    {
        try
        {
            system.dumpBugReportToFile();
        }
        catch (IOException e)
        {
            // ignored
        }

        try
        {
            String log = system.getBugReport();
            int to = R.string.bugReportTo;
            int subject = R.string.bugReportSubject;
            screen.showSendEmailScreen(to, subject, log);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            screen.showMessage(R.string.bug_report_failed);
        }
    }

    public void onStartup()
    {
        prefs.incrementLaunchCount();
        if (prefs.isFirstRun()) onFirstRun();
    }

    @Override
    public void onToggle(@NonNull Habit habit, long timestamp)
    {
        commandRunner.execute(new ToggleRepetitionCommand(habit, timestamp),
            habit.getId());
    }

    private void onFirstRun()
    {
        prefs.setFirstRun(false);
        prefs.updateLastHint(-1, DateUtils.getStartOfToday());
        screen.showIntroScreen();
    }

    public interface OnFinishedListener
    {
        void onFinish();
    }
}
