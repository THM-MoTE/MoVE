/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.util

import org.junit.Assert._
import org.junit.Test
import GeometryUtils._

class GeometryUtilsTest {
	@Test
	def closestMultipleTest(): Unit = {
		assertEquals(100.0, closestMultiple(50.0, 120.0).get, 0.01)
		assertEquals(150.0, closestMultiple(50.0, 172.0).get, 0.01)
		assertEquals(500.0, closestMultiple(50.0, 500.0).get, 0.01)
		assertEquals(550.0, closestMultiple(50.0, 547.0).get, 0.01)
		assertEquals(150.0, closestMultiple(50.0, 150.0).get,0.01)
		assertEquals(550.0, closestMultiple(50.0, 540.0).get,0.01)
		assertEquals(600.0, closestMultiple(100, 555.0).get,0.01)
		assertEquals(90.0, closestMultiple(30, 90.0).get,0.01)
		assertEquals(90.0, closestMultiple(30, 80.0).get,0.01)
		assertEquals(60.0, closestMultiple(30, 70.0).get,0.01)

		assertEquals(None, closestMultiple(100.0, 150.0))
		assertEquals(None, closestMultiple(7.0, 10.5))
		assertEquals(None, closestMultiple(500, 750))
		assertEquals(None, closestMultiple(20, 30))
	}
}
