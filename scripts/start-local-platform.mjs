import { existsSync, mkdirSync, openSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { spawn } from "node:child_process";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("..", import.meta.url)));
const workDir = join(root, "work");
const pidDir = join(workDir, "pids");
const attachmentDir = join(workDir, "attachments");
const secret = process.env.JWT_SECRET || "replace-with-a-production-secret-at-least-32-bytes";

const services = [
  {
    name: "identity-service",
    command: "java",
    args: ["-jar", join(root, "services", "identity-service", "target", "identity-service-1.0.0.jar")],
    cwd: join(root, "services", "identity-service"),
    env: {
      SERVER_PORT: "8083",
      DB_URL: "jdbc:postgresql://127.0.0.1:5432/platform_identity",
      DB_USERNAME: "platform",
      DB_PASSWORD: process.env.POSTGRES_PASSWORD || "platform",
      REDIS_HOST: "127.0.0.1",
      REDIS_DATABASE: "0",
      NACOS_SERVER_ADDR: "127.0.0.1:8848",
      NACOS_ENABLED: "true",
      JWT_ISSUER: "identity-service",
      JWT_SECRET: secret,
      ADMIN_PASSWORD: process.env.ADMIN_PASSWORD || "Admin@123456",
      CORS_ALLOWED_ORIGINS: "http://127.0.0.1:8000,http://localhost:8000",
      SPRING_CLOUD_NACOS_CONFIG_IMPORT_CHECK_ENABLED: "false",
    },
  },
  {
    name: "issue-service",
    command: "java",
    args: ["-jar", join(root, "services", "issue-tracker", "target", "issue-tracker-1.0.0.jar")],
    cwd: join(root, "services", "issue-tracker"),
    env: {
      SERVER_PORT: "8082",
      DB_URL: "jdbc:postgresql://127.0.0.1:5432/platform_identity",
      DB_USERNAME: "platform",
      DB_PASSWORD: process.env.POSTGRES_PASSWORD || "platform",
      REDIS_HOST: "127.0.0.1",
      REDIS_DATABASE: "1",
      ELASTICSEARCH_URIS: "http://127.0.0.1:9200",
      NACOS_SERVER_ADDR: "127.0.0.1:8848",
      NACOS_ENABLED: "true",
      JWT_ISSUER: "identity-service",
      JWT_SECRET: secret,
      SPRING_FLYWAY_ENABLED: "false",
      ADMIN_PASSWORD: process.env.ADMIN_PASSWORD || "Admin@123456",
      CORS_ALLOWED_ORIGINS: "http://127.0.0.1:8000,http://localhost:8000",
      ATTACHMENT_STORAGE_ROOT: attachmentDir,
      SPRING_CLOUD_NACOS_CONFIG_IMPORT_CHECK_ENABLED: "false",
    },
  },
  {
    name: "gateway",
    command: process.execPath,
    args: [join(root, "scripts", "dev-gateway.mjs")],
    cwd: root,
    env: {
      PLATFORM_HOST: "127.0.0.1",
      PLATFORM_PORT: "8000",
      IDENTITY_SERVICE_PORT: "8083",
      ISSUE_SERVICE_PORT: "8082",
    },
  },
];

function cleanEnv(extra) {
  const env = {};
  const seen = new Set();
  for (const [key, value] of Object.entries(process.env)) {
    const lower = key.toLowerCase();
    if (lower === "path" || seen.has(lower)) {
      continue;
    }
    seen.add(lower);
    env[key] = value;
  }
  env.Path = process.env.Path || process.env.PATH || process.env.path || "";
  return { ...env, ...extra };
}

function pidFile(name) {
  return join(pidDir, `${name}.pid`);
}

function isRunning(pid) {
  if (!pid || Number.isNaN(pid)) {
    return false;
  }
  try {
    process.kill(pid, 0);
    return true;
  } catch {
    return false;
  }
}

function readPid(name) {
  try {
    return Number(readFileSync(pidFile(name), "utf8").trim());
  } catch {
    return undefined;
  }
}

function startService(service) {
  const existingPid = readPid(service.name);
  if (isRunning(existingPid)) {
    console.log(`${service.name} already running: ${existingPid}`);
    return;
  }

  const out = openSync(join(workDir, `${service.name}.out.log`), "a");
  const err = openSync(join(workDir, `${service.name}.err.log`), "a");
  const child = spawn(service.command, service.args, {
    cwd: service.cwd,
    detached: true,
    env: cleanEnv(service.env),
    stdio: ["ignore", out, err],
    windowsHide: true,
  });
  child.unref();
  writeFileSync(pidFile(service.name), `${child.pid}\n`);
  console.log(`${service.name} started: ${child.pid}`);
}

function stopService(service) {
  const pid = readPid(service.name);
  if (!isRunning(pid)) {
    console.log(`${service.name} not running`);
    rmSync(pidFile(service.name), { force: true });
    return;
  }
  process.kill(pid);
  console.log(`${service.name} stopped: ${pid}`);
  rmSync(pidFile(service.name), { force: true });
}

function statusService(service) {
  const pid = readPid(service.name);
  const status = isRunning(pid) ? `running (${pid})` : "stopped";
  console.log(`${service.name}: ${status}`);
}

function ensureDirs() {
  for (const dir of [workDir, pidDir, attachmentDir]) {
    mkdirSync(dir, { recursive: true });
  }
}

const command = process.argv[2] || "start";
ensureDirs();

if (command === "start") {
  for (const service of services) {
    startService(service);
  }
} else if (command === "stop") {
  for (const service of [...services].reverse()) {
    stopService(service);
  }
} else if (command === "status") {
  for (const service of services) {
    statusService(service);
  }
} else if (command === "logs") {
  for (const service of services) {
    console.log(`${service.name}:`);
    console.log(`  stdout ${join(workDir, `${service.name}.out.log`)}`);
    console.log(`  stderr ${join(workDir, `${service.name}.err.log`)}`);
  }
} else {
  console.error(`Usage: node ${process.argv[1]} [start|stop|status|logs]`);
  process.exitCode = 2;
}
