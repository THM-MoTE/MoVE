package de.thm.move

import java.nio.file.Paths
import java.nio.file.Files
import de.thm.move.loader.parser.ModelicaParser

object LoadTest extends App {

	def findIcon(input:Array[Char], idx:Int = 0, consumed:List[Char] = List[Char]()): Int =
		if(idx < input.size) {
			input(idx) match {
				case 'I' =>
					val sub = input.slice(idx, idx+4)
					val str = new String(sub)
					println(s"str $str")
					if(str == "Icon") idx
					else findIcon(input, idx+1, input(idx) :: consumed)
				case _ => findIcon(input, idx+1, input(idx) :: consumed)
			}
		} else -1

	def findModelBlock(input:Array[Char], idx:Int = 0,consumed:List[Char] = List[Char]()): Int =
		if(idx < input.size) {
			input(idx) match {
				case 'e' =>
					val str = new String(input.slice(idx,idx+3))
					if(str == "end") {
						idx+4 + input.drop(idx+4).takeWhile(_ != '\n').size
					} else findModelBlock(input, idx+1, input(idx) :: consumed)
				case _ => findModelBlock(input, idx+1, input(idx) :: consumed)
			}
		} else -1

	def findModels(input:Array[Char], idx:Int = 0,consumed:List[Char] = List[Char]()): Boolean =
		if(idx < input.size) {
			input(idx) match {
				case 'm' =>
					val str = new String(input.slice(idx, idx+5))
					if(str == "model") {
						val modelname = input.drop(idx+6).takeWhile(_ != '\n')
						val end = findModelBlock(input, idx+6, consumed)
						val modelB = new String(input.slice(idx, end))
						println(modelB)
						findModels(input, idx+6, consumed)
					} else findModels(input, idx+1, consumed)
				case _ => findModels(input, idx+1, consumed)
			}
		} else false

	val parser = new ModelicaParser
	val path = Paths.get("/home/nico/Downloads/simple.mo")
	val str = new String(Files.readAllBytes(path), "UTF-8")
	val chars = str.toArray
	println(findModels(chars))
}
