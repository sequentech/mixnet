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

#include <gmp.h>
#include "ecn.h"

void
ecn_exp(mpz_t t1, mpz_t t2, mpz_t s,
	mpz_t rx, mpz_t ry,
	mpz_t modulus, mpz_t a, mpz_t b,
	mpz_t x, mpz_t y,
	mpz_t exponent) {

  int i;

  /* Initialize with the unit element. */
  mpz_set_si(rx, -1);
  mpz_set_si(ry, -1);

  /* Determine bit length. */
  int bitLength = (int)mpz_sizeinbase(exponent, 2);

  for (i = bitLength; i >= 0; i--) {

    /* Square. */
    ecn_square(t1, t2, s,
	       rx, ry,
	       modulus, a, b,
	       rx, ry);

    /* Multiply. */
    if (mpz_tstbit(exponent, i)) {

      ecn_mul(t1, t2, s,
	      rx, ry,
	      modulus, a, b,
	      rx, ry,
	      x, y);
    }
  }
  return;
}
