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

#include "gmp.h"
#include "gmpmee.h"

/*
 * Computes a "theoretical" optimal block width for a given exponent
 * length. This is typically not optimal in practice, so we only use
 * this for values outside the table below.
 */
static int
theoretical_block_width(int exponents_bitlen, int batch_len) {

  // This computes the theoretical optimum.
  int width = 1;
  double cost = 1.5 * exponents_bitlen;
  double square;
  double multiply;
  double old_cost;
  int width_exp;

  do {

    old_cost = cost;

    width++;
    width_exp = 1 << width;

    /* Amortized cost for the square and multiply. */
    square = ((double)exponents_bitlen) / batch_len;

    multiply = ((double)(width_exp + (1 - 1.0 / width_exp) * exponents_bitlen))
               / width;
    cost = square + multiply;

  } while (cost < old_cost);

  width--;

  if (width <= 0) {
    width = 1;
  }

  return width;
}


void
ecn_sexp(mpz_t ropx, mpz_t ropy,
	 mpz_t *basesx, mpz_t *basesy,
	 mpz_t *exponents,
	 size_t len,
	 mpz_t modulus, mpz_t a, mpz_t b)
{
  int i;
  size_t bitlen;
  size_t max_exponent_bitlen;
  size_t batch_len = 100;      /* This is somewhat arbitrary, but it
				 makes the amortized cost for squaring
				 very small in comparison to the cost
				 for multiplications. */
  size_t block_width;

  /* Compute the maximal bit length among the exponents. */
  max_exponent_bitlen = 0;
  for (i = 0; i < len; i++)
    {
      bitlen = mpz_sizeinbase(exponents[i], 2);
      if (bitlen > max_exponent_bitlen)
	{
	  max_exponent_bitlen = bitlen;
	}
    }

  /* Determine a good block width. */
  block_width = theoretical_block_width(max_exponent_bitlen, batch_len);

  ecn_sexp_block_batch(ropx, ropy,
  		       basesx, basesy,
  		       exponents,
  		       len,
  		       modulus, a, b,
  		       block_width, batch_len,
  		       max_exponent_bitlen);
}
