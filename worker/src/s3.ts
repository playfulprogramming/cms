import { BucketAlreadyExists, BucketAlreadyOwnedByYou, CreateBucketCommand, GetObjectCommand, NoSuchKey, PutBucketPolicyCommand, S3Client } from "@aws-sdk/client-s3";
import { Upload } from "@aws-sdk/lib-storage";
import * as stream from "stream";

export const S3 = new S3Client({
	region: "auto",
	endpoint: process.env.S3_ENDPOINT,
	credentials: {
		accessKeyId: process.env.S3_KEY_ID,
		secretAccessKey: process.env.S3_KEY_SECRET,
	},
	forcePathStyle: true,
});

export async function createBucket(name: string): Promise<string> {
	// If we're running in production, assume that buckets were configured manually
	if (process.env.NODE_ENV === "production")
		return name;

	// Otherwise, set up buckets for local usage
	try {
		await S3.send(new CreateBucketCommand({
			Bucket: name,
		}));

		const publicBucketPolicy = {
			Version: "2012-10-17",
			Statement: [
				{
					Action: ["s3:GetObject"],
					Effect: "Allow",
					Principal: {"AWS": ["*"]},
					Resource: [`arn:aws:s3:::${name}/*`],
					Sid: ""
				}
			]
		};
		await S3.send(new PutBucketPolicyCommand({
			Bucket: name,
			Policy: JSON.stringify(publicBucketPolicy),
		}));

		console.log(`Created bucket '${name}'.`);
	} catch (e) {
		if (e instanceof BucketAlreadyExists || e instanceof BucketAlreadyOwnedByYou) {
			console.log(`Create bucket: '${name}' already exists.`)
		} else {
			console.error(`Error creating bucket '${name}':`, e);
			throw e;
		}
	}

	return name;
}

export async function exists(bucket: string, key: string): Promise<boolean> {
	try {
		const obj = await S3.send(new GetObjectCommand({
			Bucket: bucket,
			Key: key,
		}));
		return !!obj;
	} catch (e) {
		if (e instanceof NoSuchKey) {
			return false;
		} else {
			throw e;
		}
	}
}

export async function upload(bucket: string, key: string, tags: Record<string, string>, file: stream.Readable, contentType: string) {
	console.log(`Uploading ${bucket}/${key}`);
	const searchParams = new URLSearchParams(tags);

	const upload = new Upload({
		params: {
			Bucket: bucket,
			Key: key,
			Body: file,
			ContentType: contentType,
			Tagging: searchParams.toString(),
		},
		client: S3,
	});

	upload.on("httpUploadProgress", (progress) =>
		console.log("Upload", Number(progress.loaded) / Number(progress.total), "of", progress.Bucket, "/", progress.Key));

	await upload.done();
}
