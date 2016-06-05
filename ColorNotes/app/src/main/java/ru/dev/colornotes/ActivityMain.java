package ru.dev.colornotes;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Random;


public class ActivityMain extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, DialogColors.OnDialogColorsResultListener {

    private SimpleCursorAdapterListNotes adapter;
    private final int ID_LOADER = 0;
    private final int REQUEST_CODE_ACTIVITY_NOTE = 1;

    private final int MENU_ADD_ID = 1;
    private final int MENU_DELETE_ID = 2;

    private final int CM_DELETE_ID = 1;
    private final int CM_CHANGE_COLOR_ID = 2;

    private DialogColors dialogColors;
    private static Random random;

    private ListView lvNotes;
    private int countItems;

    public static Random getRandom() {
        random = (random == null) ? new Random(System.currentTimeMillis()) : random;
        return random;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            try {
                assert getSupportActionBar() != null;
                // не показываем заголовок окна
                getSupportActionBar().setDisplayShowTitleEnabled(false);

                // отображение иконки на верхней панели
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setLogo(R.mipmap.ic_color_notes);
                getSupportActionBar().setDisplayUseLogoEnabled(true);

                // отображение стрелки "<-" (home) на верхней панели
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorActionBar, R.drawable.ic_error, e);
                return;
            }

