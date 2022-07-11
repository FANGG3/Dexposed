#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <locale>
#include <fcntl.h>
#include <sys/ptrace.h>
#include "dex_file.h"

#define  LOG    "DexLearn"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__) // 定义LOGE类型

void run();

extern "C" {
    void _init(void);
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_ndk_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    //run();
    return env->NewStringUTF(hello.c_str());

}

void _init(){
    __android_log_print(ANDROID_LOG_DEBUG,"NDK","hello jni init");

}



int openFile(){
    return open("/proc/self/fd",O_RDONLY);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_ndk_DexLearn_test(JNIEnv *env, jclass clazz, jobject m_cookie,jstring jpath) {
    // TODO: implement test()
    char *n_path = const_cast<char *>(env->GetStringUTFChars(jpath, JNI_FALSE));
    jarray jarr = reinterpret_cast<jarray>(m_cookie);
    jsize size =  env->GetArrayLength(jarr);
    LOGI("%d",size);
   jlong *long_data = env->GetLongArrayElements(reinterpret_cast<jlongArray >(jarr),JNI_FALSE);

    for (int i = 1; i < size; ++i) {
        uintptr_t a = static_cast<uintptr_t>(long_data[i]);
        const art::DexFile* dexFile =reinterpret_cast<const art::DexFile*>(a);
        LOGI("0x%x",dexFile);
        LOGI("uintptr_t 0x%x size: %d",dexFile->Begin(),dexFile->Size());
        char dex_path[255];
        memset(dex_path,0,255);
        snprintf(dex_path,255,"%s/%d%s",n_path,dexFile->Size(),".dex");
        LOGI("dump dex path: %s",dex_path);
        int fd = open(dex_path,O_CREAT|O_EXCL|O_WRONLY,00200);
        if (fd < 0) {
            LOGE("Failed");
        }
        write(fd,dexFile->begin_,dexFile->size_);
        close(fd);
    }

}