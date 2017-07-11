package com.gu.mobile.notifications.football

import java.io.{BufferedReader, InputStreamReader}
import java.util.stream.Collectors

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, DefaultAWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider, InstanceProfileCredentialsProvider}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.typesafe.config.{Config, ConfigFactory}

class Configuration {

  val credentials = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("mobile"),
    DefaultAWSCredentialsProviderChain.getInstance()
  )

  private val s3Client = {
    val c = new AmazonS3Client(credentials)
    c.setRegion(Region.getRegion(Regions.EU_WEST_1))
    c
  }

  val stack = Option(System.getenv("Stack")).getOrElse("DEV")
  val stage = Option(System.getenv("Stage")).getOrElse("DEV")

  private val conf: Config = {
    val dataStream = s3Client.getObject("mobile-notifications-dist", s"$stage/football/football.conf").getObjectContent
    val data = new BufferedReader(new InputStreamReader(dataStream)).lines.collect(Collectors.joining("\n"))
    ConfigFactory.parseString(data)
  }

  val paApiKey = conf.getString("pa.api-key")
  val paHost = conf.getString("pa.host")
  val notificationsHost = conf.getString("notifications-client.host")
  val notificationsApiKey = conf.getString("notifications-client.api-key")
  val notificationsLegacyHost = conf.getString("notifications-client.legacy.host")
  val notificationsLegacyApiKey = conf.getString("notifications-client.legacy.api-key")
  val mapiHost = conf.getString("mapi.host")
}