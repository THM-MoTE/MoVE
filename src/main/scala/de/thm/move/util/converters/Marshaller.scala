package de.thm.move.util.converters

/** A marshaller is basically a converter for both directions: encoding and decoding. */
trait Marshaller[From, To] extends Convertable[From, To] {
  override final def convert(f:From): To = encode(f)
  def encode(from:From): To
  def decode(to:To): From
}

trait StringMarshaller[A] extends Marshaller[String,A]


object Marshaller {

  implicit object StringIntMarshaller extends StringMarshaller[Int] {
    override def encode(from: String): Int = from.toInt
    override def decode(to: Int): String = to.toString
  }
  implicit object StringDoubleMarshaller extends StringMarshaller[Double] {
    override def encode(from: String): Double = from.toDouble
    override def decode(to: Double): String = to.toString
  }
}