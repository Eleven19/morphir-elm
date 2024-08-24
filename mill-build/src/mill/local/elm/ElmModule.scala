package mill.local.elm

import mill.local.CommandLine
import mill._
import mill.scalalib._
import mill.api.PathRef
import os.RelPath

trait ElmModule extends Module {
    def elmSources = T.sources{ elmSourcesFromProject() }

    def elmSourcesFromProject = T{ 
        val elmJsonFolder = elmJsonDir()
        elmProject()        
        .map(_.sourceDirectories.map{p => 
            val path = os.FilePath(p)
            val nioPath = path.toNIO
            if(nioPath.isAbsolute) {
                PathRef(os.Path(nioPath))
            } else {
                PathRef(elmJsonFolder / os.RelPath(nioPath))
            }
        }).getOrElse(Seq(PathRef(millSourcePath / "src"))) 
    }

    def allElmSourceFiles = T{ Lib.findSourceFiles(elmSources(), Seq("elm")).map(PathRef(_)) }
    def elmDocCommandLine:T[CommandLine] = T{     
        val outPath = elmDocOutPath()
        //def sourceFiles = allElmSourceFiles().map(_.path.toString) 
        CommandLine.Elm
            .withSubcommandArgs("make")              
            .withAfterArgs(s"--docs=$outPath")
            //.appendArgs(sourceFiles)
    }
    def elmDocOutDir = T{ T.ctx().dest }

    def elmDocOutPath = T{ elmDocOutDir() / "docs.json" }
    def elmDoc() = T.command {
        val outputPath = elmDocOutPath()
        val cmd = elmDocCommandLine().toSeq
        os.proc(cmd).call()
        outputPath
    }

    def elmJsonPath = T{ PathRef(millSourcePath / "elm.json") }
    final def elmJsonDir = T{ elmJsonPath().path / os.up }

    def elmProject: T[Option[ElmProject]] = T{        
        if(os.exists(elmJsonPath().path)) {
            val jsonString = os.read(elmJsonPath().path)
            val json = ujson.read(jsonString)
            val projectType = ElmProjectType.fromString(json("type").str)
            val project = projectType match {
                case ElmProjectType.Application => ElmPickler.read[ElmProject.ElmApplication](json)
                case ElmProjectType.Package => ElmPickler.read[ElmProject.ElmPackage](json)
            }
            Some(project)
        } else {
            None
        }
    }

    def elmEntryPoints:T[Option[Seq[PathRef]]] = None

    def elmMakeSourceFiles = T.sources{
        elmEntryPoints().getOrElse(allElmSourceFiles())
    }

    def targetDir = T{ T.ctx().dest }

    def elmMakeOutputFile = T.source{ targetDir() / "elm.js" }

    def elmMakeCommandLine = T{
        val elmJsonFolder = elmJsonDir()
        val sources = elmMakeSourceFiles().map(_.path.relativeTo(elmJsonFolder).toString)
        val output = elmMakeOutputFile().path.toString
        CommandLine.Elm
            .withSubcommandArgs("make")
            .appendArgs(sources)
            .appendAfterArgs(Seq("--output", output))
    }

    def elmMake = T {
        val cmd = elmMakeCommandLine()
        os.proc(cmd.toSeq).call(cwd = elmJsonDir())
        elmMakeOutputFile()
    }
    
    def compile = T {
        val outDir = T.ctx().dest
        val elmJs = elmMake()
        Seq(PathRef(outDir))
    }
}