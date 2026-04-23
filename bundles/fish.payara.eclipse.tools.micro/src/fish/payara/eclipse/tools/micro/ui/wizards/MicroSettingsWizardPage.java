/**
 * Copyright (c) 2020-2024 Payara Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package fish.payara.eclipse.tools.micro.ui.wizards;

import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.ARCHETYPE_AUTOBIND_HTTP;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.ARCHETYPE_CONTEXT_ROOT;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.ARCHETYPE_JAVA_VERSION;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.ARCHETYPE_MICRO_VERSION;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.ARCHETYPE_PAYARA_VERSION;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.ARCHETYPE_PLATFORM;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.ARCHETYPE_VERSION_5X;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.ARCHETYPE_GROUP_ID;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.ARCHETYPE_ARTIFACT_ID;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.PLATFORM_MICRO;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.PLATFORM_SERVER;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.STARTER_ARCHETYPE_GROUP_ID;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.STARTER_ARCHETYPE_ARTIFACT_ID;
import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.STARTER_ARCHETYPE_VERSION;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.archetype.catalog.Archetype;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.wizards.AbstractMavenWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import fish.payara.eclipse.tools.micro.MavenBuildTool;

public class MicroSettingsWizardPage extends AbstractMavenWizardPage {

	private Combo contextPathCombo;

	private Combo microVersionCombo;

	private Combo platformCombo;

	private Button autobindCheckbox;

	private Archetype archetype;

	private final Map<String, String> runtimeOptions = new LinkedHashMap<>();

	public MicroSettingsWizardPage(ProjectImportConfiguration projectImportConfiguration) {
		super(MicroSettingsWizardPage.class.getSimpleName(), projectImportConfiguration);
		setTitle(Messages.microSettingsPageTitle);
		setDescription(Messages.microSettingsPageDescription);
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(3, false));
		runtimeOptions.clear();
		runtimeOptions.put(Messages.platformMicroOption, PLATFORM_MICRO);
		runtimeOptions.put(Messages.platformServerOption, PLATFORM_SERVER);
		createUI(composite);
		validate();
		setControl(composite);
	}

	private void createUI(Composite parent) {
		Label platformLabel = new Label(parent, SWT.NONE);
		platformLabel.setText(Messages.platformComponentLabel);

		platformCombo = new Combo(parent, SWT.READ_ONLY);
		platformCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		platformCombo.setItems(runtimeOptions.keySet().toArray(new String[0]));
		platformCombo.addModifyListener(e -> {
			refreshVersions(getSelectedPlatform(), microVersionCombo.getText().trim());
			validate();
		});

		Label contextPathlabel = new Label(parent, SWT.NONE);
		contextPathlabel.setText(Messages.contextPathComponentLabel);

		contextPathCombo = new Combo(parent, SWT.BORDER);
		contextPathCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		addFieldWithHistory(ARCHETYPE_CONTEXT_ROOT, contextPathCombo);
		contextPathCombo.setData("name", ARCHETYPE_CONTEXT_ROOT); //$NON-NLS-1$
		contextPathCombo.addModifyListener(e -> validate());

		Label microVersionLabel = new Label(parent, SWT.NONE);
		microVersionLabel.setText(Messages.microVersionComponentLabel);

		microVersionCombo = new Combo(parent, SWT.BORDER);
		microVersionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		microVersionCombo.setData("name", ARCHETYPE_MICRO_VERSION); //$NON-NLS-1$
		microVersionCombo.addModifyListener(e -> validate());
		microVersionCombo.setItems(MicroProjectWizard.getVersions(PLATFORM_MICRO).toArray(new String[0]));

		Label autobindLabel = new Label(parent, SWT.NONE);
		autobindLabel.setText(Messages.autobindComponentLabel);

		autobindCheckbox = new Button(parent, SWT.CHECK);
		GridData gd_autobindCheckbox = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_autobindCheckbox.widthHint = 150;
		autobindCheckbox.setLayoutData(gd_autobindCheckbox);
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible) {
			validate();
		}
	}

	void setArchetype(Archetype archetype) {
		this.archetype = archetype;
		selectPlatform((String) archetype.getProperties().get(ARCHETYPE_PLATFORM));
		contextPathCombo.setText((String) archetype.getProperties().get(ARCHETYPE_CONTEXT_ROOT));
		String payaraVersion = (String) archetype.getProperties().get(ARCHETYPE_PAYARA_VERSION);
		refreshVersions(getSelectedPlatform(), payaraVersion);
		autobindCheckbox.setSelection(Boolean.valueOf((String) archetype.getProperties().get(ARCHETYPE_AUTOBIND_HTTP)));
	}

	void validate() {
		if (isVisible()) {
			String error = validateInput();
			setErrorMessage(error);
			setPageComplete(error == null);
		}
	}

	private boolean isVisible() {
		return getControl() != null && getControl().isVisible();
	}

	private String validateInput() {
		if (getSelectedPlatform() == null) {
			return Messages.platformValidationMessage;
		}

		String contextPathValue = contextPathCombo.getText().trim();
		if (contextPathValue.length() == 0) {
			return Messages.contextPathValidationMessage;
		}

		String versionValue = microVersionCombo.getText().trim();
		if (versionValue.length() == 0) {
			return Messages.microVersionValidationMessage;
		}

		return null;
	}

	public Archetype getArchetype() {
		if (isLegacyMicroProject()) {
			archetype.setGroupId(ARCHETYPE_GROUP_ID);
			archetype.setArtifactId(ARCHETYPE_ARTIFACT_ID);
			archetype.setVersion(ARCHETYPE_VERSION_5X);
		} else {
			archetype.setGroupId(STARTER_ARCHETYPE_GROUP_ID);
			archetype.setArtifactId(STARTER_ARCHETYPE_ARTIFACT_ID);
			archetype.setVersion(STARTER_ARCHETYPE_VERSION);
		}

		return archetype;
	}

	public Map<String, String> getProperties() {
		boolean legacyMicroProject = isLegacyMicroProject();
		Map<String, String> properties = archetype.getProperties()
				.entrySet()
				.stream()
				.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
		String contextRoot = contextPathCombo.getText().trim();
		try {
			contextRoot = contextRoot.startsWith("/") ? '/' + URLEncoder.encode(contextRoot.substring(1), UTF_8.name())
					: URLEncoder.encode(contextRoot, UTF_8.name());
			properties.put(ARCHETYPE_CONTEXT_ROOT, contextRoot);
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException("Invalid context root value " + contextRoot);
		}
		if (legacyMicroProject) {
			properties.put(ARCHETYPE_MICRO_VERSION, microVersionCombo.getText());
			properties.remove(ARCHETYPE_PLATFORM);
			properties.remove(ARCHETYPE_PAYARA_VERSION);
			properties.remove(ARCHETYPE_JAVA_VERSION);
		} else {
			properties.put(ARCHETYPE_PLATFORM, getSelectedPlatform());
			properties.put(ARCHETYPE_PAYARA_VERSION, microVersionCombo.getText());
			properties.put(ARCHETYPE_JAVA_VERSION, properties.getOrDefault(ARCHETYPE_JAVA_VERSION, "17"));
			properties.remove(ARCHETYPE_MICRO_VERSION);
		}
		properties.put(ARCHETYPE_AUTOBIND_HTTP, String.valueOf(autobindCheckbox.getSelection()));
		return properties;
	}

	void configureBuildTool() {
		MavenBuildTool.setStartCommand(isLegacyMicroProject() ? "start" : "dev");
	}

	private void selectPlatform(String platform) {
		String resolvedPlatform = platform == null || platform.isBlank() ? PLATFORM_MICRO : platform;
		runtimeOptions.entrySet().stream()
				.filter(entry -> entry.getValue().equals(resolvedPlatform))
				.findFirst()
				.ifPresent(entry -> platformCombo.setText(entry.getKey()));
	}

	private String getSelectedPlatform() {
		return runtimeOptions.get(platformCombo.getText());
	}

	private void refreshVersions(String platform, String selectedVersion) {
		List<String> versions = MicroProjectWizard.getVersions(platform);
		microVersionCombo.setItems(versions.toArray(new String[0]));
		if (selectedVersion != null && versions.contains(selectedVersion)) {
			microVersionCombo.setText(selectedVersion);
		} else if (!versions.isEmpty()) {
			microVersionCombo.setText(versions.get(0));
		} else {
			microVersionCombo.setText("");
		}
	}

	private boolean isLegacyMicroProject() {
		return usesLegacyMicroArchetype(getSelectedPlatform(), microVersionCombo.getText().trim());
	}

	private boolean usesLegacyMicroArchetype(String platform, String version) {
		return PLATFORM_MICRO.equals(platform) && extractMajorVersion(version) < 6;
	}

	private int extractMajorVersion(String version) {
		try {
			String[] versionToken = version.trim().split("\\.");
			return versionToken.length == 0 || versionToken[0].isBlank()
					? Integer.MAX_VALUE
					: Integer.parseInt(versionToken[0]);
		} catch (NumberFormatException ex) {
			return Integer.MAX_VALUE;
		}
	}
}
