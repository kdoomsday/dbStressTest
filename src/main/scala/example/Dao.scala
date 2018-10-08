package com.example

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import java.util.UUID

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


  /** Insertar un recordInfo. La fecha de creaci&oacute;n y el id son autogenerados */
  def insertRI(guidParent: UUID, description: String): IO[Int]

  /** Un record aleatorio, si hay */
  def randomRecord(): IO[Option[Record]]

  /** Todos los recordInfo de un record, por ID.
    * Si no existe el record la lista viene vac&iacute;a
    */
  def recordInfos(guid: UUID): IO[List[RecordInfo]]
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


  // Insercion de recordInfo
  def qInsertRI(uuidParent: String, description: String): Update0 =
    sql"Insert into record_info(guid, description) values ($uuidParent, $description)".update

  override def insertRI(guidParent: UUID, description: String): IO[Int] =
    transactor.flatMap(xa => qInsertRI(guidParent.toString(), description).run.transact(xa))

  override def randomRecord() =
    transactor.flatMap { xa =>
      sql"Select guid, price from record order by random() limit 1".query[Record].option.transact(xa)
    }

  override def recordInfos(guid: UUID): IO[List[RecordInfo]] =
    transactor.flatMap { xa =>
      sql"""Select id, guid, creation_date, description
            from record_info
            where guid=$guid""".query[RecordInfo]
        .to[List]
        .transact(xa)
    }
}
