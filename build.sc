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
        object cli extends ScalaProject with NativeImage{
            def mainClass = T { 
                val className = nativeImageMainClass()
                Option(className) 
            }

            def ivyDeps = Agg(
                ivy"com.lihaoyi::os-lib:${V.oslib}",
                ivy"com.lihaoyi::pprint:${V.pprint}",
                ivy"com.github.alexarchambault::case-app:${V.`case-app`}",
                ivy"io.getkyo::kyo-core:${V.kyo}",
                ivy"io.getkyo::kyo-direct:${V.kyo}",
                ivy"io.getkyo::kyo-sttp:${V.kyo}",
                ivy"org.graalvm.polyglot:js:${V.`graal-polyglot`}",
            )

            def moduleDeps = Seq(workspace)
            
            def jsResources = T.sources {
                val destDir = T.ctx().dest                                 
                val elmCompilerJs = packages.`morphir-elm-compiler`.elmMake().path 
                //os.copy(elmCompilerJs, destDir / "js" / "morphir-elm-compiler.js", createFolders = true)
                os.copy(elmCompilerJs, destDir / "js" , createFolders = true)
                val compileOutputs = packages.`morphir-elm-compiler`.compile()
                compileOutputs.foreach { output =>
                    os.copy.over(output.path, destDir , createFolders = true)
                }                
                Seq(PathRef(destDir))
            }

            def resources = T {                              
                super.resources() ++ jsResources()
            }

            // Native Image settings
            def nativeImageName = "morphir-cli" //TODO: Rename to morphir
            def nativeImageMainClass = T{ "org.finos.morphir.cli.Main"}
            def nativeImageClassPath    = runClasspath()
            def nativeImageGraalVmJvmId = "graalvm-java22:22.0.2"
            def nativeImageOptions = Seq(
                "--no-fallback",
                "--enable-url-protocols=http,https",                
                "-Djdk.http.auth.tunneling.disabledSchemes=",
                "-H:+UnlockExperimentalVMOptions",
                "-H:Log=registerResource:5",
                "-H:+BuildReport",                
            ) ++ (if (sys.props.get("os.name").contains("Linux")) Seq("--static") else Seq.empty)

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

    object packages extends Module {
        object `morphir-elm-compiler` extends ElmModule {
            def elmEntryPoints = T {
                Some(Seq(elmJsonDir() / "src" / "Morphir" / "Elm" / "CLI.elm").map(PathRef(_)))
            }

            def viteSources = T.sources {
                millSourcePath / "src"
            }

            def allViteSourceFiles = T {
                Lib.findSourceFiles(elmSources(), Seq("js","mjs","ts","elm")).map(PathRef(_))
            }
            
            def viteBuild() = T.task {
                val outDir = T.ctx().dest
                val _  = allViteSourceFiles()
                os.proc("npm", "run", "build").call(cwd = millSourcePath)
                os.copy.over(millSourcePath / "dist", outDir / "js", createFolders = true)
                PathRef(outDir)
            }

            def compile = T {
                val outDir = T.ctx().dest
                //val makeOutput = elmMake()      
                val viteOutput = viteBuild()()         
                // os.copy.over(viteOutput.path , outDir / "js" , createFolders = true) 
                // //os.copy.over(makeOutput.path, outDir / "js" , createFolders = true)

                Seq(PathRef(viteOutput.path))
            }
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
    val `graal-polyglot` = "24.0.2"
    object Scala {        
        val scala3x = "3.3.3"
        val defaultScalaVersion = scala3x
    }
}
