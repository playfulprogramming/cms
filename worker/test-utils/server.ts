import { setupServer } from "msw/node";
import { HttpResponse, http } from "msw";
import { HttpResponseResolver } from "msw/src/core/http";

export const server = setupServer();

interface MockEndPointBufferProps {
	path: string;
	body: ArrayBuffer;
	method?: "get" | "post" | "put" | "delete";
	status?: number;
	bodyType: "buffer";
}

interface MockEndPointTextProps {
	path: string;
	body: string;
	method?: "get" | "post" | "put" | "delete";
	status?: number;
	bodyType: "text";
}

interface MockEndPointJsonProps {
	path: string;
	body: object;
	method?: "get" | "post" | "put" | "delete";
	status?: number;
	bodyType: "json";
}

interface MockEndPointFnProps {
	path: string;
	body: HttpResponseResolver;
	method?: "get" | "post" | "put" | "delete";
	status?: number;
	bodyType: "fn";
}

type MockEndPointProps =
	| MockEndPointBufferProps
	| MockEndPointTextProps
	| MockEndPointJsonProps
	| MockEndPointFnProps;

export function mockEndpoint({
	path,
	body,
	method = "get",
	status = 200,
	bodyType,
}: MockEndPointProps) {
	server.use(
		http[method](
			path,
			bodyType === "fn"
				? (body as MockEndPointFnProps["body"])
				: () => {
						switch (bodyType) {
							case "buffer":
								return HttpResponse.arrayBuffer(
									body as MockEndPointBufferProps["body"],
									{ status },
								);
							case "json":
								return HttpResponse.json(
									body as MockEndPointJsonProps["body"],
									{ status },
								);
							case "text":
							default:
								return new HttpResponse(body as MockEndPointTextProps["body"], {
									status,
								});
						}
					},
		),
	);
}
