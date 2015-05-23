package gh.funthomas424242.forge.addon;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.addons.facets.AddonAPIFacet;
import org.jboss.forge.addon.addons.facets.AddonAddonFacet;
import org.jboss.forge.addon.addons.facets.AddonClassifierFacet;
import org.jboss.forge.addon.addons.facets.AddonImplFacet;
import org.jboss.forge.addon.addons.facets.AddonParentFacet;
import org.jboss.forge.addon.addons.facets.AddonSPIFacet;
import org.jboss.forge.addon.addons.facets.AddonTestFacet;
import org.jboss.forge.addon.addons.facets.DefaultFurnaceContainerFacet;
import org.jboss.forge.addon.addons.facets.ForgeBOMFacet;
import org.jboss.forge.addon.addons.facets.ForgeVersionFacet;
import org.jboss.forge.addon.addons.facets.FurnacePluginFacet;
import org.jboss.forge.addon.addons.facets.FurnaceVersionFacet;
import org.jboss.forge.addon.addons.project.AddonProjectConfigurator;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.facets.FacetNotFoundException;
import org.jboss.forge.addon.javaee.cdi.CDIFacet_1_1;
import org.jboss.forge.addon.maven.projects.MavenBuildSystem;
import org.jboss.forge.addon.parser.java.facets.JavaCompilerFacet;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.util.Streams;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaPackageInfoSource;


public class SpringBootStarterProject implements AddonProjectConfigurator {
	private static final String FORGE_ADDON_CLASSIFIER = "forge-addon";

	@Inject
	private FacetFactory facetFactory;

	@Inject
	private ProjectFactory projectFactory;

	@Inject
	private DependencyInstaller dependencyInstaller;

	@Inject
	private MavenBuildSystem buildSystem;

	@Override
	public void setupSimpleAddonProject(Project project, Version forgeVersion,
			Iterable<AddonId> dependencyAddons) throws FileNotFoundException,
			FacetNotFoundException {
		generateReadme(project);
		facetFactory.install(project, FurnaceVersionFacet.class);
		facetFactory.install(project, ForgeVersionFacet.class);
		project.getFacet(ForgeVersionFacet.class).setVersion(
				forgeVersion.toString());

		facetFactory.install(project, ForgeBOMFacet.class);
		facetFactory.install(project, FurnacePluginFacet.class);
		facetFactory.install(project, AddonClassifierFacet.class);
		facetFactory.install(project, JavaSourceFacet.class);
		facetFactory.install(project, ResourcesFacet.class);
		facetFactory.install(project, JavaCompilerFacet.class);
		facetFactory.install(project, DefaultFurnaceContainerFacet.class);
		facetFactory.install(project, CDIFacet_1_1.class);
		facetFactory.install(project, AddonTestFacet.class);

		JavaSourceFacet javaSource = project.getFacet(JavaSourceFacet.class);
		javaSource.saveJavaSource(Roaster.create(JavaPackageInfoSource.class)
				.setPackage(javaSource.getBasePackage()));

		installSelectedAddons(project, dependencyAddons, false);
	}

