package com.ebarrientos.json

import io.circe.Encoder
import java.util.UUID
import java.sql.Timestamp

/** Encoders para tipos que no los tienen */
object JsonEncoders {
  implicit val uuidEncoder:      Encoder[UUID]      = Encoder.encodeString.contramap[UUID](_.toString)
  implicit val timestampEncoder: Encoder[Timestamp] = Encoder.encodeString.contramap[Timestamp](_.toString)
}
