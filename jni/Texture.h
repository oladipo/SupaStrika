#ifndef _QCAR_TEXTURE_H_
#define _QCAR_TEXTURE_H_

#include <jni.h>

class Texture{
public:
	Texture();
	~Texture();
	unsigned int getWidth() const;
	unsigned int getHeight() const;
	static Texture* create(JNIEnv* env, jobject textureObject);
	unsigned int mWidth;
	unsigned int mHeight;
	unsigned int mTextureID;
	unsigned char* mData;
	unsigned int mChannelCount;

private:
	Texture(const Texture &);
	Texture& operator= (const Texture &);
};

#endif //_QCAR_TEXTURE_H_
