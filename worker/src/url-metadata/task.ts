import { URL } from "url";
import { fetchPageHtml, getOpenGraphImage, getPageTitle } from "./fetch-page-html";
import { fetchPageIcon } from "./fetch-page-icon";

interface UrlMetadataInput {
	url: string;
}

interface UrlMetadataOutput {
	title?: string;
	icon?: string;
	banner?: string;
}

export async function urlMetadataTask(input: UrlMetadataInput): Promise<UrlMetadataOutput> {
	const url = new URL(input.url);
	const root = await fetchPageHtml(url);
	if (!root) throw Error("Unable to fetch page HTML");

	const title = getPageTitle(root);
	const banner = getOpenGraphImage(root);
	const icon = await fetchPageIcon(url, root).catch(() => undefined);

	// TODO: after fetching banner/icon, need to determine format & place in object storage
	// then, return the stored IDs

	return {
		title,
		banner,
		icon,
	};
}