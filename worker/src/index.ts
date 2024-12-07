import sql from "./sql";
import { urlMetadataTask } from "./url-metadata/task";

const BATCH_SIZE = 1;

let hasRequests = true;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const tasks: Record<string, (input: any) => Promise<any>> = {
	["url-metadata"]: urlMetadataTask,
};

while (hasRequests) {
	await sql.begin(async tx => {
		const requests = await tx`SELECT * FROM TaskRequests FOR UPDATE SKIP LOCKED LIMIT ${BATCH_SIZE}`;
		hasRequests = requests.length > 0;

		for (const request of requests) {
			const taskId = String(request.id);
			const taskName = taskId.split("/").at(0);
			if (!taskName) continue;

			const task = tasks[taskName];

			// eslint-disable-next-line @typescript-eslint/no-explicit-any
			let result: any|null;
			if (task) {
				result = await task(request.input)
					.catch(e => {
						console.error(`Error while processing task ${taskId}`, e);
						return null;
					});
			} else {
				result = null;
			}

			await tx`DELETE FROM TaskRequests WHERE id=${taskId}`;
			await tx`INSERT INTO TaskResults (id, output) VALUES (${taskId}, ${result})`;
		}
	});
}
