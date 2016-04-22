package de.thm.move

import java.nio.file.Paths
import java.nio.file.Files
import de.thm.move.loader.parser.ModelicaParser

object LoadTest extends App {
	val parser = new ModelicaParser
	val pathes = List(
			Paths.get("/Users/nico/Downloads/simpleModel.mo"),
	Paths.get("/Users/nico/Downloads/test.mo"),
	Paths.get("/Users/nico/Downloads/test2.mo"),
	Paths.get("/Users/nico/Downloads/test4.mo"),
	Paths.get("/Users/nico/Downloads/test5.mo"),
	Paths.get("/Users/nico/Downloads/test6.mo")
		)
	pathes.foreach {x =>
		println(parser.parse(x).map { model =>
			"pos: "+model.icon.start +
			" start: " +model.icon.end
		})
	}
}
