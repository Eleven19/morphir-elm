#!/usr/bin/env bun

// Start bluebun to run the correct CLI command
require('bluebun').run({
    name: 'buildme',
    cliPath: __dirname,
});