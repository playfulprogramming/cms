import { Root, Element } from "hast";
import { find } from "unist-util-find";
import { getLargestManifestIcon } from "./get-largest-manifest-icon";
import * as path from "path";
import { fetchAsBrowser } from "./fetch-page-html";
import { isElement } from "./is-element";

export async function fetchPageIcon(src: URL, srcHast: Root): Promise<URL> {
	// <link rel="manifest" href="/manifest.json">
	const manifestPath: Element | undefined = find(srcHast, (node) => {
		return isElement(node) && node.tagName === "link" && String(node.properties.rel).includes("manifest");
	});

	let iconHref: URL | undefined;

	if (manifestPath?.properties?.href) {
		console.log("Found manifest", manifestPath.properties.href, "for", src.href);

		// `/manifest.json`
		const manifestRelativeURL = String(manifestPath.properties.href);
		const fullManifestURL = new URL(manifestRelativeURL, src);

		const manifest = await fetchAsBrowser(fullManifestURL)
			.then((r) => r.json())
			.catch(() => null);

		if (manifest) {
			const largestIcon = getLargestManifestIcon(manifest);
			if (largestIcon?.icon) {
				console.log("Found iconHref from manifest.json:", largestIcon.icon.src);
				iconHref = new URL(largestIcon.icon.src, src.origin);
			}
		}
	}

	if (!iconHref) {
		// <link rel="shortcut icon" type="image/png" href="https://example.com/img.png">
		// <link rel="apple-touch-icon" sizes="96x96" href="/icons/icon-96x96.png?v=2a">
		const iconExtensions = [".svg", ".png", ".jpg", ".jpeg"];
		const favicon: Element | undefined = find(srcHast, (node) => {
			if (!isElement(node) || node.tagName !== "link")
				return false;

			const rel = (node as Element).properties?.rel?.toString() ?? "";
			const href = (node as Element).properties?.href?.toString() ?? "";
			let hrefUrl: URL|undefined;
			try {
				// Need to check the file extension through URL.pathname to omit query params
				hrefUrl = new URL(href, src);
			} catch (e) {
				// do nothing
			}

			const extname = path.extname(hrefUrl?.pathname ?? "");
			return rel.includes("icon") && iconExtensions.includes(extname);
		});

		if (favicon?.properties?.href) {
			console.log("Found iconHref from <link> tags:", favicon.properties.href);
			iconHref = new URL(favicon.properties.href.toString(), src);
		}
	}

	// no icon image URL is found
	if (!iconHref) throw Error("Could not find page icon");

	return iconHref;
}
