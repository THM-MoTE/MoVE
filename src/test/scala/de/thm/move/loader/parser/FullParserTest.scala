/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader.parser


import org.junit.Assert._
import org.junit.Test

import de.thm.move.MoveSpec
import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.loader.parser.ast._

class FullParserTest extends MoveSpec {

  "A full model" should "get parsed" in {
    val formatted =
    """
    model test3
      annotation(
      Icon (
        coordinateSystem(
          extent = {{0,0},{856,504}}
        ),
        graphics = {
          Bitmap(
            extent = {{466,493}, {817,205}},
            fileName = "modelica://test3/quokka.png"
          ),
          Line(
            points = {{566,80},{511,97},{596,135},{542,186},{431,175},{414,109},{422,70}},
            color = {230,77,77},
            pattern = LinePattern.DashDotDot,
            thickness = 4.0,
            smooth = Smooth.Bezier
          ),
          Ellipse(
            lineColor = {102,77,179},
            fillColor = {204,204,51},
            lineThickness = 4.0,
            pattern = LinePattern.DashDot,
            fillPattern = FillPattern.Solid,
            extent = {{134,477},{326,384}},
            endAngle = 360
          ),
          Rectangle(
            lineColor = {0,0,0},
            fillColor = {204,204,51},
            lineThickness = 4.0,
            pattern = LinePattern.Solid,
            fillPattern = FillPattern.Solid,
            extent = {{205,179}, {348,36}}
          ),
          Polygon(
            points = {{660,393},{547,335},{637,298},{578,242},{654,237}},
                     lineColor = {0,0,0},
            fillColor = {255,0,0},
            lineThickness = 4.0,
            pattern = LinePattern.Solid,
            fillPattern = FillPattern.Solid,
            smooth = Smooth.Bezier
          ),
          Polygon(
            points = {{434,235},{358,240},{417,296},{327,333},{440,391}},
                     lineColor = {0,0,0},
            fillColor = {255,0,0},
            lineThickness = 4.0,
            pattern = LinePattern.Solid,
            fillPattern = FillPattern.Solid,
            smooth = Smooth.Bezier
          ),
          Line(
            points = {{73,469},{143,215}},
            color = {0,0,0},
            pattern = LinePattern.Dash,
            thickness = 4.0
          )
        })
      );
    end test3;
    """

    withParseSuccess(formatted)

    val oneLine =
    """
    model test4
annotation( Icon (  coordinateSystem(  extent = {{0,0},{856,504}}  ), graphics = { Bitmap(  extent = {{466,493}, {817,205}},  fileName = "modelica://test4/quokka.png"  ), Line(  points = {{566,80},{511,97},{596,135},{542,186},{431,175},{414,109},{422,70}},  color = {230,77,77},  pattern = LinePattern.DashDotDot,  thickness = 4.0,  smooth = Smooth.Bezier  ), Ellipse(  lineColor = {102,77,179},  fillColor = {204,204,51},  lineThickness = 4.0,  pattern = LinePattern.DashDot,  fillPattern = FillPattern.Solid,  extent = {{134,477},{326,384}},  endAngle = 360  ), Rectangle(  lineColor = {0,0,0},  fillColor = {204,204,51},  lineThickness = 4.0,  pattern = LinePattern.Solid,  fillPattern = FillPattern.Solid,  extent = {{205,179}, {348,36}}  ), Polygon(  points = {{660,393},{547,335},{637,298},{578,242},{654,237}},           lineColor = {0,0,0},  fillColor = {255,0,0},  lineThickness = 4.0,  pattern = LinePattern.Solid,  fillPattern = FillPattern.Solid,  smooth = Smooth.Bezier  ), Polygon(  points = {{434,235},{358,240},{417,296},{327,333},{440,391}},           lineColor = {0,0,0},  fillColor = {255,0,0},  lineThickness = 4.0,  pattern = LinePattern.Solid,  fillPattern = FillPattern.Solid,  smooth = Smooth.Bezier  ), Line(  points = {{73,469},{143,215}},  color = {0,0,0},  pattern = LinePattern.Dash,  thickness = 4.0  )}) );
end test4;
    """

    withParseSuccess(oneLine)
  }
}
