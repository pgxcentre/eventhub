package eventhub

import sbt._
import sbt.Keys._
import sbt.Tests
import java.io.{BufferedReader, InputStreamReader, FileInputStream, File}
import java.nio.charset.Charset
import java.util.Properties
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import spray.revolver.RevolverPlugin._
import sbtassembly.Plugin._
import AssemblyKeys._

object EventHubBuild extends Build {

  lazy val root = Project(id = "root",
                          base = file("."))
                          .aggregate(common, rest, backup)

  /**
   * All unit tests should run in parallel by default. This filter selects such tests
   * and afterwards parallel execution settings are applied.
   * Thus don't include word 'Integration' in unit test suite name.
   *
   * SBT command: test
   */
  def parFilter(name: String): Boolean = !(name contains "Integration")

  /**
   * Integration tests should run sequentially because they take lots of resources,
   * or shared use of resources can cause conflicts.
   * 
   * SBT command: serial:test
   **/
  def serialFilter(name: String): Boolean = (name contains "Integration")

  // config for serial execution of integration tests
  lazy val Serial = config("serial") extend(Test)

  lazy val rest = Project(id = "rest",
                          base = file("rest"))
                          .configs(Serial)
                          .settings(inConfig(Serial)(Defaults.testTasks) : _*)
                          .settings(
                            testOptions in Test := Seq(Tests.Filter(parFilter)),
                            testOptions in Serial := Seq(Tests.Filter(serialFilter))
                          )
                          .settings(parallelExecution in Serial := false : _*)
                          .settings(restSettings : _*) dependsOn(common % "test->test;compile->compile", backup)

  lazy val backup = Project(id = "backup",
                            base = file("backup"))
                            .settings(backupSettings: _*) dependsOn(common % "test->test;compile->compile")

  lazy val common = Project(id = "common",
                            base = file("common")) settings(commonSettings: _*)


  // Load system properties from a file to make configuration from Jenkins easier
  loadSystemProperties("project/build.properties")

  lazy val commonSettings =
  settings ++
  Seq(libraryDependencies ++=
        Dependencies.log ++
        Dependencies.rogue ++
        Dependencies.lift ++
        Dependencies.mongeezAll ++
        Seq(Dependencies.Compile.casbah) // TODO: put all db deps into a single dependency: val DbAll = ...
        )

  lazy val buildSettings = Seq(
    organization := "ca.pgx",
    version      := "0.1",
    scalaVersion := System.getProperty("scalaVersion", "2.10.4"),
    EclipseKeys.withSource := true
  )

  lazy val backupSettings =
  super.settings ++
  defaultSettings ++
  Seq(libraryDependencies ++=
      Dependencies.lift ++
      Dependencies.mongeezAll ++
      Dependencies.rogue ++
      Dependencies.akka)
  Seq(
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  )

  lazy val restSettings =
  super.settings ++
  defaultSettings ++
  assemblySettings ++
  Seq(mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("overview.html") => MergeStrategy.rename
    case x => old(x)
  }
  }) ++
  Revolver.settings ++
  Seq(libraryDependencies ++=
      Dependencies.sprayWithAkka ++
      Dependencies.lift ++
      Dependencies.rogue)
  Seq(
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  )

  override lazy val settings = 
  super.settings ++
  buildSettings ++
  defaultSettings ++
  Seq(libraryDependencies ++= Dependencies.testKit) ++
  Seq(
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  )

  lazy val defaultSettings = Seq(
    // Compile options
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation",
      "-feature", "-unchecked", "-language:_"),
/* Uncomment to see reflection uses: "-Xlog-reflective-calls",
This generates lots of noise in the build: "-Ywarn-adapted-args",
*/ 
    // scaladoc options - for now inheritance diagrams are not generated. This will be fixed soon.
    // "-diagrams" option requires graphviz package/app to be installed. This option is for scala version >= 2.10
    scalacOptions in Compile in doc ++= Seq("-diagrams", "-implicits"),

    javacOptions in Compile ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint:unchecked", "-Xlint:deprecation"),

    ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet,

    // Test settings

    // show full stack traces and test case durations
    // FIXME: fails to run test in client proj: testOptions in Test += Tests.Argument("-oDF"),

    parallelExecution in Test := System.getProperty("parallelExecution", "true").toBoolean,
    logBuffered in Test := System.getProperty("logBufferedTests", "true").toBoolean
  )

  def loadSystemProperties(fileName: String): Unit = {
    import scala.collection.JavaConverters._
    val file = new File(fileName)
    if (file.exists()) {
      println("Loading system properties from file `" + fileName + "`")
      val in = new InputStreamReader(new FileInputStream(file), "UTF-8")
      val props = new Properties
      props.load(in)
      in.close()
      sys.props ++ props.asScala
    }
  }

