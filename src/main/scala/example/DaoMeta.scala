package com.example

import doobie.util.Meta
import java.util.UUID

object DaoMeta {
  implicit val UUIDMeta: Meta[UUID] =
    Meta[String].imap(UUID.fromString _)(_.toString())
}
