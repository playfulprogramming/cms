import sharp from "sharp";
import * as stream from "stream";
import * as svgo from "svgo";
import { fetchAsBrowser } from "./fetch-page-html";
import path from "path";
import { exists, upload } from "src/s3";
import crypto from "crypto";

export async function imageToS3(
	url: URL,
	width: number,
	bucket: string,
	key: string,
	tags: Record<string, string>,
): Promise<string> {
	const request = await fetchAsBrowser(url);
	const body = request.body;
	if (!body) throw new Error(`Request body for ${url} is null`);

	const urlHash = crypto.createHash('md5').update(url.href).digest('hex');

	if (path.extname(url.pathname) === ".svg") {
		// If the image is an svg, optimize with svgo
		const svg = await request.text();
		const optimizedSvg = svgo.optimize(svg, { multipass: true }).data;
		const uploadKey = `${key}-${urlHash}.svg`;

		if (await exists(bucket, uploadKey)) {
			console.log(`Using existing object for ${uploadKey}`);
			return uploadKey;
		} else {
			await upload(bucket, uploadKey, tags, stream.Readable.from([optimizedSvg]));
			return uploadKey;
		}
	}

	const pipeline = sharp();
	const metadataStream = stream.Readable.fromWeb(body as never)
		.pipe(pipeline);

	const metadata = await pipeline.metadata();
	const extension = metadata.format;
	if (!extension) throw new Error(`Image format for ${url} could not be found.`);

	// rescale the image to [size]
	const transformer = sharp().resize(Math.min(width, metadata.width || width));
	const transformerStream = metadataStream.pipe(transformer);

	const uploadKey = `${key}-${urlHash}.${extension}`;
	if (await exists(bucket, uploadKey)) {
		console.log(`Using existing object for ${uploadKey}`);
		return uploadKey;
	} else {
		await upload(bucket, uploadKey, tags, transformerStream);
		return uploadKey;
	}
}