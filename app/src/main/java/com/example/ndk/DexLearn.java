package com.example.ndk;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;

import dalvik.system.DexFile;

public class DexLearn {
   // static {
   //     System.loadLibrary("ndk");
   // }
    String TAG = "DexLearn";
    Context context;
    public Field getPathList_Field(ClassLoader target_loader) {
        Class loader_cls = target_loader.getClass();
        Field f_pathList = null;
        while (loader_cls != null) {
            try {
                f_pathList = loader_cls.getDeclaredField("pathList");//获取父类的方法 dalvik.system.BaseDexClassLoader
                f_pathList.setAccessible(true);

                if (f_pathList != null) {
                    Log.i(TAG, "获取classloader："+ loader_cls.getName());
                    return f_pathList;
                }

            } catch (Exception e) {
                //e.printStackTrace();
            }
            loader_cls = loader_cls.getSuperclass(); // 获取到 dalvik.system.BaseDexClassLoader类中的 pathList 成员

        }
        return f_pathList;
    }

    public DexLearn() {
        Log.i(TAG, "hhh");
    }
//step1：拿到类所在的classloader
    //step2：获取其父类dalvik.system.BaseDexClassLoader，不论是否为自定义classloader，父类都是 BaseDexClassLoader
    //step3：反射拿到BaseDexClassLoader 的DexPathList pathList 成员
    //step4：pathList 反射拿 dexElements成员
    //step5：dexElements是一个数组，对应element中有 dexFile 成员  ### 常见脱壳点
    //step6：获取到dexFile 成员，其mCookie是一个数组，对应Native 层 DexFile指针
    //step7: 拿到mCookie后,在Native将其转化为DexFile对象，可以通过其_begin (dex文件指针) _size (大小) ，dump 出 dex

    public void getDex(ClassLoader target_loader) {
        //Class target_class = this.getClass();
        Field pathList_field = getPathList_Field(target_loader);
        Log.d(TAG, "enter getDex");
        try {
            Object pathList = pathList_field.get(target_loader);
            Field f_dexElements = pathList.getClass().getDeclaredField("dexElements");
            f_dexElements.setAccessible(true);
            Object[] o = (Object[]) f_dexElements.get(pathList);
            Log.i(TAG, "dex elements " + Arrays.toString(o));


            for (Object value : o) {
                DexFile dexFile = (DexFile) reflect_getField(value, "dexFile");
                Log.i(TAG, "dex elements " + value.getClass().getName());
                Log.i(TAG, "dexfile " + dexFile.getClass().getName());
                Object mCookie = reflect_getField(dexFile, "mCookie");
                Log.i(TAG, "mCookie " + mCookie);

                Enumeration<String> entries = dexFile.entries();
                while(entries.hasMoreElements()){
                    String className = entries.nextElement();
                    Log.i(TAG, "dexFile methods:"+className);
                    try {
                        Class.forName(className,false,target_loader);
                    }catch (Throwable throwable){

                    }
                }


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        test(mCookie,context.getFilesDir().getAbsolutePath()+"/dex");
                    }
                },10000);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static Object reflect_callMethod(Object target_obj, String name, Object... args) {
        Object ret = null;
        try {
            Method declaredMethod = target_obj.getClass().getDeclaredMethod(name);
            declaredMethod.setAccessible(true);
            ret = declaredMethod.invoke(target_obj, args);

        } catch (Exception e) {

            e.printStackTrace();
        }
        return ret;
    }

    public static Object reflect_getField(Object target_obj, String name) {
        Object ret = null;
        try {
            Field de = target_obj.getClass().getDeclaredField(name);
            de.setAccessible(true);
            ret = de.get(target_obj);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return ret;
    }

    public static native void test(Object mCookie,String path);


}
