Global / resolvers ++= Seq(
  "Akka library repository" at "https://repo.akka.io/maven",
  "Akka library snapshot repository" at "https://repo.akka.io/snapshots"
)

addSbtPlugin("ch.epfl.scala"           % "sbt-scalafix"        % "0.13.0")
addSbtPlugin("com.eed3si9n"            % "sbt-assembly"        % "2.3.0")
addSbtPlugin("com.github.reibitto"     % "sbt-welcome"         % "0.4.0")
addSbtPlugin("com.github.sbt"          % "sbt-license-report"  % "1.6.1")
addSbtPlugin("com.github.sbt"          % "sbt-native-packager" % "1.10.4")
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc"       % "2.4.4")
addSbtPlugin("com.lightbend.cinnamon"  % "sbt-cinnamon"        % "2.20.3")
addSbtPlugin("com.timushev.sbt"        % "sbt-rewarn"          % "0.1.3")
addSbtPlugin("com.timushev.sbt"        % "sbt-updates"         % "0.6.4")
addSbtPlugin("io.spray"                % "sbt-revolver"        % "0.10.0")
addSbtPlugin("org.scalameta"           % "sbt-scalafmt"        % "2.5.2")
addSbtPlugin("org.typelevel"           % "sbt-tpolecat"        % "0.5.2")
addSbtPlugin("com.github.sbt"          % "sbt-dynver"          % "5.0.1")