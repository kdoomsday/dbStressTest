package example

import cats.effect.{ ContextShift, IO }
import com.example.{ Dao, DbDao }
import com.zaxxer.hikari.HikariDataSource
import doobie.util.transactor.Transactor
import com.example.Props
import scala.concurrent.ExecutionContext


object DaoBuilder {
  /** Dao que trabaja con una db SQLite */
  def sqliteDao()(implicit cs: ContextShift[IO], connectEC: ExecutionContext, transactEC: ExecutionContext):   Dao = mkDao(sqliteTrans()(cs, connectEC, transactEC)  )
  def postgresDao()(implicit cs: ContextShift[IO], connectEC: ExecutionContext, transactEC: ExecutionContext): Dao = mkDao(postgresTrans()(cs, connectEC, transactEC))

  def sqliteTrans()(implicit cs: ContextShift[IO], connectEC: ExecutionContext, transactEC: ExecutionContext): Transactor[IO] = {
    val hds = new HikariDataSource()
    val url = propsSqlite.string("url").getOrElse[String](throw new Exception("No db"))
    println(s"Using: $url")

    hds.setJdbcUrl(url)
    Transactor.fromDataSource[IO](hds, connectEC=connectEC, transactEC=transactEC)
  }

  def postgresTrans()(implicit cs: ContextShift[IO], connectEC: ExecutionContext, transactEC: ExecutionContext): Transactor[IO] = {
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
      case Some(xa) => xa
      case None     => {
        throw new Exception(s"No db @ [${url.getOrElse("")}]")
      }
    }
  }

  @inline
  private[this] def mkDao(fTrans: Transactor[IO]): Dao = new DbDao(IO.pure(fTrans))

  private[this] val propsSqlite   = new Props("sqlite.conf")
  private[this] val propsPostgres = new Props("postgresql.conf")
}
