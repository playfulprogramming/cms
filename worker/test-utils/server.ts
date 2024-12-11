import { setupServer } from "msw/node";
import { HttpResponse, http } from "msw";

export const server = setupServer();

interface MockEndPointProps {
	path: string;
	body: string;
	type?: "get" | "post" | "put" | "delete";
	status?: number;
}

export function mockEndpoint({
	path,
	body,
	type = "get",
	status = 200,
}: MockEndPointProps) {
	server.use(
		http[type](path, () => {
			return new HttpResponse(body, { status });
		}),
	);
}
