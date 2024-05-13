import { useQuery } from "@tanstack/react-query";
import { queryAuthUser } from "../../queries/auth";
import { SignInWithGitHub } from "./SignInWithGitHub";
import { API_URL } from "../../constants";

export function GetStarted() {
	const { data: user } = useQuery(queryAuthUser());

	return (
		<div>
			<h1>Unicorn Utterances CMS</h1>
			{user === null && <SignInWithGitHub />}
			{user && (
				<>
					<p>Hi {user.name}!</p>
					<a href={`${API_URL}/auth/logout`}>Logout</a>
				</>
			)}
		</div>
	);
}
