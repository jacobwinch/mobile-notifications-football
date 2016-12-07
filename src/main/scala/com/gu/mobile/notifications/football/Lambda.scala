package com.gu.mobile.notifications.football

import akka.actor.{ActorSystem, Props}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, InstanceProfileCredentialsProvider}
import com.amazonaws.regions.Regions._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.lambda.runtime.Context
import com.gu.football.PaFootballActor
import com.gu.football.PaFootballActor.TriggerPoll
import com.gu.mobile.notifications.football.lib.{PaFootballClient, PaMatchDayClient}
import akka.pattern.ask
import akka.util.Timeout

import scala.beans.BeanProperty
import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * This is compatible with aws' lambda JSON to POJO conversion
  */
class LambdaInput() {
  @BeanProperty var name: String = _
}

object Lambda extends App {

  implicit val system = ActorSystem("goal-notifications-system")

  val paMatchDayClient = PaMatchDayClient(PaFootballClient)
  private val provider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("mobile"),
    InstanceProfileCredentialsProvider.getInstance
  )
  val dynamoDBClient: AmazonDynamoDBAsyncClient = new AmazonDynamoDBAsyncClient(provider).withRegion(EU_WEST_1)
  val tableName = "mobile-notifications-football-CODE"

  val footballActor = system.actorOf(
    Props(classOf[PaFootballActor], paMatchDayClient, dynamoDBClient, tableName),
    "goal-notifications-actor-solution"
  )

  def handler(lambdaInput: LambdaInput, context: Context): String = {
    implicit val timeout = Timeout(60.seconds)
    val done = footballActor ? TriggerPoll(context.getLogger)
    Await.ready(done, 60.seconds)
    "done"
  }
}