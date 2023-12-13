module Morphir.Snowpark.ReferenceUtils exposing (
    scalaPathToModule
    , isTypeReferenceToSimpleTypesRecord
    , isValueReferenceToSimpleTypesRecord
    , mapLiteral
    , scalaReferenceToUnionTypeCase
    , getCustomTypeParameterFieldAccess
    , getListTypeParameter
    , getFunctionInputTypes
    , mapLiteralToPlainLiteral
    , errorValueAndIssue
    , curryCall
    , getSourceTargetTypeIfFunctionType
    , getExpressionForColumnsObject
    , getInnerMaybeType
    , simplifyBooleanExpression )

import Morphir.IR.Name as Name
import Morphir.IR.FQName as FQName
import Morphir.IR.Literal exposing (Literal(..))
import Morphir.IR.Type as IrType
import Morphir.IR.Value as Value exposing (Value(..), TypedValue)
import Morphir.Scala.AST as Scala
import Morphir.Snowpark.Constants as Constants
import Morphir.Snowpark.MappingContext exposing (ValueMappingContext, MappingContextInfo, isRecordWithSimpleTypes, getLocalVariableIfDataFrameReference)
import Morphir.Snowpark.GenerationReport exposing (GenerationIssue)
import Maybe.Extra

processScalaPackageName : Name.Name -> String
processScalaPackageName name =
    (Name.toCamelCase >> String.toLower) name

scalaPathToModule : FQName.FQName -> Scala.Path
scalaPathToModule name =
    let
        packagePath = 
            FQName.getPackagePath name 
                |> List.map processScalaPackageName
        modulePath = case FQName.getModulePath name |> List.reverse of
                        (last::restInverted) -> ((Name.toTitleCase last) :: (List.map processScalaPackageName restInverted)) |> List.reverse
                        _ -> []
    in
        packagePath ++ modulePath

isValueReferenceToSimpleTypesRecord : (Value ta (IrType.Type a)) -> MappingContextInfo () -> Maybe (Scala.Path, Name.Name)
isValueReferenceToSimpleTypesRecord expression ctx =
    isTypeReferenceToSimpleTypesRecord (Value.valueAttribute expression) ctx

isTypeReferenceToSimpleTypesRecord : (IrType.Type a) -> MappingContextInfo () -> Maybe (Scala.Path, Name.Name)
isTypeReferenceToSimpleTypesRecord typeReference ctx =
    case typeReference of
        IrType.Reference _ typeName _ -> 
            if isRecordWithSimpleTypes typeName ctx then
                Just (scalaPathToModule typeName, (FQName.getLocalName typeName))
            else
                Nothing
        _ -> Nothing


mapLiteralToPlainLiteral : ta -> Literal -> Scala.Value
mapLiteralToPlainLiteral _ literal =
    case literal of
        CharLiteral val ->
            Scala.Literal (Scala.CharacterLit val)
        StringLiteral val ->                    
            Scala.Literal (Scala.StringLit val)
        BoolLiteral val ->
            Scala.Literal (Scala.BooleanLit val)
        WholeNumberLiteral val ->
            Scala.Literal (Scala.IntegerLit val)
        FloatLiteral val ->
            Scala.Literal (Scala.FloatLit val)
        _ ->
            Debug.todo "The type '_' is not implemented"

mapLiteral : ta -> Literal -> Scala.Value
mapLiteral t literal =
    Constants.applySnowparkFunc "lit" [mapLiteralToPlainLiteral t literal]


scalaReferenceToUnionTypeCase : FQName.FQName -> FQName.FQName -> Scala.Value
scalaReferenceToUnionTypeCase typeName constructorName =
    let
        nsName = scalaPathToModule constructorName
        containerObjectName = FQName.getLocalName typeName |> Name.toTitleCase
        containerObjectFieldName = FQName.getLocalName constructorName |> Name.toTitleCase
    in 
    Scala.Ref (nsName ++ [containerObjectName]) containerObjectFieldName

getCustomTypeParameterFieldAccess : Int -> String
getCustomTypeParameterFieldAccess paramIndex =
    "field" ++ (String.fromInt paramIndex)

getListTypeParameter : IrType.Type () -> Maybe (IrType.Type ())
getListTypeParameter tpe =
    case tpe of
        IrType.Reference _ ([ [ "morphir" ], [ "s", "d", "k" ] ], [ [ "list" ] ], [ "list" ]) [innertype] ->
            Just innertype
        _ ->
            Nothing

