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
#include <gmp.h>
#include "ecn.h"

void
ecn_sexp_precomp(ecn_sexp_tab table, mpz_t *basesx, mpz_t *basesy)
{
  int i, j;           /* Index variables. */
  size_t block_width; /* Width of current subtable. */
  size_t tab_len;     /* Size of current subtable. */
  mpz_t *tx;          /* Temporary variable for subtable. */
  mpz_t *ty;          /* Temporary variable for subtable. */

  mpz_t t1;           /* Temporary variable for curve operations. */
  mpz_t t2;           /* Temporary variable for curve operations. */
  mpz_t s;            /* Temporary variable for curve operations. */

  int mask;           /* Mask used for dynamic programming */
  int one_mask;       /* Mask containing a single non-zero bit. */

  mpz_init(t1);
  mpz_init(t2);
  mpz_init(s);

  block_width = table->block_width;
  tab_len = 1 << block_width;


  for (i = 0; i < table->tabs_len; i++)
    {
      /* Last block may have smaller width. */
      if (i == table->tabs_len - 1)
	{
	  block_width = table->len - (table->tabs_len - 1) * block_width;
	  tab_len = 1 << block_width;
	}

      /* Current subtable. */
      tx = table->tabsx[i];
      ty = table->tabsy[i];

      /* Initialize current subtable with all trivial products. */
      mpz_set_si(tx[0], -1);
      mpz_set_si(ty[0], -1);

      mask = 1;
      for (j = 0; j < block_width; j++)
	{
	  mpz_set(tx[mask], basesx[j]);
	  mpz_set(ty[mask], basesy[j]);
	  mask <<= 1;
	}

      /* Initialize current subtable with all non-trivial products. */
      for (mask = 1; mask < (1 << block_width); mask++)
	{
	  one_mask = mask & (-mask);

	  ecn_mul(t1, t2, s,
		  tx[mask], ty[mask],
		  table->modulus, table->a, table->b,
		  tx[mask ^ one_mask], ty[mask ^ one_mask],
		  tx[one_mask], ty[one_mask]);
	}

      basesx += block_width;
      basesy += block_width;
    }

  mpz_clear(s);
  mpz_clear(t2);
  mpz_clear(t1);
}
