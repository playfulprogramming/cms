import { API_URL } from "../../constants";
import { FetchError } from "./FetchError";

async function fetchWrapper<R>(
	endpoint: string,
	init: RequestInit
): Promise<R> {
	const res = await fetch(`${API_URL}/${endpoint}`, {
		credentials: "include",
		...init,
	});
	if (!res.ok) throw new FetchError(res);
	return await res.json();
}

export function get<R>(endpoint: string, init: RequestInit): Promise<R> {
	return fetchWrapper(endpoint, {
		method: "GET",
		headers: {
			Accept: "application/json",
		},
		...init,
	});
}

export function post<T, R>(
	endpoint: string,
	body: T,
	init: RequestInit = {}
): Promise<R> {
	return fetchWrapper(endpoint, {
		method: "POST",
		headers: {
			Accept: "application/json",
			"Content-Type": "application/json",
		},
		body: JSON.stringify(body),
		...init,
	});
}

export function put<T, R>(
	endpoint: string,
	body: T,
	init: RequestInit = {}
): Promise<R> {
	return fetchWrapper(endpoint, {
		method: "PUT",
		headers: {
			Accept: "application/json",
			"Content-Type": "application/json",
		},
		body: JSON.stringify(body),
		...init,
	});
}

export function del<R>(endpoint: string, init: RequestInit = {}): Promise<R> {
	return fetchWrapper(endpoint, {
		method: "DELETE",
		...init,
	});
}
