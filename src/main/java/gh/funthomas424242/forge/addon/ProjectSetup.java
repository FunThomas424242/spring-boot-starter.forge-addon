package gh.funthomas424242.forge.addon;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.projects.MavenBuildSystem;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.parser.java.facets.JavaCompilerFacet;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.util.Streams;

public class ProjectSetup extends AbstractUICommand {

	protected static final List<String> DEP_STARTER_LIST = Arrays.asList(
			"spring-boot-starter-parent", "spring-boot-starter-batch",
			"spring-boot-starter-jetty", "spring-boot-starter-tomcat",
			"spring-boot-starter-logging", "spring-boot-starter-aop",
			"spring-boot-starter-jpa", "spring-boot-starter-jetty-jdbc",
			"spring-boot-starter-thymeleaf", "spring-boot-starter-web",
			"spring-boot-starter-actuator", "spring-boot-starter-security",
			"spring-boot-starter-test");

	protected static final List<String> SPRING_BOOT_VERSIONS = Arrays.asList(
			"1.2.1.RELEASE", "1.2.2.RELEASE", "1.2.3.RELEASE", "1.2.4.RELEASE",
			"1.3.0.M1");

	@Inject
	protected ResourceFactory resourceFactory;

	@Inject
	protected ProjectFactory projectFactory;

	@Inject
	protected MavenBuildSystem buildSystem;

	// /////////////////////////////////////////////////////////////////////////
	//
	// Definition of interactive inputs (parameters)
	//
	// /////////////////////////////////////////////////////////////////////////

	@Inject
	@WithAttributes(label = "Specific Name in spring-boot-starter-specificname:", required = true)
	protected UIInput<String> specificName;

	@Inject
	@WithAttributes(label = "Group ID:", required = true, defaultValue = "gh.funthomas424242.springboot")
	protected UIInput<String> groupId;

	@Inject
	@WithAttributes(label = "Version:", required = true, defaultValue = "1.0.0-SNAPSHOT")
	protected UIInput<String> version;

	@Inject
	@WithAttributes(label = "Dependencies", required = true)
	protected UISelectMany<String> dependencies;

