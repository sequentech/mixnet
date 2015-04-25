/*

Copyright 2008 2009 Torbjorn Granlund, Douglas Wikstrom

This file is part of JGMPMEE.

JGMPMEE is free software: you can redistribute it
and/or modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

JGMPMEE is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with JGMPMEE.  If not, see
<http://www.gnu.org/licenses/>.

*/

#include <stdio.h>
#include <stdlib.h>

#include <gmp.h>
#include "gmpmee.h"
#include "convert.h"

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     jgmpmee_GMPMEE
 * Method:    powm
 * Signature: ([B[B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_jgmpmee_GMPMEE_powm
(JNIEnv *env, jclass clazz, jbyteArray javaBasis, jbyteArray javaExponent,
 jbyteArray javaModulus)
{

  mpz_t basis;
  mpz_t exponent;
  mpz_t modulus;
  mpz_t result;

  jbyteArray javaResult;

  /* Translate jbyteArray-parameters to their corresponding GMP
     mpz_t-elements. */
  jbyteArray_to_mpz_t(env, &basis, javaBasis);
  jbyteArray_to_mpz_t(env, &exponent, javaExponent);
  jbyteArray_to_mpz_t(env, &modulus, javaModulus);

  /* Compute modular exponentiation. */
  mpz_init(result);

  mpz_powm(result, basis, exponent, modulus);

  /* Translate result back to jbyteArray (this also allocates the
     result array on the JVM heap). */
  mpz_t_to_jbyteArray(env, &javaResult, result);

  /* Deallocate resources. */
  mpz_clear(result);
  mpz_clear(modulus);
  mpz_clear(exponent);
  mpz_clear(basis);

  return javaResult;
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    spowm
 * Signature: ([[B[[B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_jgmpmee_GMPMEE_spowm
(JNIEnv *env, jclass clazz, jobjectArray javaBases, jobjectArray javaExponents,
 jbyteArray javaModulus)
{

  int i;
  mpz_t *bases;
  mpz_t *exponents;
  mpz_t modulus;
  mpz_t result;

  jbyteArray javaResult;

  /* Extract number of bases/exponents. */
  jsize numberOfBases = (*env)->GetArrayLength(env, javaBases);

  /* Convert exponents represented as array of byte[] to array of
     mpz_t. */
  bases = gmpmee_array_alloc(numberOfBases);
  for (i = 0; i < numberOfBases; i++)
    {
      jbyteArray javaBase =
	(jbyteArray)(*env)->GetObjectArrayElement(env, javaBases, i);
      jbyteArray_to_mpz_t(env, &(bases[i]), javaBase);
    }

  /* Convert exponents represented as array of byte[] to an array of
     mpz_t. */
  exponents = gmpmee_array_alloc(numberOfBases);
  for (i = 0; i < numberOfBases; i++)
    {
      jbyteArray javaExponent =
	(jbyteArray)(*env)->GetObjectArrayElement(env, javaExponents, i);
      jbyteArray_to_mpz_t(env, &(exponents[i]), javaExponent);
    }

  /* Convert modulus represented as a byte[] to a mpz_t. */
  jbyteArray_to_mpz_t(env, &modulus, javaModulus);

  /* Call GMP's exponentiated product function. */
  mpz_init(result);
  gmpmee_spowm(result, bases, exponents, numberOfBases, modulus);

  /* Convert result to a jbyteArray. */
  mpz_t_to_jbyteArray(env, &javaResult, result);

  /* Deallocate resources. */
  mpz_clear(result);
  mpz_clear(modulus);
  gmpmee_array_clear_dealloc(exponents, numberOfBases);
  gmpmee_array_clear_dealloc(bases, numberOfBases);

  return javaResult;
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    fpowm_precomp
 * Signature: ([B[BII)J
 */
JNIEXPORT jlong JNICALL Java_jgmpmee_GMPMEE_fpowm_1precomp
(JNIEnv *env, jclass clazz, jbyteArray javaBasis, jbyteArray javaModulus,
 jint javaBlockWidth, jint javaExponentBitlen)
{
  mpz_t basis;
  mpz_t modulus;
  gmpmee_fpowm_tab *tablePtr =
    (gmpmee_fpowm_tab *)malloc(sizeof(gmpmee_fpowm_tab));

  jbyteArray_to_mpz_t(env, &basis, javaBasis);
  jbyteArray_to_mpz_t(env, &modulus, javaModulus);

  gmpmee_fpowm_init_precomp(*tablePtr, basis, modulus,
			 (int)javaBlockWidth, (int)javaExponentBitlen);
  mpz_clear(modulus);
  mpz_clear(basis);

  return (jlong)(long)tablePtr;
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    fpowm
 * Signature: (J[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_jgmpmee_GMPMEE_fpowm
(JNIEnv *env, jclass clazz, jlong javaTablePtr, jbyteArray javaExponent)
{
  mpz_t exponent;
  mpz_t result;

  jbyteArray javaResult;

  jbyteArray_to_mpz_t(env, &exponent, javaExponent);
  mpz_init(result);

  gmpmee_fpowm(result, *(gmpmee_fpowm_tab *)(long)javaTablePtr, exponent);

  /* Translate result back to jbyteArray (this also allocates the
     result array on the JVM heap). */
  mpz_t_to_jbyteArray(env, &javaResult, result);

  mpz_clear(result);
  mpz_clear(exponent);

  return javaResult;
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    fpowm_clear
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jgmpmee_GMPMEE_fpowm_1clear
(JNIEnv *env, jclass clazz, jlong javaTablePtr)
{
  gmpmee_fpowm_clear(*(gmpmee_fpowm_tab *)(long)javaTablePtr);
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    legendre
 * Signature: ([B[B)I
 */
JNIEXPORT jint JNICALL Java_jgmpmee_GMPMEE_legendre
(JNIEnv *env, jclass clazz, jbyteArray javaOp, jbyteArray javaOddPrime)
{
  mpz_t op;
  mpz_t oddPrime;
  int symbol;

  jbyteArray_to_mpz_t(env, &op, javaOp);
  jbyteArray_to_mpz_t(env, &oddPrime, javaOddPrime);

  symbol = mpz_legendre(op, oddPrime);

  mpz_clear(op);
  mpz_clear(oddPrime);

  return (jint)symbol;
}

/*
 * Class:     jgmpmee_GMPMEE
 * Method:    millerrabin_init
 * Signature: ([BZ)J
 */
JNIEXPORT jlong JNICALL Java_jgmpmee_GMPMEE_millerrabin_1init
(JNIEnv *env, jclass clazz, jbyteArray javaN, jboolean search)
{
  mpz_t n;
  gmpmee_millerrabin_state *statePtr = (void*)0;
  int trial;

  jbyteArray_to_mpz_t(env, &n, javaN);

  trial = gmpmee_millerrabin_trial(n);

  if (trial || search) {
    statePtr =
      (gmpmee_millerrabin_state *)malloc(sizeof(gmpmee_millerrabin_state));
    gmpmee_millerrabin_init(*statePtr, n);
  }
  if (!trial && search) {
    gmpmee_millerrabin_next_cand(*statePtr);
  }

  mpz_clear(n);

  return (jlong)(long)statePtr;
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    millerrabin_next_cand
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jgmpmee_GMPMEE_millerrabin_1next_1cand
(JNIEnv *env, jclass clazz, jlong javaStatePtr)
{
  gmpmee_millerrabin_next_cand(*(gmpmee_millerrabin_state *)(long)javaStatePtr);
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    millerrabin_once
 * Signature: (J[B)I
 */
JNIEXPORT jint JNICALL Java_jgmpmee_GMPMEE_millerrabin_1once
(JNIEnv *env, jclass clazz, jlong javaStatePtr, jbyteArray javaBase)
{
  mpz_t base;
  int res;

  jbyteArray_to_mpz_t(env, &base, javaBase);
  res = gmpmee_millerrabin_once(*(gmpmee_millerrabin_state *)(long)
				javaStatePtr, base);

  mpz_clear(base);

  return res;
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    millerrabin_clear
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jgmpmee_GMPMEE_millerrabin_1clear
(JNIEnv *env, jclass clazz, jlong javaStatePtr)
{
  gmpmee_millerrabin_clear(*(gmpmee_millerrabin_state *)(long)javaStatePtr);
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    millerrabin_current
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_jgmpmee_GMPMEE_millerrabin_1current
(JNIEnv *env, jclass clazz, jlong javaStatePtr)
{
  jbyteArray javaResult;

  mpz_t_to_jbyteArray(env, &javaResult,
		      (*(gmpmee_millerrabin_state *)(long)
		       javaStatePtr)->n);
  return javaResult;
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    millerrabin_safe_init
 * Signature: ([BZ)J
 */
JNIEXPORT jlong JNICALL Java_jgmpmee_GMPMEE_millerrabin_1safe_1init
(JNIEnv *env, jclass clazz, jbyteArray javaN, jboolean search)
{
  mpz_t n;
  gmpmee_millerrabin_safe_state *statePtr = (void*)0;
  int trial;

  jbyteArray_to_mpz_t(env, &n, javaN);

  trial = gmpmee_millerrabin_safe_trial(n);

  if (trial || search) {
    statePtr = (gmpmee_millerrabin_safe_state *)
      malloc(sizeof(gmpmee_millerrabin_safe_state));
    gmpmee_millerrabin_safe_init(*statePtr, n);
  }
  if (!trial && search) {
    gmpmee_millerrabin_safe_next_cand(*statePtr);
  }

  mpz_clear(n);

  return (jlong)(long)statePtr;
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    millerrabin_safe_next_cand
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jgmpmee_GMPMEE_millerrabin_1safe_1next_1cand
(JNIEnv *env, jclass clazz, jlong javaStatePtr)
{
  gmpmee_millerrabin_safe_next_cand(*(gmpmee_millerrabin_safe_state *)(long)
				      javaStatePtr);
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    millerrabin_safe_once
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_jgmpmee_GMPMEE_millerrabin_1safe_1once
(JNIEnv *env, jclass clazz, jlong javaStatePtr, jbyteArray javaBase,
 jint javaIndex) {

  mpz_t base;
  int res;

  jbyteArray_to_mpz_t(env, &base, javaBase);

  if (((int)javaIndex) % 2 == 0)
    {
      res = gmpmee_millerrabin_once((*(gmpmee_millerrabin_safe_state *)(long)
				     javaStatePtr)->nstate,
				    base);
    }
  else
    {
      res = gmpmee_millerrabin_once((*(gmpmee_millerrabin_safe_state *)(long)
				     javaStatePtr)->mstate,
				    base);
    }

  mpz_clear(base);

  return res;
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    millerrabin_safe_clear
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jgmpmee_GMPMEE_millerrabin_1safe_1clear
(JNIEnv *env, jclass clazz, jlong javaStatePtr)
{
  gmpmee_millerrabin_safe_clear(*(gmpmee_millerrabin_safe_state *)(long)
				 javaStatePtr);
}


/*
 * Class:     jgmpmee_GMPMEE
 * Method:    millerrabin_current_safe
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_jgmpmee_GMPMEE_millerrabin_1current_1safe
(JNIEnv *env, jclass clazz, jlong javaStatePtr)
{
  jbyteArray javaResult;

  mpz_t_to_jbyteArray(env, &javaResult,
		      (*(gmpmee_millerrabin_safe_state *)(long)
		      javaStatePtr)->nstate->n);
  return javaResult;
}


#ifdef __cplusplus
}
#endif
