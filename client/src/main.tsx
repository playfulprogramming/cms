import { render } from "preact";
import "./styles/global.scss";
import { GetStarted } from "./components/home/GetStarted";
import { QueryClientWrapper } from "./queries/QueryClientWrapper";

render(
	<QueryClientWrapper>
		<div>
			<GetStarted />
		</div>
	</QueryClientWrapper>,
	document.getElementById("app")!
);
