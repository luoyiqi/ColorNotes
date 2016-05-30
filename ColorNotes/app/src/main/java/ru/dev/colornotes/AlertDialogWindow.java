package ru.dev.colornotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Диалоговое окно для вывода сообщений
 */

public class AlertDialogWindow {

    private static AlertDialog.Builder alertDialogBuilderMessage;
    private static AlertDialog.Builder alertDialogBuilderConfirm;

    /**
     * Отображение диалогового окна с выводом текстового сообщения и одной кнопкой
     * @param context контекст
     * @param titleResource заголовок
     * @param messageResource сообщение
     * @param icon иконка
     * @param exception исключение
     */
    public static void showMessage(Context context, int titleResource, int messageResource, int icon, Exception exception) {

        try {
            if (alertDialogBuilderMessage == null || alertDialogBuilderMessage.getContext() != context) {
                alertDialogBuilderMessage = new AlertDialog.Builder(context);
                alertDialogBuilderMessage.setCancelable(false)
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
            }

            alertDialogBuilderMessage.setTitle(context.getResources().getString(titleResource))
                    .setIcon(icon)
                    .setMessage(context.getResources().getString(messageResource) + ". " + (exception == null ? "" : exception.getMessage()));
            alertDialogBuilderMessage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.errorOpenAlertDialog, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Отображение диалогового окна для подтверждения выполнения какого-либо действия
     * @param context контекст
     * @param titleResource заголовок
     * @param messageResource сообщение
     * @param icon иконка
     * @param btnNegative кнопка "Нет"
     * @param btnPositive кнопка "Да"
     * @param onClickPositiveButton обработчик нажатия кнопки "Да"
     */
    public static void showConfirmMessage(Context context,
                                          int titleResource,
                                          int messageResource,
                                          int icon,
                                          int btnNegative,
                                          int btnPositive,
                                          @NonNull DialogInterface.OnClickListener onClickPositiveButton,
                                          @Nullable DialogInterface.OnClickListener onClickNegativeButton) {
        try {
            if (alertDialogBuilderConfirm == null || alertDialogBuilderConfirm.getContext() != context) {
                alertDialogBuilderConfirm = new AlertDialog.Builder(context);
            }

            alertDialogBuilderConfirm.setTitle(titleResource)
                    .setIcon(icon)
                    .setMessage(context.getResources().getString(messageResource));
            if (onClickNegativeButton == null) {
                alertDialogBuilderConfirm.setNegativeButton(btnNegative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            } else {
                alertDialogBuilderConfirm.setNegativeButton(btnNegative, onClickNegativeButton);
            }
            alertDialogBuilderConfirm.setPositiveButton(btnPositive, onClickPositiveButton);
            alertDialogBuilderConfirm.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.errorOpenAlertDialog, Toast.LENGTH_LONG).show();
        }
    }
}
