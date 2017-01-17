/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.implicits

import java.util.Optional

import scala.language.implicitConversions

object MonadImplicits {
  implicit def asOption[T](opt : Optional[T]): Option[T] = {
    if(opt.isPresent) Some(opt.get)
    else None
  }
}
