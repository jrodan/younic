/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2016, The younic team (https://github.com/escv/younic)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package net.younic.tpl.thymeleaf;

import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import org.osgi.framework.BundleException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import net.younic.core.api.IResourceProvider;
import net.younic.core.api.IResourceRenderer;
import net.younic.core.api.ResourceRenderingFailedException;

@Component(
	service=IResourceRenderer.class
)
public class ThymeleafResourceRenderer implements IResourceRenderer {

	@Reference
	private IResourceProvider resourceProvider;
	
	private String docroot;

	private boolean devMode;
	private transient TemplateEngine templateEngine;
	
	@Activate
	public void activate(ComponentContext context) throws BundleException {
		this.devMode = "true".equals(context.getBundleContext().getProperty("net.younic.devmode"));
		this.docroot = context.getBundleContext().getProperty("net.younic.cms.root");
		if (this.docroot == null) {
			throw new BundleException("Missing Property \"net.younic.cms.root\"");
		}
	}
	
	@Override
	public void render(String tpl, Map<String, Object> context, Writer out) throws ResourceRenderingFailedException {
		TemplateEngine engine = obtainTemplateEngine();
		
		Context tlContext = new Context(Locale.US, context);
		tlContext.setVariable("FS", resourceProvider);

		engine.process(tpl, tlContext, out);
	}

	private TemplateEngine obtainTemplateEngine() {
		TemplateEngine engine;
		if (!devMode) {
			if (templateEngine == null) {
				this.templateEngine = new TemplateEngine();
				FileTemplateResolver templateResolver = new MergeThemeFileTemplateResolver();
				templateResolver.setPrefix(this.docroot + "/template/");
				templateResolver.setSuffix(".html");
				this.templateEngine.setTemplateResolver(templateResolver);
			}
			engine = this.templateEngine;
		} else {
			engine = new TemplateEngine();
			FileTemplateResolver templateResolver = new MergeThemeFileTemplateResolver();
			templateResolver.setPrefix(this.docroot + "/template/");
			templateResolver.setSuffix(".html");
			engine.setTemplateResolver(templateResolver);
		}
		return engine;
	}
}
