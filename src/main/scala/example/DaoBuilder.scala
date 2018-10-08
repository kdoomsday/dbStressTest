package example

import cats.effect.IO
import com.example.{ Dao, DbDao }
import com.zaxxer.hikari.HikariDataSource
import doobie.util.transactor.Transactor
import com.example.Props


object DaoBuilder {
  /** Dao que trabaja con una db SQLite */
  def sqliteDao():   Dao = mkDao(sqliteTrans()  )
  def postgresDao(): Dao = mkDao(postgresTrans())

  private[this] def sqliteTrans(): IO[Transactor[IO]] = {
    val hds = new HikariDataSource()
    val url = propsSqlite.string("url").getOrElse[String](throw new Exception("No db"))
    println(s"Using: $url")

    hds.setJdbcUrl(url)
    val transactor = Transactor.fromDataSource[IO](hds)
    IO.pure(transactor)
  }

  def postgresTrans(): IO[Transactor[IO]] = {
    val driver = propsPostgres.string("driver")
    val url = propsPostgres.string("url")
    val user = propsPostgres.string("username")
    val password = propsPostgres.string("password")

    val oTrans = for {
      d  <- driver
      u  <- url
      us <- user
      p  <- password
    } yield Transactor.fromDriverManager[IO](d, u, us, p)

    oTrans match {
      case Some(xa) => IO.pure(xa)
      case None     => {
        throw new Exception(s"No db @ [${url.getOrElse("")}]")
      }
    }

    // val xa = Transactor.fromDriverManager[IO](
    //   "org.postgresql.Driver", "jdbc:postgresql:postgres", "postql", "postql"
    // )
    // IO.pure(xa)
  }

  @inline
  private[this] def mkDao(fTrans: IO[Transactor[IO]]): Dao = new DbDao(fTrans)

  private[this] val propsSqlite   = new Props("sqlite.conf")
  private[this] val propsPostgres = new Props("postgresql.conf")
}
