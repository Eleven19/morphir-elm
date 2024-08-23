package mill.local.elm

import mill._
import mill.scalalib

trait ElmModule extends Module {
    def elmDocOutDir = T{ T.ctx().dest }

    def elmDoc() = T.command {
        os.proc("elm", "make", "src/Main.elm", "--docs=docs.json").call()
    }

    def elmJsonPath = T{ PathRef(T.workspace / "elm.json") }

    def elmMakeSources = T.sources{ millSourcePath / "src" }

    

    def elmMake(docs:Option[os.Path]) = T.task {
        val baseCmd = Seq("elm", "make")
    }
}