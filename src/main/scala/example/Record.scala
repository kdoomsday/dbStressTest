package com.example

import java.util.UUID
import java.sql.Timestamp

case class Record(guid: UUID, price: BigDecimal)

case class RecordInfo(id: Int, guid: UUID, creationDate: Timestamp, description: String)
