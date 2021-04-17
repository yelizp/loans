package com.example

import org.apache.spark.sql.{Column, DataFrame, Encoders, SparkSession}
import org.apache.spark.sql.catalyst.util.IntervalUtils.IntervalUnit
import org.apache.spark.sql.streaming.{DataStreamWriter, OutputMode, StreamingQuery, Trigger}
import org.apache.spark.sql.functions._

case class JsonAccount(AccountId:Long,AccountType:Int)
case class JsonLoan(LoanId:Long,AccountId:Long,Amount:BigDecimal)
case class JsonOutput(AccountType:Int,TotalCount:Int,TotalAmount:BigDecimal,LastMinuteCount:Int)

object JsonCustomerLoansMain {
  def outputToConsole(df:DataFrame, processingTime:String): StreamingQuery = {
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

  def outputToKafka(df:DataFrame,topic:String): Option[StreamingQuery] = {
    println("Output to console")
    val query = df.
      selectExpr( topic,"CAST(key AS STRING)", "CAST(value as STRING)").
      writeStream.
      format("kafka").
      option("kafka.bootstrap.servers", "localhost:9092").
      trigger(Trigger.ProcessingTime("5 seconds")).
      start()
    Some(query)
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
    val spark = SparkSession.builder
      .config("enableHive", false)
      .master("local")
      .getOrCreate()
    import spark.implicits._

    //    spark.sparkContext.setLogLevel("ERROR")

    val df = getDataFrame(spark, Seq("account_json","loan_json"))

    val accountDF = df.where("topic = 'account_json'")
      .withWatermark("timestamp", "30 seconds")
      .selectExpr("timestamp as AccountEventTime", "CAST(value as STRING)")
      .select($"AccountEventTime", from_json(col("value"),Encoders.product[JsonAccount].schema).alias("account"))
      .selectExpr("AccountEventTime", "account.*" )

    val loanDF = df.where("topic = 'loan_json'")
      .withWatermark("timestamp", "30 seconds")
      .selectExpr("timestamp as LoanEventTime", "CAST(value as STRING)")
      .select($"LoanEventTime", from_json(col("value"),Encoders.product[JsonLoan].schema).alias("loan"))
      .selectExpr("LoanEventTime", "loan.*" )
      .withColumn("LoanCount", lit(1))
      .withColumnRenamed("AccountId", "LoanAccountId")

    accountDF.printSchema()
    loanDF.printSchema()

    val loanAggDF = loanDF
      .join(
        accountDF,
        expr(
          "LoanAccountId = AccountId "
            + " AND (" +
            " (LoanEventTime >= AccountEventTime AND LoanEventTime < AccountEventTime + interval 30 second) OR "
            + " (AccountEventTime >= LoanEventTime AND AccountEventTime < LoanEventTime + interval 30 second))"
        )
      )
      .groupBy(
        $"AccountType",
        window($"LoanEventTime", "1 minute"),
      )
      .agg(
        sum("Amount").as("TotalAmount"),
        avg("LoanCount").as("LastMinuteCount")
      )

    loanAggDF.printSchema()

      val loanQuery = outputToConsole(loanAggDF, "1 minute")

//    val loanQuery = loanDF
//        .join(
//          accountDF,
//          expr(
//            "LoanAccountId = AccountId "
//              + " AND (" +
//                " (LoanEventTime >= AccountEventTime AND LoanEventTime < AccountEventTime + interval 30 second) OR "
//              + " (AccountEventTime >= LoanEventTime AND AccountEventTime < LoanEventTime + interval 30 second))"
//          )
//        ).withColumn("LastMinuteCount", sum("LoanCount").over(Window.partitionBy($"AccountType", "").
//      orderBy(col("sal"), col("emp_name"), col("date").asc).
//      rowsBetween(Long.MinValue, 0)
//    ))

    loanQuery.awaitTermination()

  }
}
