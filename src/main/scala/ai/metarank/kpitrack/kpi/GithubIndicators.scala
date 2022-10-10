package ai.metarank.kpitrack.kpi

import ai.metarank.kpitrack.Logging
import ai.metarank.kpitrack.kpi.GithubIndicators.ReleasesResponse.ReleaseAssetResponse
import ai.metarank.kpitrack.kpi.GithubIndicators.{RefererResponse, ReleasesResponse, RepoResponse, TrafficViewsResponse}
import ai.metarank.kpitrack.kpi.GithubIndicators.TrafficViewsResponse.DailyViewCount
import cats.effect.IO
import io.circe.Codec
import io.prometheus.client.Gauge
import org.http4s.{AuthScheme, Credentials, Headers, MediaRange, Request, Uri}
import org.http4s.client.Client
import org.http4s.circe._
import org.http4s.headers.{Accept, Authorization, MediaRangeAndQValue}

case class GithubIndicators(
    client: Client[IO],
    token: String,
    project: String,
    repo: String,
    endpoint: Uri,
    views: Gauge,
    visitors: Gauge,
    todayViews: Gauge,
    todayVisits: Gauge,
    releaseDownloads: Gauge,
    stars: Gauge,
    forks: Gauge,
    issues: Gauge,
    refererViews: Gauge,
    refererVisitors: Gauge
) extends Indicator
    with Logging {
  implicit val trafficDecoder  = jsonOf[IO, TrafficViewsResponse]
  implicit val releasesDecoder = jsonOf[IO, List[ReleasesResponse]]
  implicit val referrerDecoder = jsonOf[IO, List[RefererResponse]]
  implicit val repoDecoder     = jsonOf[IO, RepoResponse]

  override def refresh(): IO[Unit] = for {
    trafficViews <- client.expect[TrafficViewsResponse](
      Request[IO](
        uri = endpoint / "repos" / project / repo / "traffic" / "views",
        headers = Headers(Authorization(Credentials.Token(AuthScheme.Bearer, token)))
      )
    )
    todayTrafficOption <- IO(trafficViews.views.lastOption)
    _                  <- info(s"views: $trafficViews")
    downloadResponse <- client.expect[List[ReleasesResponse]](
      Request[IO](
        uri = endpoint / "repos" / project / repo / "releases",
        headers = Headers(Authorization(Credentials.Token(AuthScheme.Bearer, token)))
      )
    )
    repoResponse <- client.expect[RepoResponse](
      Request[IO](
        uri = endpoint / "repos" / project / repo,
        headers = Headers(Authorization(Credentials.Token(AuthScheme.Bearer, token)))
      )
    )
    referrerResponse <- client.expect[List[RefererResponse]](
      Request[IO](
        uri = endpoint / "repos" / project / repo / "traffic" / "popular" / "referrers",
        headers = Headers(Authorization(Credentials.Token(AuthScheme.Bearer, token)))
      )
    )
  } yield {
    views.set(trafficViews.count)
    visitors.set(trafficViews.uniques)
    todayTrafficOption match {
      case Some(DailyViewCount(_, count, uniques)) =>
        todayViews.set(count)
        todayVisits.set(uniques)
      case None => //
    }
    downloadResponse match {
      case Nil => releaseDownloads.set(0)
      case _   => releaseDownloads.set(downloadResponse.map(_.downloadCount).sum)
    }
    stars.set(repoResponse.stargazers_count)
    forks.set(repoResponse.forks)
    issues.set(repoResponse.open_issues)
    referrerResponse.foreach(ref => {
      refererViews.labels(ref.referrer).set(ref.count)
      refererVisitors.labels(ref.referrer).set(ref.uniques)
    })
  }

}

object GithubIndicators {
  import io.circe.generic.semiauto._
  case class TrafficViewsResponse(count: Int, uniques: Int, views: List[DailyViewCount])

  object TrafficViewsResponse {
    case class DailyViewCount(timestamp: String, count: Int, uniques: Int)

    implicit val dvcCodec: Codec[DailyViewCount]       = deriveCodec
    implicit val tvrCodec: Codec[TrafficViewsResponse] = deriveCodec
  }

  case class ReleasesResponse(name: String, assets: List[ReleaseAssetResponse]) {
    def downloadCount = assets match {
      case Nil => 0
      case _   => assets.map(_.download_count).sum
    }
  }
  object ReleasesResponse {
    case class ReleaseAssetResponse(download_count: Int)
    implicit val rrCodec: Codec[ReleasesResponse]      = deriveCodec
    implicit val rarCodec: Codec[ReleaseAssetResponse] = deriveCodec
  }

  case class RepoResponse(open_issues: Int, forks: Int, stargazers_count: Int)
  object RepoResponse {
    implicit val rrc: Codec[RepoResponse] = deriveCodec
  }

  case class RefererResponse(referrer: String, count: Int, uniques: Int)
  object RefererResponse {
    implicit val rrc: Codec[RefererResponse] = deriveCodec
  }

  def create(client: Client[IO], env: Map[String, String]): IO[GithubIndicators] = for {
    endpoint <- IO.fromEither(Uri.fromString("https://api.github.com"))
    token    <- IO.fromOption(env.get("GITHUB_TOKEN"))(new Exception("github token missing"))
    project  <- IO.fromOption(env.get("GITHUB_PROJECT"))(new Exception("project missing"))
    repo     <- IO.fromOption(env.get("GITHUB_REPO"))(new Exception("project missing"))
  } yield {
    GithubIndicators(
      client = client,
      token = token,
      project = project,
      repo = repo,
      endpoint = endpoint,
      views = Gauge.build("metarank_github_views_total", "number of views").register(),
      visitors = Gauge.build("metarank_github_visitors_total", "number of visitors").register(),
      todayViews = Gauge.build("metarank_github_views_today", "number of views today").register(),
      todayVisits = Gauge.build("metarank_github_visitors_today", "number of visitors today").register(),
      releaseDownloads = Gauge.build("metarank_github_release_downloads", "number of total downloads").register(),
      stars = Gauge.build("metarank_github_stars", "number of stars").register(),
      forks = Gauge.build("metarank_github_forks", "number of forks").register(),
      issues = Gauge.build("metarank_github_open_issues", "number of open issues").register(),
      refererViews =
        Gauge.build("metarank_github_traffic_referers_views", "views by referer").labelNames("source").register(),
      refererVisitors =
        Gauge.build("metarank_github_traffic_referers_visitors", "visitors by referer").labelNames("source").register()
    )
  }
}
