package de.thm.move.controllers

import java.nio.file.Paths
import javafx.scene.control.ButtonType
import javafx.stage.Window

import de.thm.move.loader.ShapeConverter
import de.thm.move.loader.parser.ModelicaParserLike
import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.shapes.ResizableShape
import implicits.FxHandlerImplicits._
import implicits.MonadImplicits._

import de.thm.move.models.ModelicaCodeGenerator.FormatSrc._
import de.thm.move.views.{Dialogs, SaveDialog}
import de.thm.move.Global._
import de.thm.move.util.PointUtils._

import scala.util.{Failure, Success}

class FileCtrl(owner: => Window) {

  private var usedFile: Option[SrcFile] = None

  private def showSrcCodeDialog():Option[FormatSrc] = {
    val dialog = new SaveDialog
    val selectOpt:Option[ButtonType] = dialog.showAndWait()
    selectOpt.map {
      case dialog.onelineBtn => Oneline
      case dialog.prettyBtn => Pretty
    }
  }

  private def showScaleDialog(): Option[Int] = {
    val dialog = Dialogs.newScaleDialog()
    val scaleOp:Option[String] = dialog.showAndWait()
    scaleOp.map(_.toInt).filter(x => x>=minScaleFactor && x<=maxScaleFactor)
  }

  def openFile:Option[(Point,List[ResizableShape])] = {
    val chooser = Dialogs.newModelicaFileChooser()
    chooser.setTitle("Open..")

    val fileOp = Option(chooser.showOpenDialog(owner))
    (for {
      file <- fileOp
      path = Paths.get(file.toURI)
      scaleFactor <- showScaleDialog()
    } yield {
        val parser = ModelicaParserLike()
        parser.parse(path) match {
          case Success(modelList) =>
            val model = modelList.head //TODO ask user which model if list > 1

            usedFile = Some(SrcFile(path, model))

            val systemSize = ShapeConverter.gettCoordinateSystemSizes(model)
            val converter = new ShapeConverter(scaleFactor,
              systemSize,
              path)
            val shapes = converter.getShapes(model)
            val scaledSystem = systemSize.map(_*scaleFactor)
            Some(scaledSystem, shapes)
          case Failure(ex) =>
            val excDialog = Dialogs.newExceptionDialog(ex)
            excDialog.showAndWait()
            None
        }
      }) getOrElse {
      val dialog = Dialogs.newErrorDialog("Can't load the given file or scale the icons." +
        "\nPlease try again with a valid file and scale factor!")
      dialog.showAndWait()
      None
    }
  }

}
