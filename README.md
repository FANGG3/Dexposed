# Dexposed
Learn：Android8.1.0下的类抽取类型脱壳  

step1：拿到类所在的classloader  
step2：获取其父类dalvik.system.BaseDexClassLoader，不论是否为自定义classloader，父类都是 BaseDexClassLoader  
step3：反射拿到BaseDexClassLoader 的DexPathList pathList 成员  
step4：pathList 反射拿 dexElements成员  
step5：dexElements是一个数组，对应element中有 dexFile 成员  ### 常见脱壳点  
step6：获取到dexFile 成员，其mCookie是一个数组，对应Native 层 DexFile指针  
step7: 拿到mCookie后,在Native将其转化为DexFile对象，可以通过其_begin (dex文件指针) _size (大小) ，dump 出 dex  

[参考](https://github.com/AlienwareHe/RDex)
