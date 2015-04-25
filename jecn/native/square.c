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
ecn_square(mpz_t t1, mpz_t t2, mpz_t s,
	   mpz_t rx, mpz_t ry,
	   mpz_t modulus, mpz_t a, mpz_t b,
	   mpz_t x, mpz_t y) {

  /* If this is the unit point or a point of order two, then return
     the unit point. */
  if (mpz_cmp_si(x, -1) == 0 || mpz_cmp_si(y, 0) == 0) {
    mpz_set_si(rx, -1);
    mpz_set_si(ry, -1);
    return;
  }

  /* s = (3x^2 + a) / 2y */
  mpz_mul(t1, x, x);
  mpz_mod(t1, t1, modulus);
  mpz_mul_ui(t1, t1, 3);
  mpz_add(t1, t1, a);
  mpz_mod(t1, t1, modulus);

  mpz_add(t2, y, y);
  mpz_invert(t2, t2, modulus);

  mpz_mul(s, t1, t2);
  mpz_mod(s, s, modulus);

  /* rx = s^2 - 2x */
  mpz_mul(t1, s, s);
  mpz_mul_ui(t2, x, 2);
  mpz_sub(t1, t1, t2);

  /* ry = s(x - rx) - y */
  mpz_sub(t2, x, t1);
  mpz_mul(t2, s, t2);
  mpz_sub(t2, t2, y);

  /* We assign the destination parameters in the end to allow them to
     be identical to the inputs. */
  mpz_mod(rx, t1, modulus);
  mpz_mod(ry, t2, modulus);

  return;
}
