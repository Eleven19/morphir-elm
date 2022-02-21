module Morphir.IR.Repo exposing (..)

import Dict exposing (Dict)
import Morphir.Dependency.DAG as DAG exposing (DAG)
import Morphir.Elm.ModuleName exposing (toIRModuleName)
import Morphir.Elm.ParsedModule as ParsedModule exposing (ParsedModule)
import Morphir.File.FileChanges exposing (FileChanges)
import Morphir.IR.Distribution exposing (Distribution(..))
import Morphir.IR.FQName exposing (FQName)
import Morphir.IR.Module as Module exposing (ModuleName)
import Morphir.IR.Name exposing (Name)
import Morphir.IR.Package as Package exposing (PackageName)
import Morphir.IR.Type as Type exposing (Type)
import Morphir.Value.Native as Native
import Set exposing (Set)


type alias Repo =
    { packageName : PackageName
    , dependencies : Dict PackageName (Package.Definition () (Type ()))
    , modules : Dict ModuleName (Module.Definition () (Type ()))
    , nativeFunctions : Dict FQName Native.Function
    , moduleDependencies : DAG ModuleName
    }


type alias SourceCode =
    String


type alias Errors =
    List Error


type Error
    = ModuleNotFound ModuleName
    | ModuleHasDependents ModuleName (Set ModuleName)


{-| Creates a repo from scratch when there is no existing IR.
-}
empty : PackageName -> Repo
empty packageName =
    { packageName = packageName
    , dependencies = Dict.empty
    , modules = Dict.empty
    , nativeFunctions = Dict.empty
    , moduleDependencies = DAG.empty
    }


{-| Creates a repo from an existing IR.
-}
fromDistribution : Distribution -> Result Errors Repo
fromDistribution distro =
    case distro of
        Library packageName _ packageDef ->
            packageDef.modules
                |> Dict.toList
                |> List.foldl
                    (\( moduleName, accessControlledModuleDef ) repoResultSoFar ->
                        repoResultSoFar
                            |> Result.andThen
                                (\repoSoFar ->
                                    repoSoFar
                                        |> insertModule moduleName accessControlledModuleDef.value
                                )
                    )
                    (Ok (empty packageName))


{-| Adds native functions to the repo. For now this will be mainly used to add `SDK.nativeFunctions`.
-}
mergeNativeFunctions : Dict FQName Native.Function -> Repo -> Result Errors Repo
mergeNativeFunctions newNativeFunction repo =
    Ok
        { repo
            | nativeFunctions =
                repo.nativeFunctions
                    |> Dict.union newNativeFunction
        }


{-| Apply all file changes to the repo in one step.
-}
applyFileChanges : FileChanges -> Repo -> Result Errors Repo
applyFileChanges fileChanges repo =
    parseNewElmModules fileChanges
        |> Result.andThen orderElmModulesByDependency
        |> Result.andThen
            (\parsedModules ->
                parsedModules
                    |> List.foldl
                        (\parsedModule repoResultForModule ->
                            let
                                moduleName : ModuleName
                                moduleName =
                                    ParsedModule.moduleName parsedModule
                                        |> toIRModuleName repo.packageName

                                typeNames : List Name
                                typeNames =
                                    extractTypeNames parsedModule
                            in
                            extractTypes parsedModule typeNames
                                |> Result.map orderTypesByDependency
                                |> Result.andThen
                                    (\types ->
                                        types
                                            |> List.foldl
                                                (\( typeName, typeDef ) repoResultForType ->
                                                    repoResultForType
                                                        |> Result.andThen (insertType moduleName typeName typeDef)
                                                )
                                                repoResultForModule
                                    )
                        )
                        (Ok repo)
            )


parseNewElmModules : FileChanges -> Result Errors (List ParsedModule)
parseNewElmModules fileChanges =
    Debug.todo "implement"


orderElmModulesByDependency : List ParsedModule -> Result Errors (List ParsedModule)
orderElmModulesByDependency parsedModules =
    Debug.todo "implement"


extractTypeNames : ParsedModule -> List Name
extractTypeNames parsedModule =
    Debug.todo "implement"


extractTypes : ParsedModule -> List Name -> Result Errors (List ( Name, Type.Definition () ))
extractTypes parsedModule typeNames =
    Debug.todo "implement"


orderTypesByDependency : List ( Name, Type.Definition () ) -> List ( Name, Type.Definition () )
orderTypesByDependency =
    Debug.todo "implement"


extractValueSignatures : ParsedModule -> List ( Name, Type () )
extractValueSignatures parsedModule =
    Debug.todo "implement"


{-| Insert or update a single module in the repo passing the source code in.
-}
mergeModuleSource : ModuleName -> SourceCode -> Repo -> Result Errors Repo
mergeModuleSource moduleName sourceCode repo =
    Debug.todo "implement"


{-| Insert a module if it's not in the repo yet.
-}
insertModule : ModuleName -> Module.Definition () (Type ()) -> Repo -> Result Errors Repo
insertModule moduleName moduleDef repo =
    -- TODO: add validation
    Ok
        { repo
            | modules =
                repo.modules
                    |> Dict.insert moduleName moduleDef
        }


deleteModule : ModuleName -> Repo -> Result Errors Repo
deleteModule moduleName repo =
    let
        validationErrors : Maybe Errors
        validationErrors =
            case repo.modules |> Dict.get moduleName of
                Nothing ->
                    Just [ ModuleNotFound moduleName ]

                Just _ ->
                    let
                        dependentModules =
                            repo.moduleDependencies |> DAG.incomingEdges moduleName
                    in
                    if Set.isEmpty dependentModules then
                        Nothing

                    else
                        Just [ ModuleHasDependents moduleName dependentModules ]
    in
    validationErrors
        |> Maybe.map Err
        |> Maybe.withDefault
            (Ok
                { repo
                    | modules =
                        repo.modules
                            |> Dict.remove moduleName
                    , moduleDependencies =
                        repo.moduleDependencies
                            |> Dict.remove moduleName
                }
            )


insertType : ModuleName -> Name -> Type.Definition () -> Repo -> Result Errors Repo
insertType moduleName typeName typeDef repo =
    Debug.todo "implement"