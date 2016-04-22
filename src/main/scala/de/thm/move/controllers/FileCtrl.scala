package de.thm.move.controllers

import java.net.URI
import java.nio.file.Paths
import javafx.scene.control.ButtonType
import javafx.stage.Window
import javafx.scene.Node
import de.thm.move.loader.ShapeConverter
import de.thm.move.loader.parser.ModelicaParserLike
import de.thm.move.loader.parser.ast.Model
import de.thm.move.models.CommonTypes.Point
import de.thm.move.models.ModelicaCodeGenerator
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

  def saveFile(shapes:List[Node], width:Double,height:Double): Unit = {
    val chooser = Dialogs.newModelicaFileChooser()
    chooser.setTitle("Save as..")
    val fileOp = Option(chooser.showSaveDialog(owner))
    (for (
      file <- fileOp;
      uri = file.toURI;
      srcFormat <- showSrcCodeDialog();
      pxPerMm <- showScaleDialog()
    ) yield {
        val generator = new ModelicaCodeGenerator(srcFormat, pxPerMm, width, height)

        usedFile match {
          case Some(src@SrcFile(oldpath, Model(modelname, _))) =>
            val lines = generator.generateExistingFile(modelname, uri, shapes)
            val before = src.getBeforeModel.getOrElse("")
            val after = src.getAfterModel.getOrElse("")
            generator.writeToFile(before,lines, after)(uri)
          case _ =>
            val filenamestr = Paths.get(uri).getFileName.toString
            val modelName = if(filenamestr.endsWith(".mo")) filenamestr.dropRight(3) else filenamestr
            val lines = generator.generate(modelName, uri, shapes)
            generator.writeToFile("",lines, "")(uri)
        }
      }) getOrElse {
      val dialog = Dialogs.newErrorDialog("Can't save to the given path or scale the icons." +
        "\nPlease try again with a valid path and scale factor!")
      dialog.showAndWait()
    }
  }

}
