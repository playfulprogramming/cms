import { Element, Root } from "hast";
import { fromHtml } from "hast-util-from-html";
import { find } from "unist-util-find";
import { isElement } from "./is-element";

export async function fetchAsBrowser(input: string | URL, init?: RequestInit) {
	const response = await fetch(input, {
		...init,
		headers: {
			"User-Agent":
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
			"Accept-Language": "en",
			...init?.headers,
		},
	});
	const isSuccess = `${response.status}`.startsWith("2");
	if (!isSuccess)
		throw new Error(`Request ${input} returned an error: ${response.status}`);
	return response;
}

export async function fetchPageHtml(src: string | URL): Promise<Root | null> {
	const srcHTML = await fetchAsBrowser(src).then((r) => r.text());

	const srcHast = fromHtml(srcHTML);
	return srcHast;
}

export function getPageTitle(srcHast: Root) {
	// find <title> element in response HTML
	const titleEl = find<Element>(srcHast, { tagName: "title" });
	const titleContentEl = titleEl && titleEl.children[0];
	const title =
		titleContentEl?.type === "text" ? titleContentEl.value : undefined;

	return title?.trim();
}

export async function getOpenGraphImage(src: URL, srcHast: Root): Promise<URL> {
	const metaNames = ["twitter:image", "og:image"];
	const metaNode = find<Element>(
		srcHast,
		(e) =>
			isElement(e) &&
			e.tagName === "meta" &&
			metaNames.includes(e.properties.property + ""),
	);
	if (!metaNode) throw Error("No image metadata found");

	return new URL(metaNode.properties.content + "", src);
}

export function escapeHtml(unsafe: string) {
	return unsafe
		.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g, "&gt;")
		.replace(/"/g, "&quot;")
		.replace(/'/g, "&#039;");
}
