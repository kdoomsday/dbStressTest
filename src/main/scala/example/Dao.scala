package com.example

import cats.effect.IO
import doobie._
import doobie.util.transactor.Transactor

trait Dao {
  /** Insertar un Record. Devuelve el número de elementos insertados (1) */
  def insert(record: Record): IO[Int]
}

/** Implementación que trabaja contra una BD */
class DbDao(val transactor: IO[Transactor[IO]]) extends Dao {
  import doobie.imports._

  def qInsert(guid: String, price: BigDecimal): Update0 =
    sql"""Insert into Record(guid, price) Values($guid, $price)""".update


  override def insert(record: Record): IO[Int] =
    transactor.flatMap(xa => qInsert(record.guid.toString(), record.price).run.transact(xa))
}
