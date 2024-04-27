import { magenta, print, type Props } from 'bluebun';
import { getCommandHelp } from '../../../../utils/getCommandHelp';

interface RestoreProps extends Props {
    customElmRestore: string;
}

export default {
    name: 'help',
    description: '🔄 Print help for restore elm dependencies',
    alias: ['fetch'],
    run: async (props: RestoreProps) => {
        await getCommandHelp(props);
    },
};