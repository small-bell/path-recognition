package com.smallbell.pathrecg.utils;

import android.util.Log;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OkHttpImageUploaderUtil {
    public static OkHttpClient client = new OkHttpClient();
    public static MediaType mediaType = MediaType.parse("image/png");


    public static Response updateHeadImg(String url, File f) {
        try {
            RequestBody fileBody = RequestBody.create(mediaType, f);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", f.getName(), fileBody)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            return client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OkHttpImageUploaderUtil", "exception: " + e.toString());
            Log.e("OkHttpImageUploaderUtil", "exception: " + e.getMessage());
        }
        return null;
    }


//    public static void main(String[] args) {
//        String url = "http://127.0.0.1:8000/upload";
//        File f = new File("E:\\Temp\\article.png");
//        Response response = new OkHttpUtil().updateHeadImg(url, f);
//        try {
//            System.out.println(response.body().string());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
