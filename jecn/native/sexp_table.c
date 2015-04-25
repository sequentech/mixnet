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
#include "gmp.h"
#include "ecn.h"

/*
 * Returns the index'th bit of each of the first block_width integers
 * in the array. The least significant bit in the output is the bit
 * extracted from the first integer in the input array.
 */
static int
getbits(mpz_t *op, int index, size_t block_width)
{
  int i;
  int bits = 0;

  for (i = block_width - 1; i >= 0; i--)
    {
      bits <<= 1;
      if (mpz_tstbit(op[i], index))
	{
	  bits |= 1;
	}
    }
  return bits;
}

void
ecn_sexp_table(mpz_t ropx, mpz_t ropy,
	       ecn_sexp_tab table,
	       mpz_t *exponents,
	       size_t max_exponent_bitlen)
{
  int i;
  int index;
  int mask;
  size_t bitlen;

  mpz_t t1;
  mpz_t t2;
  mpz_t s;
  mpz_t *exps;

  size_t len = table->len;
  size_t tabs_len = table->tabs_len;
  size_t block_width = table->block_width;
  size_t last_block_width = len - (tabs_len - 1) * block_width;
  mpz_t **tabsx = table->tabsx;
  mpz_t **tabsy = table->tabsy;

  mpz_init(t1);
  mpz_init(t2);
  mpz_init(s);

  /* Initialize result variable. */
  mpz_set_si(ropx, -1);
  mpz_set_si(ropy, -1);

  /* Execute simultaneous square-and-multiply. */
  for (index = max_exponent_bitlen - 1; index >= 0; index--)
    {

      /* Square ... */
      ecn_square(t1, t2, s,
		 ropx, ropy,
		 table->modulus, table->a, table->b,
		 ropx, ropy);

      /* ... and multiply. */
      i = 0;
      exps = exponents;
      while (i < tabs_len)
	{
	  if (i == tabs_len - 1)
	    {
	      mask = getbits(exps, index, last_block_width);
	    }
	  else
	    {
	      mask = getbits(exps, index, block_width);
	    }

	  ecn_mul(t1, t2, s,
	  	  ropx, ropy,
	  	  table->modulus, table->a, table->b,
	  	  ropx, ropy,
	  	  tabsx[i][mask], tabsy[i][mask]);
	  i++;
	  exps += block_width;
	}
    }

  mpz_clear(s);
  mpz_clear(t2);
  mpz_clear(t1);

}
