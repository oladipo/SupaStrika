#ifndef _QCAR_SAMPLEUTILS_H_
#define _QCAR_SAMPLEUTILS_H_

// Includes:
#include <stdio.h>
#include <android/log.h>

// Utility for logging:
#define LOG_TAG    "QCAR"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

class SampleUtils{
public:
	static void printMatrix(const float* matrix);
	static void checkGlError(const char* operation);
	static void setRotationMatrix(float angle, float x, float y, float z,
			float *nMatrix);
    /// Set the translation components of this 4x4 matrix.
    static void translatePoseMatrix(float x, float y, float z,
        float* nMatrix = NULL);
	static void rotatePoseMatrix(float angle, float x, float y, float z,
			float* nMatrix = NULL);
	static void scalePoseMatrix(float x, float y, float z, float* nMatrix = NULL);
	static void multiplyMatrix(float *matrixA, float *matrixB, float *matrixC);
	static unsigned int initShader(unsigned int shaderType, const char* source);
	static unsigned int createProgramFromBuffer(const char* vertexShaderBuffer,
			const char* fragmentShaderBuffer);


};

#endif // _QCAR_SAMPLEUTILS_H_
