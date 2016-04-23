package com.heyoe.utilities;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoUtility {

    //    convert video to byte[] from Uri============================================start
    public static byte[] convertVideoToBytes(Context context, Uri uri){
        byte[] videoBytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            FileInputStream fis = new FileInputStream(new File(getRealPathFromURI(context, uri)));
            FileInputStream fis = new FileInputStream(new File(String.valueOf(uri)));
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);

            videoBytes = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoBytes;
    }
    //    convert video to byte[] from Uri============================================end

}