// Dependencies

object Dependencies {

  object Compile {
    // Compile

      // LIFT
      val liftVersion = "2.5.1"
      val liftJson = "net.liftweb" %% "lift-json" % liftVersion
      val liftCommon = "net.liftweb" %% "lift-common" % liftVersion
      val liftRecord = "net.liftweb" %% "lift-mongodb-record" % liftVersion

      // AKKA
//      val akkaSystem = Seq(
//  "com.typesafe.akka" % "akka-actor_2.10" % "2.2.1",
//  "com.typesafe.akka" % "akka-remote_2.10" % "2.2.1",
//  "com.typesafe.akka" % "akka-kernel_2.10" % "2.2.1",
//  "com.typesafe.akka" % "akka-slf4j_2.10" % "2.2.1",
//  "com.typesafe.akka" % "akka-testkit_2.10"  % "2.2.1" % "test"
//   )

  val akkaV = "2.2.4" // TODO: update to 2.3.0 very soon
  val sprayV = "1.2.0" // TODO: update to 1.3 soon
  val spray = Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV,
    "io.spray"  	  %   "spray-http"    % "1.2.0",
    "io.spray"            %   "spray-httpx"   % "1.2.0",
    "io.spray"            %%   "spray-json"    % "1.2.5"
  )

  val akkaSystem = Seq(
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV,
    "org.specs2"          %%  "specs2"        % "2.2.3" % "test"
  )

    val rogueField = "com.foursquare" %% "rogue-field"         % "2.2.1" intransitive()
    val rogueCore = "com.foursquare" %% "rogue-core"          % "2.3.0" intransitive()
    val rogueLift = "com.foursquare" %% "rogue-lift"          % "2.3.0" intransitive()
    val rogueIndex = "com.foursquare" %% "rogue-index"          % "2.3.0" intransitive()
    val casbah = "org.mongodb" %% "casbah" % "2.7.1"
    val mongeez = "org.mongeez" % "mongeez" % "0.9.4"

    // LOG
    val logback      = "ch.qos.logback" % "logback-classic"              % "1.0.13"
    val logbackJavaCompiler = "org.codehaus.janino" % "janino" % "2.6.1"

    // DATE
    val dateScala = "org.scalaj" % "scalaj-time_2.10.0-M7" % "0.6"

    object Test {
      val junit        = "junit"                       % "junit"                        % "4.11"             % "test" // Common Public License 1.0
//      val logback      = "ch.qos.logback"              % "logback-classic"              % "1.0.7"            % "test" // EPL 1.0 / LGPL 2.1
      val mockito      = "org.mockito"                 % "mockito-all"                  % "1.8.1"            % "test" // MIT
      // changing the scalatest dependency must be reflected in akka-docs/rst/dev/multi-jvm-testing.rst
      val scalatest    = "org.scalatest"              %% "scalatest"                    % "2.1.7"      % "test" // ApacheV2
//      val scalacheck   = "org.scalacheck"             %% "scalacheck"                   % "1.10.0"           % "test" // New BSD
//      val log4j        = "log4j"                       % "log4j"                        % "1.2.14"           % "test" // ApacheV2
      val specs2 = "org.specs2"        %% "specs2"             % "2.3.12"           % "test"
      val testDb = "com.github.fakemongo" % "fongo" % "1.5.1" % "test"
    }
  }
  
  import Compile._

  val rogue = Seq(Compile.rogueField, Compile.rogueCore, Compile.rogueLift, Compile.rogueIndex) 

  val lift = Seq(Compile.liftRecord, Compile.liftJson)

/*
  val testKit = Seq(Test.junit, Test.scalatest, Test.specs2, Test.seleniumServer, Test.seleniumChrome, Test.guava, Test.guavaGwt, Test.testDb)

  val rogue = Seq(Compile.rogueField, Compile.rogueCore, Compile.rogueLift, Compile.rogueIndex)

  val date = Seq(dateScala)
*/

  val log = Seq(Compile.logback, Compile.logbackJavaCompiler)

  val akka = Compile.akkaSystem
  val sprayWithAkka = Compile.spray ++ akka

  val   mongeezAll = Seq(Compile.mongeez)

  val testKit = Seq(Test.junit, Test.scalatest, Test.specs2, Test.testDb)

 }

}

