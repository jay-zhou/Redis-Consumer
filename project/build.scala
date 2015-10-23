import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._



object MessageConsumerBuild extends Build {
  lazy val buildSettings =  Seq(
    version := "0.1-SNAPSHOT",
    organization := "jzhou.ethoca",
    scalaVersion := "2.10.4"
  )


  assemblyMergeStrategy in assembly := {
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
    case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", xs @ _*) => MergeStrategy.last
    case PathList("com", "google", xs @ _*) => MergeStrategy.last
    case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
    case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
    case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
//    case PathList(ps @ _*) if ps.last endsWith "pom.properties" =>
 //   MergeStrategy.discard
    case "about.html" => MergeStrategy.rename
    case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
    case "META-INF/mailcap" => MergeStrategy.last
    case "META-INF/mimetypes.default" => MergeStrategy.last
    case "plugin.properties" => MergeStrategy.last
    case "log4j.properties" => MergeStrategy.last
    case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
  }



  lazy val app = Project(
    "MessageConsumer",
    file("."),
    settings = buildSettings ++  Seq(
      parallelExecution in Test := false,
      libraryDependencies ++= Seq(
        ("redis.clients" % "jedis" % "2.7.3" )

       )

     )
  )

  assemblyExcludedJars in assembly :=  {
          val cp = (fullClasspath in assembly).value
          cp filter {_.data.getName == "avro-ipc-1.7.7-tests.jar" } }


}

