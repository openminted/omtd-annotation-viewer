package eu.openminted.annotationviewer.client.typelist.event;

import com.gwtplatform.dispatch.annotation.GenEvent;

@GenEvent
public class SetTypeVisibility {
	String typeName;
	boolean visible;
}
