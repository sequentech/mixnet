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
#include <stdlib.h>
#include <gmp.h>
#include "ecn.h"

void
ecn_sexp_clear(ecn_sexp_tab table)
{
  int i, j;                                /* Index variables. */
  size_t tabs_len = table->tabs_len;       /* Number of sub tables. */
  size_t block_width = table->block_width; /* Width of each sub table. */
  size_t tab_len = 1 << block_width;       /* Size of each sub table. */
  mpz_t *tx;                               /* Temporary table variable. */
  mpz_t *ty;                               /* Temporary table variable. */


  for (i = 0; i < tabs_len; i++)
    {

      /* Last block may have smaller width. */
      if (i == tabs_len - 1)
	{
	  block_width = table->len - (tabs_len - 1) * block_width;
	  tab_len = 1 << block_width;
	}

      /* Deallocate all integers in table. */
      tx = table->tabsx[i];
      ty = table->tabsy[i];
      for (j = 0; j < tab_len; j++)
	{
	  mpz_clear(tx[j]);
	  mpz_clear(ty[j]);
	}

      /* Deallocate table. */
      free(tx);
      free(ty);
    }

  /* Deallocate table of tables. */
  free(table->tabsx);
  free(table->tabsy);

  /* Clear curve parameters. */
  mpz_clear(table->modulus);
  mpz_clear(table->a);
  mpz_clear(table->b);
}
