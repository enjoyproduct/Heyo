package com.heyoe.utilities;

import java.io.File;

/**
 * Created by Administrator on 1/26/2016.
 */
public class FileUtility {
    public static String getFilenameFromPath(String fileLocalPath) {
        return fileLocalPath.substring(fileLocalPath.lastIndexOf("/") + 1);
    }
    public static void deleteFilesInDirectory(String dirPath){
        File f = new File(dirPath);
        File file[] = f.listFiles();
        for(File df : file){
            df.delete();

        }

    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        else if(dir!= null && dir.isFile())
            return dir.delete();
        else {
            return false;
        }
    }

}
