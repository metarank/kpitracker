package ai.metarank.kpitrack.kpi

import ai.metarank.kpitrack.Logging
import ai.metarank.kpitrack.kpi.DockerhubIndicators.RepoResponse
import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.prometheus.client.Gauge
import org.http4s.{Request, Uri}
import org.http4s.client.Client
import org.http4s.circe._

case class DockerhubIndicators(
    client: Client[IO],
    project: String,
    repo: String,
    endpoint: Uri,
    pulls: Gauge
) extends Indicator
    with Logging {
  implicit val repoDecoder = jsonOf[IO, RepoResponse]
  override def refresh(): IO[Unit] = for {
    response <- client.expect[RepoResponse](
      Request[IO](
        uri = endpoint / "v2" / "repositories" / project / repo
      )
    )
    _ <- info(response.toString)
  } yield {
    pulls.set(response.pull_count)
  }
}

object DockerhubIndicators extends Logging  {
  case class RepoResponse(pull_count: Int)
  object RepoResponse {
    implicit val rrCodec: Codec[RepoResponse] = deriveCodec
  }

  def create(client: Client[IO], env: Map[String, String]) = for {
    endpoint <- IO.fromEither(Uri.fromString("https://hub.docker.com"))
    project  <- IO.fromOption(env.get("DOCKERHUB_PROJECT"))(new Exception("project missing"))
    repo     <- IO.fromOption(env.get("DOCKERHUB_REPO"))(new Exception("project missing"))
    _ <- info(s"Docker hub: project=$project repo=$repo")
  } yield {
    DockerhubIndicators(
      client = client,
      project = project,
      repo = repo,
      endpoint = endpoint,
      pulls = Gauge.build("metarank_dockerhub_pulls", "number of pulls").register()
    )
  }
}
