/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.util

import de.thm.move.MoveSpec
import GeometryUtils._

class GeometryUtilsTest extends MoveSpec {
	"GeometryUtils.`closestMultiple`" should "return the closest multiple of A to B if it exists" in {
		closestMultiple(50.0, 120.0) shouldBe Some(100.0)
		closestMultiple(50.0, 172.0) shouldBe Some(150.0)
		closestMultiple(50.0, 500.0) shouldBe Some(500.0)
		closestMultiple(50.0, 547.0) shouldBe Some(550.0)
		closestMultiple(50.0, 150.0) shouldBe Some(150.0)
		closestMultiple(50.0, 540.0) shouldBe Some(550.0)
		closestMultiple(100, 555.0) shouldBe Some(600.0)
		closestMultiple(30, 90.0) shouldBe Some(90.0)
		closestMultiple(30, 80.0) shouldBe Some(90.0)
		closestMultiple(30, 70.0) shouldBe Some(60.0)
	}

	it should "not return the closest multiple if it doesn't exist" in {
		closestMultiple(100.0, 150.0) shouldBe None
		closestMultiple(7.0, 10.5) shouldBe None
		closestMultiple(500, 750) shouldBe None
		closestMultiple(20, 30) shouldBe None
	}
}
