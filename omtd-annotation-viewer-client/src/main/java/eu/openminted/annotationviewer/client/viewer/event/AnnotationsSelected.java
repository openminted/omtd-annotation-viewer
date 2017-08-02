package eu.openminted.annotationviewer.client.viewer.event;

import java.util.List;

import com.gwtplatform.dispatch.annotation.GenEvent;

@GenEvent
public class AnnotationsSelected {
	List<Integer> ids;
}
