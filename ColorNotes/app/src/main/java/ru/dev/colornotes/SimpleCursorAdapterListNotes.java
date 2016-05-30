package ru.dev.colornotes;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Адаптер для формирования списка заметок
 */
public class SimpleCursorAdapterListNotes extends SimpleCursorAdapter {

    private Context context;
    private Cursor cursor;
    private boolean showCheckBox;
    public ArrayList<Boolean> itemsChecked;
    public ArrayList<Integer> itemsId;

    public SimpleCursorAdapterListNotes(Context context, int layout, Cursor cursor, String[] from, int[] to, int flags, boolean showCheckBox) {
        super(context, layout, cursor, from, to, flags);

        this.context = context;
        this.cursor = cursor;
        this.showCheckBox = showCheckBox;
        this.itemsChecked = new ArrayList<>();
        this.itemsId = new ArrayList<>();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        try {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item, parent, false);
            }
            cursor = (Cursor)getItem(position);
            int id = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ID_NOTE));
            int color = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_COLOR));
            int colorDark = DialogColors.getColorDark(context, color);
            int idWidget = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ID_WIDGET));

            TextView tvItem = (TextView)convertView.findViewById(R.id.tvItem);
            tvItem.setText(cursor.getString(cursor.getColumnIndex(DB.COLUMN_TEXT_NOTE)));
            tvItem.setTextColor(colorDark);

            TextView tvDateAdd = (TextView)convertView.findViewById(R.id.tvDateAdd);
            tvDateAdd.setText(cursor.getString(cursor.getColumnIndex(DB.COLUMN_DATE_ADD)));

            convertView.setBackgroundColor(color);

            CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.cbCheckItem);
            // храним массив отметок элементов по номеру позиции в списке
            if (position >= itemsChecked.size()) {
                itemsChecked.add(position, false);
            }

            if (!showCheckBox) {
                checkBox.setVisibility(View.GONE);
            } else {
                if (idWidget != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    checkBox.setVisibility(View.GONE);
                }
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemsChecked.set(position, ((CheckBox) v).isChecked());
                    }
                });
                checkBox.setChecked(itemsChecked.get(position));
            }

            // храним ИД-ы заметок по номеру позиции в списке
            if (position >= itemsId.size()) {
                itemsId.add(position, id);
            } else {
                itemsId.set(position, id);
            }

            // если нет виджета, то не показываем иконку прикрепления
            ImageView ivPin = (ImageView)convertView.findViewById(R.id.ivPin);
            if (idWidget == AppWidgetManager.INVALID_APPWIDGET_ID) {
                ivPin.setVisibility(View.GONE);
            } else {
                ivPin.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }
}