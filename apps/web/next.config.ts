import type { NextConfig } from "next";

const config: NextConfig = {
  async rewrites() {
    const api = process.env.API_URL ?? "http://127.0.0.1:8080";
    return [{ source: "/api/:path*", destination: `${api}/api/:path*` }];
  },
};

export default config;

