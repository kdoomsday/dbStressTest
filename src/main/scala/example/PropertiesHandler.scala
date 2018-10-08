package com.example

import scala.util.Try
import java.util.Properties

trait PropertiesHandler {
  def string(name: String): Option[String]
  def int(name: String):Option[Int]
}

/** Carga propiedades de un nombre que esté en el classpath */
case class Props(path: String) extends PropertiesHandler {
  private[this] val p = new Properties()
  p.load(Thread.currentThread.getContextClassLoader().getResourceAsStream(path))

  override def string(name: String): Option[String] =
    if (p.containsKey(name))
      Option(p.getProperty(name))
    else
      None

  override def int(name: String): Option[Int] = to(name, _.toInt)

  /** Convierte a lo que sea, None si la conversion falla */
  def to[T](name: String, f: String => T): Option[T] =
    Try { string(name).map(f) }.toOption.flatten
}