            try {
                // открываем соединение с БД
                DB.init(this).openConnection();
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorDbCreate, R.drawable.ic_error, e);
                finish();
            }

            try {
                // создаем адаптер
                String[] from = new String[]{DB.COLUMN_TEXT_NOTE, DB.COLUMN_DATE_ADD, DB.COLUMN_COLOR };
                int[] to = new int[]{R.id.tvItem, R.id.tvDateAdd, R.id.llItem };

                adapter = new SimpleCursorAdapterListNotes(this, R.layout.item, null, from, to, 0, false);

                lvNotes = ((ListView) findViewById(R.id.lvNotes));
                lvNotes.setAdapter(adapter);

                // создаем лоадер для чтения данных
                getSupportLoaderManager().initLoader(ID_LOADER, null, this);
                lvNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        openNoteForEdit(id, view);
                    }
                });

                // назначаем контекстное меню
                registerForContextMenu(lvNotes);

                random = new Random(System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorCursorLoaderManager, R.drawable.ic_error, e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // пункты меню приложения
            menu.add(0, MENU_DELETE_ID, 0, R.string.menu_delete)
                    .setIcon(R.drawable.ic_trash)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(MENU_DELETE_ID).setVisible(lvNotes.getCount() > 0);
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
        try {
            // выбор конкретного пункта меню
            Intent intent;

            if (item.getItemId() == MENU_ADD_ID) {
                try {
                    intent = getIntentForAddNote(this);
                    startActivityForResult(intent, REQUEST_CODE_ACTIVITY_NOTE);
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorOpenActivity, R.drawable.ic_error, e);
                }
            } else if (item.getItemId() == MENU_DELETE_ID) {
                try {
                    intent = new Intent(this, ActivityMultiDeleteNotes.class);
                    startActivityForResult(intent, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorOpenActivity, R.drawable.ic_error, e);
                }
            } else if (item.getItemId() == android.R.id.home) {
                finish();
            }
            return super.onOptionsItemSelected(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Формирование объекта типа Intent для старта activity при добавлении заметки
     * @param context контекст
     * @return Intent
     */
    public static Intent getIntentForAddNote(Context context) {
        Intent intent = new Intent(context, ActivityNote.class);

        intent.putExtra(DB.COLUMN_ID_NOTE, 0);
        intent.putExtra(DB.COLUMN_TEXT_NOTE, "");

        // цвет создаваемой заметки задаем рандомно
        // генерируем случайное число в диапозоне от 0 до кол-ва цветов
        int numColor = getRandom().nextInt(DialogColors.getArrayColors().length);
        int color = DialogColors.getArrayColors()[numColor];
        intent.putExtra(DB.COLUMN_COLOR, ContextCompat.getColor(context, color));

        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // обновление списка заметок
            getSupportLoaderManager().getLoader(ID_LOADER).forceLoad();
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorUnknown, R.drawable.ic_error, e);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            // закрытие соединения с БД
            DB.getInstance().closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
            DB.init(this).openConnection();
            // обновление списка заметок
            getSupportLoaderManager().getLoader(ID_LOADER).forceLoad();
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorUnknown, R.drawable.ic_error, e);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ListViewNotesCursorLoader(this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int count = data.getCount();
        if ((countItems == 0 && count > 0) || (countItems > 0 && count == 0)) {
            // обновляем меню
            invalidateOptionsMenu();
        }
        countItems = count;
        adapter.swapCursor(data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        try {
            menu.add(0, CM_CHANGE_COLOR_ID, 0, R.string.context_menu_change_color);
            menu.add(0, CM_DELETE_ID, 0, R.string.context_menu_delete);
            super.onCreateOptionsMenu(menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        try {
            final AdapterView.AdapterContextMenuInfo cmInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            if (item.getItemId() == CM_DELETE_ID) {

                Cursor cursor = DB.getInstance().getNoteInfoById(cmInfo.id);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int idWidget = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ID_WIDGET));
                    if (idWidget > AppWidgetManager.INVALID_APPWIDGET_ID) {
                        // проверяем, существует ли такой виджет
                        int[] appWidgetIds = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new ComponentName(this, WidgetNote.class));
                        for (int id : appWidgetIds) {
                            if (id == idWidget) {
                                AlertDialogWindow.showMessage(this, R.string.titleMessage, R.string.banDeleteNote, R.drawable.ic_info, null);
                                return true;
                            }
                        }
                        // виджета нет, но связка с ним почему-то осталась, удаляем эту связку
                        DB.getInstance().unsetWidget(idWidget);
                    }
                }

                DialogInterface.OnClickListener deleteNote = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DB.getInstance().deleteNote(cmInfo.id);
                            getSupportLoaderManager().getLoader(ID_LOADER).forceLoad();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                AlertDialogWindow.showConfirmMessage(this, R.string.titleConfirm, R.string.textConfirmDelete, R.drawable.ic_delete, R.string.btn_negative, R.string.btn_positive, deleteNote, null);
            } else if (item.getItemId() == CM_CHANGE_COLOR_ID) {
                try {
                    if (dialogColors == null) dialogColors = new DialogColors();
                    dialogColors.idNote = cmInfo.id;
                    dialogColors.show(getFragmentManager(), "dialogColors");
                } catch (Exception e) {
                    AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorCreateDialogColor, R.drawable.ic_error, e);
                }
            }
            return super.onContextItemSelected(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Реализация метода onSetColor интерфейса DialogColors.OnDialogColorsResultListener выбора цвета
     * @param idNote ИД заметки
     * @param color цвет заметки
     */
    @Override
    public void onSetColor(long idNote, int color) {
        try {
            DB.getInstance().changeColorNote(idNote, color);
            getSupportLoaderManager().getLoader(ID_LOADER).forceLoad();
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorChangeColor, R.drawable.ic_error, e);
        }
    }

    /**
     * Открытие окна редактирования заметки
     * @param id ИД заметки
     * @param view элемент списка заметок
     */
    public void openNoteForEdit(long id, View view) {
        try {
            int color = ((ColorDrawable)view.getBackground()).getColor();
            String text = ((TextView)view.findViewById(R.id.tvItem)).getText().toString();

            // открываем заметку для редактирования
            Intent intent = new Intent(getApplicationContext(), ActivityNote.class);
            intent.putExtra(DB.COLUMN_ID_NOTE, id);
            intent.putExtra(DB.COLUMN_TEXT_NOTE, text);
            intent.putExtra(DB.COLUMN_COLOR, color);

            startActivityForResult(intent, REQUEST_CODE_ACTIVITY_NOTE);
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorGetData, R.drawable.ic_error, e);
        }
    }

    // Отображение данных в списке и их обновление
    static class ListViewNotesCursorLoader extends CursorLoader {

        public ListViewNotesCursorLoader(Context context) {
            super(context);
        }

        @Override
        protected Cursor onLoadInBackground() {
            try {
                return DB.getInstance().getAllNotes();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
