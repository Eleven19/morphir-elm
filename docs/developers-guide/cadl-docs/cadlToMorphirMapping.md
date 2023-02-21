# Mapping [Cadl feature concepts](https://microsoft.github.io/cadl/language-basics/overview) to [Morphir](https://package.elm-lang.org/packages/finos/morphir-elm/18.1.0/Morphir-IR-Type)

---

| <div style="width:100px"></div>                                                              | CADL Type <div style="width:450px"></div>                                                                                                                                                                                                                                                                                                                  | Morphir Type<div style="width: 350px"></div>                                | Comment <div style="width:350px"></div>                                                                                                                                                                                                                                              |
|----------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Namespaces](https://microsoft.github.io/cadl/language-basics/namespaces)                    | `namespace Petstore`                                                                                                                                                                                                                                                                                                                                       | `module PetStore exposing (...)`                                            | Namespaces in CADL map to [Modules](https://package.elm-lang.org/packages/Morgan-Stanley/morphir-elm/latest/Morphir-IR-Module) in Morphir                                                                                                                                            |      
| [Models](https://microsoft.github.io/cadl/language-basics/models)                            | `model Dog { name: string;  age: number}`                                                                                                                                                                                                                                                                                                                  | `type alias Dog = { name: string, age: int}`                                | Models in CADL map to [Records](https://package.elm-lang.org/packages/finos/morphir-elm/18.1.0/Morphir-IR-Value#record) in Morphir                                                                                                                                                   |
| [Enums](https://microsoft.github.io/cadl/language-basics/enums)                              | `enum Direction {East; West; North; South}`                                                                                                                                                                                                                                                                                                                | `type Direction `<br/> `= East` &#124; `West` &#124; `North` &#124; `South` | Enums in CADL map to [Union Types](https://package.elm-lang.org/packages/finos/morphir-elm/18.1.0/Morphir-IR-Type) in Mophir                                                                                                                                                         |
| [Union Type](https://package.elm-lang.org/packages/finos/morphir-elm/18.1.0/Morphir-IR-Type) |||
|                                                                                              | -<span style="color: grey; font-style: italic;" > Unnamed Union <br> `alias Breed = Breagle` &#124; `GermanShepherd` &#124; `GoldenRetriever` <br> <span style="color: grey; font-style: italic;" >- Named Union <br> `union Breed {`<br> &ensp; `beagle: Beagle,` <br> &ensp; `shepherd: GermanShepherd.` <br> &ensp; `retiever: GoldenRetriever`<br> `}` |`type Breed` <br> &ensp; `= Beagle Beagle`<br> &ensp; &ensp; &#124; `Shepherd GermanShepherd ` <br> &ensp; &ensp; &#124; `Retriever GoldenRetriever`| Named unions in CADL maps to a Custom Type with  a `type parameter` in Morphir. Any other detail of the type is captured in Morphir's `Decorators(Custom Attributes).` <br> <span style="color: red; font-style: italic;" >NB: unnamed Unions are currently not supported in morphir |


## [Type Relations](https://microsoft.github.io/cadl/language-basics/type-relations)

##### Boolean
Boolean in CADL, maps to [`bool`](https://package.elm-lang.org/packages/elm/core/latest/Basics#Bool), a `true` or `false` value in Morphir.

##### Integer
In Morphir, this maps to the type [`int`.](https://package.elm-lang.org/packages/elm/core/latest/Basics#Int) The `integer` type assignment is valid CADL, but <br/>
<span style="color: red; font-style:italic;"> Things to note :: </span>
1. When dealing with emitters such as OpenApiSpec(OAS) it defaults to an object. To obtain an actual int value, specify a subtype `int64`.

##### Float
The `float` type in CADL, maps directly to type [`float`](https://package.elm-lang.org/packages/elm/core/latest/Basics#Float) in Morphi. <span style="color: red; font-style:italic;">Same issues with `integer` type is applicable </span>

##### String
The `string` type, in CADL maps directly to [`string`](https://package.elm-lang.org/packages/finos/morphir-elm/18.1.0/Morphir-SDK-String) type in Morphir, a sequence of characters,

##### PlainDate
`PlainDate` type in CADL, maps to [`localDate`](https://package.elm-lang.org/packages/finos/morphir-elm/18.1.0/Morphir-SDK-LocalDate) type in morphir, defined as a gregorian date with no timezone information. 

##### PlainTime
The `PlainTime` in CADL map's to [`localTime`](https://package.elm-lang.org/packages/finos/morphir-elm/18.1.0/Morphir-SDK-LocalTime) type in morphir, defined as basic time without a timezone.