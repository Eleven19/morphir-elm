import $meta._
import $ivy.`io.github.alexarchambault.mill::mill-native-image::0.1.26`
import io.github.alexarchambault.millnativeimage.NativeImage
import mill.local.elm.ElmModule
import mill.main.RootModule
import mill._, mill.scalalib._

object root extends RootModule with ElmModule {    

    def isCI = T.input {
        val result = sys.env.getOrElse("CI", "false")
        Seq("true","1","yes").contains(result) 
    }

    def gulp(steps: String*) = T.command {        
        for (step <- steps) {
            println(s"Running gulp $step")
        }
    }    

    def npmInstall() = T.command {
        val installSubCommand = isCI() match {
            case true => "ci"
            case false => "install"
        }
        os.proc("npm", installSubCommand).call()
    }

    def setup() = T.command {
        npmInstall()
    }

    object morphir extends Module {
        object cli extends ScalaProject {
            def mainClass = Some("org.finos.morphir.cli.Main")
            def ivyDeps = Agg(
                ivy"com.lihaoyi::os-lib:${V.oslib}",
                ivy"com.lihaoyi::pprint:${V.pprint}",
                ivy"com.github.alexarchambault::case-app:${V.`case-app`}",
                ivy"io.getkyo::kyo-core:${V.kyo}",
                ivy"io.getkyo::kyo-direct:${V.kyo}",
                ivy"io.getkyo::kyo-sttp:${V.kyo}"
            )

            def moduleDeps = Seq(workspace)
        }

        object lang extends Module {
            object elm extends ScalaProject{}
        }

        object workspace extends ScalaProject {
            def ivyDeps = Agg(
                ivy"com.lihaoyi::os-lib:${V.oslib}",
                ivy"io.lemonlabs::scala-uri:${V.`scala-uri`}",
                ivy"io.github.kitlangton::neotype:${V.neotype}"
            )
        }
    }

    object ci extends RootModule {
    
    }



    trait ScalaProject extends ScalaModule {
        def scalaVersion = V.Scala.defaultScalaVersion
    }
}


object V {
    val `case-app` = "2.1.0-M29"
    val kyo = "0.11.0"
    val oslib = "0.10.4"
    val pprint = "0.9.0"
    val neotype = "0.3.0"
    val `scala-uri` = "4.0.3"
    object Scala {        
        val scala3x = "3.3.3"
        val defaultScalaVersion = scala3x
    }
}
