#include <jni.h>

#ifndef _convert
#define _convert
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Translates the representation of a positive integer given as a
 * jbyteArray in two's complement representation into its
 * representation as a GMP mpz_t element. It initializes gmpValue, so
 * it should point to an uninitialized variable before the call.
 */
void
jbyteArray_to_mpz_t(JNIEnv* env, mpz_t* gmpValue, jbyteArray javaBytes);

/*
 * Translates the representation of a positive integer given as a GMP
 * mpz_t element into its representation as a two's complement in a
 * jbyteArray. It allocates a jbyteArray in JVM memory space, so it
 * should be uninitialized before the call.
 */
void
mpz_t_to_jbyteArray(JNIEnv* env, jbyteArray* javaBytes, mpz_t gmpValue);

#ifdef __cplusplus
}
#endif
#endif
