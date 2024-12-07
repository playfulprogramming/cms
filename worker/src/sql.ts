import postgres from "postgres";

export default postgres(process.env.POSTGRES_URL, {
	user: process.env.POSTGRES_USER,
	password: process.env.POSTGRES_PASSWORD,
});
