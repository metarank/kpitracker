package ai.metarank.kpitrack

import cats.effect.{IO, Ref}
import io.prometheus.client.Gauge

case class Metrics(githubStars: Ref[IO, Gauge])
