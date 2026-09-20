import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "ForgeCloud",
  description: "Developer CI/CD and deployment platform",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}

