import { render } from "preact";
import "./styles/global.scss";
import { SignInWithGitHub } from "./components/home/SignInWithGitHub";

render(
  <div>
	Hi
	<p>
		<SignInWithGitHub />
	</p>
  </div>,
  document.getElementById("app")!
);
