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

test("Should fetch basic page icon", async () => {
	const html = `
<!DOCTYPE html>
<head>
	<title>Test</title>
	<link rel="shortcut icon" type="image/png" href="/img.png">
</head>
<body>
	<h1>Test</h1>
</body>
</html>
`.trim();

	const domain = "https://example.com/test";
	mockEndpoint({
		path: domain,
		body: html,
		method: "get",
		bodyType: "text",
	});
	const srcHast = await fetchPageHtml(new URL(domain));
	const iconHref = await fetchPageIcon(new URL(domain), srcHast!);

	expect(iconHref).toEqual(new URL("https://example.com/img.png"));
});
