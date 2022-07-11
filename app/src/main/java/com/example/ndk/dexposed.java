package com.example.ndk;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class dexposed implements IXposedHookLoadPackage {
    static Context context ;
    static LinkedList<ClassLoader> loader_list = new LinkedList();
    static ClassLoader p_loader ;
    static  DexLearn dexLearn;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        dexLearn = new DexLearn();
        try  {
            Class<?>  ContextClass  =  XposedHelpers.findClass("android.content.ContextWrapper",  lpparam.classLoader);
            findAndHookMethod(ContextClass,  "getApplicationContext",  new  XC_MethodHook()  {
                @SuppressLint("UnsafeDynamicallyLoadedCode")
                @Override
                protected  void  afterHookedMethod(MethodHookParam  param)  throws  Throwable  {
                    super.afterHookedMethod(param);
                    if  (context  !=  null)
                        return;
                    context  =  (Context)  param.getResult();
                    XposedBridge.log("得到上下文");

                    //加载so
                    File file = new File(context.getFilesDir().getAbsolutePath() + "/dex");
                    Log.i("creatfile",file.getAbsolutePath());
                    file.mkdirs();
                    System.load(getPath()+"/libndk.so");
                }
            });

        }  catch  (Throwable  t)  {
            XposedBridge.log("获取上下文出错");
            XposedBridge.log(t);
        }


        XposedBridge.hookAllMethods(ClassLoader.class, "loadClass", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (context == null){return;}
                XposedBridge.log("dex enter");
                Class cls = (Class) param.getResult();
                if(cls != null) {
                    ClassLoader loader = cls.getClassLoader();
                    if (loader_list.contains(loader)){return;}
                    loader_list.add(loader);
                    XposedBridge.log("dex "+loader_list.size());
                    if (cls.getName().contains("java.lang")){return;}
                    if (loader.equals(null)){return;}
                            dexLearn.context = context;
                            dexLearn.getDex(loader);
                }
            }
        });





    }

    public static String getPath() {
        List<ApplicationInfo> list = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo info : list) {
            if (info.packageName.equals("com.example.ndk")) {
                    return info.nativeLibraryDir;
            }
        }
        return "";
    }
}
