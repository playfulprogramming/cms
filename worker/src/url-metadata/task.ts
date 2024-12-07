import { URL } from "url";
import { fetchPageHtml, getOpenGraphImage, getPageTitle } from "./fetch-page-html";
import { fetchPageIcon } from "./fetch-page-icon";
import { imageToS3 } from "./image-to-s3";
import { createBucket } from "src/s3";
import crypto from "crypto";

interface UrlMetadataInput {
	url: string;
}

interface UrlMetadataOutput {
	title?: string;
	icon?: string;
	banner?: string;
}

export const BUCKET_REMOTE_ICONS = await createBucket("remote-icons");
export const BUCKET_REMOTE_BANNERS = await createBucket("remote-banners");

function handleError(name: string): (e: Error) => undefined {
	return (e) => {
		console.error(`Error in ${name}:`, e);
		return undefined;
	};
}

export async function urlMetadataTask(input: UrlMetadataInput): Promise<UrlMetadataOutput> {
	const url = new URL(input.url);
	const root = await fetchPageHtml(url);
	if (!root) throw Error("Unable to fetch page HTML");

	const key = crypto.createHash('md5').update(input.url).digest('hex');

	const title = getPageTitle(root);

	const icon = await fetchPageIcon(url, root)
		.then(url => imageToS3(url, 24, BUCKET_REMOTE_ICONS, key))
		.catch(handleError("fetchPageIcon"));

	const banner = await getOpenGraphImage(url, root)
		.then(url => imageToS3(url, 896, BUCKET_REMOTE_BANNERS, key))
		.catch(handleError("getOpenGraphImage"));

	return {
		title,
		banner,
		icon,
	};
}
