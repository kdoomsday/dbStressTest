package example

import cats.effect.IO
import com.example.{ Dao, Record }
import fs2.{ Stream, StreamApp }
import fs2.StreamApp.ExitCode
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.blaze._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID
import scala.math.BigDecimal
import scala.util.{ Failure, Success, Try }

object Hello extends StreamApp[IO] {
  val testService = HttpService[IO] {
    case GET -> Root / "test" =>
      Ok("echo: " + System.currentTimeMillis())
  }

  def insertService(dao: Dao) = HttpService[IO] {
    case GET -> Root / "insert" / stringGuid / stringPrice =>
      getRecord(stringGuid, stringPrice) map (r => insert(r, dao)) match {
        case Success(a) => a
        case Failure(e) => Response(Status.BadRequest).withBody(e.getMessage)
      }

    case GET -> Root / "insertRandom" =>
      insert(Record(UUID.randomUUID(), BigDecimal("42.0")), dao)
  }

  /** Parse strings into a Record */
  private[this] def getRecord(stringGuid: String, stringPrice: String): Try[Record] = Try {
    val guid   = UUID.fromString(stringGuid)
    val price  = BigDecimal(stringPrice)
    Record(guid, price)
  }

  /** Insert a record and build the response */
  private[this] def insert(record: Record, dao: Dao): IO[Response[IO]] =
    dao.insert(record).flatMap(i => Response(Status.Ok).withBody(i.toString()))


  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    val dao =
      if (args contains "postgresql") {
        println("Choosing postgresql")
        DaoBuilder.postgresDao()
      }
      else {
        println("Choosing sqlite")
        DaoBuilder.sqliteDao()
      }

    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(testService, "/")
      .mountService(insertService(dao), "/")
      .serve
  }

}
