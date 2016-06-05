package ru.dev.colornotes;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class ActivityWidgetConfig extends AppCompatActivity {

    private final int MENU_ADD_ID = 1;

    private int idWidget = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultIntent;

    private String[] from = new String[] { DB.COLUMN_TEXT_NOTE, DB.COLUMN_DATE_ADD, DB.COLUMN_COLOR };
    private int[] to = new int[] { R.id.tvItem, R.id.tvDateAdd, R.id.llItem };

    private ListView lvNotes;

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
            lvNotes = ((ListView) findViewById(R.id.lvNotes));
            lvNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        // сохраняем привязку заметки и виджета
                        DB.init(ActivityWidgetConfig.this).openConnection();
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
            fillListView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Заполнение списка заметок
     */
    private void fillListView() {
        try {
            // создаем адаптер
            DB.init(this).openConnection();
            Cursor cursor = DB.getInstance().getNotesNotWidget();
            SimpleCursorAdapterListNotes adapter = new SimpleCursorAdapterListNotes(this, R.layout.item, cursor, from, to, 0, false);
            lvNotes.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorCursorLoaderManager, R.drawable.ic_error, e);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // пункты меню
            menu.add(0, MENU_ADD_ID, 0, R.string.menu_add)
                    .setIcon(R.drawable.ic_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            return super.onCreateOptionsMenu(menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == MENU_ADD_ID) {
            try {
                // добавление новой заметки
                Intent intent = ActivityMain.getIntentForAddNote(this);
                startActivityForResult(intent, 0);
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorOpenActivity, R.drawable.ic_error, e);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // обновляем список заметок
        fillListView();
        super.onActivityResult(requestCode, resultCode, data);
    }
}
