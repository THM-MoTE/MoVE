package de.thm.move.views.shapes

import javafx.scene.input.MouseEvent

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.Global
import de.thm.move.history.History
import de.thm.move.models.CommonTypes.Point
import de.thm.move.util.PointUtils._
import de.thm.move.util.JFxUtils._
import de.thm.move.views.anchors.Anchor

/** An element that is represented by a path.
  *
  * This trait adds moving/resizing the shape for free.
  * Due to a initializing problem please be careful and make sure you overwrite
  * getAnchors:List[Anchors] as follows:
  * {{{
  *   overwrite val getAnchors: List[Anchor] = genAnchors
  * }}}
  * genAnchors is already implemented, all you have to do is add the line above to your '''concrete''' class.
  * This trait can't overwrite getAnchors, this would result in a NullPointerException or in a emptylist because
  * edgeCount isn't proper initialized when getAnchors will be initialized!
 */
trait PathLike {
  self: ResizableShape =>
  val edgeCount:Int

  protected def genAnchors = List.tabulate(edgeCount) { idx =>
    val (x,y) = getEdgePoint(idx)
    val anchor = new Anchor(x,y)

    var startP = (0.0,0.0)
    var mouseP = startP
    anchor.setOnMousePressed(withConsumedEvent { me:MouseEvent =>
      startP = (me.getSceneX,me.getSceneY)
      mouseP = startP
    })
    anchor.setOnMouseDragged(withConsumedEvent { mv: MouseEvent =>
      val delta = (mv.getSceneX - mouseP.x, mv.getSceneY - mouseP.y)
      resizeWithAnchor(idx, delta)
      mouseP = (mv.getSceneX, mv.getSceneY)
    })
    anchor.setOnMouseReleased(withConsumedEvent { mv:MouseEvent =>
      //calculate delta (offset from original position) for un-/redo
      val deltaRedo = (mv.getSceneX - startP.x, mv.getSceneY - startP.y)
      val deltaUndo = deltaRedo.map(_*(-1))

      val cmd = History.
        newCommand(
          resizeWithAnchor(idx, deltaRedo),
          resizeWithAnchor(idx, deltaUndo)
        )
      Global.history.save(cmd)
    })
    anchor
  }

  rotateProperty().addListener { (_:Number, newV:Number) =>
    indexWithAnchors.foreach { case (idx, anchor) =>
      val (x,y) = localToParentPoint(getEdgePoint(idx))
      anchor.setCenterX(x)
      anchor.setCenterY(y)
    }
  }

  private lazy val indexes:List[Int] = (0 until edgeCount).toList
  private lazy val indexWithAnchors = indexes.zip(getAnchors)

  private def moveAnchor(anchor:Anchor, delta:Point): Unit = {
    anchor.setCenterX(anchor.getCenterX + delta.x)
    anchor.setCenterY(anchor.getCenterY + delta.y)
  }

  private def resizeWithAnchor(idx:Int,delta:Point) = {
    val anchor = getAnchors(idx)
    resize(idx,delta)
    moveAnchor(anchor,delta)
  }

  def getEdgePoint(idx:Int): Point
  def resize(idx:Int, delta:Point): Unit
  override def move(delta:Point):Unit = indexWithAnchors.foreach {
    case (idx, anchor) =>
      resize(idx, delta)
      moveAnchor(anchor,delta)
  }
}
