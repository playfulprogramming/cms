import { mockEndpoint } from "../../test-utils/server";
import { expect, test } from "vitest";
import {
	fetchAsBrowser,
	fetchPageHtml,
	getOpenGraphImage,
	getPageTitle,
} from "./fetch-page-html";
import { removePositions } from "../../test-utils/hast";

test("Should throw error when 400 status code", async () => {
	const domain = "https://example.com/test";
	mockEndpoint({
		path: domain,
		body: {},
		method: "get",
		status: 400,
		bodyType: "json",
	});
	await expect(() => fetchAsBrowser(domain)).rejects.toThrow();
});

const html = `
<!DOCTYPE html>
<head>
	<title>Test</title>
	<meta property="og:image" content="/image.jpg">
</head>
<body>
	<h1>Test</h1>
</body>
</html>
`.trim();

test("should fetch page HTML", async () => {
	const domain = "https://example.com/test";
	mockEndpoint({
		path: domain,
		body: html,
		bodyType: "text",
	});
	const response = await fetchPageHtml(domain);
	expect(removePositions(response)).toMatchInlineSnapshot(`
		{
		  "children": [
		    {
		      "type": "doctype",
		    },
		    {
		      "children": [
		        {
		          "children": [
		            {
		              "type": "text",
		              "value": "
			",
		            },
		            {
		              "children": [
		                {
		                  "type": "text",
		                  "value": "Test",
		                },
		              ],
		              "properties": {},
		              "tagName": "title",
		              "type": "element",
		            },
		            {
		              "type": "text",
		              "value": "
			",
		            },
		            {
		              "children": [],
		              "properties": {
		                "content": "https://example.com/image.jpg",
		                "property": "og:image",
		              },
		              "tagName": "meta",
		              "type": "element",
		            },
		            {
		              "type": "text",
		              "value": "
		",
		            },
		          ],
		          "properties": {},
		          "tagName": "head",
		          "type": "element",
		        },
		        {
		          "type": "text",
		          "value": "
		",
		        },
		        {
		          "children": [
		            {
		              "type": "text",
		              "value": "
			",
		            },
		            {
		              "children": [
		                {
		                  "type": "text",
		                  "value": "Test",
		                },
		              ],
		              "properties": {},
		              "tagName": "h1",
		              "type": "element",
		            },
		            {
		              "type": "text",
		              "value": "

		",
		            },
		          ],
		          "properties": {},
		          "tagName": "body",
		          "type": "element",
		        },
		      ],
		      "properties": {},
		      "tagName": "html",
		      "type": "element",
		    },
		  ],
		  "data": {
		    "quirksMode": false,
		  },
		  "type": "root",
		}
	`);
});

test("should get page title", async () => {
	const domain = "https://example.com/test";
	mockEndpoint({
		path: domain,
		body: html,
		bodyType: "text",
	});
	const root = await fetchPageHtml(domain);
	const response = await getPageTitle(root!);
	expect(response).toBe("Test");
});

test("Should gather image URL from OpenGraph metadata", async () => {
	const domain = "https://example.com/test";
	mockEndpoint({
		path: domain,
		body: html,
		bodyType: "text",
	});
	const root = await fetchPageHtml(domain);
	const response = await getOpenGraphImage(new URL(domain), root!);
	expect(response).toMatchInlineSnapshot(`https://example.com/image.jpg`);
});