	@Inject
	@WithAttributes(label = "Spring Boot Version", required = true, defaultValue = "1.2.1.RELEASE")
	protected UISelectOne<String> springBootVersion;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(ProjectSetup.class)
				.name("create-spring-boot-starter-project")
				.category(Categories.create("Project"));
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {

		dependencies.setValueChoices(DEP_STARTER_LIST).setNote(
				"Auswahl der Abhängigkeiten");

		springBootVersion.setValueChoices(SPRING_BOOT_VERSIONS).setNote(
				"Auswahl der Spring Boot Version");

		// add the inputs
		builder.add(specificName);
		builder.add(groupId);
		builder.add(version);
		builder.add(dependencies);
		builder.add(springBootVersion);
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {

		final String projectGroupId = groupId.getValue();
		final String specificProjectName = specificName.getValue();
		final String projectArtifactId = "spring-boot-starter-"
				+ specificProjectName;
		final String projectVersion = version.getValue();

		final UIOutput log = context.getUIContext().getProvider().getOutput();
		log.info(log.out(), "Erstelle Projekt: " + projectArtifactId);
		final File dir = new File(projectArtifactId);
		dir.mkdirs();

		// AddonRegistry registry = ...
		// Imported<InputComponentFactory> imported =
		// registry.getServices(InputComponentFactory.class);
		// InputComponentFactory factory = imported.get();

		// System.out.println("Display UI: " + projectName);
		// final String newProjectName = projectName.getValue().toString();
		// System.out.println("Erstelle Projekt " + newProjectName);

		final Resource<File> projectDir = resourceFactory.create(dir);
		log.info(log.out(), "Verwende als Projektverzeichnis " + projectDir);

		// final DirectoryResource location = projectDir.reify(
		// DirectoryResource.class).getOrCreateChildDirectory("test2");
		// System.out.println("Location directory" + location);

		List<Class<? extends ProjectFacet>> facets = new ArrayList<>();
		facets.add(ResourcesFacet.class);
		facets.add(MetadataFacet.class);
		facets.add(JavaSourceFacet.class);
		facets.add(JavaCompilerFacet.class);
		facets.add(MavenPluginFacet.class);
		facets.add(DependencyFacet.class);
		final Project project = projectFactory.createProject(projectDir,
				buildSystem, facets);
		
		generateReadme(project);
		generateLicense(project);

		final MetadataFacet metadata = project.getFacet(MetadataFacet.class);
		// add project coordinates
		metadata.setProjectName(projectArtifactId);
		metadata.setProjectGroupName(projectGroupId);
		metadata.setProjectVersion(projectVersion);

		// // add parent coordinates
		// final MavenFacet mavenData = project
		// .getFacet(MavenFacet.class);
		// mavenData.
		// <parent>
		// <groupId>org.springframework.boot</groupId>
		// <artifactId>spring-boot-starter-parent</artifactId>
		// <version>1.2.1.RELEASE</version>
		// </parent>

		final String springBootVersion = this.springBootVersion.getValue();

		final DependencyFacet dependencyData = project
				.getFacet(DependencyFacet.class);

		for (String depStarterName : this.dependencies.getValue()) {
			final DependencyBuilder dep = DependencyBuilder.create()
					.setGroupId("org.springframework.boot")
					.setArtifactId(depStarterName)
					.setVersion(springBootVersion)
					// .setScopeType(org.jboss.forge.project.dependencies.ScopeType.Runtime);
					.setScopeType("compile");
			if(depStarterName.endsWith("-parent")){
				dep.setPackaging("pom");
			}
			dependencyData.addDirectDependency(dep);
		}

		return Results
				.success("Command 'create-spring-boot-starter-project' successfully executed!");
	}

	// @Override
	// public void setupSimpleAddonProject(Project project, Version
	// forgeVersion) throws FileNotFoundException,
	// FacetNotFoundException {
	// generateReadme(project);
	// facetFactory.install(project, FurnaceVersionFacet.class);
	// facetFactory.install(project, ForgeVersionFacet.class);
	// project.getFacet(ForgeVersionFacet.class).setVersion(
	// forgeVersion.toString());
	//
	// facetFactory.install(project, ForgeBOMFacet.class);
	// facetFactory.install(project, FurnacePluginFacet.class);
	// facetFactory.install(project, AddonClassifierFacet.class);
	// facetFactory.install(project, JavaSourceFacet.class);
	// facetFactory.install(project, ResourcesFacet.class);
	// facetFactory.install(project, JavaCompilerFacet.class);
	// facetFactory.install(project, DefaultFurnaceContainerFacet.class);
	// facetFactory.install(project, CDIFacet_1_1.class);
	// facetFactory.install(project, AddonTestFacet.class);
	//
	// JavaSourceFacet javaSource = project.getFacet(JavaSourceFacet.class);
	// javaSource.saveJavaSource(Roaster.create(JavaPackageInfoSource.class)
	// .setPackage(javaSource.getBasePackage()));
	//
	// installSelectedAddons(project, dependencyAddons, false);
	// }
	//

	// private Project createProject(final Project parent, String moduleName,
	// String artifactId,
	// Class<? extends ProjectFacet>... requiredProjectFacets) {
	// DirectoryResource location = parent.getRoot()
	// .reify(DirectoryResource.class)
	// .getOrCreateChildDirectory(moduleName);
	//
	// List<Class<? extends ProjectFacet>> facets = new ArrayList<>();
	// facets.add(ResourcesFacet.class);
	// facets.addAll(Arrays.asList(requiredProjectFacets));
	// Project project = projectFactory.createProject(location, buildSystem,
	// facets);
	//
	// MetadataFacet metadata = project.getFacet(MetadataFacet.class);
	// metadata.setProjectName(artifactId);
	// return project;
	// }

	/**
	 * @param project
	 */
	protected void generateReadme(Project project) {
		final String readmeTemplate = Streams.toString(getClass()
				.getResourceAsStream("README.md"));
		final FileResource<?> child = project.getRoot()
				.reify(DirectoryResource.class)
				.getChildOfType(FileResource.class, "README.md");

		// TODO: Replace with template addon
		MetadataFacet metadata = project.getFacet(MetadataFacet.class);
		String content = readmeTemplate.replaceAll(
				"\\{\\{ADDON_GROUP_ID\\}\\}", metadata.getProjectGroupName());
		content = content.replaceAll("\\{\\{ADDON_ARTIFACT_ID\\}\\}",
				metadata.getProjectName());
		child.createNewFile();
		child.setContents(content);
	}
	

	/**
	 * @param project
	 */
	protected void generateLicense(Project project) {
		
		final String readmeTemplate = Streams.toString(getClass()
				.getResourceAsStream("LICENSE"));
		
		final FileResource<?> child = project.getRoot()
				.reify(DirectoryResource.class)
				.getChildOfType(FileResource.class, "LICENSE");

		child.createNewFile();
		child.setContents(readmeTemplate.toString());
	}


}