package eu.openminted.annotationviewer.client.application;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

import eu.openminted.annotationviewer.client.annotationtree.AnnotationTreeModule;
import eu.openminted.annotationviewer.client.typelist.TypeListModule;
import eu.openminted.annotationviewer.client.viewer.DocumentModule;


public class ApplicationModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
    	
    	install(new DocumentModule());
    	install(new AnnotationTreeModule());
    	install(new TypeListModule());

        bindPresenter(ApplicationPresenter.class, ApplicationPresenter.MyView.class, ApplicationView.class,
                ApplicationPresenter.MyProxy.class);
        
       
    }
}