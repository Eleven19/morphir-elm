import { magenta, print, type Props } from 'bluebun';
import { getCommandHelp } from '../../../utils/getCommandHelp';

interface RestoreProps extends Props {
    customElmRestore: string;
}

export default {
    name: 'elm',
    description: '🔄 Restore elm projects and dependencies',
    alias: ['fetch'],
    run: async (props: RestoreProps) => {
        await getCommandHelp(props);

    },
};