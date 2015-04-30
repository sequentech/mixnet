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
ecn_mul(mpz_t t1, mpz_t t2, mpz_t s,
	mpz_t rx, mpz_t ry,
	mpz_t modulus, mpz_t a, mpz_t b,
	mpz_t x1, mpz_t y1,
	mpz_t x2, mpz_t y2) {

  /* If the first point is the unit element, then we copy the other
     point. */
  if (mpz_cmp_si(x1, -1) == 0) {
    mpz_set(rx, x2);
    mpz_set(ry, y2);
    return;
  }

  /* If the second point is the unit element, then we copy the other
     point. */
  if (mpz_cmp_si(x2, -1) == 0) {
    mpz_set(rx, x1);
    mpz_set(ry, y1);
    return;
  }

  /* If the second point is inverse of the first point, then we return
     the unit point. */
  mpz_add(t1, y1, y2);
  if (mpz_cmp(x1, x2) == 0 && mpz_cmp(t1, modulus) == 0) {
    mpz_set_si(rx, -1);
    mpz_set_si(ry, -1);
    return;
  }

  /* If the first and second points are identical, then we square it. */
  if (mpz_cmp(x1, x2) == 0 && mpz_cmp(y1, y2) == 0) {
    ecn_square(t1, t2, s, rx, ry, modulus, a, b, x1, x2);
    return;
  }

  /* s = (y1 - y2) / (x1 - x2) */
  mpz_sub(t1, y1, y2);
  mpz_sub(t2, x1, x2);
  mpz_invert(t2, t2, modulus);
  mpz_mul(s, t1, t2);

  /* rx = s^2 - (x1 + x2) */
  mpz_mul(t1, s, s);
  mpz_sub(t1, t1, x1);
  mpz_sub(t1, t1, x2);

  /* ry = s(x1 - rx) - y1 */
  mpz_sub(t2, x1, t1);
  mpz_mul(t2, s, t2);
  mpz_sub(t2, t2, y1);

  /* We assign the destination parameters in the end to allow them to
     be identical to the inputs. */
  mpz_mod(rx, t1, modulus);
  mpz_mod(ry, t2, modulus);

  return;
}
