package de.thm.move.controllers

import java.net.URI
import java.nio.file.Paths
import javafx.scene.control.{ChoiceDialog, ButtonType}
import javafx.stage.Window
import javafx.scene.Node
import de.thm.move.loader.ShapeConverter
import de.thm.move.loader.parser.ModelicaParserLike
import de.thm.move.loader.parser.ast.Model
import de.thm.move.models.CommonTypes.Point
import de.thm.move.models.{SrcFile, ModelicaCodeGenerator}
import de.thm.move.views.shapes.ResizableShape
import implicits.FxHandlerImplicits._
import implicits.MonadImplicits._

import de.thm.move.models.ModelicaCodeGenerator.FormatSrc._
import de.thm.move.models.UserInputException
import de.thm.move.views.{Dialogs, SaveDialog}
import de.thm.move.Global._
import de.thm.move.util.PointUtils._

import scala.collection.JavaConverters._
import scala.util.{Try,Failure, Success}

class FileCtrl(owner: => Window) {

  private var usedFile: Option[SrcFile] = None

  private def showSrcCodeDialog():FormatSrc = {
    val dialog = new SaveDialog
    val selectOpt:Option[ButtonType] = dialog.showAndWait()
    selectOpt.map {
      case dialog.onelineBtn => Oneline
      case dialog.prettyBtn => Pretty
      case _ => Pretty
    } getOrElse(Pretty)
  }

  private def showScaleDialog(): Try[Int] = {
    val dialog = Dialogs.newScaleDialog()
    val scaleOp:Option[String] = dialog.showAndWait()
    scaleOp.map(_.toInt).
    filter(x => x>=minScaleFactor && x<=maxScaleFactor) match {
      case Some(x) => Success(x)
      case _ => Failure(UserInputException("Specify a valid scale-factor between 1 and 100!"))
    }
  }

  private def chooseModelDialog(xs:List[Model]): Model = {
    if(xs.size > 1) {
      val names = xs.map(_.name)
      val dialog:ChoiceDialog[String] = new ChoiceDialog(names.head, names.asJava)
      val opt:Option[String] = dialog.showAndWait()
      opt match {
        case Some(name) => xs.find(_.name == name).get
        case _ => chooseModelDialog(xs)
      }
    } else xs.head
  }

  def openFile:Try[(Point,List[ResizableShape])] = {
    val chooser = Dialogs.newModelicaFileChooser()
    chooser.setTitle("Open..")

    val fileTry = Option(chooser.showOpenDialog(owner)) match {
      case Some(x) => Success(x)
      case _ => Failure(UserInputException("Select a modelica file to open!"))
    }
    for {
      file <- fileTry
      path = Paths.get(file.toURI)
      scaleFactor <- showScaleDialog()
      parser = ModelicaParserLike()
      modelList <- parser.parse(path)
    } yield {
      val model = chooseModelDialog(modelList)

      usedFile = Some(SrcFile(path, model))

      val systemSize = ShapeConverter.gettCoordinateSystemSizes(model)
      val converter = new ShapeConverter(scaleFactor,
        systemSize,
        path)
      val shapes = converter.getShapes(model)
      val scaledSystem = systemSize.map(_*scaleFactor)
      (scaledSystem, shapes)
    }
  }

  def saveFile(shapes:List[Node], width:Double,height:Double): Try[Unit] = {
    val chooser = Dialogs.newModelicaFileChooser()
    chooser.setTitle("Save as..")
    val fileTry = Option(chooser.showSaveDialog(owner)) match {
      case Some(x) => Success(x)
      case _ => Failure(UserInputException("Select a file for saving!"))
    }
    for (
      file <- fileTry;
      uri = file.toURI;
      srcFormat = showSrcCodeDialog();
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
    }
  }

  def openImage: Option[URI] = {
    val chooser = Dialogs.newBitmapFileChooser()
    chooser.setTitle("Open bitmap")
    val fileOp = Option(chooser.showOpenDialog(owner))
    fileOp map { file =>
      file.toURI
    }
  }
}
