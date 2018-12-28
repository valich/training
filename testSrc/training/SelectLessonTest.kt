package training

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

import com.intellij.testGuiFramework.impl.*
import org.junit.Test
import training.learn.CourseManager
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 *  Use next gradle task to invoke test:
 *  gradle -Dtest.single=SelectLessonTest clean test -Didea.gui.test.alternativeIdePath="<path_to_installed_IDE>"
 */
class SelectLessonTest : GuiTestCase() {

  @Test
  fun selectLessonTest() {
    openLearnProject()
    selectRubyLang()
    ideFrame {
      waitForBackgroundTasksToFinish()
      toolwindow(id = "Learn") {
        content {
          linkLabel("Editor Basics").click()
          linkLabel("Select").click()
          invokeAction("EditorNextWordWithSelection")
          invokeAction("EditorSelectWord")
          invokeAction("EditorSelectWord")
          invokeAction("EditorUnSelectWord")
          invokeAction("\$SelectAll")
        }
      }
    }
    assertTrue(assertNotNull(CourseManager.instance.findLesson("Select")).passed , "lesson should be passed")
  }

  private fun selectRubyLang(){
    ideFrame {
      waitForBackgroundTasksToFinish()
      toolwindow(id = "Learn") {
        content {
          button("Change language").click()
          radioButton("Ruby (3 lessons) ").select()
          button("Start Learning").click()
        }
      }
    }
  }

  private fun openLearnProject() {
    welcomeFrame {
      actionLink("Learn IntelliJ IDEA").click()
    }
  }

}