package com.example

import doobie.util.meta.Meta
import java.util.UUID

object DaoMeta {
  implicit val UUIDMeta: Meta[UUID] =
    Meta[String].xmap(UUID.fromString _, _.toString())
}
