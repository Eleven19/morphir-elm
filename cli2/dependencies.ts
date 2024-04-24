import * as util from "util";
import * as fs from "fs";
import { error } from "ajv/dist/vocabularies/applicator/dependencies";
const fsReadFile = util.promisify(fs.readFile);

export async function getDependecies(
  localDependencies: string[]
): Promise<any[]> {
  const loadedDependencies = localDependencies.map(async (dependencyPath) => {
    let protocol = getProtocol(dependencyPath);
    console.log("Protocol:", protocol);
    if (fs.existsSync(dependencyPath)) {
      const dependencyIR = (await fsReadFile(dependencyPath)).toString();
      return JSON.parse(dependencyIR);
    } else {
      throw new Error(`${dependencyPath} does not exist`);
    }
  });
  return Promise.all(loadedDependencies);
}

function getProtocol(path: string) {
  try {
    // If the path does not contain a : assume it is a file path
    if (!path.includes(":")) {
      return "file";
    }
    let url = new URL(path);
    return url.protocol;
  } catch (e) {
    if (error instanceof TypeError) {
      return "file";
    }
  }
}