package ru.dev.colornotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;

/**
 * Диалоговое окно выбора цвета заметки
 */
public class DialogColors extends DialogFragment {

    public long idNote;
    private static int[] arrayColors = getArrayColors();
    private static int[] arrayColorsDark = getArrayColorsDark();

    public static int[] getArrayColors() {
        return arrayColors == null ? new int[] {
                R.color.colorNoteGreen,
                R.color.colorNoteYellow,
                R.color.colorNoteBlue,
                R.color.colorNotePink,
                R.color.colorNotePurple,
                R.color.colorNoteWhite,
                R.color.colorNoteRed,
                R.color.colorNoteOrange,
                R.color.colorNoteBrown,
                R.color.colorNoteSea
        } : arrayColors;
    }

    public static int[] getArrayColorsDark() {
        return arrayColorsDark == null ? new int[] {
                R.color.colorNoteGreenDark,
                R.color.colorNoteYellowDark,
                R.color.colorNoteBlueDark,
                R.color.colorNotePinkDark,
                R.color.colorNotePurpleDark,
                R.color.colorNoteWhiteDark,
                R.color.colorNoteRedDark,
                R.color.colorNoteOrangeDark,
                R.color.colorNoteBrownDark,
                R.color.colorNoteSeaDark
        } : arrayColorsDark;
    }

    // интерфейс выбора цвета для передачи результата в родительскую activity
    public interface OnDialogColorsResultListener {
        /**
         * Установка цвета заметки
         *
         * @param idNote ИД заметки
         * @param color цвет заметки
         */
        void onSetColor(long idNote, int color);
    }

    private OnDialogColorsResultListener onComplete;

    @Override
    public void onAttach(Activity activity) {
        try {
            // прикрепление activity, которая реализует интерфейс OnDialogColorsResultListener
            super.onAttach(activity);
            onComplete = (OnDialogColorsResultListener)getActivity();
        } catch (final ClassCastException e) {
            throw new ClassCastException("Calling Activity must implement OnDialogColorsResultListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

            // иконка
            alertDialogBuilder.setIcon(R.drawable.ic_colors);

            // заголовок
            View viewDialogTitle = getActivity().getLayoutInflater().inflate(R.layout.dialog_colors_title, null);
            alertDialogBuilder.setCustomTitle(viewDialogTitle);

            // тело диалога
            View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_colors, null);
            GridView gvColors = (GridView) v.findViewById(R.id.gvColors);

            String[] arrayColorsName = new String[] {
                    getActivity().getResources().getString(R.string.colorNameGreen),
                    getActivity().getResources().getString(R.string.colorNameYellow),
                    getActivity().getResources().getString(R.string.colorNameBlue),
                    getActivity().getResources().getString(R.string.colorNamePink),
                    getActivity().getResources().getString(R.string.colorNamePurple),
                    getActivity().getResources().getString(R.string.colorNameWhite),
                    getActivity().getResources().getString(R.string.colorNameRed),
                    getActivity().getResources().getString(R.string.colorNameOrange),
                    getActivity().getResources().getString(R.string.colorNameBrown),
                    getActivity().getResources().getString(R.string.colorNameSea)
            };
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_color, arrayColorsName){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    Button view = (Button) super.getView(position, convertView, parent);
                    view.setBackgroundColor(ContextCompat.getColor(getActivity(), arrayColors[position]));
                    view.setTextColor(ContextCompat.getColor(getActivity(), arrayColorsDark[position]));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onComplete.onSetColor(idNote, ((ColorDrawable) v.getBackground()).getColor());
                            getDialog().dismiss();
                        }
                    });
                    return view;
                }
            };
            gvColors.setAdapter(adapter);

            // кнопка Отмена
            v.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                }
            });
            alertDialogBuilder.setView(v);

            return alertDialogBuilder.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Получение темного цвета относительного главного цвета заметки
     * @param context контекст
     * @param color цвет
     * @return int
     */
    public static int getColorDark(Context context, int color) {
        try {
            int colorDark = ContextCompat.getColor(context, R.color.colorNoteGreenDark);

            for (int i = 0; i < arrayColors.length; i++) {
                if (color == ContextCompat.getColor(context, arrayColors[i])) {
                    colorDark = ContextCompat.getColor(context, arrayColorsDark[i]);
                    break;
                }
            }
            return colorDark;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
