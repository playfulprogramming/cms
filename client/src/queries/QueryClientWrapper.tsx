import {
	QueryCache,
	QueryClient,
	QueryClientProvider,
} from "@tanstack/react-query";
import { FetchError } from "./fetch/FetchError";
import { lazy, Suspense } from "preact/compat";

const queryClient = new QueryClient({
	defaultOptions: {
		queries: {
			staleTime: 30 * 1000, // 30 seconds
			retry: (failureCount, error) => {
				// Don't retry for auth-related errors
				if (
					error instanceof FetchError &&
					(error.status === 400 || error.status === 401)
				) {
					return false;
				}

				// Retry others just once
				return failureCount <= 1;
			},
		},
	},
	queryCache: new QueryCache(),
});

const ReactQueryDevtools = lazy(() =>
	import.meta.env.MODE === "development"
		? import("@tanstack/react-query-devtools").then(
				({ ReactQueryDevtools }) => ReactQueryDevtools
		  )
		: Promise.resolve({ default: () => null })
);

export function QueryClientWrapper(props: React.PropsWithChildren<{}>) {
	return (
		<QueryClientProvider client={queryClient}>
			{props.children}
			<Suspense fallback={null}>
				<ReactQueryDevtools />
			</Suspense>
		</QueryClientProvider>
	);
}
