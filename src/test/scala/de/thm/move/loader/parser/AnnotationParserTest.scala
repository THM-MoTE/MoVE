package de.thm.move.loader.parser

import de.thm.move.MoveSpec
import de.thm.move.loader.parser.ast._

import scala.util.parsing.input.NoPosition

class AnnotationParserTest extends MoveSpec {
 "A parser for annotations" should "parse documentation annotations" in {
   val model =
     """
       |model test
       |  annotation(Documentation,
       |  Icon (
       |        coordinateSystem(
       |          extent = {{0,0},{856,504}}
       |        ),
       |        graphics = {
       |          Bitmap(
       |            extent = {{466,493}, {817,205}},
       |            fileName = "modelica://test3/quokka.png"
       |          )
       |        })
       |     );
       |end test;
     """.stripMargin
   println( withParseSuccess(model) )


   val model2 =
     """
       |model test
       |  annotation(Documentation(<html>ölajsdf</html>),
       |  Icon (
       |        coordinateSystem(
       |          extent = {{0,0},{856,504}}
       |        ),
       |        graphics = {
       |          Bitmap(
       |            extent = {{466,493}, {817,205}},
       |            fileName = "modelica://test3/quokka.png"
       |          )
       |        })
       |     );
       |end test;
     """.stripMargin
   println( withParseSuccess(model2) )

   val model3 =
     """
       |model test
       |  annotation(Documentation(info="<html>
       |  <p>This Baroreceptor model originates from a paper of Warner from 1958.
       |  It includes the effects that baroreceptors only generate signals above a threshold blood pressure and that they are both sensitive to static blood pressure levels as well as to an increase or decrease in blood pressure.</p>
       |  <p>It was extended by Dr. Seidel in his thesis to include a saturation effect and a broadening function.</p>
       |</html>"),
       |  Icon (
       |        coordinateSystem(
       |          extent = {{0,0},{856,504}}
       |        ),
       |        graphics = {
       |          Bitmap(
       |            extent = {{466,493}, {817,205}},
       |            fileName = "modelica://test3/quokka.png"
       |          )
       |        })
       |     );
       |end test;
     """.stripMargin
   println( withParseSuccess(model3))

   val model4 =
     """
       |model test
       |  annotation(Documentation(info="<html>
       |  <p>This Baroreceptor model originates from a paper of Warner from 1958.
       |  It includes the effects that baroreceptors only generate signals above a threshold blood pressure and that they are both sensitive to static blood pressure levels as well as to an increase or decrease in blood pressure.</p>
       |  <p>It was extended by Dr. Seidel in his thesis to include a saturation effect and a broadening function.</p>
       |</html>"), Diagram(coordinateSystem(extent = {{-100, -100}, {100, 100}}, preserveAspectRatio = true, initialScale = 0.1, grid = {2, 2})),
       |  Icon (
       |        coordinateSystem(
       |          extent = {{0,0},{856,504}}
       |        ),
       |        graphics = {
       |          Bitmap(
       |            extent = {{466,493}, {817,205}},
       |            fileName = "modelica://test3/quokka.png"
       |          )
       |        })
       |     );
       |end test;
     """.stripMargin
   println(withParseSuccess(model4))
 }
}
