package com.example

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

trait Dao {
  /** Insertar un Record. Devuelve el número de elementos insertados (1) */
  def insert(record: Record): IO[Int]

  /** Consultar los n tope elementos, ordenados por precio.precio
    * @param n Cuántos elementos se desean
    * @param ascending Si se debe ordenar ascendentemente o al contrario
    * @return A lo sumo n elementos, ordenados de acuerdo a [[ascending]]. Puede
    * traer menos elementos si no hay suficientes en la BD
    */
  def query(n: Int, ascending: Boolean): IO[List[Record]]
}

/** Implementación que trabaja contra una BD */
class DbDao(val transactor: IO[Transactor[IO]]) extends Dao {
  import doobie.imports._
  import com.example.DaoMeta._

  def qInsert(guid: String, price: BigDecimal): Update0 =
    sql"""Insert into Record(guid, price) Values($guid, $price)""".update


  override def insert(record: Record): IO[Int] =
    transactor.flatMap(xa => qInsert(record.guid.toString(), record.price).run.transact(xa))

  // Consulta para la funcion query
  def qQuery(n: Int, ascending: Boolean) =
    sql"""Select guid, price from Record
          order by price """ ++
          (if (ascending) fr"ASC" else fr"DESC") ++
          fr"limit $n"

  override def query(n: Int, ascending: Boolean): IO[List[Record]] =
    transactor.flatMap(xa => qQuery(n, ascending).query[Record].to[List].transact(xa))
}
