package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_TOGGLE_HABIT -> {
                val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
                if (habitId != -1L) {
                    val pendingResult = goAsync()
                    scope.launch {
                        try {
                            val repo = HabitRepository.getInstance(context)
                            repo.toggleCompletion(habitId)
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val ids = appWidgetManager.getAppWidgetIds(
                                ComponentName(context, HabitWidgetProvider::class.java)
                            )
                            appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.habit_widget_list)
                            for (id in ids) {
                                updateAppWidget(context, appWidgetManager, id)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            ACTION_REFRESH_WIDGET -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, HabitWidgetProvider::class.java)
                )
                appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.habit_widget_list)
                for (id in ids) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_HABIT = "com.example.widget.ACTION_TOGGLE_HABIT"
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"
        const val EXTRA_HABIT_ID = "extra_habit_id"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_habit_2x4)

            // Service Intent for the ListView
            val serviceIntent = Intent(context, HabitWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.habit_widget_list, serviceIntent)
            views.setEmptyView(R.id.habit_widget_list, R.id.habit_widget_empty)

            // Header Click Intent -> Open MainActivity
            val appIntent = Intent(context, MainActivity::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val appPendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                flags
            )
            views.setOnClickPendingIntent(R.id.widget_title, appPendingIntent)

            // Refresh Click Intent
            val refreshIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                flags
            )
            views.setOnClickPendingIntent(R.id.widget_refresh_btn, refreshPendingIntent)

            // PendingIntent Template for List Items (Check action)
            val toggleIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_HABIT
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                toggleIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
            views.setPendingIntentTemplate(R.id.habit_widget_list, togglePendingIntent)

            // Update header completion stats
            try {
                val db = AppDatabase.getInstance(context)
                val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val habits = db.habitDao().getActiveHabitsSync()
                val completions = db.habitDao().getCompletionsForDateSync(todayStr)
                val total = habits.size
                val done = completions.count { comp -> habits.any { it.id == comp.habitId } }
                val progressText = if (total > 0) "$done/$total Done" else "No Habits"
                views.setTextViewText(R.id.widget_progress_text, progressText)
            } catch (_: Exception) {
                views.setTextViewText(R.id.widget_progress_text, "Habits")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
