/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import javafx.scene.Node
import javafx.scene.input.MouseEvent

import de.thm.move.views.panes.SnapLike
import de.thm.move.views.shapes.MovableShape
import de.thm.move.models.CommonTypes._
import de.thm.move.util.JFxUtils._
import de.thm.move.util.PointUtils._
import de.thm.move.history.History
import de.thm.move.Global

/** Behaviour for moving selected ResizableShapes. */
trait SelectedMoveCtrl {
  this: SelectionCtrlLike =>

  def getSnapLike:SnapLike

  def getMoveHandler: (MouseEvent => Unit) = {
    var mouseP = (0.0,0.0)
    var startP = mouseP

    def moveElement(mv: MouseEvent): Unit =
      (mv.getEventType, mv.getSource) match {
        case (MouseEvent.MOUSE_PRESSED, shape: MovableShape) =>
          mouseP = (mv.getSceneX,mv.getSceneY)
          startP = mouseP //save start-point for undo
        case (MouseEvent.MOUSE_DRAGGED, node: Node with MovableShape) =>
          //translate from original to new position
          val delta = (mv.getSceneX - mouseP.x, mv.getSceneY - mouseP.y)
          //if clicked shape is in selection:
          // move all selected
          //else: move only clicked shape
          withParentMovableElement(node) { shape =>
            val allShapes =
              if(getSelectedShapes.contains(shape)) getSelectedShapes
              else List(shape)

            allShapes.foreach(_.move(delta))
            //don't forget to use the new mouse-point as starting-point
            mouseP = (mv.getSceneX,mv.getSceneY)
          }
        case (MouseEvent.MOUSE_RELEASED, node: Node with MovableShape) =>
          withParentMovableElement(node) { shape =>
            val movedShapes =
              if(getSelectedShapes.contains(shape)) getSelectedShapes
              else List(shape)

            SnapLike.applySnapToGrid(getSnapLike, node)

            //calculate delta (offset from original position) for un-/redo
            val deltaRedo = (mv.getSceneX - startP.x, mv.getSceneY - startP.y)
            val deltaUndo = deltaRedo.map(_*(-1))
            val cmd = History.
              newCommand(
                movedShapes.foreach(_.move(deltaRedo)),
                movedShapes.foreach(_.move(deltaUndo))
              )
            Global.history.save(cmd)
          }
        case _ => //unknown event
      }

    moveElement
  }

  def move(p:Point): Unit = getSelectedShapes.foreach(_.move(p))
}
