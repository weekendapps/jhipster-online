/**
 * Copyright 2017-2024 the original author or authors from the JHipster project.
 * <p>
 * This file is part of the JHipster Online project, see https://github.com/jhipster/jhipster-online
 * for more information.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jhipster.online.service;

import io.github.jhipster.online.config.ApplicationProperties;
import io.github.jhipster.online.service.enums.CiCdTool;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JHipsterService {

    public final Logger log = LoggerFactory.getLogger(JHipsterService.class);

    private static final String FORCE_INSIGHT = "--force-insight";

    private static final String SKIP_CHECKS = "--skip-checks";

    private static final String SKIP_INSTALL = "--skip-install";

    private static final String FORCE = "--force";

    private final LogsService logsService;

    private final Executor taskExecutor;

    private final String jhipsterCommand;

    private final String npmCommand;

    private final Integer timeout;

    public JHipsterService(LogsService logsService, ApplicationProperties applicationProperties, Executor taskExecutor) {
        this.logsService = logsService;
        this.taskExecutor = taskExecutor;

        jhipsterCommand = applicationProperties.getJhipsterCmd().getCmd();
        npmCommand = applicationProperties.getNpmCmd().getCmd();
        timeout = applicationProperties.getJhipsterCmd().getTimeout();

        log.info("JHipster service will be using \"{}\" to run generator-jhipster.", jhipsterCommand);
    }

    public void installNpmDependencies(String generationId, File workingDir) throws IOException {
        this.logsService.addLog(generationId, "Installing the JHipster version used by the project");
        this.runProcess(generationId, workingDir, npmCommand, "install", "--ignore-scripts", "--package-lock-only");
    }

    public void generateApplication(String generationId, File workingDir) throws IOException {
        this.logsService.addLog(generationId, "Running JHipster");
        this.runProcess(
                generationId,
                workingDir,
                jhipsterCommand,
                FORCE_INSIGHT,
                SKIP_CHECKS,
                SKIP_INSTALL,
                "--skip-cache",
                "--skip-git",
                FORCE
            );
    }

    public void runImportJdl(String generationId, File workingDir, String jdlFileName) throws IOException {
        this.logsService.addLog(generationId, "Running `jhipster import-jdl`");
        this.runProcess(
                generationId,
                workingDir,
                jhipsterCommand,
                "import-jdl",
                jdlFileName + ".jh",
                FORCE_INSIGHT,
                SKIP_CHECKS,
                SKIP_INSTALL,
                FORCE
            );
    }

    public void addCiCd(String generationId, File workingDir, CiCdTool ciCdTool) throws IOException {
        if (ciCdTool == null) {
            this.logsService.addLog(generationId, "Continuous Integration system not supported, aborting");
            throw new IllegalArgumentException("Invalid Continuous Integration system");
        }
        this.logsService.addLog(generationId, "Running `jhipster ci-cd`");
        this.runProcess(
                generationId,
                workingDir,
                jhipsterCommand,
                "ci-cd",
                "--autoconfigure-" + ciCdTool.command(),
                FORCE_INSIGHT,
                SKIP_CHECKS,
                SKIP_INSTALL,
                FORCE
            );
    }

    void runProcess(String generationId, File workingDir, String... command) throws IOException {
        log.info("Running command: \"{}\" in directory:  \"{}\"", command, workingDir);
        String osName = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;
        if (osName.contains("win")) {
            // For Windows
            List<String> fullCommand = new ArrayList<>();
            fullCommand.add("cmd");
            fullCommand.add("/c");
            fullCommand.addAll(Arrays.asList(command));
            processBuilder = new ProcessBuilder().directory(workingDir).command(fullCommand).redirectError(ProcessBuilder.Redirect.DISCARD);
        } else {
            // For other operating systems
            processBuilder = new ProcessBuilder().directory(workingDir).command(command).redirectError(ProcessBuilder.Redirect.DISCARD);
        }
        
        Process p = processBuilder.start();

        taskExecutor.execute(
            () -> {
                try {
                    p.waitFor(timeout, TimeUnit.SECONDS);
                    p.destroyForcibly();
                } catch (InterruptedException e) {
                    log.error("Unable to execute process successfully.", e);
                    Thread.currentThread().interrupt();
                }
            }
        );

        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            log.debug(line);
            this.logsService.addLog(generationId, line);
        }
        input.close();
    }
}
