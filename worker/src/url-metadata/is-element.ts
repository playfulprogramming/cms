import { Root, Element, Node } from "hast";

export const isElement = (e: Root | Element | Node | undefined): e is Element =>
	e?.type == "element";
