#An Android.mk file must begin with the definition of the LOCAL_PATH variable.
#It is used to locate source files in the development tree

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := QCAR-prebuilt
LOCAL_SRC_FILES = ../build/lib/$(TARGET_ARCH_ABI)/libQCAR.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../build/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := SupaStriker
USE_OPENGL_ES_1_1 := false

# Set OpenGL ES version-specific settings.

ifeq ($(USE_OPENGL_ES_1_1), true)
    OPENGLES_LIB  := -lGLESv1_CM
	OPENGLES_DEF  := -DUSE_OPENGL_ES_1_1
else
    OPENGLES_LIB  := -lGLESv2
	OPENGLES_DEF  := -DUSE_OPENGL_ES_2_0
endif

LOCAL_CFLAGS := -Wno-write-strings -Wno-psabi $(OPENGLES_DEF)

LOCAL_LDLIBS := \
    -llog $(OPENGLES_LIB)
    
LOCAL_SHARED_LIBRARIES := QCAR-prebuilt

LOCAL_SRC_FILES := SupaStriker.cpp Texture.cpp SampleUtils.cpp

LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)