	@Override
	public void setupComplexAddonProject(Project project, Version forgeVersion,
			Iterable<AddonId> dependencyAddons) throws FileNotFoundException,
			FacetNotFoundException {
		generateReadme(project);
		MetadataFacet metadata = project.getFacet(MetadataFacet.class);
		String projectName = metadata.getProjectName();
		metadata.setProjectName(projectName + "-parent");

		project.getFacet(PackagingFacet.class).setPackagingType("pom");

		facetFactory.install(project, AddonParentFacet.class);
		facetFactory.install(project, ForgeBOMFacet.class);
		project.getFacet(ForgeVersionFacet.class).setVersion(
				forgeVersion.toString());

		Project addonProject = createSubmoduleProject(project, "addon",
				projectName, AddonAddonFacet.class);
		Project apiProject = createSubmoduleProject(project, "api", projectName
				+ "-api", AddonAPIFacet.class, CDIFacet_1_1.class);
		Project implProject = createSubmoduleProject(project, "impl",
				projectName + "-impl", AddonImplFacet.class, CDIFacet_1_1.class);
		Project spiProject = createSubmoduleProject(project, "spi", projectName
				+ "-spi", AddonSPIFacet.class);
		Project testsProject = createSubmoduleProject(project, "tests",
				projectName + "-tests", AddonTestFacet.class);

		Dependency apiProjectDependency = apiProject.getFacet(
				MetadataFacet.class).getOutputDependency();
		Dependency implProjectDependency = implProject.getFacet(
				MetadataFacet.class).getOutputDependency();

		Dependency spiProjectDependency = DependencyBuilder.create(
				spiProject.getFacet(MetadataFacet.class).getOutputDependency())
				.setClassifier(FORGE_ADDON_CLASSIFIER);

		Dependency addonProjectDependency = DependencyBuilder.create(
				addonProject.getFacet(MetadataFacet.class)
						.getOutputDependency()).setClassifier(
				FORGE_ADDON_CLASSIFIER);

		dependencyInstaller.installManaged(
				project,
				DependencyBuilder.create(addonProjectDependency).setVersion(
						"${project.version}"));
		dependencyInstaller.installManaged(
				project,
				DependencyBuilder.create(apiProjectDependency).setVersion(
						"${project.version}"));
		dependencyInstaller.installManaged(
				project,
				DependencyBuilder.create(implProjectDependency).setVersion(
						"${project.version}"));
		dependencyInstaller.installManaged(
				project,
				DependencyBuilder.create(spiProjectDependency).setVersion(
						"${project.version}"));

		for (Project p : Arrays.asList(addonProject, apiProject, implProject,
				spiProject)) {
			JavaSourceFacet javaSource = p.getFacet(JavaSourceFacet.class);
			javaSource.saveJavaSource(Roaster.create(
					JavaPackageInfoSource.class).setPackage(
					javaSource.getBasePackage()));
		}

		installSelectedAddons(project, dependencyAddons, true);
		installSelectedAddons(addonProject, dependencyAddons, false);
		installSelectedAddons(apiProject, dependencyAddons, false);
		installSelectedAddons(testsProject, dependencyAddons, false);

		dependencyInstaller.install(addonProject,
				DependencyBuilder.create(apiProjectDependency));
		dependencyInstaller.install(
				addonProject,
				DependencyBuilder.create(implProjectDependency)
						.setOptional(true).setScopeType("runtime"));
		dependencyInstaller.install(addonProject,
				DependencyBuilder.create(spiProjectDependency));

		dependencyInstaller.install(
				implProject,
				DependencyBuilder.create(apiProjectDependency).setScopeType(
						"provided"));
		dependencyInstaller.install(
				implProject,
				DependencyBuilder.create(spiProjectDependency).setScopeType(
						"provided"));

		dependencyInstaller.install(
				apiProject,
				DependencyBuilder.create(spiProjectDependency).setScopeType(
						"provided"));

		dependencyInstaller.install(testsProject, addonProjectDependency);
	}

	/**
	 * @param project
	 */
	private void generateReadme(Project project) {
		final String readmeTemplate = Streams.toString(getClass()
				.getResourceAsStream("README.md"));
		FileResource<?> child = project.getRoot()
				.reify(DirectoryResource.class)
				.getChildOfType(FileResource.class, "README.md");

		// TODO: Replace with template addon
		MetadataFacet metadata = project.getFacet(MetadataFacet.class);
		String content = readmeTemplate.replaceAll(
				"\\{\\{ADDON_GROUP_ID\\}\\}", metadata.getProjectGroupName());
		content = content.replaceAll(
				"\\{\\{ADDON_ARTIFACT_ID\\}\\}", metadata.getProjectName());
		child.createNewFile();
		child.setContents(content);
	}

	@Override
	public void installSelectedAddons(final Project project,
			Iterable<AddonId> addons, boolean managed) {
		if (addons != null)
			for (AddonId addon : addons) {
				Dependency dependency = toDependency(addon);
				if (managed) {
					if (!dependencyInstaller.isManaged(project, dependency)) {
						dependencyInstaller.installManaged(project, dependency);
					}
				} else {
					if (!dependencyInstaller.isInstalled(project, dependency)) {
						dependencyInstaller.install(project, dependency);
					}
				}
			}
	}

	@Override
	public Dependency toDependency(AddonId addon) {
		String[] mavenCoords = addon.getName().split(":");
		Dependency dependency = DependencyBuilder.create()
				.setGroupId(mavenCoords[0]).setArtifactId(mavenCoords[1])
				.setVersion(addon.getVersion().toString())
				.setClassifier(FORGE_ADDON_CLASSIFIER);
		return dependency;
	}

	/**
	 * Checks if the {@link Project} depends on the provided {@link AddonId}
	 */
	@Override
	public boolean dependsOnAddon(final Project project, AddonId addonId) {
		Dependency dependency = toDependency(addonId);
		return dependencyInstaller.isInstalled(project, dependency);
	}

	private Project createSubmoduleProject(final Project parent,
			String moduleName, String artifactId,
			Class<? extends ProjectFacet>... requiredProjectFacets) {
		DirectoryResource location = parent.getRoot()
				.reify(DirectoryResource.class)
				.getOrCreateChildDirectory(moduleName);

		List<Class<? extends ProjectFacet>> facets = new ArrayList<>();
		facets.add(ResourcesFacet.class);
		facets.addAll(Arrays.asList(requiredProjectFacets));
		Project project = projectFactory.createProject(location, buildSystem,
				facets);

		MetadataFacet metadata = project.getFacet(MetadataFacet.class);
		metadata.setProjectName(artifactId);
		return project;
	}
}