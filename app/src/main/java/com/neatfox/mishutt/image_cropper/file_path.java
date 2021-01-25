package com.neatfox.mishutt.image_cropper;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class file_path {

    public static String getFilePath(Context context, Uri uri){
        boolean isKitKat= true;
        if(isKitKat && DocumentsContract.isDocumentUri(context,uri)){
            if (isExternalStorageDocument(uri)) {

                String docId=DocumentsContract.getDocumentId(uri);
                String[] split=docId.split(":");
                String type=split[0];

                if("primary".equalsIgnoreCase(type)){
                    return Environment.getExternalStorageDirectory()+"/"+split[1];
                }
            }
            else if(isDownloadsDocument(uri)){
                String id=DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.parseLong(id));

                return getDataColumn(context, contentUri,null,null);
            }
            else if(isMediaDocument(uri)){
                String docId=DocumentsContract.getDocumentId(uri);
                String[] split=docId.split(":");
                String type=split[0];
                Uri contentUri =null;
                if("image".equals(type)){
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if("video".equals(type)){
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if("audio".equals(type)){
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                String selection="_id=?";
                String[] selectionarg=new String[]{split[1]};

                return getDataColumn(context, contentUri,selection,selectionarg);
            }

        }
        else if("content".equalsIgnoreCase(uri.getScheme())){
            if(isGooglePhotosUri(uri))
            {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context,uri,null,null);
        }
        else if("file".equalsIgnoreCase(uri.getScheme())){
            return uri.getPath();
        }
        return null;
    }


    //Let's create some methods before accessing path

    public static String getDataColumn(Context context,Uri uri,String selection,String[] selectionargs){
        Cursor cursor=null;
        final String column="_data";
        final String[] projection={column};
        try {
            cursor=context.getContentResolver().query(uri,projection,selection,selectionargs,null);
            if(cursor!=null && cursor.moveToFirst()){
                int index=cursor.getColumnIndexOrThrow(column);
                return  cursor.getString(index);
            }
        }
        finally {
            if(cursor!=null){
                ((Cursor) cursor).close();
            }
        }

        return null;

    }

    public static boolean isExternalStorageDocument(Uri uri){
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri){
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri){
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri){
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
