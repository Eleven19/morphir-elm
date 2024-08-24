import {Elm} from "./Morphir/Elm/CLI.elm"

export const Morphir = Elm.Morphir;
//Elm.Morphir.Elm.CLI.init();

export function worker() {
    Elm.Morphir.Elm.CLI.init();
}