package com.tracelink.prodsec.synapse.mvc;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

/**
 * A special ModelAndView to handle the Synapse "Well View" and Flash attributes
 * for notifications
 *
 * @author csmith
 */
public class SynapseModelAndView extends ModelAndView {

	/**
	 * the key for the "well view"
	 */
	public static final String CONTENT_VIEW_NAME = "contentViewName";

	/**
	 * the name of the framing HTML for the Synapse view
	 */
	public static final String DEFAULT_VIEW_TEMPLATE = "fragments/container";

	/**
	 * Flash attribute key to show a green notification in the Synapse View
	 */
	public static final String SUCCESS_FLASH = "success";

	/**
	 * Flash attribute key to show a red notification in the Synapse View
	 */
	public static final String FAILURE_FLASH = "failure";

	private final List<String> styles = new ArrayList<>();
	private final List<String> scripts = new ArrayList<>();

	/**
	 * Create the Synapse ModelAndView using the specified view name as the "well
	 * view".
	 *
	 * @param contentViewName the name of the template file to use to show the "well
	 *                        view"
	 */
	public SynapseModelAndView(String contentViewName) {
		setViewName(DEFAULT_VIEW_TEMPLATE);
		addObject(CONTENT_VIEW_NAME, contentViewName);
		addObject("styles", styles);
		addObject("scripts", scripts);
	}

	/**
	 * Adds a style to the Synapse View (added in the HTML head). This is a
	 * reference to the "styles/" location which should be under
	 * "/src/main/resources/static/styles/"
	 *
	 * @param style the css style file to use
	 * @return this Synapse ModelAndView
	 */
	public SynapseModelAndView addStyleReference(String style) {
		styles.add(style);
		return this;
	}

	/**
	 * Adds a script to the Synapse View (added in the HTML head). This is a
	 * reference to the "scripts/" location which should be under
	 * "/src/main/resources/static/scripts/"
	 *
	 * @param script the js script file to use
	 * @return this Synapse ModelAndView
	 */
	public SynapseModelAndView addScriptReference(String script) {
		scripts.add(script);
		return this;
	}

	/**
	 * Setting the view name also removes all Synapse logic and styles/scripts.
	 * <p>
	 * This reverts the Synapse ModelAndView to a simple ModelAndView
	 */
	@Override
	public void setViewName(String viewName) {
		this.getModel().remove(CONTENT_VIEW_NAME);
		this.getModel().remove("styles");
		this.getModel().remove("scripts");
		super.setViewName(viewName);
	}

	/**
	 * Setting the view also removes all Synapse logic and styles/scripts.
	 * <p>
	 * This reverts the Synapse ModelAndView to a simple ModelAndView
	 */
	@Override
	public void setView(View view) {
		this.getModel().remove(CONTENT_VIEW_NAME);
		this.getModel().remove("styles");
		this.getModel().remove("scripts");
		super.setView(view);
	}
}
