package ebarrientos.stream

import cats.Monad
import cats.effect.IO
import com.example.{ Record, RecordInfo }
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import java.sql.Timestamp
import java.util.UUID
import com.example.DaoMeta._

/** Implementación de DaoS usando Doobie */
class DbDaoS[F[_] : Monad](transactor: Transactor[F]) extends DaoS[F] {
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

  def insertRI(guidParent: UUID, description: String): F[Int] = ???
  def recordInfos(guid: UUID): Stream[F, RecordInfo] = ???
  def markOldInfos(date: Timestamp): F[Int] = ???
  def query(n: Int, ascending: Boolean): Stream[F, Record] = ???
}
