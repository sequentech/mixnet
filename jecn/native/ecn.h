/*

Copyright 2011 Douglas Wikström

This file is part of a package for Vfork that provides native
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

#include <gmp.h>

typedef struct
{
  mpz_t modulus;          /**< Modulus used in computations. */
  mpz_t a;                /**< x-coefficient of curve. */
  mpz_t b;                /**< constant coefficient of curve. */
  size_t len;             /**< Total number of bases/exponents. */
  size_t block_width;     /**< Number of bases/exponents in each block. */
  size_t tabs_len;        /**< Number of blocks. */
  mpz_t **tabsx;          /**< Table of tables, one sub-table for each block. */
  mpz_t **tabsy;          /**< Table of tables, one sub-table for each block. */

} ecn_sexp_tab[1]; /* Magic references. */

typedef struct
{

  ecn_sexp_tab tab;       /**< Underlying simultaneous exponentiation table. */
  size_t slice_bit_len;   /**< Bit length of each slice. */

} ecn_fexp_tab[1]; /* Magic references. */


/**
 * Returns the square of the input point.
 *
 * @param t1 Temporary variable.
 * @param t2 Temporary variable.
 * @param s Temporary variable.
 * @param rx Destination of x-coordinate of result.
 * @param ry Destination of y-coordinate of result.
 * @param modulus Modulus of field.
 * @param a x-coefficient of curve.
 * @param b Constant coefficient of curve.
 * @param x x-coordinate of input point.
 * @param y y-coordinate of input point.
 */
void
ecn_square(mpz_t t1, mpz_t t2, mpz_t s,
	   mpz_t rx, mpz_t ry,
	   mpz_t modulus, mpz_t a, mpz_t b,
	   mpz_t x, mpz_t y);

/**
 * Returns the product of the input points.
 *
 * @param t1 Temporary variable.
 * @param t2 Temporary variable.
 * @param s Temporary variable.
 * @param rx Destination of x-coordinate of result.
 * @param ry Destination of y-coordinate of result.
 * @param modulus Modulus of field.
 * @param a x-coefficient of curve.
 * @param b Constant coefficient of curve.
 * @param x1 x-coordinate of first input point.
 * @param y1 y-coordinate of first input point.
 * @param x2 x-coordinate of second input point.
 * @param y2 y-coordinate of second input point.
 */
void
ecn_mul(mpz_t t1, mpz_t t2, mpz_t s,
	mpz_t rx, mpz_t ry,
	mpz_t modulus, mpz_t a, mpz_t b,
	mpz_t x1, mpz_t y1,
	mpz_t x2, mpz_t y2);

/**
 * Returns the power of a point to the given exponent. In contrast to
 * the squaring and multiplication functions, this function requires
 * the input and destination points to be distinct.
 *
 * @param t1 Temporary variable.
 * @param t2 Temporary variable.
 * @param s Temporary variable.
 * @param rx Destination of x-coordinate of result.
 * @param ry Destination of y-coordinate of result.
 * @param modulus Modulus of field.
 * @param a x-coefficient of curve.
 * @param b Constant coefficient of curve.
 * @param x x-coordinate of input point.
 * @param y y-coordinate of input point.
 * @param exponent Exponent.
 */
void
ecn_exp(mpz_t t1, mpz_t t2, mpz_t s,
	mpz_t rx, mpz_t ry,
	mpz_t modulus, mpz_t a, mpz_t b,
	mpz_t x, mpz_t y,
	mpz_t exponent);

/**
 * Initializes a table used for precomputation.
 *
 * @param table Table to be initialized.
 * @param modulus Modulus of field.
 * @param a x-coefficient of curve.
 * @param b Constant coefficient of curve.
 * @param len Total number of bases.
 * @param block_width Number of bases exponentiated simultaneously.
 */
void
ecn_sexp_init(ecn_sexp_tab table,
	      mpz_t modulus, mpz_t a, mpz_t b,
	      size_t len, size_t block_width);

/**
 * Releases the memory allocated by the pre-computed table.
 *
 * @param table Table to be initialized.
 */
void
ecn_sexp_clear(ecn_sexp_tab table);

/**
 * Performs pre-computation for the given table and bases.
 *
 * @param table Table to be initialized.
 * @param basesx x-coefficients of the bases.
 * @param basesy y-coefficients of the bases.
 */
void
ecn_sexp_precomp(ecn_sexp_tab table, mpz_t *basesx, mpz_t *basesy);

/**
 * Raises the bases of the pre-computed table simultaneously to the
 * given exponents.
 *
 * @param ropx Destination of x-coordinate of result.
 * @param ropy Destination of y-coordinate of result.
 * @param table Table to be initialized.
 * @param exponents Exponents.
 * @param max_exponent_bitlen Maximal number of bits in the exponents.
 */
void
ecn_sexp_table(mpz_t ropx, mpz_t ropy,
	       ecn_sexp_tab table,
	       mpz_t *exponents,
	       size_t max_exponent_bitlen);

void
ecn_sexp_block_batch(mpz_t ropx, mpz_t ropy,
		     mpz_t *basesx, mpz_t *basesy,
		     mpz_t *exponents,
		     size_t len,
		     mpz_t modulus, mpz_t a, mpz_t b,
		     size_t block_width, size_t batch_len,
		     size_t max_exponent_bitlen);

void
ecn_sexp(mpz_t ropx, mpz_t ropy,
	 mpz_t *basesx, mpz_t *basesy,
	 mpz_t *exponents,
	 size_t len,
	 mpz_t modulus, mpz_t a, mpz_t b);


mpz_t *
ecn_array_alloc(size_t len);

mpz_t *
ecn_array_alloc_init(size_t len);

void
ecn_array_clear_dealloc(mpz_t *a, size_t len);
