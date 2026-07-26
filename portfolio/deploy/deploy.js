/**
 * Deploy frontend + backend together in one command.
 *
 * Naming on the server:
 *   Frontend: zip local "dist" → upload → unzip to /var/www/frontend-portfolio/dist
 *   Backend:  backend-1.0.jar → /var/www/backend-portfolio/portfolio.jar
 *
 * Run from this folder (portfolio/deploy):
 *   node deploy.js                 # build both in parallel, then deploy both
 *   npm run deploy                 # same
 *   node deploy.js --frontend      # frontend only
 *   node deploy.js --backend       # backend only
 *   node deploy.js --skip-build    # upload existing builds only
 */

const { spawn } = require("child_process");
const fs = require("fs");
const path = require("path");

// This file lives in: portfolio/deploy
const DEPLOY_DIR = __dirname;
const FRONTEND_DIR = path.resolve(DEPLOY_DIR, "..");
const REPO_ROOT = path.resolve(DEPLOY_DIR, "..", "..");

const CONFIG = {
  host: process.env.DEPLOY_HOST || "161.118.178.60",
  user: process.env.DEPLOY_USER || "ubuntu",
  keyPath:
    process.env.DEPLOY_KEY ||
    path.join(DEPLOY_DIR, "ssh-key-2026-04-03 (1).key"),
  remoteFrontendDir: process.env.DEPLOY_FRONTEND_DIR || "/var/www/frontend-portfolio",
  // Deployed frontend lives at: /var/www/frontend-portfolio/dist
  remoteFrontendBuildFolder: "dist",
  remoteBackendDir: process.env.DEPLOY_BACKEND_DIR || "/var/www/backend-portfolio",
  // Deployed backend jar lives at: /var/www/backend-portfolio/portfolio.jar
  remoteBackendJar: "portfolio.jar",
  backendService: "backend-portfolio.service",
  springProfile: process.env.SPRING_PROFILES_ACTIVE || "prod",
  frontendLocalDir: FRONTEND_DIR,
  backendLocalDir: path.join(REPO_ROOT, "portfolio-backend", "backend"),
  localJarPattern: /^backend-.*\.jar$/,
};

const args = new Set(process.argv.slice(2));
const skipBuild = args.has("--skip-build");
const onlyFrontend = args.has("--frontend");
const onlyBackend = args.has("--backend");
const doFrontend = !onlyBackend;
const doBackend = !onlyFrontend;

function log(step, msg) {
  console.log(`[${step}] ${msg}`);
}

function fail(msg) {
  console.error(`\nERROR: ${msg}`);
  process.exit(1);
}

function runAsync(command, commandArgs, options = {}) {
  return new Promise((resolve, reject) => {
    log("run", `${command} ${commandArgs.join(" ")}`);
    const child = spawn(command, commandArgs, {
      stdio: "inherit",
      shell: options.shell ?? false,
      cwd: options.cwd,
      env: process.env,
    });
    child.on("error", reject);
    child.on("close", (code) => {
      if (code === 0) resolve();
      else reject(new Error(`Command failed (${code}): ${command} ${commandArgs.join(" ")}`));
    });
  });
}

function run(command, commandArgs, options = {}) {
  return runAsync(command, commandArgs, options);
}

function ssh(remoteCommand) {
  return run("ssh", [
    "-i",
    CONFIG.keyPath,
    "-o",
    "StrictHostKeyChecking=accept-new",
    `${CONFIG.user}@${CONFIG.host}`,
    remoteCommand,
  ]);
}

function scp(localPath, remotePath) {
  return run("scp", [
    "-i",
    CONFIG.keyPath,
    "-o",
    "StrictHostKeyChecking=accept-new",
    "-r",
    localPath,
    `${CONFIG.user}@${CONFIG.host}:${remotePath}`,
  ]);
}

async function assertKey() {
  if (!fs.existsSync(CONFIG.keyPath)) {
    fail(`SSH key not found: ${CONFIG.keyPath}`);
  }

  // OpenSSH rejects keys that are readable by other users (common on Windows).
  if (process.platform === "win32") {
    const user = process.env.USERNAME || process.env.USER;
    if (user) {
      log("ssh", "Restricting SSH key permissions for OpenSSH...");
      await run("icacls", [CONFIG.keyPath, "/inheritance:r"]);
      await run("icacls", [CONFIG.keyPath, "/grant:r", `${user}:(R)`]);
    }
  }
}

