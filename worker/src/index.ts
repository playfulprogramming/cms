import { S3 } from "./s3";
import sql from "./sql";
import { urlMetadataTask } from "./url-metadata/task";
import { setTimeout } from "node:timers/promises";

const BATCH_SIZE = 1;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const tasks: Record<string, (input: any) => Promise<any>> = {
	["url-metadata"]: urlMetadataTask,
};

async function pollTasks(): Promise<boolean> {
	let hadRequests: boolean = false;

	await sql.begin(async tx => {
		const requests = await tx`SELECT * FROM TaskRequests FOR UPDATE SKIP LOCKED LIMIT ${BATCH_SIZE}`;
		hadRequests = requests.length > 0;

		await Promise.all(requests.map(async request => {
			const taskName = String(request.task);
			const id = String(request.id);
			console.log(`Begin task: ${taskName} / ${id}`);

			const task = tasks[taskName];

			// eslint-disable-next-line @typescript-eslint/no-explicit-any
			let result: any|null;
			if (task) {
				result = await task(request.input)
					.catch(e => {
						console.error(`Error while processing task ${id}`, e);
						return null;
					});
			} else {
				result = null;
			}

			await tx`DELETE FROM TaskRequests WHERE task=${taskName} AND id=${id}`;
			await tx`INSERT INTO TaskResults (task, id, output) VALUES (${taskName}, ${id}, ${result})`;

			console.log(`End task: ${taskName} / ${id}`, result);
		}));
	});

	return hadRequests;
}

let lastTaskTime = Date.now();

try {
	while (true) {
		const hadRequests = await pollTasks();

		if (hadRequests) {
			lastTaskTime = Date.now();
		}

		// WORKER_EXIT_WHEN_DONE will be used in production, to avoid running when no tasks are needed
		// - workers are then manually invoked by the fly.io API on any incoming request
		if (process.env.WORKER_EXIT_WHEN_DONE && Date.now() - lastTaskTime > 10000) {
			console.log("10s without any new tasks; exiting task loop...")
			break;
		} else {
			await setTimeout(100);
		}
	}
} finally {
	console.log("Closing sql connection...");
	await sql.end();
	S3.destroy();

	console.log("Exiting...");
}
