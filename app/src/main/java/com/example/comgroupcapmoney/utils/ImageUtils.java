package com.example.comgroupcapmoney.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static File compressForUpload(Context ctx, Uri source) throws IOException {
        InputStream is = ctx.getContentResolver().openInputStream(source);
        if (is == null) throw new IOException("Cannot open input stream for " + source);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        is.close();

        int origWidth = options.outWidth;
        int origHeight = options.outHeight;
        if (origWidth <= 0 || origHeight <= 0) {
            throw new IOException("Invalid image dimensions");
        }

        int maxSide = Math.max(origWidth, origHeight);
        int inSampleSize = 1;
        while (maxSide / (inSampleSize * 2) >= 2560) {
            inSampleSize *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;

        is = ctx.getContentResolver().openInputStream(source);
        Bitmap decoded = BitmapFactory.decodeStream(is, null, options);
        if (is != null) is.close();

        if (decoded == null) {
            throw new IOException("Failed to decode bitmap");
        }

        // Scale down to max 1280px
        int width = decoded.getWidth();
        int height = decoded.getHeight();
        int currentMax = Math.max(width, height);
        Bitmap scaled = decoded;
        if (currentMax > 1280) {
            float ratio = 1280f / currentMax;
            int newW = Math.round(width * ratio);
            int newH = Math.round(height * ratio);
            scaled = Bitmap.createScaledBitmap(decoded, newW, newH, true);
            if (scaled != decoded) {
                decoded.recycle();
            }
        }

        // Check rotation via ExifInterface
        int rotationAngle = 0;
        try {
            is = ctx.getContentResolver().openInputStream(source);
            if (is != null) {
                ExifInterface exif = new ExifInterface(is);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    rotationAngle = 90;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    rotationAngle = 180;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    rotationAngle = 270;
                }
                is.close();
            }
        } catch (Exception e) {
            // Ignore exif errors
        }

        Bitmap rotated = scaled;
        if (rotationAngle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle);
            rotated = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
            if (rotated != scaled) {
                scaled.recycle();
            }
        }

        // Compress JPEG 80, 70, 60 if > 1MB
        int[] qualities = {80, 70, 60};
        byte[] finalBytes = null;
        for (int q : qualities) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            rotated.compress(Bitmap.CompressFormat.JPEG, q, baos);
            finalBytes = baos.toByteArray();
            if (finalBytes.length <= 1024 * 1024 || q == 60) {
                break;
            }
        }

        rotated.recycle();

        File uploadDir = new File(ctx.getCacheDir(), "uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File outFile = new File(uploadDir, "upload_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(outFile);
        fos.write(finalBytes);
        fos.flush();
        fos.close();

        return outFile;
    }

    public static void clearOldUploads(Context ctx) {
        long cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 hours
        cleanDir(new File(ctx.getCacheDir(), "uploads"), cutoff);
        cleanDir(new File(ctx.getCacheDir(), "captures"), cutoff);
    }

    private static void cleanDir(File dir, long cutoff) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && f.lastModified() < cutoff) {
                        f.delete();
                    }
                }
            }
        }
    }

    public static Uri createCaptureUri(Context ctx) {
        File captureDir = new File(ctx.getCacheDir(), "captures");
        if (!captureDir.exists()) {
            captureDir.mkdirs();
        }
        File file = new File(captureDir, "capture_" + System.currentTimeMillis() + ".jpg");
        try {
            file.createNewFile();
        } catch (IOException e) {
            // proceed anyway
        }
        String authority = ctx.getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(ctx, authority, file);
    }
}
