package org.finos.morphir.cli.command
import caseapp.*
import org.finos.morphir.cli.given 

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
    pprint.pprintln("Running make...")
    pprint.pprintln(options)

    pprint.pprintln("Resources...")
    val cls = classOf[MakeOptions]
    val elmCompilerJs = os.read( os.resource / "js"/"morphir-elm-compiler.js") //os.read(os.resource(cls) / "morphir-elm-compiler.js")
    pprint.pprintln(elmCompilerJs)
  }
}