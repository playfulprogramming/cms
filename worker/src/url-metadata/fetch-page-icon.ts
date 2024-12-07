import { Root, Element } from "hast";
import { find } from "unist-util-find";
import { getLargestManifestIcon } from "./get-largest-manifest-icon";
import * as path from "path";

export async function fetchPageIcon(src: URL, srcHast: Root): Promise<string> {
	// <link rel="manifest" href="/manifest.json">
	const manifestPath: Element | undefined = find(srcHast, {
		type: "element",
		tagName: "link",
		rel: "manifest",
	});

	let iconHref: string | undefined;

	if (manifestPath?.properties?.href) {
		// `/manifest.json`
		const manifestRelativeURL = String(manifestPath.properties.href);
		const fullManifestURL = new URL(manifestRelativeURL, src).href;

		const manifest = await fetch(fullManifestURL)
			.then((r) => r.status === 200 && r.json())
			.catch(() => null);

		if (manifest) {
			const largestIcon = getLargestManifestIcon(manifest);
			if (largestIcon?.icon)
				iconHref = new URL(largestIcon.icon.src, src.origin).href;
		}
	}

	if (!iconHref) {
		// fetch `favicon.ico`
		// <link rel="shortcut icon" type="image/png" href="https://example.com/img.png">
		for (const extension of [".svg", ".png", ".jpg", ".jpeg"]) {
			const favicon: Element | undefined = find(srcHast, (node) => {
				if (node.type !== "element" || (node as Element).tagName !== "link")
					return false;

				const rel = (node as Element).properties?.rel?.toString() ?? "";
				const href = (node as Element).properties?.href?.toString() ?? "";

				return rel.includes("icon") && path.extname(href) === extension;
			});

			if (favicon?.properties?.href) {
				iconHref = new URL(favicon.properties.href.toString(), src).href;
				break;
			}
		}
	}

	// no icon image URL is found
	if (!iconHref) throw Error("Could not find page icon");

	return iconHref;
}
