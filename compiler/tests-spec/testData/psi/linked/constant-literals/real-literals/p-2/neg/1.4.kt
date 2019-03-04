/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-draft
 * PLACES: constant-literals, real-literals -> paragraph 2 -> sentence 1
 * NUMBER: 4
 * DESCRIPTION: Real literals with a not allowed digits and exponent mark followed by a float suffix.
 */

val value = 0.0f00e0

val value = 00.0f0e-0
val value = 000.00F9E9
val value = 0000.000F0e1

val value = 1.0F0e
val value = 22.00F9e+f
val value = 333.000f5ef
val value = 4444.0000F1EF2EF3EF4EF5EF6EF7EF8EF9
val value = 55555.0f7e+0F
val value = 666666.00F000000000000e-0000000000000
val value = 7777777.000f9E0000000000000F
val value = 88888888.0000f2e+0000000000000
val value = 999999999.0F4E+F

val value = 7.888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888f8e+0
val value = 0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f8e8
val value = 0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f0E0000001
val value = 0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f6E010
