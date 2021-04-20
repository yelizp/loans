package com.example

import org.apache.spark.sql.{Column, DataFrame, Encoders, SparkSession}
import org.apache.spark.sql.catalyst.util.IntervalUtils.IntervalUnit
import org.apache.spark.sql.streaming.{DataStreamWriter, OutputMode, StreamingQuery, Trigger}
import org.apache.spark.sql.functions._
import scala.concurrent.duration._
import org.apache.spark.sql.internal.SQLConf.SHUFFLE_PARTITIONS

case class JsonAccount(AccountId:Long,AccountType:Int, EventTime:java.sql.Timestamp)
case class JsonLoan(LoanId:Long,AccountId:Long,Amount:BigDecimal, EventTime:java.sql.Timestamp)
case class JsonOutput(AccountType:Int,TotalCount:Int,TotalAmount:BigDecimal,LastMinuteCount:Int)

object JsonCustomerLoansMain {
  val numShufflePartitions = 1
  val delayThreshold = 30.seconds

  val spark = SparkSession.builder
    .config("enableHive", false)
    .master("local[1]")
    .getOrCreate()
  import spark.implicits._

  spark.sessionState.conf.setConf(SHUFFLE_PARTITIONS, numShufflePartitions)

  def outputToMemory(df:DataFrame, queryName:String): StreamingQuery = {
    println("Output to memory")
    val query = df.
      writeStream.
      format("memory").
      queryName(queryName).
      start()
    query
  }

  def outputToConsole(df:DataFrame, processingTime:Duration): StreamingQuery = {
    println("Output to console")
    val query = df.
      writeStream.
      format("console").
      option("truncate", "false").
      option("numRows", 25).
      outputMode(OutputMode.Append()).
      trigger(Trigger.ProcessingTime(processingTime)).
      start()
    query
  }

  def outputToKafka(df:DataFrame,topic:String, duration:Duration): StreamingQuery = {
    println("Output to Kafka")
    val query = df.
      writeStream.
      format("kafka").
      option("kafka.bootstrap.servers", "localhost:9092").
      option("checkPointLocation", "checkpoints").
      option("topic", topic).
      trigger(Trigger.ProcessingTime(duration)).
      start()
    query
  }

  def getDataFrame(spark:SparkSession, topics:Seq[String]) : DataFrame = {
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", topics.mkString(","))
//      .option("startingOffsets", "earliest")
      .load()
  }

  def main(args: Array[String]): Unit = {
    //    spark.sparkContext.setLogLevel("ERROR")

    val df = getDataFrame(spark, Seq("account_json","loan_json"))

    val accountDF = df.where("topic = 'account_json'")
      .selectExpr("CAST(value as STRING)")
      .select(from_json(col("value"),Encoders.product[JsonAccount].schema).alias("account"))
      .selectExpr("account.*" )
      .withColumnRenamed("EventTime","AccountEventTime")
      .withWatermark("AccountEventTime", delayThreshold.toString())

    val loanDF = df.where("topic = 'loan_json'")
      .selectExpr("CAST(value as STRING)")
      .select(from_json(col("value"),Encoders.product[JsonLoan].schema).alias("loan"))
      .selectExpr("loan.*" )
      .withColumnRenamed("EventTime","LoanEventTime")
      .withColumnRenamed("AccountId", "LoanAccountId")
      .withWatermark("LoanEventTime", delayThreshold.toString())

//        val query = outputToKafka(
//          loanDF.select(
//    //      accountDF.select(
//            col("LoanId").cast("String").as("key"),
//            to_json(
//              struct(
//                'LoanEventTime, 'LoanId, 'AccountId, 'Amount
//              )
//            ).cast("String").as("value")
//          ),
//          "output_json",
//          1.minutes
//        )
//
//    query.awaitTermination()


    accountDF.printSchema()
    loanDF.printSchema()

    var loanAggDF = loanDF
      .join(
        accountDF,
        expr(
          "LoanAccountId = AccountId "
          )
        )
      .select('LoanEventTime, 'AccountType, 'Amount)
      .groupBy(
        window($"LoanEventTime", "1 minute", "30 second").as("time"),
        $"AccountType"
      )
      .agg(
        sum("Amount").as("LastMinuteTotalAmount"),
        count("AccountType").as("LastMinuteCount")
      )
//
////    loanAggDF = loanAggDF
////      .groupBy('AccountType)
////      .agg(
////        sum("LastMinuteCount").as("TotalCount"),
////        sum("LastMinuteTotalAmount").as("TotalAmount"),
////        last("LastMinuteCount").as("LastMinuteCount")
////      )
//
////    loanAggDF.printSchema()
//
////    val loanQuery = outputToConsole(loanAggDF, 1.minutes)
//
    val loanQuery = outputToKafka(
      loanAggDF.select(
//      accountDF.select(
        col("AccountType").cast("String").as("key"),
        to_json(
          struct(
            col("time.start"), 'AccountType, 'LastMinuteTotalAmount, 'LastMinuteCount
//            col("time.start"), 'AccountType, 'LastMinuteTotalAmount, 'LastMinuteCount
//            col("AccountType")
//            ,col("LastMinuteTotalAmount")
//            ,col("LastMinuteCount")
          )
        ).cast("String").as("value")
      ),
      "output_json",
      1.minutes
    )
    loanQuery.awaitTermination()
  }
}
