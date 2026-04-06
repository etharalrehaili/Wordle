#include <jni.h>
#include <string>

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_khammin_core_data_ndk_KeyManager_getAuthToken(JNIEnv *env, jobject) {
    return env->NewStringUTF(AUTH_TOKEN);
}

JNIEXPORT jstring JNICALL
Java_com_khammin_core_data_ndk_KeyManager_getBaseUrl(JNIEnv *env, jobject) {
    return env->NewStringUTF(BASE_URL);
}

JNIEXPORT jstring JNICALL
Java_com_khammin_core_data_ndk_KeyManager_getFirebaseDbUrl(JNIEnv *env, jobject) {
    return env->NewStringUTF(FIREBASE_DB_URL);
}

JNIEXPORT jstring JNICALL
Java_com_khammin_core_data_ndk_KeyManager_getGoogleApiKey(JNIEnv *env, jobject) {
    return env->NewStringUTF(GOOGLE_API_KEY);
}

JNIEXPORT jstring JNICALL
Java_com_khammin_core_data_ndk_KeyManager_getBaseHost(JNIEnv *env, jobject) {
    return env->NewStringUTF(BASE_HOST);
}

} // extern "C"
