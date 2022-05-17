package com.smallbell.pathrecg.utils;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class ImageCompressUtils {
    public static void compress(Context context, File file, ImageCompressCallBack callback) {
//        getPath()
        Luban.with(context)
                .load(file)
                .ignoreBy(100)
                .setTargetDir(context.getExternalFilesDir(null).getAbsolutePath())
//                .filter(new CompressionPredicate() {
//                    @Override
//                    public boolean apply(String path) {
//                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
//                    }
//                })
                .setCompressListener(callback)
                .launch();
    }

    class ImageCompressCallBack implements OnCompressListener {

        @Override
        public void onStart() {

        }

        @Override
        public void onSuccess(File file) {

        }

        @Override
        public void onError(Throwable e) {

        }
    }
}
