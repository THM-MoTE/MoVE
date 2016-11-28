/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Node
import javafx.scene.control.{ButtonType, ChoiceDialog}
import javafx.stage.Window
import javax.imageio.ImageIO

import de.thm.move.Global._
import de.thm.move.implicits.MonadImplicits._
import de.thm.move.loader.ShapeConverter
import de.thm.move.loader.parser.ModelicaParserLike
import de.thm.move.loader.parser.ast.Model

import de.thm.move.models.ModelicaCodeGenerator.FormatSrc._
import de.thm.move.models.{ModelicaCodeGenerator, SrcFile, SvgCodeGenerator, UserInputException}
import de.thm.move.types._
import de.thm.move.util.converters.Marshaller._
import de.thm.move.views.dialogs.{Dialogs, ExternalChangesDialog, SrcFormatDialog}
import de.thm.move.views.shapes.ResizableShape

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/** Controller for interaction with files.
  * This controller asks the user to select a appropriate file and uses the selected file for it's
  * functions.
  */
class FileCtrl(owner: Window) {
  case class FormatInfos(pxPerMm:Int, srcFormat:Option[FormatSrc])

  /** Informations about the current open file */
  private var openedFile: Option[SrcFile] = None
  /** Sourcecode format specified by the user - either Oneline or Pretty */
  private var formatInfos: Option[FormatInfos] = None

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
    val scaleOp:Option[List[Int]] = dialog.showAndWait()
    scaleOp.map(_.head).
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


  private def parseFile(path:Path): Try[SrcFile] = {
    val parser = ModelicaParserLike()
    for {
      modelList <- parser.parse(path)
    } yield {
      val model = chooseModelDialog(modelList)
      SrcFile(path, model)
    }
  }

  private def parseFileExc(path:Path): SrcFile =
    parseFile(path).get

  /** Let the user choose a modelica file; parses this file and returns the
    * path to the file, coordinate-system bounds & the shapes of the modelica model.
    */
  def openFile:Try[(Path, Point,List[ResizableShape])] = {
    val chooser = Dialogs.newModelicaFileChooser()
    chooser.setTitle("Open..")

    val fileTry = Option(chooser.showOpenDialog(owner)) match {
      case Some(x) => Success(x)
      case _ => Failure(UserInputException("Select a modelica file to open!"))
    }
    for {
      file <- fileTry
      path = Paths.get(file.toURI)
      (point, shapes) <- openFile(path)
    } yield (path, point, shapes)
  }

  def openFile(path:Path):Try[(Point,List[ResizableShape])] = {
    val existsTry =
      if (Files.exists(path)) Success(path)
      else Failure(UserInputException(s"$path doesn't exist!"))
    for {
      _ <- existsTry
      srcFile <- parseFile(path)
      scaleFactor <- showScaleDialog()
    } yield {
      val model = srcFile.model
      val systemSize = ShapeConverter.gettCoordinateSystemSizes(model)
      val converter = new ShapeConverter(scaleFactor,
        systemSize,
        path)
      val shapesWithWarnings = converter.getShapes(model)
      val shapes = shapesWithWarnings.map(_._1)
      val warnings = shapesWithWarnings.flatMap(_._2)
      val scaledSystem = systemSize.map(_*scaleFactor)
      if(warnings.nonEmpty) {
        Dialogs.newListDialog(warnings,
          "Some properties can't get used.\nThey will be overridden when saving the file!").
          showAndWait()
      }
      openedFile = Some(srcFile)
      formatInfos = Some(FormatInfos(scaleFactor, None))
      (scaledSystem, shapes)
    }
  }

  /** Warns the user that the given SrcFile got changed from another program and let the
    *  user decide if he wants to reparse the file or cancel the operation.
    *  - If the user wants to reparse the file, the file is reparsed and the returned SrcFile
    *    is the reparsed filecontent. (openedFile variable wasn't changed)
    *  - If the user chooses cancel None is returned
    */
  private def warnExternalChanges(src:SrcFile): Option[SrcFile] = {
    val dialog = new ExternalChangesDialog(src.file.toString)
    val selectedOption:Option[ButtonType] = dialog.showAndWait()
    selectedOption.
      filter { _ == dialog.overwriteAnnotationsBtn }.
      map { _ =>
        parseFile(src.file)
      } flatMap {
        case Success(src) =>
          Some(src)
        case Failure(ex) =>
          Dialogs.newExceptionDialog(ex, "Error while reparsing file").showAndWait()
          None
      }
  }

  /** Check for external file changes and react on it before calling `f`. */
  private def awareExternalChanges[A](srcOpt:Option[SrcFile])(f: Option[SrcFile] => Try[A]): Try[A]= {
    srcOpt match {
      case Some(src) if src.noExternalChanges => f(srcOpt) //no changes; just call f
      case Some(src) =>
        //external changes; ask the user for his decision and
        //call `f` if he likes to save the file.
        //If not fail with a UserInputException
        warnExternalChanges(src).
          map(x => f(Some(x))).
          getOrElse(Failure(new UserInputException("Didn't save the file")))
      case None => f(srcOpt) //no opened file; just call f
    }
  }