function findBuiltJar() {
  const targetDir = path.join(CONFIG.backendLocalDir, "target");
  if (!fs.existsSync(targetDir)) {
    fail(`Backend target folder missing: ${targetDir}. Build the backend first.`);
  }

  const jars = fs
    .readdirSync(targetDir)
    .filter(
      (name) =>
        CONFIG.localJarPattern.test(name) &&
        !name.endsWith(".jar.original") &&
        !name.includes("sources") &&
        !name.includes("javadoc")
    )
    .map((name) => ({
      name,
      full: path.join(targetDir, name),
      size: fs.statSync(path.join(targetDir, name)).size,
    }))
    .sort((a, b) => b.size - a.size);

  if (!jars.length) {
    fail(`No Spring Boot jar found in ${targetDir}`);
  }

  return jars[0].full;
}

async function buildFrontend() {
  log("build", "Building Vue frontend (npm run build)...");
  const npmCmd = process.platform === "win32" ? "npm.cmd" : "npm";
  await run(npmCmd, ["run", "build"], {
    cwd: CONFIG.frontendLocalDir,
    shell: true,
  });
  log("build", "Frontend build complete.");
}

async function buildBackend() {
  log("build", "Building Spring Boot backend...");
  const mvnw =
    process.platform === "win32"
      ? path.join(CONFIG.backendLocalDir, "mvnw.cmd")
      : path.join(CONFIG.backendLocalDir, "mvnw");
  await run(mvnw, ["-DskipTests", "clean", "package"], {
    cwd: CONFIG.backendLocalDir,
    shell: true,
  });
  log("build", "Backend build complete.");
}

async function buildBothInParallel() {
  const jobs = [];
  if (doFrontend) jobs.push(buildFrontend());
  if (doBackend) jobs.push(buildBackend());

  if (!jobs.length) return;

  log("build", `Starting ${jobs.length} build(s) in parallel...`);
  await Promise.all(jobs);
  log("build", "All builds finished.");
}

async function deployFrontend() {
  // Zip local "dist" folder → upload → unzip into remote "dist"
  const folderName = CONFIG.remoteFrontendBuildFolder; // "dist"
  const buildDir = path.join(CONFIG.frontendLocalDir, folderName);
  if (!fs.existsSync(buildDir)) {
    fail(`Frontend build folder missing: ${buildDir}`);
  }

  const stamp = new Date().toISOString().replace(/[:.]/g, "-");
  const localZip = path.join(
    CONFIG.frontendLocalDir,
    `${folderName}-deploy-${stamp}.zip`
  );
  const remoteZip = `${CONFIG.remoteFrontendDir}/${folderName}-deploy-${stamp}.zip`;

  log("frontend", `Creating zip of "${folderName}" folder...`);
  if (fs.existsSync(localZip)) fs.unlinkSync(localZip);

  if (process.platform === "win32") {
    await run("powershell.exe", [
      "-NoProfile",
      "-Command",
      `Compress-Archive -Path '${buildDir}\\*' -DestinationPath '${localZip}' -Force`,
    ]);
  } else {
    await run("zip", ["-r", localZip, "."], { cwd: buildDir });
  }

  log("frontend", "Uploading zip...");
  await ssh(`mkdir -p ${CONFIG.remoteFrontendDir}`);
  await scp(localZip, remoteZip);

  log("frontend", `Deleting old "${folderName}", then unzipping as "${folderName}"...`);
  // Single remote script: avoid &&/|| precedence pitfalls with set -e
  await ssh(
    [
      "set -e",
      `cd ${CONFIG.remoteFrontendDir}`,
      // PowerShell zip → unzip often drops +x on dirs; restore so rm can delete
      `if [ -d ${folderName} ]; then chmod -R u+rwX ${folderName}; fi`,
      `rm -rf ${folderName}`,
      `mkdir -p ${folderName}`,
      // unzip exits 1 on warnings (e.g. Windows backslash paths)
      `set +e`,
      `unzip -qo '${remoteZip}' -d ${folderName}`,
      `ec=$?`,
      `set -e`,
      `test "$ec" -eq 0 -o "$ec" -eq 1`,
      // u+rwX for deploy user; a+rX so nginx/www-data can serve/traverse
      `chmod -R u+rwX,a+rX ${folderName}`,
      `rm -f '${remoteZip}'`,
      `rm -f ${folderName}-deploy-*.zip`,
      `echo Frontend deployed to ${CONFIG.remoteFrontendDir}/${folderName}`,
    ].join("; ")
  );

  fs.unlinkSync(localZip);
  log(
    "frontend",
    `Done. Live folder: ${CONFIG.remoteFrontendDir}/${folderName}`
  );
}

