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

const BUCKET = await createBucket(process.env.S3_BUCKET);

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

	const iconPromise = fetchPageIcon(url, root)
		.then(url => imageToS3(url, 24, BUCKET, `remote-icon-${key}`, { from: "url-metadata/icon", origin: url.origin }))
		.catch(handleError("fetchPageIcon"));

	const bannerPromise = getOpenGraphImage(url, root)
		.then(url => imageToS3(url, 896, BUCKET, `remote-banner-${key}`, { from: "url-metadata/banner", origin: url.origin }))
		.catch(handleError("getOpenGraphImage"));

	const [icon, banner] = await Promise.all([iconPromise, bannerPromise]);

	return {
		title,
		banner,
		icon,
	};
}
