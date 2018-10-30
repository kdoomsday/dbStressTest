package com.ebarrientos.services

import scala.util.Try


object BooleanVar {
  def unapply(str: String): Option[Boolean] =
    if (!str.isEmpty())
      Try { java.lang.Boolean.valueOf(str).booleanValue() }.toOption
    else
      None
}
