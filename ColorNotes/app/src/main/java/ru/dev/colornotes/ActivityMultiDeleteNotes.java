package ru.dev.colornotes;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import java.util.ArrayList;

public class ActivityMultiDeleteNotes extends AppCompatActivity {

    private final int MENU_DELETE_ID = 1;

    private SimpleCursorAdapterListNotes adapter;
    private ArrayList<Integer> itemsCheckedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            try {
                assert getSupportActionBar() != null;

                // не показываем заголовок окна
                getSupportActionBar().setDisplayShowTitleEnabled(false);

                // вид ActionBar
                getSupportActionBar().setCustomView(R.layout.actionbar_multi_delete_notes);
                getSupportActionBar().setDisplayShowCustomEnabled(true);

                View view = getSupportActionBar().getCustomView();
                ((CheckBox) view.findViewById(R.id.cbCheckedAll)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        for (int i = 0; i < adapter.itemsChecked.size(); i++) {
                            adapter.itemsChecked.set(i, isChecked);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialogWindow.showMessage(getApplicationContext(), R.string.titleError, R.string.errorActionBar, R.drawable.ic_error, e);
                finish();
                return;
            }

            try {
                // создаем адаптер
                String[] from = new String[] { DB.COLUMN_TEXT_NOTE, DB.COLUMN_DATE_ADD, DB.COLUMN_COLOR };
                int[] to = new int[] { R.id.tvItem, R.id.tvDateAdd, R.id.llItem };

                Cursor cursor = DB.getInstance().getNotesNotWidget();
                adapter = new SimpleCursorAdapterListNotes(this, R.layout.item, cursor, from, to, 0, true);

                ListView lvNotes = ((ListView) findViewById(R.id.lvNotes));
                lvNotes.setAdapter(adapter);
                lvNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        CheckBox cbCheckItem = (CheckBox)view.findViewById(R.id.cbCheckItem);
                        cbCheckItem.setChecked(!cbCheckItem.isChecked());
                        cbCheckItem.callOnClick();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialogWindow.showMessage(this, R.string.titleError, R.string.errorCursorLoaderManager, R.drawable.ic_error, e);
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
            menu.add(0, MENU_DELETE_ID, 0, "").setIcon(R.drawable.ic_trash).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
                case MENU_DELETE_ID:
                    itemsCheckedId = new ArrayList<>();
                    for (int i = 0; i < adapter.itemsChecked.size(); i++) {
                        if (adapter.itemsChecked.get(i)) {
                            itemsCheckedId.add(adapter.itemsId.get(i));
                        }
                    }
                    if (itemsCheckedId.size() > 0) {
                        DialogInterface.OnClickListener deleteNote = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DB.getInstance().multiDeleteNotes(itemsCheckedId.toArray());
                                    dialog.dismiss();
                                    finish();
                                } catch (Exception e) {
                                    dialog.dismiss();
                                    AlertDialogWindow.showMessage(getApplication(), R.string.titleError, R.string.errorDelete, R.drawable.ic_error, e);
                                }
                            }
                        };
                        AlertDialogWindow.showConfirmMessage(this, R.string.titleConfirm, R.string.textConfirmMultiDelete, R.drawable.ic_delete, R.string.btn_negative, R.string.btn_positive, deleteNote, null);
                    } else {
                        AlertDialogWindow.showMessage(this, R.string.titleMessage, R.string.textNotCheckedItems, R.drawable.ic_info, null);
                    }
                    break;
            }
            return super.onOptionsItemSelected(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
