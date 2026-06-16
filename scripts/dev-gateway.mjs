import { createReadStream } from "node:fs";
import { stat } from "node:fs/promises";
import { createServer, request as httpRequest } from "node:http";
import { extname, join, normalize, relative, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("..", import.meta.url)));
const issueRoot = join(root, "apps", "issue-tracker-web", "dist");
const identityRoot = join(root, "apps", "identity-web", "dist");
const host = process.env.PLATFORM_HOST || "127.0.0.1";
const port = Number(process.env.PLATFORM_PORT || 8000);
const identityPort = Number(process.env.IDENTITY_SERVICE_PORT || 8083);
const issuePort = Number(process.env.ISSUE_SERVICE_PORT || 8082);

const contentTypes = new Map([
  [".css", "text/css; charset=utf-8"],
  [".html", "text/html; charset=utf-8"],
  [".ico", "image/x-icon"],
  [".js", "text/javascript; charset=utf-8"],
  [".json", "application/json; charset=utf-8"],
  [".png", "image/png"],
  [".svg", "image/svg+xml"],
  [".woff", "font/woff"],
  [".woff2", "font/woff2"],
]);

function proxy(request, response, targetPort, targetPath) {
  const upstream = httpRequest({
    hostname: "127.0.0.1",
    port: targetPort,
    method: request.method,
    path: targetPath,
    headers: {
      ...request.headers,
      host: `127.0.0.1:${targetPort}`,
      "x-forwarded-host": request.headers.host || "",
      "x-forwarded-proto": "http",
    },
  }, (upstreamResponse) => {
    response.writeHead(upstreamResponse.statusCode || 502, upstreamResponse.headers);
    upstreamResponse.pipe(response);
  });
  upstream.on("error", (error) => {
    response.writeHead(502, { "Content-Type": "application/json; charset=utf-8" });
    response.end(JSON.stringify({ message: `Upstream unavailable: ${error.message}` }));
  });
  request.pipe(upstream);
}

function isInside(base, filePath) {
  const child = relative(base, filePath);
  return child === "" || (!child.startsWith("..") && !child.includes(`..${sep}`));
}

async function serveStatic(response, base, pathname, fallbackToIndex) {
  const relativePath = pathname === "/" ? "index.html" : pathname.replace(/^\/+/, "");
  let filePath = normalize(join(base, relativePath));
  if (!isInside(base, filePath)) {
    response.writeHead(403);
    response.end("Forbidden");
    return;
  }

  try {
    const fileStat = await stat(filePath);
    if (!fileStat.isFile()) {
      throw new Error("Not a file");
    }
  } catch {
    if (!fallbackToIndex) {
      response.writeHead(404);
      response.end("Not found");
      return;
    }
    filePath = join(base, "index.html");
  }

  response.writeHead(200, {
    "Content-Type": contentTypes.get(extname(filePath)) || "application/octet-stream",
    "Cache-Control": "no-store",
  });
  createReadStream(filePath).pipe(response);
}

function isIdentityPath(pathname) {
  return pathname.startsWith("/api/auth/")
    || pathname.startsWith("/api/navigation/")
    || pathname === "/api/admin/users"
    || pathname.startsWith("/api/admin/users/")
    || pathname === "/api/admin/roles"
    || pathname.startsWith("/api/admin/identity/")
    || pathname.startsWith("/api/users/");
}

function isIdentityWebPath(pathname) {
  return pathname === "/login"
    || pathname === "/register"
    || pathname === "/admin/identity"
    || pathname.startsWith("/admin/identity/")
    || pathname === "/admin/users"
    || pathname.startsWith("/admin/users/")
    || pathname.startsWith("/identity/");
}

createServer(async (request, response) => {
  const url = new URL(request.url || "/", `http://${request.headers.host || `${host}:${port}`}`);
  if (isIdentityPath(url.pathname)) {
    proxy(request, response, identityPort, `${url.pathname}${url.search}`);
    return;
  }
  if (url.pathname.startsWith("/api/")) {
    proxy(request, response, issuePort, `${url.pathname}${url.search}`);
    return;
  }
  if (url.pathname === "/identity") {
    response.writeHead(302, { Location: "/admin/identity/organizations" });
    response.end();
    return;
  }
  if (isIdentityWebPath(url.pathname)) {
    const staticPath = url.pathname.startsWith("/identity/")
      ? url.pathname.slice("/identity".length)
      : url.pathname;
    await serveStatic(response, identityRoot, staticPath, true);
    return;
  }
  await serveStatic(response, issueRoot, url.pathname, true);
}).listen(port, host, () => {
  console.log(`Unified platform dev gateway: http://${host}:${port}`);
});
