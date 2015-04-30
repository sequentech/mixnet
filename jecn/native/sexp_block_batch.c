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

#include <stdio.h>
#include <gmp.h>
#include "ecn.h"

void
ecn_sexp_block_batch(mpz_t ropx, mpz_t ropy,
		     mpz_t *basesx, mpz_t *basesy,
		     mpz_t *exponents,
		     size_t len,
		     mpz_t modulus, mpz_t a, mpz_t b,
		     size_t block_width, size_t batch_len,
		     size_t max_exponent_bitlen)
{
  int i;
  ecn_sexp_tab table;
  mpz_t tmpx;
  mpz_t tmpy;

  mpz_t t1;
  mpz_t t2;
  mpz_t s;

  mpz_init(tmpx);
  mpz_init(tmpy);

  mpz_init(t1);
  mpz_init(t2);
  mpz_init(s);

  if (len < batch_len) {
    batch_len = len;
  }

  ecn_sexp_init(table, modulus, a, b, batch_len, block_width);


  /* Initialize result to unit element. */
  mpz_set_si(ropx, -1);
  mpz_set_si(ropy, -1);

  for (i = 0; i < len; i += batch_len)
    {

      /* Last batch may be slightly shorter. */
      if (len - i < batch_len)
  	{
  	  batch_len = len - i;

  	  ecn_sexp_clear(table);
  	  ecn_sexp_init(table, modulus, a, b, batch_len, block_width);
  	}

      /* Perform computation for batch */
      ecn_sexp_precomp(table, basesx, basesy);

      /* Compute batch. */
      ecn_sexp_table(tmpx, tmpy, table, exponents, max_exponent_bitlen);

      /* Multiply with result so far. */
      ecn_mul(t1, t2, s,
  	      ropx, ropy,
  	      modulus, a, b,
  	      ropx, ropy,
  	      tmpx, tmpy);

      /* Move on to next batch. */
      basesx += batch_len;
      basesy += batch_len;
      exponents += batch_len;
    }

  mpz_clear(s);
  mpz_clear(t2);
  mpz_clear(t1);

  mpz_clear(tmpy);
  mpz_clear(tmpx);

  ecn_sexp_clear(table);
}
