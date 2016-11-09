import sbt._
import java.io.FileNotFoundException
import java.io.File

object Resources {

	/** Try's to find a java home directory by either using the
		* environment variable `JAVA_HOME` or the system property `java.home`.
		*/
	def getJavaHome: File = {
		val javaHome =
	    Option(System.getenv("JAVA_HOME")).map(_+"/jre").
	      orElse(Option(System.getProperty("java.home")))
		javaHome match {
			case Some(str) =>
				file(str)
			case None =>
				throw new FileNotFoundException("$JAVA_HOME is undefined as well as the system property `java.home`." +
																			"Setup a environment variable JAVA_HOME")
		}
	}

	def checkExists(file:File): File = {
		if(file.exists()) file
		else throw new FileNotFoundException(s"Can't find needed resource: $file")
	}
}
