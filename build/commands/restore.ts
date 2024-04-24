import { magenta, print, type Props } from 'bluebun';
import { getCommandHelp } from '../utils/getCommandHelp';

interface RestoreProps extends Props {
    noElm?: boolean;
    noModels?: boolean;
    modelsHome: string;
    customElmRestore: string;
}

export default {
    name: 'restore',
    description: '🔄 Restore packages and models',
    alias: ['update'],
    run: async (props: RestoreProps) => {
        const { modelsHome = Bun.env.MORPHIR_MODELS_HOME, customElmRestore = Bun.env.MORPHIR_CUSTOM_ELM_RESTORE } = props.options;
        console.log(magenta('Restoring packages and models...'));
        console.log('Models Home:', modelsHome);
        console.log('Custom Elm Restore:', customElmRestore);
        console.log('No Elm:', props.options.noElm);
        console.log('No Models:', props.options.noModels);

        await getCommandHelp(props);
    },
};