getFunctionInputTypes : IrType.Type () -> Maybe (List (IrType.Type ()))
getFunctionInputTypes tpe =
   case tpe of
       IrType.Function _ fromType toType ->
          let
            toTypes = getFunctionInputTypes toType
                        |> Maybe.withDefault []
          in
          Just (fromType::toTypes)
       _ -> Nothing


errorValueAndIssue : GenerationIssue -> (Scala.Value, List GenerationIssue)
errorValueAndIssue issue =
    (Scala.Literal (Scala.StringLit issue), [ issue ])

curryCall : (TypedValue, List (TypedValue)) -> TypedValue
curryCall (func, args) =
    let
        resolveOneArgumentOfType tpe =
            case tpe of
                IrType.Function _ _ returnType ->
                    returnType
                _ -> tpe
    in
    args
        |> List.foldl (\arg current  -> Value.Apply (resolveOneArgumentOfType (Value.valueAttribute current)) current arg) func

getSourceTargetTypeIfFunctionType : IrType.Type a -> Maybe (IrType.Type a, IrType.Type a) 
getSourceTargetTypeIfFunctionType tpe =
   case tpe of
       IrType.Function _ fromType toType ->
            Just (fromType, toType)
       _ ->
            Nothing


getExpressionForColumnsObject : IrType.Type () -> ValueMappingContext -> Maybe Scala.Value
getExpressionForColumnsObject tpe ctx =
    let
        fullNameExpr = 
            isTypeReferenceToSimpleTypesRecord tpe ctx.typesContextInfo
                |> Maybe.map (\(path, name) -> Scala.Ref path (Name.toTitleCase name))
        localVariableExpr =
            getLocalVariableIfDataFrameReference tpe ctx
                |> Maybe.map (\name -> Scala.Variable name)
    in
    Maybe.Extra.orElse fullNameExpr localVariableExpr

getInnerMaybeType : IrType.Type ()  -> Maybe (IrType.Type ())
getInnerMaybeType tpe  =
   case tpe of
      IrType.Reference _ ([["morphir"],["s","d","k"]],[["maybe"]],["maybe"]) [typeArg] ->
         Just typeArg
      _ -> 
         Nothing


simplifyBooleanExpression : Scala.Value -> Scala.Value
simplifyBooleanExpression exp =
    case exp of
        Scala.BinOp left "&&" right ->
            let
                simplLeft =
                    simplifyBooleanExpression left
                simplRight =
                    simplifyBooleanExpression right
            in
            case (simplLeft, simplRight) of
                (Scala.Apply (Scala.Ref _ "lit") [ Scala.ArgValue Nothing (Scala.Literal (Scala.BooleanLit True)) ], result) ->
                    result
                (result, Scala.Apply (Scala.Ref _ "lit") [ Scala.ArgValue Nothing (Scala.Literal (Scala.BooleanLit True)) ]) ->
                    result
                (Scala.Apply (Scala.Ref _ "lit") [ Scala.ArgValue Nothing (Scala.Literal (Scala.BooleanLit False)) ], _) ->
                    simplLeft
                (_, Scala.Apply (Scala.Ref _ "lit") [ Scala.ArgValue Nothing (Scala.Literal (Scala.BooleanLit False)) ]) ->
                    simplRight
                _ ->
                    exp
        Scala.BinOp left "||" right ->
            let
                simplLeft =
                    simplifyBooleanExpression left
                simplRight =
                    simplifyBooleanExpression right
            in
            case (simplLeft, simplRight) of
                (Scala.Apply (Scala.Ref _ "lit") [ Scala.ArgValue Nothing (Scala.Literal (Scala.BooleanLit True)) ], _) ->
                    simplLeft
                (_, Scala.Apply (Scala.Ref _ "lit") [ Scala.ArgValue Nothing (Scala.Literal (Scala.BooleanLit True)) ]) ->
                    simplRight
                (Scala.Apply (Scala.Ref _ "lit") [ Scala.ArgValue Nothing (Scala.Literal (Scala.BooleanLit False)) ], result) ->
                    result
                (result, Scala.Apply (Scala.Ref _ "lit") [ Scala.ArgValue Nothing (Scala.Literal (Scala.BooleanLit False)) ]) ->
                    result
                _ ->
                    exp
        Scala.UnOp "!" (Scala.Literal (Scala.BooleanLit v)) ->
            Scala.UnOp "!" (Scala.Literal (Scala.BooleanLit (not v)))
        _ ->
            exp
