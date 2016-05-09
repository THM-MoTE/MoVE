/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import javafx.scene.control.{ButtonType, ChoiceDialog}
import javafx.stage.Window
import javafx.scene.Node

import de.thm.move.loader.ShapeConverter
import de.thm.move.loader.parser.ModelicaParserLike
import de.thm.move.loader.parser.ast.Model
import de.thm.move.models.CommonTypes.Point
import de.thm.move.models.{ModelicaCodeGenerator, SrcFile, SvgCodeGenerator, UserInputException}
import de.thm.move.views.shapes.ResizableShape
import implicits.FxHandlerImplicits._
import implicits.MonadImplicits._
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc._
import de.thm.move.views.dialogs.SrcFormatDialog
import de.thm.move.Global._
import de.thm.move.util.PointUtils._
import de.thm.move.views.dialogs.{Dialogs, SrcFormatDialog}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class FileCtrl(owner: => Window) {

  case class SaveInfos(targetUri:URI, pxPerMm:Int, srcFormat:FormatSrc)

  private var usedFile: Option[SrcFile] = None
  private var saveInfos: Option[SaveInfos] = None

  private def showSrcCodeDialog():FormatSrc = {
    val dialog = new SrcFormatDialog
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

  private def save(existingFile:Option[SrcFile], targetUri:URI, srcFormat:FormatSrc,
    pxPerMm:Int, shapes:List[Node], width:Double,height:Double): Unit = {
    val generator = new ModelicaCodeGenerator(srcFormat, pxPerMm, width, height)
    existingFile match {
      case Some(src@SrcFile(oldpath, Model(modelname, _))) =>
        val lines = generator.generateExistingFile(modelname, targetUri, shapes)
        val before = src.getBeforeModel
        val after = src.getAfterModel
        generator.writeToFile(before,lines, after)(targetUri)
      case _ =>
        val filenamestr = Paths.get(targetUri).getFileName.toString
        val modelName = if(filenamestr.endsWith(".mo")) filenamestr.dropRight(3) else filenamestr
        val lines = generator.generate(modelName, targetUri, shapes)
        generator.writeToFile("",lines, "")(targetUri)
    }
  }

  def saveFile(shapes:List[Node], width:Double,height:Double): Try[Path] = {
    saveInfos match {
      case Some(SaveInfos(target,px,format)) =>
        save(usedFile, target, format, px, shapes,width,height)
        Success(Paths.get(target))
      case _ => saveNewFile(shapes, width, height)
    }
  }

  def saveNewFile(shapes:List[Node], width:Double,height:Double): Try[Path] = {
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
      save(usedFile, uri, srcFormat, pxPerMm, shapes, width, height)
      saveInfos = Some(SaveInfos(uri,pxPerMm, srcFormat))
      Paths.get(uri)
    }
  }

  def exportAsSvg(shapes:List[Node], width:Double,height:Double): Try[Unit] = {
    val chooser = Dialogs.newSvgFileChooser()
    chooser.setTitle("Export as svg..")
    val fileTry = Option(chooser.showSaveDialog(owner)) match {
      case Some(x) => Success(x)
      case _ => Failure(UserInputException("Select a file for export!"))
    }
    for{
      file <- fileTry
      path = Paths.get(file.toURI)
      } yield {
      val generator = new SvgCodeGenerator
      val str = generator.generatePrettyPrinted(shapes, width, height)
      generator.writeToFile(str)(path)
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
