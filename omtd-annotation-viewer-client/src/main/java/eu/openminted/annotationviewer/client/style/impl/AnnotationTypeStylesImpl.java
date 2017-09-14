package eu.openminted.annotationviewer.client.style.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Element;
import eu.openminted.annotationviewer.client.style.AnnotationTypeStyle;
import eu.openminted.annotationviewer.client.style.AnnotationTypeStyles;

public class AnnotationTypeStylesImpl implements AnnotationTypeStyles {

	private Set<AnnotationTypeStyles.Handler> handlers = new HashSet<>();
	private Map<String, AnnotationTypeStyle> typeStyles = new HashMap<>();

	public AnnotationTypeStylesImpl() {
	}

	@Override
	public void addHandler(Handler handler) {
		handlers.add(handler);
	}

	@Override
	public void removeHandler(Handler handler) {
		handlers.remove(handler);
	}

	@Override
	public AnnotationTypeStyle getStyle(String typeName) {
		AnnotationTypeStyle style = typeStyles.get(typeName);
		if (style == null) {
			String bgColor = colorNames[typeStyles.size()];
			String fgColor = getContrastingColor(bgColor);

			style = new AnnotationTypeStyleImpl(typeName, bgColor, fgColor, handlers);
			typeStyles.put(typeName, style);
		}
		return style;
	}

	static native float getComputedFontSize(Element element) /*-{
																if ($doc.defaultView && $doc.defaultView.getComputedStyle) {
																var style = $doc.defaultView.getComputedStyle(element, null).getPropertyValue("font-size");
																return parseFloat(style);
																}
																return 0;
																}-*/;

	static native String getContrastingColor(String color) /*-{
															var d = $doc.createElement("div");
															d.style.color = color;
															$doc.body.appendChild(d)
															//Color in RGB 
															var rgbString = $wnd.getComputedStyle(d).color;
															d.remove();
															var rgb = rgbString.split("(")[1].split(")")[0].split(",")
															
															// Counting the perceptive luminance - human eye favors green color... 
															var a = 1 - ( 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2])/255;
															
															if (a < 0.5)
															return "black"
															else
															return "white"; // dark colors - white font
															}-*/;

	@Override
	public Iterator<AnnotationTypeStyle> getStyles() {
		return typeStyles.values().iterator();
	}

	public static final String[] colorNames = {
			// "aliceblue",
			"antiquewhite", "aqua", "aquamarine",
			// "azure",
			"beige", "bisque",
			// "black",
			// "blanchedalmond",
			"blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral",
			"cornflowerblue", "cornsilk", "crimson", "cyan",
			// "darkblue",
			// "darkcyan",
			// "darkgoldenrod",
			// "darkgray",
			// "darkgreen",
			// "darkgrey",
			// "darkkhaki",
			// "darkmagenta",
			// "darkolivegreen",
			// "darkorange",
			// "darkorchid",
			// "darkred",
			// "darksalmon",
			// "darkseagreen",
			// "darkslateblue",
			// "darkslategray",
			// "darkslategrey",
			// "darkturquoise",
			// "darkviolet",
			// "deeppink",
			// "deepskyblue",
			// "dimgray",
			// "dimgrey",
			"dodgerblue", "firebrick",
			// "floralwhite",
			"forestgreen", "fuchsia", "gainsboro",
			// "ghostwhite",
			"gold", "goldenrod", "gray", "grey", "green", "greenyellow",
			// "honeydew",
			"hotpink", "indianred",
			// "indigo",
			// "ivory",
			"khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon",
			// "lightblue",
			// "lightcoral",
			// "lightcyan",
			// "lightgoldenrodyellow",
			// "lightgray",
			// "lightgreen",
			// "lightgrey",
			// "lightpink",
			// "lightsalmon",
			// "lightseagreen",
			// "lightskyblue",
			// "lightslategray",
			// "lightslategrey",
			// "lightsteelblue",
			// "lightyellow",
			"lime", "limegreen",
			// "linen",
			"magenta", "maroon",
			// "mediumaquamarine",
			// "mediumblue",
			// "mediumorchid",
			// "mediumpurple",
			// "mediumseagreen",
			// "mediumslateblue",
			// "mediumspringgreen",
			// "mediumturquoise",
			// "mediumvioletred",
			// "midnightblue",
			// "mintcream",
			"mistyrose", "moccasin",
			// "navajowhite",
			// "navy",
			// "oldlace",
			"olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise",
			"palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue", "purple", "red",
			"rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen",
			// "seashell",
			"sienna", "silver", "skyblue", "slateblue", "slategray", "slategrey",
			// "snow",
			"springgreen", "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet", "wheat",
			// "white",
			// "whitesmoke",
			"yellow", "yellowgreen", };
}
