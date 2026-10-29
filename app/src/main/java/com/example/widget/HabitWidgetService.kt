package com.example.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.database.AppDatabase
import com.example.data.entity.HabitCompletionEntity
import com.example.data.entity.HabitEntity
import com.example.data.repository.HabitRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return HabitRemoteViewsFactory(applicationContext)
    }
}

class HabitRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var habits: List<HabitEntity> = emptyList()
    private var completionsMap: Map<Long, HabitCompletionEntity> = emptyMap()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        try {
            val db = AppDatabase.getInstance(context)
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            habits = db.habitDao().getActiveHabitsSync()
            val completions = db.habitDao().getCompletionsForDateSync(todayStr)
            completionsMap = completions.associateBy { it.habitId }
        } catch (_: Exception) {
            habits = emptyList()
            completionsMap = emptyMap()
        }
    }

    override fun onDestroy() {
        habits = emptyList()
        completionsMap = emptyMap()
    }

    override fun getCount(): Int = habits.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position < 0 || position >= habits.size) {
            return RemoteViews(context.packageName, R.layout.widget_habit_item)
        }

        val habit = habits[position]
        val isCompleted = completionsMap.containsKey(habit.id)

        val views = RemoteViews(context.packageName, R.layout.widget_habit_item).apply {
            setTextViewText(R.id.widget_item_title, habit.title)
            val statusSuffix = if (isCompleted) " • Completed!" else ""
            setTextViewText(
                R.id.widget_item_subtitle,
                "+${habit.xpReward} XP • ${habit.category}$statusSuffix"
            )

            if (isCompleted) {
                setImageViewResource(R.id.widget_item_check_btn, R.drawable.widget_check_done)
            } else {
                setImageViewResource(R.id.widget_item_check_btn, R.drawable.widget_check_undone)
            }

            // Fill-in Intent for check action
            val fillInIntent = Intent().apply {
                putExtra(HabitWidgetProvider.EXTRA_HABIT_ID, habit.id)
            }
            setOnClickFillInIntent(R.id.widget_item_check_btn, fillInIntent)
            setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
        }

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in habits.indices) habits[position].id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
