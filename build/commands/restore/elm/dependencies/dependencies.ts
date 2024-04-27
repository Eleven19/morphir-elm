import { magenta, print, type Props } from 'bluebun';
import { getCommandHelp } from '../../../../utils/getCommandHelp';

interface RestoreProps extends Props {
    customElmRestore: string;
}

export default {
    name: 'dependencies',
    description: '🔄 Restore elm dependencies',
    alias: ['fetch'],
    run: async (props: RestoreProps) => {
        print("TODO: Restore elm dependencies");
        console.log("Props", props);
    },
};