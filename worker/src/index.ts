import sql from "./sql";
import { urlMetadataTask } from "./url-metadata/task";
import { setTimeout } from "node:timers/promises";

const BATCH_SIZE = 1;

let hasRequests = true;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const tasks: Record<string, (input: any) => Promise<any>> = {
	["url-metadata"]: urlMetadataTask,
};

while (true) {
	await sql.begin(async tx => {
		const requests = await tx`SELECT * FROM TaskRequests FOR UPDATE SKIP LOCKED LIMIT ${BATCH_SIZE}`;
		hasRequests = requests.length > 0;

		await Promise.all(requests.map(async request => {
			console.log(request);
			const taskName = String(request.task);
			const id = String(request.id);

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

			console.log("result", result);
			await tx`DELETE FROM TaskRequests WHERE task=${taskName} AND id=${id}`;
			await tx`INSERT INTO TaskResults (task, id, output) VALUES (${taskName}, ${id}, ${result})`;
		}));
	});

	// WORKER_EXIT_WHEN_DONE will be used in production, to avoid running when no tasks are needed
	// - workers are then manually invoked by the fly.io API on any incoming request
	if (process.env.WORKER_EXIT_WHEN_DONE && !hasRequests) {
		break;
	} else {
		await setTimeout(1000);
	}
}

await sql.end();