async function ensureProdSystemdUnit() {
  const profile = CONFIG.springProfile;
  const unitBody = `[Unit]
Description=Portfolio Spring Boot Backend (prod)
After=network.target

[Service]
User=ubuntu
WorkingDirectory=${CONFIG.remoteBackendDir}
Environment=SPRING_PROFILES_ACTIVE=${profile}
ExecStart=/usr/bin/java -Dspring.profiles.active=${profile} -jar ${CONFIG.remoteBackendDir}/${CONFIG.remoteBackendJar}
Restart=always
RestartSec=5
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
`;

  log("backend", `Ensuring systemd unit runs with profile=${profile}...`);
  // Write unit via base64 to avoid shell escaping issues
  const b64 = Buffer.from(unitBody, "utf8").toString("base64");
  await ssh(
    [
      `set -e`,
      `mkdir -p ${CONFIG.remoteBackendDir}`,
      `echo '${b64}' | base64 -d | sudo tee /etc/systemd/system/${CONFIG.backendService} > /dev/null`,
      `sudo systemctl daemon-reload`,
    ].join(" && ")
  );
}

async function deployBackend() {
  // backend-1.0.jar → portfolio.jar
  // Upload as portfolio.jar.new, then replace live portfolio.jar
  const jarPath = findBuiltJar();
  const jarName = CONFIG.remoteBackendJar; // "portfolio.jar"
  const remoteTmp = `${CONFIG.remoteBackendDir}/${jarName}.new`;
  const remoteLive = `${CONFIG.remoteBackendDir}/${jarName}`;
  const localRenamed = path.join(DEPLOY_DIR, jarName);

  await ensureProdSystemdUnit();

  // Rename locally so scp always uploads a file named portfolio.jar
  log(
    "backend",
    `Renaming ${path.basename(jarPath)} → ${jarName}`
  );
  fs.copyFileSync(jarPath, localRenamed);

  try {
    log("backend", `Uploading ${jarName}...`);
    await scp(localRenamed, remoteTmp);

    // Critical path only: stop → replace jar → start.
    // Do not put pgrep/verification in this chain — a failed check used to
    // abort after upload and leave portfolio.jar.new with no portfolio.jar.
    log("backend", `Stopping service, replacing with ${jarName}...`);
    await ssh(
      [
        `set -e`,
        `sudo systemctl stop ${CONFIG.backendService} || true`,
        // Overwrite in place (no backup cp — disk was filling with old jars)
        `mv -f '${remoteTmp}' '${remoteLive}'`,
        `test -f '${remoteLive}'`,
        `test ! -f '${remoteTmp}'`,
        `sudo systemctl start ${CONFIG.backendService}`,
      ].join(" && ")
    );

    log("backend", "Verifying service...");
    await ssh(
      [
        `sleep 3`,
        `systemctl is-active ${CONFIG.backendService}`,
        `echo 'Backend deployed as ${remoteLive} (spring.profiles.active=${CONFIG.springProfile})'`,
        `ls -la '${remoteLive}'`,
      ].join(" && ")
    );
  } finally {
    if (fs.existsSync(localRenamed)) fs.unlinkSync(localRenamed);
  }

  log("backend", `Done. Live jar: ${remoteLive}`);
}

async function deployBothInParallel() {
  const jobs = [];
  if (doFrontend) jobs.push(deployFrontend());
  if (doBackend) jobs.push(deployBackend());

  if (!jobs.length) return;

  log("deploy", `Uploading ${jobs.length} target(s) in parallel...`);
  await Promise.all(jobs);
  log("deploy", "All uploads finished.");
}

async function main() {
  console.log("=== Portfolio app deploy (frontend + backend) ===");
  console.log(`Target: ${CONFIG.user}@${CONFIG.host}`);
  console.log(`Key:    ${CONFIG.keyPath}`);
  console.log(`Frontend → ${CONFIG.remoteFrontendDir}/${CONFIG.remoteFrontendBuildFolder}`);
  console.log(`Backend  → ${CONFIG.remoteBackendDir}/${CONFIG.remoteBackendJar}`);
  console.log(
    `Scope:  frontend=${doFrontend} backend=${doBackend} skipBuild=${skipBuild}\n`
  );

  await assertKey();

  if (!skipBuild) {
    await buildBothInParallel();
  }

  await deployBothInParallel();

  console.log("\nDeploy finished successfully.");
  console.log("Site: https://praffulagarwal.co.in");
}

main().catch((err) => {
  console.error(`\nERROR: ${err.message}`);
  process.exit(1);
});
