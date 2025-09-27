package com.fahmy.countapp.Data;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
}
