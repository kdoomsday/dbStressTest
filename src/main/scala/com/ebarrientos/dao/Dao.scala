package com.ebarrientos.dao

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import java.sql.Timestamp
import java.util.UUID
import com.ebarrientos.{ Record, RecordInfo }

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

  /** Marcar los recordInfos viejos. Devuelve cuantos fueron actualizados */
  def markOldInfos(date: Timestamp): IO[Int]
}
