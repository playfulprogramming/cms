import { mockEndpoint } from "../../test-utils/server";
import { expect, test } from "vitest";
import {
	fetchAsBrowser,
	fetchPageHtml,
	getOpenGraphImage,
	getPageTitle,
} from "./fetch-page-html";
import { removePositions } from "../../test-utils/hast";
import { fetchPageIcon } from "./fetch-page-icon";

test("Should fetch basic page icon", () => {
	const domain = "https://example.com/test";
	mockEndpoint({ path: domain, body: "", method: "get", status: 400 });
	fetchPageIcon;
});
