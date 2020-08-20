/*
 * Copyright 2020 tommyk-gears.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tommykgears.maven.plugins.bom;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;

/**
 * Imports properties from a maven POM project.
 */
@Mojo(name = "import-properties", defaultPhase = LifecyclePhase.INITIALIZE)
public class ImportPropertiesMojo extends AbstractMojo {

	@Parameter(required = true)
	protected List<Dependency> artifacts;

	//// standard maven properties
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Component
	private ProjectBuilder projectBuilder;

	@Component
	private RepositorySystem repoSystem;

	protected void importProperties(Dependency artifactMetadata) throws MojoExecutionException {
		try {
			Artifact artifact = repoSystem.createDependencyArtifact(artifactMetadata);
			getLog().info("Importing properties from " + artifact);

			ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session
					.getProjectBuildingRequest());
			buildingRequest.setProject(null);
			buildingRequest.setResolveDependencies(false);
			MavenProject bomProject = projectBuilder.build(artifact, buildingRequest).getProject();

			bomProject.getProperties()
					.forEach((Object key, Object value) -> project.getProperties().putIfAbsent(key, value));
		}
		catch (ProjectBuildingException ex) {
			getLog().error("Failed to resolve artifact", ex);
			throw new MojoExecutionException("Could not read artifact " + artifactMetadata.toString(), ex);
		}
	}

	@Override
	public void execute() throws MojoExecutionException {
		for (Dependency artifact : artifacts) {
			importProperties(artifact);
		}
	}
}
