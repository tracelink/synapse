package com.tracelink.prodsec.plugin.sonatype.controller;

import com.tracelink.prodsec.plugin.sonatype.SonatypePlugin;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeAppService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The Sonatype mappings controller handles requests to the page for
 * viewing and editing mappings between Synapse {@link ProjectModel}s and
 * {@link SonatypeApp}s.
 *
 * @author mcool
 */
@Controller
@RequestMapping(SonatypePlugin.MAPPINGS_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class SonatypeMappingsController {
    private final ProductsService productsService;
    private final SonatypeAppService appService;

    public SonatypeMappingsController(@Autowired ProductsService productsService,
                                      @Autowired SonatypeAppService appService) {
        this.productsService = productsService;
        this.appService = appService;
    }

    /**
     * Returns necessary model objects and content view in a {@link
     * SynapseModelAndView} object to render the Sonatype application mappings
     * page.
     *
     * @return {@link SynapseModelAndView} containing all info to render the
     * Sonatype application mappings page
     */
    @GetMapping("")
    public SynapseModelAndView getMappings() {
        SynapseModelAndView mv = new SynapseModelAndView("sonatype-mappings");

        List<SonatypeApp> mappedApps = appService.getMappedApps();
        List<ProjectModel> mappedSynapseProjects = mappedApps.stream().map(SonatypeApp::getSynapseProject)
                .collect(Collectors.toList());
        List<ProjectModel> unmappedSynapseProjects = productsService.getAllProjects().stream()
                .filter(a -> !mappedSynapseProjects.contains(a)).collect(Collectors.toList());

        mv.addObject("mappedApps", mappedApps);
        mv.addObject("synapseProjects", unmappedSynapseProjects);
        mv.addObject("sonatypeApps", appService.getUnmappedApps());
        return mv;
    }

    /**
     * Creates a mapping between the given Synapse project and Sonatype app.
     *
     * @param project            name of the Synapse project to link; if it
     *                           does not exist, no mapping is created
     * @param app                name of the Sonatype app to link; if it does
     *                           not exist, no mapping is created
     * @param redirectAttributes redirect attributes to render Flash attributes
     *                           for success
     * @return string redirecting to the mappings page
     */
    @PostMapping("create")
    public String createMapping(@RequestParam String project, @RequestParam String app,
                                RedirectAttributes redirectAttributes) {
        appService.createMapping(productsService.getProject(project), app);
        redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Created new mapping.");
        return "redirect:" + SonatypePlugin.MAPPINGS_PAGE;
    }

    /**
     * Deletes the Synapse mapping associated with the given Sonatype app.
     *
     * @param app                name of the Sonatype app to delete mapping
     *                           from, if it exists
     * @param redirectAttributes redirect attributes to render Flash attributes
     *                           for success
     * @return string redirecting to the mappings page
     */
    @PostMapping("delete")
    public String deleteMapping(@RequestParam String app, RedirectAttributes redirectAttributes) {
        appService.deleteMapping(app);
        redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Removed mapping.");
        return "redirect:" + SonatypePlugin.MAPPINGS_PAGE;
    }
}
