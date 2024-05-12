const AUTHORIZE_URL=`${import.meta.env.VITE_API_URL}/auth/github/login`;

export function SignInWithGitHub() {
	return (
		<a href={AUTHORIZE_URL}>Sign in with GitHub</a>
	);
}
