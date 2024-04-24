import {print, type Props, red} from 'bluebun';
export default{
    name: 'build',
    description: "Build the code",
    run: async (props: Props) => {
        print(red("Running build command..."));
    }
};