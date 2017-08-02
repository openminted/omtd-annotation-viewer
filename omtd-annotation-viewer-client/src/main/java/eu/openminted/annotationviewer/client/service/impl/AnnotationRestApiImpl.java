package eu.openminted.annotationviewer.client.service.impl;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import eu.openminted.annotationviewer.client.service.AnnotationRestApi;
import eu.openminted.annotationviewer.client.service.dto.Document;
import eu.openminted.annotationviewer.client.uima.Cas;
import eu.openminted.annotationviewer.client.uima.jsog.JsogCasDeserialiser;
import rx.Single;
import rx.subjects.AsyncSubject;

public class AnnotationRestApiImpl implements AnnotationRestApi {

	private final String url;
	private final JsogCasDeserialiser deserialiser = new JsogCasDeserialiser();

	public AnnotationRestApiImpl(String url) {
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		this.url = url;

	}

	public Single<Document> getDocument(String archiveId, String documentId) {
		
		AsyncSubject<Document> subject = AsyncSubject.create();
		try {

			RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
					url + "document/" + archiveId + '/' + documentId);
			Request request = builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// displayError("Couldn't retrieve JSON");
				}

				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {
						Document document = parseDocumentResponse(response);
						subject.onNext(document);
						subject.onCompleted();
									} else {
						subject.onError(new Throwable("error"));
					}
				}
			});
		} catch (RequestException e) {
			subject.onError(new Throwable("error"));
		}
		return subject.toSingle();
	}

	private Document parseDocumentResponse(Response response) {
		JSONValue json = JSONParser.parseStrict(response.getText());
		JSONObject obj = json.isObject();

		if (obj == null) {
			throw new JSONException("Unexpected type returned as Document response");
		}

		Cas cas = null;
		
		for (String key : obj.keySet()) {
			switch (key) {
			case "cas":
				JSONArray array = obj.get("cas").isArray();
				if (array == null) {
					throw new JSONException("'cas' field value should be an array");
				}
				cas = deserialiser.deserialise(array);
				break;
			default:
				break;
			}
		}
		
		if ( cas == null ) {
			throw new JSONException("No 'cas' field returned in Document response");
		} 
		
		return new Document(cas);
	}

	
}
