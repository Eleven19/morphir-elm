package mill.local.elm
import upickle.default.{ReadWriter => RW, macroRW}
import upickle.implicits.key

@key("type")
sealed trait ElmProject extends Product with Serializable { self =>
    final def projectType:String = self match {
        case _:ElmProject.ElmPackage => "package"
        case _:ElmProject.ElmApplication => "application"
    }

    def sourceDirectories:Seq[String]
}

object ElmProject {
    implicit val rw:RW[ElmProject] =  RW.merge(ElmPackage.rw, ElmApplication.rw)
    implicit val readWriter:ElmPickler.ReadWriter[ElmProject] = 
        ElmPickler.ReadWriter.merge(ElmPackage.readWriter, ElmApplication.readWriter)

    @key("package")
    final case class ElmPackage() extends ElmProject {
        def sourceDirectories: Seq[String] = Seq("src")
    }
    object ElmPackage {
        implicit val rw:RW[ElmPackage] = macroRW
        implicit val readWriter:ElmPickler.ReadWriter[ElmPackage] = ElmPickler.macroRW
    }
    
    @key("application")
    final case class ElmApplication(@key("source-directories")sourceDirectories:Seq[String]) extends ElmProject
    object ElmApplication {
        implicit val rw:RW[ElmApplication] = macroRW
        implicit val readWriter:ElmPickler.ReadWriter[ElmApplication] = ElmPickler.macroRW
    }
}

sealed abstract class ElmProjectType extends Product with Serializable { self =>
    final def productType:String = self match {
        case _:ElmProjectType.Application => "application"
        case _:ElmProjectType.Package => "package"
    }
}

object ElmProjectType {
    implicit val rw:RW[ElmProjectType] = RW.merge(Application.rw, Package.rw)

    type Application = Application.type 
    @key("application")
    case object Application extends ElmProjectType {
        implicit val rw:RW[Application.type] = macroRW
    }

    type Package = Package.type
    @key("library")
    case object Package extends ElmProjectType{
        implicit val rw:RW[Package.type] = macroRW
    }

    def fromString(s:String):ElmProjectType = s match {
        case "application" => Application
        case "package" => Package
        case _ => throw new IllegalArgumentException(s"Unknown ElmProjectType: $s")
    }
}

object ElmPickler extends upickle.AttributeTagged {
    override def tagName:String = "type"
//     def camelToKebab(s: String) = {
//     s.replaceAll("([A-Z])","#$1").split('#').map(_.toLowerCase).mkString("-")
//   }

//   def kebabToCamel(s: String) = {
//     val res = s.split("-", -1).map(x => s"${x(0).toUpper}${x.drop(1)}").mkString
//     s"${s(0).toLower}${res.drop(1)}"
//   }

//   override def objectAttributeKeyReadMap(s: CharSequence) =
//     kebabToCamel(s.toString)
//   override def objectAttributeKeyWriteMap(s: CharSequence) =
//     camelToKebab(s.toString)

//   override def objectTypeKeyReadMap(s: CharSequence) =
//     kebabToCamel(s.toString)
//   override def objectTypeKeyWriteMap(s: CharSequence) =
//     camelToKebab(s.toString)
}