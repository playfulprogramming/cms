import { QueryFunction, queryOptions } from "@tanstack/react-query";
import * as api from "./fetch/fetchWrapper";
import { FetchError } from "./fetch/FetchError";

type AuthUserResponse = {
	id: number;
	login: string;
	name: string;
};

const getAuthUser: QueryFunction<AuthUserResponse | null, ["getAuthUser"]> = ({
	signal,
}) => {
	return api.get<AuthUserResponse>(`auth/user`, { signal }).catch((e) => {
		// If the user is logged out, return null as a successful response
		if (e instanceof FetchError && e.status === 401) return null;
		else throw e;
	});
};

export function queryAuthUser() {
	return queryOptions({
		queryKey: ["getAuthUser"],
		queryFn: getAuthUser,
	});
}
