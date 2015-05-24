//package gh.funthomas424242.forge.addon;
//
//import java.util.Arrays;
//
//import org.jboss.forge.addon.addons.ui.FurnaceAddonSetupStep;
//import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
//import org.jboss.forge.addon.projects.AbstractProjectType;
//import org.jboss.forge.addon.projects.ProjectFacet;
//import org.jboss.forge.addon.ui.wizard.UIWizardStep;
//
//public class ProjectType extends AbstractProjectType{
//	@Override
//	public String getType() {
//		return "Forge Addon (JAR)";
//	}
//
//	@Override
//	public Class<? extends UIWizardStep> getSetupFlow() {
//		return FurnaceAddonSetupStep.class;
//	}
//
//	@Override
//	public Iterable<Class<? extends ProjectFacet>> getRequiredFacets() {
//		return Arrays
//				.<Class<? extends ProjectFacet>> asList(MavenPluginFacet.class);
//	}
//
//	@Override
//	public String toString() {
//		return "addon";
//	}
//
//	@Override
//	public int priority() {
//		return 500;
//	}
//}
