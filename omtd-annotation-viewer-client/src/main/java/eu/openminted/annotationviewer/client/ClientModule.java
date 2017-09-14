package eu.openminted.annotationviewer.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import com.gwtplatform.mvp.shared.proxy.RouteTokenFormatter;

import eu.openminted.annotationviewer.client.application.ApplicationModule;

public class ClientModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		install(new DefaultModule.Builder().tokenFormatter(RouteTokenFormatter.class).defaultPlace(NameTokens.HOME)
				.errorPlace(NameTokens.HOME).unauthorizedPlace(NameTokens.HOME).build());

		install(new ApplicationModule());
	}
}
