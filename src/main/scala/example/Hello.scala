package example

import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._
import com.example.{ Dao, Record }
import fs2.Stream
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

object Hello extends IOApp {
  val testService = HttpRoutes.of[IO] {
    case GET -> Root / "test" =>
      Ok("echo: " + System.currentTimeMillis())
  }

  def insertService(dao: Dao) = HttpRoutes.of[IO] {
    case GET -> Root / "insert" / stringGuid / stringPrice =>
      getRecord(stringGuid, stringPrice) map (r => insert(r, dao)) match {
        case Success(a) => a
        case Failure(e) => Response(Status.BadRequest).withBody(e.getMessage)
      }

    case GET -> Root / "insertRandom" =>
      val (full, dec) = (Random.nextInt(100), Random.nextInt(100))
      val num = BigDecimal(s"$full.$dec")
      insert(Record(UUID.randomUUID(), num), dao)

    case GET -> Root / "insertRI" / stringGuid / description =>
      Try { UUID.fromString(stringGuid) } match {
        case Success(uuid) =>
          dao.insertRI(uuid, description)
            .map(_.toString)
            .recoverWith{ case t => IO.pure(t.getMessage)}
            .flatMap[Response[IO]](text => Ok(text))

        case Failure(e)    => Response(Status.BadRequest).withBody(e.getMessage)
      }

    case GET -> Root / "insertIntoRRecord" / description =>
      val inserted = insIntoRandom(dao, description)
      inserted flatMap { i => if (i > 0) Ok(s"insertados $i registros")
                              else BadRequest(s"No ee consiguió record") }
  }


  def queryService(dao: Dao) = HttpRoutes.of[IO] {
    case GET -> Root / "query" / IntVar(n) / BooleanVar(ascending) =>
      // dao.query(n, ascending).flatMap(rs => Ok(rs.asJson))
      dao.query(n, ascending).flatMap(rs => Ok(rs.asJson))

    case GET -> Root / "recordInfos" / guidString =>
      Try { UUID.fromString(guidString) } match {
        case Success(uuid) =>
          dao.recordInfos(uuid).map(_.mkString("\n"))
                               .map(s => s"[$s]")
                               .flatMap (content => Ok(content))
        case Failure(e) =>
          BadRequest(s"Invalid uuid: $guidString")
      }
  }

  def updateService(dao: Dao) = HttpRoutes.of[IO] {
    case GET -> Root / "updateOlds" =>
      dao.markOldInfos(oldDate()) flatMap (i => Ok(s"Actualizados $i registros"))
  }

  /* ***** Fin services ***** */

  private[this] def oldDate(): Timestamp =
    Timestamp.valueOf(java.time.LocalDateTime.now().minusMinutes(5))

  private[this] def insIntoRandom(dao: Dao, description: String): IO[Int] = {
    dao.randomRecord()
      .flatMap { oRec =>
        oRec.map { record =>
          dao.insertRI(record.guid, description)
        }.getOrElse(IO.pure(0))
      }
  }

  /** Parse strings into a Record */
  private[this] def getRecord(stringGuid: String, stringPrice: String): Try[Record] = Try {
    val guid   = UUID.fromString(stringGuid)
    val price  = BigDecimal(stringPrice)
    Record(guid, price)
  }

  /** Insert a record and build the response */
  private[this] def insert(record: Record, dao: Dao): IO[Response[IO]] =
    dao.insert(record).flatMap(i => Ok(i.toString()))



  /** Application main.
    * Adding "postgresql" as a param makes it choose postgresql for the database
    */
  override def run(args: List[String]): IO[ExitCode] = {
    val dao =
      if (args contains "postgresql") {
        println("Choosing postgresql")
        DaoBuilder.postgresDao()
      }
      else {
        println("Choosing sqlite")
        DaoBuilder.sqliteDao()
      }

    val httpApp = Router(
      "/" -> testService,
      "/" -> insertService(dao),
      "/" -> queryService(dao),
      "/" -> updateService(dao)
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

object BooleanVar {
  def unapply(str: String): Option[Boolean] =
    if (!str.isEmpty())
      Try { java.lang.Boolean.valueOf(str).booleanValue() }.toOption
    else
      None
}
