package com.ebarrientos.dao.stream

import cats.Monad
import cats.effect.IO
import com.ebarrientos.{ Record, RecordInfo }
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import java.sql.Timestamp
import java.util.UUID
import com.ebarrientos.dao.DaoMeta._

/** Implementación de DaoS usando Doobie */
class DbDaoS[F[_] : Monad] (transactor: Transactor[F]) extends DaoS[F] {
  private[this] val xa: Transactor[F] = transactor

  def qInsert(guid: String, price: BigDecimal): Update0 =
    sql"""Insert into Record(guid, price) Values($guid, $price)""".update

  def insert(record: Record): F[Int] =
    qInsert(record.guid.toString(), record.price)
      .run
      .transact(xa)

  def randomRecord(): F[Option[Record]] =
    sql"Select guid, price from record order by random() limit 1"
      .query[Record]
      .option
      .transact(xa)

  // Insercion de recordInfo
  def qInsertRI(uuidParent: String, description: String): Update0 =
    sql"Insert into record_info(guid, description) values ($uuidParent, $description)".update

  def insertRI(guidParent: UUID, description: String): F[Int] =
    qInsertRI(guidParent.toString(), description)
      .run
      .transact(xa)


  def recordInfos(guid: UUID): Stream[F, RecordInfo] =
    sql"""Select id, guid, creation_date, description
            from record_info
            where guid=$guid""".query[RecordInfo]
      .stream
      .transact(xa)


  def markOldInfos(date: Timestamp): F[Int] =
    sql"""Update record_info set description = (description || ' OLD')
            where creation_date < $date and description not like '% OLD'""".update
      .run
      .transact(xa)


  // Consulta para la funcion query
  def qQuery(n: Int, ascending: Boolean) =
    sql"""Select guid, price from Record
          order by price """ ++
      (if (ascending) fr"ASC" else fr"DESC") ++
      fr"limit $n"

  def query(n: Int, ascending: Boolean): Stream[F, Record] =
    qQuery(n, ascending)
      .query[Record]
      .stream
      .transact(xa)
}

object DbDaoS {
  def apply[F[_]: Monad](transactor: Transactor[F]): DbDaoS[F] = new DbDaoS(transactor)
}
