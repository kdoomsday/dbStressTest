package com.ebarrientos

import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._
import com.ebarrientos.dao.{ Dao, DaoBuilder, DbDao }
import com.ebarrientos.dao.stream.{ DaoS, DbDaoS }
import com.ebarrientos.services.{ ListServices, StreamServices }
import fs2.Stream
import io.circe.Encoder
import java.sql.Timestamp
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.server.blaze._
import org.http4s.circe._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID
import scala.math.BigDecimal
import scala.util.{ Failure, Random, Success, Try }
import io.circe.syntax._
import io.circe.generic.auto._
import doobie.util.transactor.Transactor

object ServerMain extends IOApp {

  def getPostgresDaoS(): DaoS[IO] = DbDaoS[IO](DaoBuilder.postgresTrans())
  def getSqliteDaoS  (): DaoS[IO] = DbDaoS[IO](DaoBuilder.sqliteTrans())

  def streamServices(dao: DaoS[IO]) = Seq(
    StreamServices.testService,
    StreamServices.insertService(dao),
    StreamServices.queryService(dao),
    StreamServices.updateService(dao)
  )

  def listServices(dao: Dao) = Seq(
    ListServices.testService,
    ListServices.insertService(dao),
    ListServices.queryService(dao),
    ListServices.updateService(dao)
  )

  /** Application main.
    * Adding "postgresql" as a param makes it choose postgresql for the database
    */
  override def run(args: List[String]): IO[ExitCode] = {
    // val services = if (args contains "list") (trans: Transactor[IO]) => listServices(DbDao(trans))
    //                else (trans: Transactor[IO]) => streamServices(DbDaoS(trans))

    val trans =
      if (args contains "postgresql") {
        println("Choosing postgresql")
        DaoBuilder.postgresTrans()
      }
      else {
        println("Choosing sqlite")
        DaoBuilder.sqliteTrans()
      }

    val httpApp = Router(
      listServices(DbDao(trans)).map("/list/" -> _) ++ streamServices(DbDaoS(trans)).map("/stream/" -> _): _*
      // services(trans).map("/" -> _): _*
    ).orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}

