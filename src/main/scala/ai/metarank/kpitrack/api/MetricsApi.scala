package ai.metarank.kpitrack.api

import cats.effect.IO
import io.prometheus.client.CollectorRegistry
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import fs2.{Chunk, Stream}
import io.prometheus.client.exporter.common.TextFormat

import java.io.{ByteArrayOutputStream, OutputStreamWriter}
import java.nio.ByteBuffer

case class MetricsApi(registry: CollectorRegistry) {
  val routes = HttpRoutes.of[IO] { case GET -> Root / "metrics" =>
    Ok(writeMetrics())
  }

  def writeMetrics(): Stream[IO, Byte] = {
    val stream = new ByteArrayOutputStream()
    val writer = new OutputStreamWriter(stream)
    TextFormat.write004(writer, registry.metricFamilySamples())
    writer.close()
    val bytes = stream.toByteArray
    Stream.chunk(Chunk.byteBuffer(ByteBuffer.wrap(stream.toByteArray)))
  }
}
