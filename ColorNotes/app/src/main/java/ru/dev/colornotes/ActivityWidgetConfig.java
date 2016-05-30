package ru.dev.colornotes;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class ActivityWidgetConfig extends AppCompatActivity {

    public final static String WIDGET_PREFERENCE = "widget_preference";
    public final static String PREFIX_NOTE = "note";

    private int idWidget = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // извлекаем ID конфигурируемого виджета
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                idWidget = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }

            // проверяем его корректность
            if (idWidget == AppWidgetManager.INVALID_APPWIDGET_ID) {
                finish();
            }

            // формируем intent ответа
            resultIntent = new Intent();
            resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, idWidget);

            // отрицательный ответ. Если пользователь нажмет "Назад", система получит ответ, что виджет
            // создавать не надо.
            setResult(RESULT_CANCELED, resultIntent);

            setContentView(R.layout.activity_main);
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogWindow.showMessage(this, R.string.titleError, 0, R.drawable.ic_error, e);
            return;
        }

        try {
            // создаем адаптер
            String[] from = new String[] { DB.COLUMN_TEXT_NOTE, DB.COLUMN_DATE_ADD, DB.COLUMN_COLOR };
            int[] to = new int[] { R.id.tvItem, R.id.tvDateAdd, R.id.llItem };

            DB.init(this).openConnection();
            Cursor cursor = DB.getInstance().getNotesNotWidget();
            SimpleCursorAdapterListNotes adapter = new SimpleCursorAdapterListNotes(this, R.layout.item, cursor, from, to, 0, false);

            ListView lvNotes = ((ListView) findViewById(R.id.lvNotes));
            lvNotes.setAdapter(adapter);
            lvNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        // сохраняем привязку заметки и виджета
                        DB.getInstance().setIdWidget(id, idWidget);

                        // обновляем виджет
                        WidgetNote.updateWidgetByIdWidget(getApplicationContext(), idWidget);

                        setResult(RESULT_OK, resultIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorCursorLoaderManager, R.drawable.ic_error, e);
            finish();
        }
    }
}
