package ai.metarank.kpitrack.kpi

import cats.effect.IO
import com.slack.api.methods.request.users.UsersListRequest
import com.slack.api.{Slack, SlackConfig}
import io.prometheus.client.Gauge
import scala.jdk.CollectionConverters._

case class SlackIndicators(slack: Slack, token: String, users: Gauge) extends Indicator {
  override def refresh(): IO[Unit] = IO {
    val req      = UsersListRequest.builder.token(token).build()
    val resp     = slack.methods(token).usersList(req)
    val userList = resp.getMembers.asScala.toList
    users.labels("all").set(userList.size)
    users.labels("active").set(userList.count(u => u.isEmailConfirmed && !u.isInvitedUser))
  }
}

object SlackIndicators {
  def create(env: Map[String, String]): IO[SlackIndicators] = for {
    token <- IO.fromOption(env.get("SLACK_TOKEN"))(new Exception("slack_token is missing"))
  } yield {
    new SlackIndicators(
      slack = Slack.getInstance(),
      token = token,
      users = Gauge.build("metarank_slack_users", "number of users in slack").labelNames("type").register()
    )
  }
}
