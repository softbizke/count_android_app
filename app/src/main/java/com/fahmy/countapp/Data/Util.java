package com.fahmy.countapp.Data;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.fahmy.countapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {


    //file heper

    public File getFileFromUri(@Nullable Uri uri, Context context) {
        if (uri == null) return null;

        try {
            String mimeType = context.getContentResolver().getType(uri);
            String extension = null;
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            }
            String filename = "upload_" + System.currentTimeMillis() + (extension != null ? "." + extension : ".jpg");
            File tempFile = new File(context.getCacheDir(), filename);

            try (InputStream in = context
                    .getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(tempFile)) {

                if (in == null) return null;

                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }

            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Double extractWeight(String description) {
        if (description == null) return null;

        Pattern KG_PATTERN =
                Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*kg", Pattern.CASE_INSENSITIVE);
        Matcher matcher = KG_PATTERN.matcher(description);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return 0.0; // no match or parse error
    }


    public static String formatDate(String isoDateString) {
        ZonedDateTime zonedDateTime = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            zonedDateTime = ZonedDateTime.parse(isoDateString);

            ZonedDateTime userDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.ENGLISH);
            return userDateTime.format(formatter);
        }
        return isoDateString;
    }




    public static AlertDialog showDialog(Context context, String message, int colorResId) {
        AlertDialog progressDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_progress, null);
        builder.setView(view);
        builder.setCancelable(false);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        tvMessage.setText(message);

        // Change color if provided
        if (colorResId != 0) {
            progressBar.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(context, colorResId),
                    android.graphics.PorterDuff.Mode.SRC_IN
            );
        }

        progressDialog = builder.create();
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return progressDialog;
    }

    public static void hideDialog(AlertDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
