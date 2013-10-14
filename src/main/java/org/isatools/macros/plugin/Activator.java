package org.isatools.macros.plugin;

import org.isatools.isacreator.plugins.host.service.PluginMenu;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;


public class Activator implements BundleActivator {

    private BundleContext context = null;

    public void start(BundleContext context) {
        this.context = context;
        Hashtable dict = new Hashtable();
        System.out.println("Going to register menu service");
        context.registerService(
                PluginMenu.class.getName(), new WorkflowVisualizationPlugin(), dict);
    }

    public void stop(BundleContext context) {
    }
}