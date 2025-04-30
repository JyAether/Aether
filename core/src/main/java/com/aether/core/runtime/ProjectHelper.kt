/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aether.core.runtime

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * Helper that creates or uses a provided Project for psi operations. For example, finding the
 * PsiManager.
 */
object ProjectHelper {

  private var overriddenProject: Project? = null
  private val kotlinCoreEnvironment: KotlinCoreEnvironment by lazy { createCoreEnvironment() }

  fun getProject(): Project {
    return overriddenProject ?: kotlinCoreEnvironment.project
  }

  fun setProjectOverride(project: Project) {
    this.overriddenProject = project
  }

  private fun createCoreEnvironment(): KotlinCoreEnvironment {
    val disposable = Disposer.newDisposable()
    return KotlinCoreEnvironment.createForProduction(
        disposable, getConfiguration(), EnvironmentConfigFiles.JVM_CONFIG_FILES)
  }

  fun getConfiguration(): CompilerConfiguration {
    val configuration = CompilerConfiguration()
    configuration.put(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
        PrintingMessageCollector(System.err, MessageRenderer.PLAIN_RELATIVE_PATHS, false))
    return configuration
  }
}
