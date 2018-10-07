package example

import cats.effect.IO
import com.example.{ Dao, DbDao }
import com.zaxxer.hikari.HikariDataSource
import doobie.util.transactor.Transactor


object DaoBuilder {
  /** Dao que trabaja con una db SQLite */
  def sqliteDao():   Dao = mkDao(sqliteTrans()  )
  def postgresDao(): Dao = mkDao(postgresTrans())

  private[this] def sqliteTrans(): IO[Transactor[IO]] = {
    val hds = new HikariDataSource()
    hds.setJdbcUrl("jdbc:sqlite:/home/doomsday/Documents/db/db.sqlite")
    val transactor = Transactor.fromDataSource[IO](hds)
    IO.pure(transactor)
  }

  def postgresTrans(): IO[Transactor[IO]] = {
    val xa = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver", "jdbc:postgresql:postgres", "postql", "postql"
    )
    IO.pure(xa)
  }

  @inline
  private[this] def mkDao(fTrans: IO[Transactor[IO]]): Dao = new DbDao(fTrans)
}
