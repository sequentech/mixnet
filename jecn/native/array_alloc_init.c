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

#include <stdlib.h>
#include "gmp.h"

mpz_t *
ecn_array_alloc_init(size_t len) {
  int i;
  mpz_t * array = (mpz_t *)malloc(len * sizeof(mpz_t));

  for (i = 0; i < len; i++) {
    mpz_init(array[i]);
  }
  return array;
}
