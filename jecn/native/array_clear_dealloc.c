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

#include <stdlib.h>
#include "gmp.h"

void
ecn_array_clear_dealloc(mpz_t *a, size_t len) {
  int i;

  for (i = 0; i < len; i++)
    {
      mpz_clear(a[i]);
    }
  free(a);
}
