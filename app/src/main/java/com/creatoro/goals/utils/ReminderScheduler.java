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

package com.creatoro.goals.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.creatoro.goals.AppScope;
import com.creatoro.goals.HabitLogger;
import com.creatoro.goals.commands.ChangeHabitColorCommand;
import com.creatoro.goals.commands.Command;
import com.creatoro.goals.commands.CommandRunner;
import com.creatoro.goals.commands.ToggleRepetitionCommand;
import com.creatoro.goals.intents.IntentScheduler;
import com.creatoro.goals.intents.PendingIntentFactory;
import com.creatoro.goals.models.Habit;
import com.creatoro.goals.models.HabitList;
import com.creatoro.goals.models.HabitMatcher;
import com.creatoro.goals.models.Reminder;

import java.util.Calendar;

import javax.inject.Inject;

import static com.creatoro.goals.utils.DateUtils.applyTimezone;
import static com.creatoro.goals.utils.DateUtils.getStartOfDay;
import static com.creatoro.goals.utils.DateUtils.removeTimezone;

@AppScope
public class ReminderScheduler implements CommandRunner.Listener
{
    private final PendingIntentFactory pendingIntentFactory;

    private final IntentScheduler intentScheduler;

    private final HabitLogger logger;

    private CommandRunner commandRunner;

    private HabitList habitList;

    @Inject
    public ReminderScheduler(@NonNull PendingIntentFactory pendingIntentFactory,
                             @NonNull IntentScheduler intentScheduler,
                             @NonNull HabitLogger logger,
                             @NonNull CommandRunner commandRunner,
                             @NonNull HabitList habitList)
    {
        this.pendingIntentFactory = pendingIntentFactory;
        this.intentScheduler = intentScheduler;
        this.logger = logger;
        this.commandRunner = commandRunner;
        this.habitList = habitList;
    }

    @Override
    public void onCommandExecuted(@NonNull Command command,
                                  @Nullable Long refreshKey)
    {
        if(command instanceof ToggleRepetitionCommand) return;
        if(command instanceof ChangeHabitColorCommand) return;
        scheduleAll();
    }

    public void schedule(@NonNull Habit habit, @Nullable Long reminderTime)
    {
        if (!habit.hasReminder()) return;
        if (habit.isArchived()) return;
        Reminder reminder = habit.getReminder();
        if (reminderTime == null) reminderTime = getReminderTime(reminder);
        long timestamp = getStartOfDay(removeTimezone(reminderTime));

        PendingIntent intent =
            pendingIntentFactory.showReminder(habit, reminderTime, timestamp);
        intentScheduler.schedule(reminderTime, intent);
        logger.logReminderScheduled(habit, reminderTime);
    }

    public void scheduleAll()
    {
        HabitList reminderHabits =
            habitList.getFiltered(HabitMatcher.WITH_ALARM);
        for (Habit habit : reminderHabits)
            schedule(habit, null);
    }

    public void startListening()
    {
        commandRunner.addListener(this);
    }

    public void stopListening()
    {
        commandRunner.removeListener(this);
    }

    @NonNull
    private Long getReminderTime(@NonNull Reminder reminder)
    {
        Calendar calendar = DateUtils.getStartOfTodayCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
        calendar.set(Calendar.MINUTE, reminder.getMinute());
        calendar.set(Calendar.SECOND, 0);
        Long time = calendar.getTimeInMillis();

        if (DateUtils.getLocalTime() > time) time += AlarmManager.INTERVAL_DAY;

        return applyTimezone(time);
    }
}
