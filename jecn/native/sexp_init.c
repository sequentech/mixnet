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
ecn_sexp_init(ecn_sexp_tab table,
	      mpz_t modulus, mpz_t a, mpz_t b,
	      size_t len, size_t block_width)
{
  int i, j;        /* Index parameters. */
  size_t tab_len;  /* Size of a subtable. */
  mpz_t *tx;       /* Temporary variable for subtable. */
  mpz_t *ty;       /* Temporary variable for subtable. */

  /* Initialize the curve parameters of the table. */
  mpz_init(table->modulus);
  mpz_set(table->modulus, modulus);

  mpz_init(table->a);
  mpz_set(table->a, a);

  mpz_init(table->b);
  mpz_set(table->b, b);

  /* Initialize length parameters. */
  table->len = len;
  table->block_width = block_width;
  if (len < block_width) {
    table->block_width = len;
  }
  table->tabs_len = (len + block_width - 1) / block_width;

  /* Allocate and initialize space for pointers to tables. */
  table->tabsx = (mpz_t **)malloc(table->tabs_len * sizeof(mpz_t *));
  table->tabsy = (mpz_t **)malloc(table->tabs_len * sizeof(mpz_t *));

  tab_len = 1 << block_width;
  for (i = 0; i < table->tabs_len; i++)
    {

      /* Last block may be more narrow than the other. */
      if (i == table->tabs_len - 1
	  && len - (table->tabs_len - 1) * block_width < block_width)
	{
	  block_width = len - (table->tabs_len - 1) * block_width;
	  tab_len = 1 << block_width;
	}

      /* Allocate and initialize a table. */
      table->tabsx[i] = (mpz_t *)malloc(tab_len * sizeof(mpz_t));
      tx = table->tabsx[i];
      table->tabsy[i] = (mpz_t *)malloc(tab_len * sizeof(mpz_t));
      ty = table->tabsy[i];

      /* Initialize mpz_t's. */
      for (j = 0; j < tab_len; j++)
	{
	  mpz_init(tx[j]);
	  mpz_init(ty[j]);
	}
    }
}
