package ninja.sakib.kutupicker.utils;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class ImageUtil {
    public static final int COMPRESS_QUALITY = 80;

    public static ArrayList<String> readAllImages(Activity activity, int page, int limit) {
        int offset = (page * limit) - limit;
        String orderParams = "date_added DESC LIMIT " + offset + "," + limit;

        ArrayList<String> loadedImages = new ArrayList<>();
        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String projection[] = new String[]{MediaStore.MediaColumns.DATA};
        Cursor cursor = activity.getContentResolver().query(imagesUri, projection, null, null, orderParams);
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String imagePath = cursor.getString(index);
            loadedImages.add(imagePath);
        }

        return loadedImages;
    }

    public static String saveToStorage(Bitmap bitmap) {
        try {
            String dataRootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
            String filePath = dataRootPath + "/" + Calendar.getInstance().getTimeInMillis() + ".jpg";
            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, fileOutputStream);
            fileOutputStream.flush();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
