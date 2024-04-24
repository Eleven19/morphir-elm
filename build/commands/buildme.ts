import { print, type Props, red } from 'bluebun';
import { getCommandHelp } from '../utils/getCommandHelp';

export default {
    name: 'buildme',
    description: '',
    run: async (props: Props) => {
        if (props.first) {
            print(red(`Unknown command: ${props.first}`));
        }
        await getCommandHelp(props);
    },
};