import {
    calcWidestCommandName,
    type CommandTree,
    commandTree,
    cyan,
    gray,
    green,
    magenta,
    print,
    type Props,
    white,
} from 'bluebun';
import { type Choice } from 'prompts';

/**
 * The formatting from bluebun for the help command is not great.
 * I forked code from https://github.com/jamonholmgren/bluebun/blob/main/src/command-help.ts
 *
 * TODO: create helpers for better styling control for this & other features
 *
 * Some packages to checkout:
 * - https://github.com/wobsoriano/blipgloss
 * - https://github.com/wobsoriano/bun-promptx
 */
async function formatHelp(initialProps: Props & { notes?: string }) {
    const { name, commandPath, notes } = initialProps;

    const categoryToFilter = commandPath?.length ? commandPath[0] : undefined;

    const _tree = await commandTree(initialProps);
    const tree = categoryToFilter ? { [categoryToFilter]: _tree[categoryToFilter] } : _tree;

    const widest = calcWidestCommandName(tree) + 10;

    function generateHelp(cmdTree: CommandTree, prefix: string): string[] {
        return Object.keys(cmdTree).flatMap((key) => {
            const command = cmdTree[key];
            const lines: string[] = [];

            const fullName = `${prefix} ${command.name === name ? '' : command.name}`.trim();
            lines.push(`${cyan(fullName).padEnd(widest)} ${gray(command.description)}`);

            if (command.subcommands) {
                lines.push(...generateHelp(command.subcommands, `${fullName}`));
            }

            return lines;
        });
    }

    const helpLines = generateHelp(tree, name);

    // https://texteditor.com/multiline-text-art/
    const banner = `
███╗   ███╗ ██████╗ ██████╗ ██████╗ ██╗  ██╗██╗██████╗       ███████╗██╗     ███╗   ███╗
████╗ ████║██╔═══██╗██╔══██╗██╔══██╗██║  ██║██║██╔══██╗      ██╔════╝██║     ████╗ ████║
██╔████╔██║██║   ██║██████╔╝██████╔╝███████║██║██████╔╝█████╗█████╗  ██║     ██╔████╔██║
██║╚██╔╝██║██║   ██║██╔══██╗██╔═══╝ ██╔══██║██║██╔══██╗╚════╝██╔══╝  ██║     ██║╚██╔╝██║
██║ ╚═╝ ██║╚██████╔╝██║  ██║██║     ██║  ██║██║██║  ██║      ███████╗███████╗██║ ╚═╝ ██║
╚═╝     ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝     ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝      ╚══════╝╚══════╝╚═╝     ╚═╝
                        ██████╗ ██╗   ██╗██╗██╗     ██████╗
                        ██╔══██╗██║   ██║██║██║     ██╔══██╗
                        ██████╔╝██║   ██║██║██║     ██║  ██║
                        ██╔══██╗██║   ██║██║██║     ██║  ██║
                        ██████╔╝╚██████╔╝██║███████╗██████╔╝
                        ╚═════╝  ╚═════╝ ╚═╝╚══════╝╚═════╝
morphir-elm build
`.trim();

    const help = `
${green(banner)}

${magenta(`Usage: ${white(`${name} <command>`)}`)}

${magenta('Commands:')}
${helpLines.join('\n')}
${notes ? `\n${magenta('Notes:')} ${notes}\n` : ''}
`;

    return help;
}

export async function getCommandHelp(props: Props & { notes?: string }) {
    print(await formatHelp(props));
}

// This function is used to get the choices for subcommands of a command
export async function getSubCommandsChoices(initialProps: Props) {
    // Get the command tree for the initialProps
    const categoryToFilter = initialProps.commandPath?.length
        ? initialProps.commandPath[0]
        : undefined;
    const _tree = await commandTree(initialProps);
    const tree = categoryToFilter ? { [categoryToFilter]: _tree[categoryToFilter] } : _tree;

    // This function is used to generate a list of choices from a command tree
    function generateCommandList(cmdTree: CommandTree): Choice[] {
        // For each key in the command tree, generate a choice
        return Object.keys(cmdTree).flatMap((key) => {
            const command = cmdTree[key];
            const lines: Choice[] = [];

            // Add a choice for the command
            lines.push({
                title: `${command.name} (${command.description})`,
                value: command.name,
                selected: true,
            });

            // If the command has subcommands, recursively generate choices for the subcommands and add them to the list
            if (command.subcommands) {
                lines.push(...generateCommandList(command.subcommands));
            }

            // Return the list of choices
            return lines;
        });
    }

    // Return a list of choices for all subcommands
    return generateCommandList(tree).filter((command) => command.value !== categoryToFilter);
}
