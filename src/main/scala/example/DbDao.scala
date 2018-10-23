package com.example

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import java.sql.Timestamp
import java.util.UUID

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

  override def markOldInfos(date: Timestamp): IO[Int] =
    transactor.flatMap { xa =>
      sql"""Update record_info set description = (description || ' OLD')
            where creation_date < $date and description not like '% OLD'""".update
        .run
        .transact(xa)
    }
}
