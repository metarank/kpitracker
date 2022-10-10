package ai.metarank.kpitrack.kpi

import cats.effect.IO

trait Indicator {
  def refresh(): IO[Unit]
}
