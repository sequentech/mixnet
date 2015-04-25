/*

Copyright 2011 Douglas Wikström

This file is part of a package for Verificatum that provides native
elliptic curve code (ECN).

ECN is free software: you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your
option) any later version.

ECN is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
License for more details.

You should have received a copy of the GNU Lesser General Public
License along with ECN.  If not, see <http://www.gnu.org/licenses/>.

*/

#include <stdio.h>
#include <stdlib.h>

#include <gmp.h>
#include "convert.h"
#include "ecn.h"

#ifdef __cplusplus
extern "C" {
#endif

void mpz_t_pair_to_2DjbyteArray(JNIEnv *env, jobjectArray* res,
				mpz_t first, mpz_t second) {

  jbyteArray firstArray;
  jbyteArray secondArray;

  // Get the byte array class
  jclass byteArrayClass = (*env)->FindClass(env, "[B");

  *res = (*env)->NewObjectArray(env, (jsize)2, byteArrayClass, NULL);

  mpz_t_to_jbyteArray(env, &firstArray, first);
  mpz_t_to_jbyteArray(env, &secondArray, second);

  (*env)->SetObjectArrayElement(env, *res, (jsize)0, firstArray);
  (*env)->SetObjectArrayElement(env, *res, (jsize)1, secondArray);
}

JNIEXPORT jobjectArray JNICALL Java_jecn_ECN_exp
  (JNIEnv *env, jclass clazz, jbyteArray javaModulus, jbyteArray javaA,
   jbyteArray javaB, jbyteArray javaX, jbyteArray javaY,
   jbyteArray javaExponent)
{
  mpz_t modulus;
  mpz_t a;
  mpz_t b;
  mpz_t x;
  mpz_t y;
  mpz_t exponent;

  mpz_t t1;
  mpz_t t2;
  mpz_t s;

  mpz_t rx;
  mpz_t ry;

  jobjectArray javaResult;

  /* Translate jbyteArray-parameters to their corresponding GMP
     mpz_t-elements. */
  jbyteArray_to_mpz_t(env, &modulus, javaModulus);
  jbyteArray_to_mpz_t(env, &a, javaA);
  jbyteArray_to_mpz_t(env, &b, javaB);
  jbyteArray_to_mpz_t(env, &x, javaX);
  jbyteArray_to_mpz_t(env, &y, javaY);
  jbyteArray_to_mpz_t(env, &exponent, javaExponent);

  /* Compute exponentiation. */

  mpz_init(t1);
  mpz_init(t2);
  mpz_init(s);
  mpz_init(rx);
  mpz_init(ry);

  ecn_exp(t1, t2, s, rx, ry, modulus, a, b, x, y, exponent);

  mpz_t_pair_to_2DjbyteArray(env, &javaResult, rx, ry);

  /* Deallocate resources. */
  mpz_clear(ry);
  mpz_clear(rx);
  mpz_clear(s);
  mpz_clear(t2);
  mpz_clear(t1);


  mpz_clear(exponent);
  mpz_clear(y);
  mpz_clear(x);
  mpz_clear(b);
  mpz_clear(a);
  mpz_clear(modulus);

  return javaResult;
}


/*
 * Class:     jecn_ECN
 * Method:    sexp
 * Signature: ([B[B[B[[B[[B[[B)[[B
 */
JNIEXPORT jobjectArray JNICALL Java_jecn_ECN_sexp
  (JNIEnv *env, jclass clazz,
   jbyteArray javaModulus, jbyteArray javaA, jbyteArray javaB,
   jobjectArray javaBasesx, jobjectArray javaBasesy,
   jobjectArray javaExponents) {

  int i;
  mpz_t *basesx;
  mpz_t *basesy;
  mpz_t *exponents;
  mpz_t modulus;
  mpz_t a;
  mpz_t b;
  mpz_t resultx;
  mpz_t resulty;

  /* Extract number of bases/exponents. */
  jsize numberOfBases = (*env)->GetArrayLength(env, javaBasesx);

  jobjectArray javaResult;

  /* Convert bases represented arrays of byte[] to arrays of mpz_t. */
  basesx = ecn_array_alloc(numberOfBases);
  basesy = ecn_array_alloc(numberOfBases);
  for (i = 0; i < numberOfBases; i++)
    {
      jbyteArray javaBasex =
	(jbyteArray)(*env)->GetObjectArrayElement(env, javaBasesx, i);
      jbyteArray_to_mpz_t(env, &(basesx[i]), javaBasex);

      jbyteArray javaBasey =
	(jbyteArray)(*env)->GetObjectArrayElement(env, javaBasesy, i);
      jbyteArray_to_mpz_t(env, &(basesy[i]), javaBasey);
    }

  /* Convert exponents represented as array of byte[] to an array of
     mpz_t. */
  exponents = ecn_array_alloc(numberOfBases);
  for (i = 0; i < numberOfBases; i++)
    {
      jbyteArray javaExponent =
	(jbyteArray)(*env)->GetObjectArrayElement(env, javaExponents, i);
      jbyteArray_to_mpz_t(env, &(exponents[i]), javaExponent);
    }

  /* Convert curve parameters represented as byte[] to mpz_t. */
  jbyteArray_to_mpz_t(env, &modulus, javaModulus);
  jbyteArray_to_mpz_t(env, &a, javaA);
  jbyteArray_to_mpz_t(env, &b, javaB);

  /* Call GMP's exponentiated product function. */
  mpz_init(resultx);
  mpz_init(resulty);

  ecn_sexp(resultx, resulty,
  	   basesx, basesy,
  	   exponents,
  	   numberOfBases,
  	   modulus, a, b);

  mpz_t_pair_to_2DjbyteArray(env, &javaResult, resultx, resulty);

  /* Deallocate resources. */
  mpz_clear(resultx);
  mpz_clear(resulty);
  mpz_clear(b);
  mpz_clear(a);
  mpz_clear(modulus);
  ecn_array_clear_dealloc(exponents, numberOfBases);
  ecn_array_clear_dealloc(basesy, numberOfBases);
  ecn_array_clear_dealloc(basesx, numberOfBases);

  return javaResult;
}

int
fexp_block_width(int bit_length, int size) {

  int width = 2;
  double cost = 1.5 * bit_length;
  double oldCost;
  do {

    oldCost = cost;

    // Amortized cost for table.
    double t = ((1 << width) - width + bit_length) / size;

    // Cost for multiplication.
    double m = (bit_length / width);

    cost = t + m;

    width++;

  } while (cost < oldCost);

  // We reduce the theoretical value by one to account for the
  // overhead.
  return width - 1;
}

/*
 * Class:     jecn_ECN
 * Method:    fexp_precompute
 * Signature: ([B[B[B[B[BII)J
 */
JNIEXPORT jlong JNICALL Java_jecn_ECN_fexp_1precompute
  (JNIEnv *env, jclass clazz,
   jbyteArray javaModulus, jbyteArray javaA, jbyteArray javaB,
   jbyteArray javaBasisx, jbyteArray javaBasisy, jint bitLength, jint size) {

  int i;
  int block_width;
  mpz_t modulus;
  mpz_t a;
  mpz_t b;

  mpz_t basisx;
  mpz_t basisy;
  mpz_t exponent;

  mpz_t *basesx;
  mpz_t *basesy;

  mpz_t t1;
  mpz_t t2;
  mpz_t s;

  ecn_fexp_tab *tablePtr =
    (ecn_fexp_tab *)malloc(sizeof(ecn_fexp_tab));

  /* Convert curve parameters represented as byte[] to mpz_t. */
  jbyteArray_to_mpz_t(env, &modulus, javaModulus);
  jbyteArray_to_mpz_t(env, &a, javaA);
  jbyteArray_to_mpz_t(env, &b, javaB);

  jbyteArray_to_mpz_t(env, &basisx, javaBasisx);
  jbyteArray_to_mpz_t(env, &basisy, javaBasisy);
  mpz_init(exponent);

  block_width = fexp_block_width((int)bitLength, (int)size);

  (*tablePtr)->slice_bit_len =
    (((int)bitLength) + (block_width - 1)) / block_width;

  ecn_sexp_init((*tablePtr)->tab, modulus, a, b, block_width, block_width);

  mpz_init(t1);
  mpz_init(t2);
  mpz_init(s);

  basesx = ecn_array_alloc_init(block_width);
  basesy = ecn_array_alloc_init(block_width);

  mpz_set_ui(exponent, 1);
  mpz_mul_2exp(exponent, exponent, (*tablePtr)->slice_bit_len);

  mpz_set(basesx[0], basisx);
  mpz_set(basesy[0], basisy);

  for (i = 1; i < block_width; i++) {

    ecn_exp(t1, t2, s,
	    basesx[i], basesy[i],
	    modulus, a, b,
	    basesx[i - 1], basesy[i - 1],
	    exponent);
  }

  ecn_sexp_precomp((*tablePtr)->tab, basesx, basesy);

  ecn_array_clear_dealloc(basesy, block_width);
  ecn_array_clear_dealloc(basesx, block_width);

  mpz_clear(s);
  mpz_clear(t2);
  mpz_clear(t1);

  mpz_clear(exponent);
  mpz_clear(basisy);
  mpz_clear(basisx);

  mpz_clear(b);
  mpz_clear(a);
  mpz_clear(modulus);

  return (jlong)(long)tablePtr;
}

/*
 * Class:     jecn_ECN
 * Method:    fexp
 * Signature: (J[B)[[B
 */
JNIEXPORT jobjectArray JNICALL Java_jecn_ECN_fexp
(JNIEnv *env, jclass clazz, jlong javaTablePtr, jbyteArray javaExponent) {

  int i;
  mpz_t exponent;
  mpz_t *exponents;
  ecn_fexp_tab *tablePtr = (ecn_fexp_tab*)(void*)javaTablePtr;
  int block_width = (*tablePtr)->tab->block_width;

  mpz_t resultx;
  mpz_t resulty;

  jobjectArray javaResult;

  jbyteArray_to_mpz_t(env, &exponent, javaExponent);
  exponents = ecn_array_alloc_init(block_width);

  for (i = 0; i < block_width; i++) {
    mpz_tdiv_r_2exp(exponents[i], exponent, (*tablePtr)->slice_bit_len);
    mpz_tdiv_q_2exp(exponent, exponent, (*tablePtr)->slice_bit_len);
  }

  mpz_init(resultx);
  mpz_init(resulty);

  ecn_sexp_table(resultx, resulty,
		 (*tablePtr)->tab,
		 exponents,
		 (*tablePtr)->slice_bit_len);

  mpz_t_pair_to_2DjbyteArray(env, &javaResult, resultx, resulty);

  mpz_clear(resulty);
  mpz_clear(resultx);
  ecn_array_clear_dealloc(exponents, block_width);
  mpz_clear(exponent);

  return javaResult;
}

/*
 * Class:     jecn_ECN
 * Method:    fexp_clear
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jecn_ECN_fexp_1clear
(JNIEnv *env, jclass clazz, jlong javaTablePtr) {

  ecn_fexp_tab *tablePtr = (ecn_fexp_tab*)(void*)javaTablePtr;
  ecn_sexp_clear((*tablePtr)->tab);
  free(tablePtr);
}



#ifdef __cplusplus
}
#endif
