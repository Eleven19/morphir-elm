package org.finos.morphir.cli.command
import caseapp.*
import org.finos.morphir.cli.given 
import org.graalvm.polyglot.*
import scala.util.Using

final case class MakeOptions(
    @Name("p")
    @ValueDescription("project root directory")
    @HelpMessage("Set root directory of the project where morphir.json is located. (default: .)")
    projectDir: os.Path = os.pwd,
    @Name("o")
    @ValueDescription("output file")
    @HelpMessage("Set the target file location where the Morphir IR will be saved. (default: morphir-ir.json)")
    output: os.Path = os.pwd / "morphir-ir.json",
    @Name("t")
    @HelpMessage("Only include type information in the IR, no values. (default: false)")
    typesOnly: Boolean = false,
    @Name("i")
    @HelpMessage("Use indentation in the generated JSON file. (default: false)")
    indentJson:Boolean = false,
    @Name("I")
    @ValueDescription("path or url")
    @HelpMessage("Include additional Morphir distributions as a dependency. Can be specified multiple times. Can be a path, url, or data-url.")
    include: List[String] = Nil
)

object Make extends Command[MakeOptions] {
  def run(options: MakeOptions, remainingArgs: RemainingArgs): Unit = {      
    //val elmCompilerJs = os.read( os.resource / "js"/"morphir-elm-compiler.js") 
    val elmCompilerJs = os.read( os.resource / "js"/"compiler.js") 
     
    //pprint.pprintln(elmCompilerJs)
    val elmCompilerSource =
      Source
        .newBuilder("js", elmCompilerJs, "morphir-elm-compiler.js")
        .mimeType("application/javascript")
        .build()
    val bootstrap =
      """
        |import * as Elm from "./morphir-elm-compiler.js";
        |cli = Elm.Morphir.Elm.CLI;
        |console.log("CLI", cli);
        |export const worker = Elm.Morphir.Elm.CLI.init();
        |console.log(worker.ports);
        |""".stripMargingi
    val code = "export const foo = 42;"
    val source = 
      Source
        .newBuilder("js", code, "bootstrap.mjs")
        .mimeType("application/javascript_module")
        .build()
    pprint.log("Ready...")
    val context = 
      Context.newBuilder("js")      
        .allowIO(true)
        .option("js.esm-eval-returns-exports", "true")
        .build()
    Using(context) { ctx =>
      pprint.log("Before eval")

      val exports = ctx.eval(elmCompilerSource)
      ctx.eval(source)
      //ctx.eval("js", elmCompilerJs)     
      //pprint.log(exports.getMember("worker"))
      //val CLI = ctx.eval("js", "export const cli = this.Elm.Morphir.Elm.CLI);console.log(cli);")
      pprint.log("After eval") 
      
      // val res0 = ctx.eval("js", "Elm.Morphir.Elm.CLI.init()");
      // pprint.pprintln((1,res0))
    }
  }
}