package ru.dev.colornotes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

public class ActivityNote extends AppCompatActivity implements DialogColors.OnDialogColorsResultListener {

    private final int MENU_CHANGE_COLOR_ID = 1;
    private final int MENU_DELETE_ID = 2;
    private final int MENU_SAVE_ID = 3;

    private long idNote;
    private String oldTextNote;
    private ScrollView svNote;
    private EditText etNote;

    private DialogColors dialogColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_note);

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
                AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorActionBar, R.drawable.ic_error, e);
                finish();
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
                svNote = (ScrollView) findViewById(R.id.svNote);
                etNote = (EditText) findViewById(R.id.etNote);

                // получаем данные о заметке
                Intent intent = getIntent();
                idNote = intent.getLongExtra(DB.COLUMN_ID_NOTE, 0);
                oldTextNote = intent.getStringExtra(DB.COLUMN_TEXT_NOTE);
                etNote.setText(oldTextNote);
                int color = intent.getIntExtra(DB.COLUMN_COLOR, 0);

                // цвет заметки
                setColorWindowNote(color);

                // устанавливаем курсор в конец
                etNote.setSelection(etNote.getText().length());
            } catch (Exception e) {
                AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorFillData, R.drawable.ic_error, e);
                finish();
            }

            try {
                // при клике на текстовое поле показывать клавиатуру
                etNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //etNote.requestFocusFromTouch();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(etNote, 0);
                    }
                });

            } catch (Exception e) {
                AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorSetMethods, R.drawable.ic_error, e);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            menu.add(0, MENU_DELETE_ID, 0, R.string.menu_delete)
                    .setIcon(R.drawable.ic_trash)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(MENU_DELETE_ID).setVisible(idNote > 0);
            menu.add(0, MENU_CHANGE_COLOR_ID, 0, R.string.menu_change_color)
                    .setIcon(R.drawable.ic_colors)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0, MENU_SAVE_ID, 0, R.string.menu_save)
                    .setIcon(R.drawable.ic_save)
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
            switch (item.getItemId()) {
                case android.R.id.home:
                    finish();
                    break;
                case MENU_CHANGE_COLOR_ID:
                    try {
                        if (dialogColors == null)
                            dialogColors = new DialogColors();
                        dialogColors.idNote = idNote;
                        dialogColors.show(getFragmentManager(), "dialogColors");
                    } catch (Exception e) {
                        AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorCreateDialogColor, R.drawable.ic_error, e);
                    }
                    break;
                case MENU_DELETE_ID:
                    DialogInterface.OnClickListener deleteNote = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                // удаление заметки
                                try {
                                    DB.getInstance().deleteNote(idNote);
                                } catch (Exception e) {
                                    AlertDialogWindow.showMessage(ActivityNote.this, R.string.titleError, R.string.errorDelete, R.drawable.ic_error, e);
                                    return;
                                }
                                finish();
                                dialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    AlertDialogWindow.showConfirmMessage(this, R.string.titleConfirm, R.string.textConfirmDelete, R.drawable.ic_delete, R.string.btn_negative, R.string.btn_positive, deleteNote, null);
                    break;
                case MENU_SAVE_ID:
                    saveNote(true);
                    break;
            }
            return super.onOptionsItemSelected(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Сохраняет изменения в заметке и завершает работу текущего activity
     * @param showToastResultOk показывать всплывающее сообщение при успешном сохранении
     * @return boolean
     */
    public boolean saveNote(boolean showToastResultOk) {
        try {
            if (idNote == 0 && !etNote.getText().toString().trim().equals("")) {
                // добавление новой заметки
                try {
                    idNote = DB.getInstance().addNote(etNote.getText().toString(), getColorNote());
                    oldTextNote = etNote.getText().toString();
                    // обновляем меню, чтобы появилась кнопка удаления заметки
                    invalidateOptionsMenu();
                    if (showToastResultOk) {
                        Toast.makeText(this, getResources().getString(R.string.successSave), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorAdd, R.drawable.ic_error, e);
                    return false;
                }
            } else if (idNote > 0 && !etNote.getText().toString().trim().equals("")) {
                // редактирование
                try {
                    DB.getInstance().changeNote(idNote, etNote.getText().toString(), getColorNote());
                    oldTextNote = etNote.getText().toString();
                    if (showToastResultOk) {
                        Toast.makeText(this, getResources().getString(R.string.successSave), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorEdit, R.drawable.ic_error, e);
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Подтверждение сохранения заметки
     */
    /*private void confirmSave() {
        try {
            if (etNote.getText().toString().trim().equals("") || etNote.getText().toString().trim().equals(oldTextNote)) {
                finish();
                return;
            }
            // обработчик кнопки "Сохранить"
            DialogInterface.OnClickListener saveNoteClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        if (saveNote()) {
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            // обработчик кнопки "Не сохранять"
            DialogInterface.OnClickListener notSaveClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        dialog.cancel();
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            AlertDialogWindow.showConfirmMessage(this, R.string.titleConfirm, R.string.textConfirmSave, R.drawable.ic_save, R.string.btn_negative, R.string.btn_positive, saveNoteClick, notSaveClick);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /**
     * Реализация метода onSetColor интерфейса DialogColors.OnDialogColorsResultListener выбора цвета
     * @param idNote ИД заметки
     * @param color цвет заметки
     */
    @Override
    public void onSetColor(long idNote, int color) {
        try {
            // меняем цвет окна заметки
            setColorWindowNote(color);
            if (idNote > 0) {
                // сохраняем цвет заметки
                DB.getInstance().changeColorNote(idNote, color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        // Обработчик нажания клавиши "Назад"
        super.onBackPressed();
        saveNote(false);
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
            DB.init(this).openConnection();
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

    /**
     * Получение цвет заметки по background
     * @return int цвет
     */
    private int getColorNote() {
        return ((ColorDrawable)svNote.getBackground()).getColor();
    }

    /**
     * Окрашивания окна в цвет заметки
     * @param color цвет заметки
     */
    private void setColorWindowNote(int color) {
        try {
            // цвет StatusBar
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //    getWindow().setStatusBarColor(DialogColors.getColorDark(this, color));
            //}

            // цвет ActionBar
            //if (getSupportActionBar() != null) {
            //    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(DialogColors.getColorDark(this, color)));
            //}

            // цвет фона текста
            svNote.setBackgroundColor(color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // автоматическое сохранение
        saveNote(false);
    }
}
