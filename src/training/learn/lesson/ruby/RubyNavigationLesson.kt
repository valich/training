package training.learn.lesson.ruby

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.PlatformDataKeys
import training.commands.kotlin.TaskContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import javax.swing.JTextField
import kotlin.concurrent.thread

class RubyNavigationLesson(module: Module) : KLesson("Basic Navigation", module, "ruby") {
  override val lessonContent: LessonContext.() -> Unit
    get() = {

      triggerTask("GotoDeclaration") {
        caret(5, 18)
        text("Use ${action(it)} to jump to the declaration of a class or interface.")
      }

      triggerTask("GotoClass") {
        text("Try to find a class with ${action(it)}")
      }
      task {
        text("Write 'Us' and press ${action("QuickImplementations")} to see the definition of the selected class")
        if (TaskContext.inTestMode) {
          thread(name = "TestLearningPlugin") {
            System.err.println("Sleep")
            Thread.sleep(1000)
            System.err.println("Write text!")
            DataManager.getInstance().dataContextFromFocusAsync.onSuccess { dataContext ->
              val component = dataContext.getData(PlatformDataKeys.CONTEXT_COMPONENT) ?: return@onSuccess
              if (component is JTextField) {

              }
            }
          }
          Thread.sleep(1000)
        }

        trigger("QuickImplementations")
      }
      complete()
    }

  override val existedFile: String?
    get() = "app/controllers/users_controller.rb"
}
