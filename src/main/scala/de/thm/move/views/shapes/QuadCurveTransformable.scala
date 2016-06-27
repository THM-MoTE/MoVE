/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes

/** A non-curved/normal shape that can get transformed into a curved shape */
trait QuadCurveTransformable extends ResizableShape {
  /** Returns a version with bezier-curves of this shape */
  def toCurvedShape: AbstractQuadCurveShape
}
