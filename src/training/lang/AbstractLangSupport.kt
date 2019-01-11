/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package training.lang

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.wm.ToolWindowAnchor
import training.learn.exceptons.NoSdkException
import java.io.File

abstract class AbstractLangSupport : LangSupport {
  override val defaultProjectName:String
    get() = "LearnProject"

  override fun getProjectFilePath(projectName: String): String {
    return ProjectUtil.getBaseDir() + File.separator + projectName
  }

  override fun getToolWindowAnchor(): ToolWindowAnchor {
    return ToolWindowAnchor.LEFT
  }

  protected fun checkSdkPresence(sdk: Sdk?) {
    if (sdk == null) {
      throw NoSdkException()
    }
  }

}