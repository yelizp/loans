package com.example

import java.io.File

import org.apache.avro.{Schema, SchemaBuilder}
import org.apache.avro.io.Encoder
import org.apache.spark.sql.{Encoders, Row, SparkSession}
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.streaming.{DataStreamWriter, OutputMode, Trigger}
import org.apache.spark.sql.avro.functions._
import org.apache.spark.sql.catalyst.util.IntervalUtils.IntervalUnit
import org.apache.spark.sql.functions.{expr, lit, sum, window}
import org.apache.spark.sql.types.TimestampType
case class Account(AccountId:Long,AccountType:Int)
case class Loan(LoanId:Long, AccountId:Long, Amount:Double)
case class LoanAgg(AccountType:Int, TotalCount:Int, TotalAmount:Double, LastMinuteCount:Int)

object Test {
  val rootPath = new File(".").getCanonicalPath
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .config("enableHive", false)
      .master("local")
      .getOrCreate()
    import spark.implicits._

    spark.sparkContext.setLogLevel("ERROR")

    val accountStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "account")
      .option("startingOffsets", "earliest")
      .load()
    accountStream.printSchema()

    val accountSchema = new Schema.Parser().parse(new File(s"${rootPath}${File.separator}schema${File.separator}account.avsc")).toString()
    val accountDF = accountStream.select($"timestamp", from_avro($"value", accountSchema).as("account"))
      .withWatermark("timestamp", s"30 ${IntervalUnit.SECOND}")

    accountDF.printSchema()

    val loanStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "loan")
      .option("startingOffsets", "earliest")
      .load()

    loanStream.printSchema()

    val loanSchema = new Schema.Parser().parse(new File(s"${rootPath}${File.separator}schema${File.separator}loan.avsc")).toString()
    val loanDF = loanStream//.select($"timestamp", $"value".as("loan"))
      .withWatermark("timestamp", s"30 ${IntervalUnit.SECOND}")
      .writeStream
      .format(DataStreamWriter.SOURCE_NAME_CONSOLE)
      .option("truncate", "false")
      .outputMode(OutputMode.Append())
      .trigger(Trigger.ProcessingTime(10000))
      .start()
      .awaitTermination(100000)

//    val query = loanDF
//      .withColumnRenamed("timestamp", "LoanEventTime")
//      .withColumn("numLoans", lit(1))
//      .agg(sum($"loan.Amount")).alias("TotalAmount")
////      .agg(sum($"numLoans")).alias("TotalCount")
//      .join(
//        accountDF
//        .withColumnRenamed("timestamp", "AccountEventTime"),
//        expr("loan.AccountId = account.AccountId AND " +
//          "((LoanEventTime BETWEEN AccountEventTime AND AccountEventTime + interval 30 second) OR " +
//          "(AccountEventTime BETWEEN LoanEventTime AND LoanEventTime + interval 30 second))"))
//      .groupBy(
//        window($"LoanEventTime", "1 minute"),$"account.AccountType")
//      .agg(sum($"numLoans")).alias("LastMinuteCount")
//      .writeStream
//      .format(DataStreamWriter.SOURCE_NAME_CONSOLE)
//      .outputMode(OutputMode.Append())
//      .trigger(Trigger.Continuous(5000))
//      .start()
//      .awaitTermination(100000)

//    accountDF.writeStream.format(DataStreamWriter.SOURCE_NAME_CONSOLE).outputMode(OutputMode.Append()).trigger(Trigger.Continuous(5000)).start().awaitTermination(100000)

  }
}
