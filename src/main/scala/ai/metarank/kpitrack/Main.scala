package ai.metarank.kpitrack

import ai.metarank.kpitrack.api.MetricsApi
import ai.metarank.kpitrack.kpi.{DockerhubIndicators, GithubIndicators, Indicator}
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.client.BlazeClientBuilder

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import cats.implicits._
import io.prometheus.client.CollectorRegistry
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

object Main extends IOApp with Logging {
  override def run(args: List[String]): IO[ExitCode] = {
    startApi().use(api =>
      createHttpClient().use(client =>
        for {
          _ <- info("starting Metarank metric tracker")
          env <- IO {
            System.getenv().asScala.toMap
          }
          github       <- GithubIndicators.create(client, env)
          docker       <- DockerhubIndicators.create(client, env)
          totalMetrics <- IO(CollectorRegistry.defaultRegistry.metricFamilySamples().asIterator().asScala.toList.size)
          _            <- info(s"registered $totalMetrics metrics")
          _            <- fs2.Stream.repeatEval[IO, Unit](tick(List(github, docker))).compile.drain

        } yield {
          ExitCode.Success
        }
      )
    )
  }

  def tick(indicators: List[Indicator]): IO[Unit] = for {
    _ <- indicators.map(_.refresh()).sequence
    _ <- IO.sleep(60.minute)
  } yield {
    ///
  }

  def startApi() = {
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(Router("/" -> MetricsApi(CollectorRegistry.defaultRegistry).routes).orNotFound)
      .serve
      .compile
      .drain
      .background
  }
  def createHttpClient() = {
    BlazeClientBuilder[IO]
      .withConnectTimeout(10.second)
      .withRequestTimeout(10.second)
      .resource
  }
}