  /** Saves the icon represented by the shapes and their width, height to an existing file.
    * If there is no existing file the user get asked to save a new file.
    */
  def saveFile(shapes:List[Node], width:Double,height:Double): Try[Path] = {
    awareExternalChanges(openedFile) { newFile =>
      val codeGen = generateCodeAndWriteToFile(shapes, width, height) _
      (newFile, formatInfos) match {
        case (Some(src@SrcFile(filepath, modelAst)), Some(FormatInfos(pxPerMm, Some(format)))) => //file was opened & saved before
          codeGen(Left(src), pxPerMm, format)
          val newSrc = parseFileExc(filepath) //reparse for getting new positional values
          openedFile = Some(newSrc) //update timestamp
          Success(filepath)
        case (Some(src@SrcFile(filepath, modelAst)), Some(FormatInfos(pxPerMm, None))) => //file was opened but not saved before; we need a formating
          val format = showSrcCodeDialog()
          codeGen(Left(src), pxPerMm, format)
          val newSrc = parseFileExc(filepath) //reparse for getting new positional values
          openedFile = Some(newSrc) //update timestamp
          formatInfos = Some(FormatInfos(pxPerMm, Some(format))) //update info
          Success(filepath)
        case (None, None) => //never saved this file; we need all informations
          saveAsFile(shapes, width, height)
        case _ =>
          println(s"Developer WARNING: saveFile() both None: $openedFile $formatInfos")
          Failure(new IllegalStateException("Internal state crashed! Reopen file and try again."))
      }
    }
  }

  def saveAsFile(shapes:List[Node], width:Double,height:Double): Try[Path] = {
    val codeGen = generateCodeAndWriteToFile(shapes, width, height) _
    val chooser = Dialogs.newModelicaFileChooser()
    chooser.setTitle("Save as..")
    val fileTry = Option(chooser.showSaveDialog(owner)) match {
      case Some(x) => Success(x)
      case _ => Failure(UserInputException("Select a file for saving!"))
    }
    for {
      file <- fileTry
      pxPerMm <- showScaleDialog()
      filepath = Paths.get(file.toURI)
      format = showSrcCodeDialog()
    } yield {
      codeGen(Right(filepath), pxPerMm, format)
      openedFile = Some(parseFileExc(filepath)) //update timestamp; we've written the file -> there can't be an error
      formatInfos = Some(FormatInfos(pxPerMm, Some(format))) //update info
      filepath
    }
  }

  private def generateCodeAndWriteToFile(shapes:List[Node],
                                         width:Double,
                                         height:Double)
                                        (srcEither:Either[SrcFile, Path],
                                         pxPerMm:Int,
                                         format:FormatSrc): Unit = {
    val generator = new ModelicaCodeGenerator(format, pxPerMm, width, height)
    srcEither match {
      case Left(src) =>
        val targetUri = src.file.toUri
        val lines = generator.generateExistingFile(src.model.name, targetUri, shapes)
        val before = src.getBeforeModel
        val after = src.getAfterModel
        generator.writeToFile(before,lines, after)(targetUri)
      case Right(filepath) =>
        val targetUri = filepath.toUri
        val filenamestr = Paths.get(targetUri).getFileName.toString
        val modelName = if(filenamestr.endsWith(".mo")) filenamestr.dropRight(3) else filenamestr
        val lines = generator.generate(modelName, targetUri, shapes)
        generator.writeToFile("",lines, "")(targetUri)
    }
  }

  /** Exports the given Icon represented by
    * the given shapes and width,height into an user-selected svg-file
    */
  def exportAsSvg(shapes:List[Node], width:Double,height:Double): Try[Unit] = {
    val chooser = Dialogs.newSvgFileChooser()
    chooser.setTitle(fontBundle.getString("export.svg"))
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

  /** Exports the given Icon represented by
    * the given shapes and width,height into an user-selected png-file
    */
  def exportAsBitmap(root:Node): Try[Unit] = {
   val chooser = Dialogs.newPngFileChooser()
    chooser.setTitle(fontBundle.getString("export.jpg"))
    val fileTry = Option(chooser.showSaveDialog(owner)) match {
      case Some(x) => Success(x)
      case _ => Failure(UserInputException("Select a file for export!"))
    }
    for {
      file <- fileTry
      image = root.snapshot(null, null)
      filename = file.getName
      suffix = filename.substring(filename.lastIndexOf(".")+1)
      _ <- Try (ImageIO.write(SwingFXUtils.fromFXImage(image, null), suffix, file))
    } yield ()
  }

  /** Lets the user pick an image and returns the URI of the selected file */
  def openImage: Option[URI] = {
    val chooser = Dialogs.newBitmapFileChooser()
    chooser.setTitle(fontBundle.getString("open.image"))
    val fileOp = Option(chooser.showOpenDialog(owner))
    fileOp map { file =>
      file.toURI
    }
  }
}
