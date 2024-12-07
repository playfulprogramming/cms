namespace NodeJS {
	interface ProcessEnv {
		POSTGRES_USER: string;
		POSTGRES_PASSWORD: string;
		POSTGRES_URL: string;

		S3_ENDPOINT: string;
		S3_KEY_ID: string;
		S3_KEY_SECRET: string;
	}
}
