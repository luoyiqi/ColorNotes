package ru.dev.colornotes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

/**
 * Виджет заметки
 */
public class WidgetNote extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // обновляем все виджеты
        for (int id : appWidgetIds) {
            updateWidgetByIdWidget(context, id);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        try {
            super.onDeleted(context, appWidgetIds);

            DB.init(context).openConnection();
            for (int idWidget : appWidgetIds) {
                // удаляем привязку заметки и виджета
                DB.getInstance().unsetWidget(idWidget);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обновление внешнего вида виджета
     * @param context контекст
     * @param idNote ИД заметки
     * @param text текст заметки
     * @param color цвет заметки
     * @param idWidget ИД виджета
     */
    public static void updateWidget(Context context, long idNote, String text, int color, int idWidget) {

        // помещаем данные в текстовые поля
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_note);
        remoteViews.setTextViewText(R.id.tvItem, text);
        remoteViews.setInt(R.id.colorNote, "setBackgroundColor", color);

        int darkColor = DialogColors.getColorDark(context, color);
        remoteViews.setInt(R.id.darkColorNote, "setBackgroundColor", darkColor);

        // при клике на заметку открываем активити для редатирования
        Intent intent = new Intent(context, ActivityNote.class);
        intent.putExtra(DB.COLUMN_ID_NOTE, idNote);
        intent.putExtra(DB.COLUMN_TEXT_NOTE, text);
        intent.putExtra(DB.COLUMN_COLOR, color);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, idWidget, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.colorNote, pendingIntent);

        // обновляем виджет
        AppWidgetManager.getInstance(context).updateAppWidget(idWidget, remoteViews);
    }

    /**
     * Обновление внешнего вида виджета
     * @param context контекст
     * @param idWidget ID виджета
     */
    public static void updateWidgetByIdWidget(Context context, int idWidget) {
        try {
            DB.init(context).openConnection();
            Cursor cursor = DB.getInstance().getNoteInfoByIdWidget(idWidget);
            if (cursor.getCount() == 0) {
                return;
            }

            cursor.moveToFirst();
            long idNote = cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID_NOTE));
            String text = cursor.getString(cursor.getColumnIndex(DB.COLUMN_TEXT_NOTE));
            int color = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_COLOR));

            updateWidget(context, idNote, text, color, idWidget);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обновление виджета на ИД связанной с ним заметки
     * @param context контекст
     * @param idNote ИД заметки
     */
    public static void updateWidgetByIdNote(Context context, long idNote) {
        try {
            DB.init(context).openConnection();
            Cursor cursor = DB.getInstance().getNoteInfoById(idNote);
            if (cursor.getCount() == 0) {
                return;
            }

            cursor.moveToFirst();
            int idWidget = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ID_WIDGET));
            String text = cursor.getString(cursor.getColumnIndex(DB.COLUMN_TEXT_NOTE));
            int color = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_COLOR));

            if (idWidget == AppWidgetManager.INVALID_APPWIDGET_ID) {
                return;
            }

            updateWidget(context, idNote, text, color, idWidget);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
